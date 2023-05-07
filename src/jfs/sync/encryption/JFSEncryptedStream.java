/*
 * Copyright (C) 2010-2023 Martin Goellnitz
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA, 02110-1301, USA
 */
package jfs.sync.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sevenzip.compression.lzma.Decoder;
import sevenzip.compression.lzma.Encoder;


/**
 *
 * Provide an encrypted stream where its contents are compressed with the most promising method according to a large set
 * of configuration options.
 *
 */
public class JFSEncryptedStream extends OutputStream {

    public static final int DONT_CHECK_LENGTH = -2;

    // TODO: We'd like to have this true :-)
    public static final boolean CONCURRENCY = false;

    // Constant absolute maximum size to try bzip2 compression
    private static final int BZIP_MAX_LENGTH = 5190000;

    public static final byte COMPRESSION_NONE = 1;

    public static final byte COMPRESSION_BZIP2 = 2;

    public static final byte COMPRESSION_DEFLATE = 4;

    public static final byte COMPRESSION_LZMA = 8;

    public static final int COMPRESSION_BUFFER_SIZE = 10240;

    private static final int SPACE_RESERVE = 3;

    private static final Logger LOG = LoggerFactory.getLogger(JFSEncryptedStream.class);

    private final Cipher cipher;

    private ByteArrayOutputStream delegate;

    private OutputStream baseOutputStream;


    public static OutputStream createOutputStream(long compressionLimit, OutputStream baseOutputStream, long length, Cipher cipher)
            throws IOException {
        OutputStream result = null;
        Runtime runtime = Runtime.getRuntime();
        long freeMem = runtime.totalMemory()/SPACE_RESERVE;
        if (length>=freeMem) {
            LOG.warn("JFSEncryptedStream.createOutputStream() GC {}/{}", freeMem, runtime.maxMemory());
            runtime.gc();
            freeMem = runtime.totalMemory()/SPACE_RESERVE;
        } // if
        if ((length<compressionLimit)&&(length<freeMem)) {
            result = new JFSEncryptedStream(baseOutputStream, cipher);
        } else {
            if (length<freeMem) {
                LOG.info("JFSEncryptedStream.createOutputStream() not compressing");
            } else {
                LOG.warn("JFSEncryptedStream.createOutputStream() due to memory constraints ({}/{}) not compressing", length, freeMem);
            } // if
            ObjectOutputStream oos = new ObjectOutputStream(baseOutputStream);
            oos.writeByte(COMPRESSION_NONE);
            oos.writeLong(length);
            oos.flush();
            result = baseOutputStream;
            if (cipher!=null) {
                result = new CipherOutputStream(result, cipher);
            } // if
        } // if
        return result;
    } // createOutputStream


    private JFSEncryptedStream(OutputStream baseOutputStream, Cipher cipher) {
        this.delegate = new ByteArrayOutputStream();
        this.baseOutputStream = baseOutputStream;
        this.cipher = cipher;
    } // JFSEncryptedOutputStream()


    @Override
    public void write(int b) throws IOException {
        delegate.write(b);
    } // write()


