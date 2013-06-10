package jfs.sync.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;

import jfs.conf.JFSConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InOutStreamingBuffer {

    private static Log log = LogFactory.getLog(InOutStreamingBuffer.class);

    java.util.concurrent.BlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(JFSConfig.getInstance().getBufferSize()/4);

    boolean open = true;


    void close() {
        System.out.println("close()");
        open = false;
    } // close()


    int read() {
        int result = -1;
        Integer head = queue.peek();
        if (head==null) {
            if (open) {
                synchronized (queue) {
                    try {
                        System.out.println("read() blocking");
                        queue.wait();
                        System.out.println("read() going on");
                    } catch (InterruptedException e) {
                        log.error("read()", e);
                    } // try/catch
                } // synchronized
            } else {
                System.out.println("read() already closed");
            } // if
        } // if
        if ((head!=null)||open) {
            result = queue.poll();
        } // if
          // System.out.println("read("+(rc++ )+") "+result);
        return result;
    } // read()


    void write(int b) {
        synchronized (queue) {
            try {
                if ( !queue.offer(b)) {
                    if (log.isDebugEnabled()) {
                        log.debug("write() notifying");
                    } // if
                    queue.notify();
                    queue.put(b);
                } // if
            } catch (InterruptedException e) {
                log.error("write()", e);
            } // try/catch
        } // synchronized
          // System.out.println("write "+queue.size());
    } // write()

    private class BufferOutputStream extends OutputStream {

        InOutStreamingBuffer buffer;


        public BufferOutputStream(InOutStreamingBuffer buffer) {
            this.buffer = buffer;
        } // BufferOutputStream()


        @Override
        public void write(int b) throws IOException {
            buffer.write((0xFF&b));
        } // write()


        public void close() {
            buffer.close();
        } // close()

    } // BufferOutputStream

    private class BufferInputStream extends InputStream {

        InOutStreamingBuffer buffer;


        public BufferInputStream(InOutStreamingBuffer buffer) {
            this.buffer = buffer;
        } // BufferInputStream()


        @Override
        public int read() throws IOException {
            return buffer.read();
        } // read()

    } // BufferInputStream


    public InputStream getInputStream() {
        return new BufferInputStream(this);
    } // getInputStream()


    public OutputStream getOutputStream() {
        return new BufferOutputStream(this);
    } // getOutputStream()

} // TriggerBuffer