    @Override
    public void write(byte[] b) throws IOException {
        // log.debug("write() "+b.length+"b");
        delegate.write(b);
    } // write()


    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        // log.debug("write() "+len+"b");
        delegate.write(b, off, len);
    } // write()


    @Override
    public void flush() throws IOException {
        LOG.debug("flush()");
        delegate.flush();
    } // flush()


    /**
     * Thread implementation to de-couple compression and have it work concurrent.
     */
    private class CompressionThread extends Thread {

        public byte[] compressedValue;


        public CompressionThread(byte[] compressedValue) {
            this.compressedValue = compressedValue;
        } // CompressionThread()

    } // CompressionThread


    private void internalClose() throws IOException {
        delegate.close();
        byte[] bytes = delegate.toByteArray();
        final byte[] originalBytes = bytes;
        long l = bytes.length;

        byte marker = COMPRESSION_NONE;

        LOG.info("internalClose() checking for compressions.");

        CompressionThread dt = new CompressionThread(originalBytes) {

            @Override
            public void run() {
                try {
                    ByteArrayOutputStream deflaterStream = new ByteArrayOutputStream();
                    Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
                    OutputStream dos = new DeflaterOutputStream(deflaterStream, deflater, COMPRESSION_BUFFER_SIZE);
                    dos.write(originalBytes);
                    dos.close();
                    compressedValue = deflaterStream.toByteArray();
                } catch (Throwable e) {
                    LOG.error("run()", e);
                } // try/catch
            } // run()

        };

        CompressionThread bt = new CompressionThread(originalBytes) {

            @Override
            public void run() {
                try {
                    if (originalBytes.length<BZIP_MAX_LENGTH) {
                        ByteArrayOutputStream bzipStream = new ByteArrayOutputStream();
                        OutputStream bos = new BZip2CompressorOutputStream(bzipStream);
                        bos.write(originalBytes);
                        bos.close();
                        compressedValue = bzipStream.toByteArray();
                    } // if
                } catch (Throwable e) {
                    LOG.error("run()", e);
                } // try/catch
            } // run()

        };

        CompressionThread lt = new CompressionThread(originalBytes) {

            /*
             * // "  -a{N}:  set compression mode - [0, 1], default: 1 (max)\n" +
             * "  -d{N}:  set dictionary - [0,28], default: 23 (8MB)\n"
             * +"  -fb{N}: set number of fast bytes - [5, 273], default: 128\n"
             * +"  -lc{N}: set number of literal context bits - [0, 8], default: 3\n"
             * +"  -lp{N}: set number of literal pos bits - [0, 4], default: 0\n"
             * +"  -pb{N}: set number of pos bits - [0, 4], default: 2\n"
             * +"  -mf{MF_ID}: set Match Finder: [bt2, bt4], default: bt4\n"+"  -eos:   write End Of Stream marker\n");
             */
            private static final int dictionarySize = 1<<23;

            private static final int lc = 3;

            private static final int lp = 0;

            private static final int pb = 2;

            private static final int fb = 128;

            public int algorithm = 2;

            public int matchFinderIndex = 1; // 0, 1, 2


            @Override
            public void run() {
                try {
                    Encoder encoder = new Encoder();
                    encoder.SetEndMarkerMode(false);
                    encoder.SetAlgorithm(algorithm); // Whatever that means
                    encoder.SetDictionarySize(dictionarySize);
                    encoder.SetNumFastBytes(fb);
                    encoder.SetMatchFinder(matchFinderIndex);
                    encoder.SetLcLpPb(lc, lp, pb);

                    ByteArrayOutputStream lzmaStream = new ByteArrayOutputStream();
                    ByteArrayInputStream inStream = new ByteArrayInputStream(originalBytes);

                    encoder.WriteCoderProperties(lzmaStream);
                    encoder.Code(inStream, lzmaStream, -1, -1, null);
                    compressedValue = lzmaStream.toByteArray();
                    // Back-Check for Tests
//                    ByteArrayInputStream backCheckStream = new ByteArrayInputStream(compressedValue);
//                    byte[] properties = new byte[5];
//                    int readBytes = backCheckStream.read(properties, 0, properties.length);
//                    LOG.debug("CompressionThread.run() readBytes={}", readBytes);
//                    if (readBytes!=properties.length) {
//                        throw new Exception("Short read for LZMA decoder parameters.");
//                    } else {
//                        Decoder decoder = new Decoder();
//                        if (!decoder.SetDecoderProperties(properties)) {
//                            throw new Exception("Could not set LZMA decoder parameters.");
//                        }
//                    }
                } catch (Throwable e) {
                    LOG.error("run()", e);
                } // try/catch
            } // run()

        };

        dt.start();
        bt.start();
        lt.start();

        try {
            dt.join();
            bt.join();
            lt.join();
        } catch (InterruptedException e) {
            LOG.error("run()", e);
        } // try/catch

        int compressedLength = dt.compressedValue.length;
        if ((compressedLength>5)&&(compressedLength<l)) {
            marker = COMPRESSION_DEFLATE;
            bytes = dt.compressedValue;
            l = bytes.length;
        } // if

        compressedLength = lt.compressedValue.length;
        if ((compressedLength>5)&&(compressedLength<l)) {
            marker = COMPRESSION_LZMA;
            bytes = lt.compressedValue;
            l = bytes.length;
        } // if

        compressedLength = bt.compressedValue.length;
        if ((compressedLength>5)&&(compressedLength<l)) {
            marker = COMPRESSION_BZIP2;
            bytes = bt.compressedValue;
            LOG.warn("internalClose() using bzip2 and saving {} bytes.", (l-bytes.length));
            l = bytes.length;
        } // if

        if (marker==COMPRESSION_NONE) {
            LOG.info("internalClose() using no compression");
        } // if
        if (marker==COMPRESSION_LZMA) {
            LOG.info("internalClose() using lzma");
        } // if
        if (l<10) {
            LOG.error("internalClose() short write");
        }

        ObjectOutputStream oos = new ObjectOutputStream(baseOutputStream);
        oos.writeByte(marker);
        oos.writeLong(originalBytes.length);
        oos.flush();
        OutputStream out = baseOutputStream;
        if (cipher!=null) {
            out = new CipherOutputStream(out, cipher);
        } // if
        out.write(bytes);
        out.close();
        delegate = null;
        baseOutputStream = null;
    } // internalClose()


    /**
     * De-coupling of stream handling needs a thread which closes the stream at the end of the operation.
     */
    private class ClosingThread extends Thread {

        private final JFSEncryptedStream stream;


        public ClosingThread(JFSEncryptedStream stream) {
            super();
            this.stream = stream;
        }


        @Override
        public void run() {
            try {
                stream.internalClose();
            } catch (IOException ioe) {
                LOG.error("run()", ioe);
            } // try/catch
        } // run()

    } // ClosingThread


    @Override
    public void close() throws IOException {
        if (CONCURRENCY) {
            // fire and forget!

            // TODO: Set maximum number of threads for this
            // TODO: How to allow subsequent calls like file time setting to succeed?
            Thread t = new ClosingThread(this);
            t.start();
        } else {
            internalClose();
        } // if
    } // close()


    /**
     *
     * @param fis
     * @param expectedLength length to be expected or -2 if you don't want the check
     * @param cipher
     * @return
     */
    public static InputStream createInputStream(InputStream fis, long expectedLength, Cipher cipher) throws IOException {
        try {
            InputStream in = fis;
            ObjectInputStream ois = new ObjectInputStream(in);
            byte marker = readMarker(ois);
            long l = readLength(ois);
            LOG.debug("JFSEncryptedStream.createInputStream() length check {} == {}?", expectedLength, l);
            if (expectedLength!=DONT_CHECK_LENGTH) {
                if (l!=expectedLength) {
                    LOG.error("JFSEncryptedStream.createInputStream() length check failed");
                    return null;
                } // if
            } // if
            if (cipher==null) {
                LOG.error("JFSEncryptedStream.createInputStream() no cipher for length {}", expectedLength);
            } else {
                in = new CipherInputStream(in, cipher);
            } // if
            if (marker==COMPRESSION_DEFLATE) {
                Inflater inflater = new Inflater(true);
                in = new InflaterInputStream(in, inflater, COMPRESSION_BUFFER_SIZE);
            } // if
            if (marker==COMPRESSION_BZIP2) {
                in = new BZip2CompressorInputStream(in);
            } // if
            if (marker==COMPRESSION_LZMA) {
                byte[] properties = new byte[5];
                int readBytes = in.read(properties, 0, properties.length);
                LOG.info("JFSEncryptedStream.createInputStream() readBytes={}", readBytes);
                if (readBytes!=properties.length) {
                    LOG.warn("JFSEncryptedStream.createInputStream() short read for LZMA decoder parameters.");
                }
                Decoder decoder = new Decoder();
                if (!decoder.SetDecoderProperties(properties)) {
                    LOG.warn("JFSEncryptedStream.createInputStream() could not set LZMA decoder parameters.");
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                decoder.Code(in, outputStream, l);
                in.close();
                outputStream.close();
                LOG.debug("JFSEncryptedStream.createInputStream() {}", outputStream.size());
                in = new ByteArrayInputStream(outputStream.toByteArray());
            } // if
            return in;
        } catch (Error error) {
            throw new IOException("createInputStream() "+error.getLocalizedMessage(), error);
        } // try/catch
    } // createInputStream()


    public static long readLength(ObjectInputStream ois) throws IOException {
        return ois.readLong();
    } // readLength()


    public static byte readMarker(ObjectInputStream ois) throws IOException {
        byte marker = ois.readByte();
        LOG.info("JFSEncryptedStream.readMarker() marker {}", marker);
        return marker;
    } // readMarker()

} // JFSEncryptedStream
