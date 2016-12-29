/*
 * Copyright (C) 2010-2015, Martin Goellnitz
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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import jfs.conf.JFSConfig;
import jfs.sync.base.AbstractJFSFileProducerFactory;
import jfs.sync.util.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract story access implementing encrypted files with encrypted file names.
 */
public abstract class AbstractEncryptedStorageAccess {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractEncryptedStorageAccess.class);

    /** 111 codes for now - so there's some room left */
    private static final char[] FILE_NAME_CHARACTERS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '.', ' ', '_', '-',
        'T', 'S', 'C', 'O', 'P', 'L', 'R', 'M', 'A', 'D', 'E', 'F', 'B', 'V', 'U', 'H', 'W', '<', '>', '*', ':', '/', '|', 'G',
        'I', 'J', 'K', 'N', 'Q', 'X', 'Y', 'Z', 'ä', 'ö', 'ü', 'ß', 'Ä', 'Ö', 'Ü', '+', '=', '{', '}', '[', ']', '$', '\'', '@',
        '!', '&', '%', '~', ',', '#', ';', '(', ')', '»', '«', '÷', '´', '`', 'é', 'è', 'à', '²', '?', '"', 'ê', 'ï', 'ó'};

    private static final char[] DYNAMIC_SPECIAL_CODES = {'<', '>', '?', '*', ':', '"'};

    private static final char[] CODES = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_', 'a', 'b', 'c', 'd', 'e',
        'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'ä', 'ö', 'ü',
        'ß', 'Ä', 'Ö', 'Ü', '\'', 'µ', '÷', '@', '!', '&', '%', '~', '#', '(', ')', '[', ']', ',', '.', '{', '}', '´', '`', 'á',
        'é', 'í', 'ó', 'ú', 'Á', 'É', 'Í', 'Ó', 'Ú', 'à', 'è', 'ì', 'ò', 'ù', 'À', 'È', 'Ì', 'Ò', 'Ù', 'â', 'ê', 'î', 'ô', 'û',
        'Â', 'Ê', 'Î', 'Ô', 'Û', '»', '«', 'å', 'Å', 'ñ', 'Ñ', 'Ë', 'ë'};

    private final byte[] reverseCodes = new byte[256];

    private final byte[] reverseCharacters = new byte[256];

    private final int longIndex;

    private final Map<String, String> encryptionCache = new HashMap<>();

    private final Map<String, String> decryptionCache = new HashMap<>();

    private int decodingMask = 32;

    private int bits = 6;

    private static final String SALT = "#Mb6{Z-Öu9Rw4[D_jHn~CeKx2QiV]=a8F@1öG5+p}7Äü01-T";

    private static final String FILESALT = "4Om27Z+6nF[h'8Ec}L0_ds9J=3Her~5Ke7rv]1-ÜLö9ä@#yX";


    /**
     * Storage Access with encrypted everything - names, contents and optional meta data.
     * When used in six bit mode, most file storage back ends will be able to store the generated files names.
     * While some may need short path names where seven bit default is more helpful.
     *
     * @param shortenPaths extend to seven bit file name character table
     */
    public AbstractEncryptedStorageAccess(boolean shortenPaths) {
        if (CODES.length!=128) {
            throw new RuntimeException("Character missing in 7bit table");
        } // if
        if (shortenPaths) {
            decodingMask = 64;
            bits = 7;
        } // if
        for (int i = 0; i<CODES.length; i++) {
            char c = CODES[i];
            reverseCodes[c] = (byte) i;
        } // for
        for (byte i = 0; i<FILE_NAME_CHARACTERS.length; i++) {
            reverseCharacters[FILE_NAME_CHARACTERS[i]] = i;
        } // for
        longIndex = reverseCharacters['|'];
    } // AbstractDisguiseStorageAccess()


    public abstract String getSeparator();


    public abstract String getCipherSpec();


    protected byte[] getCredentials(String relativePath) {
        return getCredentials(relativePath, SALT);
    } // getCredentials()


    public byte[] getFileCredentials(String password) {
        return getCredentials(password, FILESALT);
    } // getFileCredentials()


    protected String[] getPathAndName(String relativePath) {
        return AbstractJFSFileProducerFactory.getPathAndName(relativePath, getSeparator());
    } // getPathAndName()


    protected String getLastPathElement(String defaultValue, String relativePath) {
        String result = defaultValue;
        int idx = relativePath.lastIndexOf(getSeparator());
        if (idx>=0) {
            idx++;
            result = relativePath.substring(idx);
        } // if
        return result;
    } // getLastPathElement()


    protected String getPassword(String relativePath) {
        String result = "";

        String pwd = JFSConfig.getInstance().getEncryptionPassPhrase();

        // Argh: We forgot to do this on the windows side so now we have to go this way
        relativePath = relativePath.replace('/', '\\');

        int i = 0;
        int j = relativePath.length()-1;
        while ((i<pwd.length())||(j>=0)) {
            if (i<pwd.length()) {
                result += pwd.charAt(i++);
            } // if
            if (j>=0) {
                result += relativePath.charAt(j--);
            } // if
        } // while

        return result;
    } // getPassword()


    protected byte[] getCredentials(String relativePath, String salt) {
        String localPwd = getPassword(relativePath)+salt;
        byte[] localBytes = getEncodedFileName("", localPwd);
        byte[] credentials = new byte[32];
        System.arraycopy(localBytes, 0, credentials, 0, 32);
        return credentials;
    } // getCredentials()


    private void generateSpecialCodes(String relativePath, List<String> specialCodes, List<Integer> specialLengths) {
        String specialCode = getLastPathElement("", relativePath);
        int specialLength = specialCode.length();

        specialCodes.add(specialCode);
        specialLengths.add(specialLength);
        if (specialLength>2) {
            String scCap = specialCode.substring(1);
            specialCodes.add(scCap);
            specialLengths.add(scCap.length());
            String scSing = specialCode.substring(0, specialLength-1);
            specialCodes.add(scSing);
            specialLengths.add(scSing.length());
        } // if

        if (specialLength!=0) {
            String[] pathAndName = getPathAndName(relativePath);
            String specialCode2 = getLastPathElement("", pathAndName[0]);
            int specialLength2 = specialCode2.length();
            if (specialLength2>2) {
                String scCap = specialCode2.substring(1);
                specialCodes.add(scCap);
                specialLengths.add(scCap.length());
                String scSing = specialCode2.substring(0, specialLength2-1);
                specialCodes.add(scSing);
                specialLengths.add(scSing.length());
            } // if
        } // if
    } // generateSpecialCodes()


    protected String getDecodedFileName(String relativePath, byte[] bytes) {
        List<String> specialCodes = new ArrayList<>();
        List<Integer> specialLengthes = new ArrayList<>();

        generateSpecialCodes(relativePath, specialCodes, specialLengthes);

        StringBuilder result = new StringBuilder();
        int index = 0;
        int bc = 0;
        int currentBitSize = 6;
        for (int i = 0; i<bytes.length; i++) {
            for (int mask = 128; mask>0; mask = mask>>1) {
                int bit = ((bytes[i]&mask)==0) ? 0 : 1;
                index = (index<<1)+bit;
                bc++;
                if (bc==currentBitSize) {
                    char code = FILE_NAME_CHARACTERS[index];
                    if (code=='|') {
                        currentBitSize = 7;
                    } else {
                        if (code!='/') {
                            String value = ""+code;
                            for (int sci = 0; sci<specialCodes.size(); sci++) {
                                if (code==DYNAMIC_SPECIAL_CODES[sci]) {
                                    value = specialCodes.get(sci);
                                } // if
                            } // for
                            result.append(value);
                        } else {
                            // TODO: this is the element which will later whipe out trailling stuff
                            i = bytes.length; // end first loop
                            mask = 0; // break second loop
                        } // if
                        currentBitSize = 6;
                    } // if
                    bc = 0;
                    index = 0;
                } // if
            } // for
        } // for
        return result.toString();
    } // getDecodedFileName()


    protected byte[] getEncodedFileName(String relativePath, String name) {
        List<String> specialCodes = new ArrayList<>();
        List<Integer> specialLengths = new ArrayList<>();
        generateSpecialCodes(relativePath, specialCodes, specialLengths);

        List<Byte> resultList = new ArrayList<>();

        int bc = 0;
        byte value = 0;
        for (int i = 0; i<name.length(); i++) {
            char code = name.charAt(i);
            if ((int) code>256) {
                LOG.error("getEncodedFileName() Strange code at "+name.charAt(i)+" ("+relativePath+":"+name+")");
            } // if
            boolean noSpecial = true;
            for (int sci = 0; (sci<specialCodes.size())&&(noSpecial); sci++) {
                Integer sl = specialLengths.get(sci);
                if (sl!=0) {
                    String sc = specialCodes.get(sci);
                    if (name.indexOf(sc, i)==i) {
                        // if (log.isWarnEnabled()) {
                        // log.warn("using special character '"+DYNAMIC_SPECIAL_CODES[sci]+" "+specialCodes.get(sci)+"' in "
                        // +relativePath+getSeparator()+name);
                        // } // if
                        code = DYNAMIC_SPECIAL_CODES[sci];
                        noSpecial = false;
                        i--;
                        i += sl;
                    } // if
                } // if
            } // for
            byte index = reverseCharacters[code];
            // counters[code]++ ;
            if (index>longIndex) {
                // issue prefix character with 6 bits
                for (int mask = 32; mask>0; mask = mask>>1) {
                    int bit = (longIndex&mask)==0 ? 0 : 1;
                    value = (byte) ((value<<1)+bit);
                    bc++;
                    if (bc==8) {
                        resultList.add(value);
                        bc = 0;
                        value = 0;
                    } // if
                } // for
                // issue long 7 bit character
                for (int mask = 64; mask>0; mask = mask>>1) {
                    int bit = (index&mask)==0 ? 0 : 1;
                    // System.out.print(bit);
                    value = (byte) ((value<<1)+bit);
                    bc++;
                    if (bc==8) {
                        resultList.add(value);
                        bc = 0;
                        value = 0;
                    } // if
                } // for
                // System.out.print("|");
            } else {
                // issue short 6 bit character
                for (int mask = 32; mask>0; mask = mask>>1) {
                    int bit = (index&mask)==0 ? 0 : 1;
                    // System.out.print(bit);
                    value = (byte) ((value<<1)+bit);
                    bc++;
                    if (bc==8) {
                        resultList.add(value);
                        bc = 0;
                        value = 0;
                    } // if
                } // for
                // System.out.print("|");
            } // if
        } // for
        if (bc>0) {
            value = (byte) (value<<(8-bc));
            if (bc==2) {
                value = (byte) (value|(reverseCharacters['/']));
            } // if
            if (bc==1) {
                value = (byte) (value|(2*reverseCharacters['/']));
            } // if
            resultList.add(value);
        } // if
        // System.out.println(" - "+bc);

        byte[] result = new byte[resultList.size()];

        int i = 0;
        for (byte b : resultList) {
            result[i++] = b;
        } // for

        return result;
    } // getEncodedFileName()


    protected String getDecryptedFileName(String relativePath, String name) {
        if (decryptionCache.containsKey(relativePath+getSeparator()+name)) {
            return decryptionCache.get(relativePath+getSeparator()+name);
        } // if
        try {
            List<Byte> resultList = new ArrayList<>();

            // System.out.print("D: ");
            int bc = 0;
            byte value = 0;
            for (int i = 0; i<name.length(); i++) {
                char code = name.charAt(i);
                byte index = reverseCodes[code];
                for (int mask = decodingMask; mask>0; mask = mask>>1) {
                    int bit = (index&mask)==0 ? 0 : 1;
                    // System.out.print(bit);
                    value = (byte) ((value<<1)+bit);
                    bc++;
                    if (bc==8) {
                        // System.out.print(" "+value+" ");
                        resultList.add(value);
                        bc = 0;
                        value = 0;
                    } // if
                } // for
            } // for

            byte[] decodedBytes = new byte[resultList.size()];
            int i = 0;
            for (byte b : resultList) {
                decodedBytes[i++] = b;
            } // for

            // System.out.println("");
            Cipher decrypter = SecurityUtils.getCipher(getCipherSpec(), Cipher.DECRYPT_MODE, getCredentials(relativePath));
            byte[] decryptedBytes = decrypter.doFinal(decodedBytes);
            // name = new String(decryptedBytes, "UTF-8");
            String decryptedName = getDecodedFileName(relativePath, decryptedBytes);
            decryptionCache.put(relativePath+getSeparator()+name, decryptedName);
            name = decryptedName;
        } catch (NoSuchAlgorithmException nsae) {
            LOG.error("getDecryptedFileName() No Such Algorhithm "+nsae.getLocalizedMessage());
        } catch (NoSuchPaddingException nspe) {
            LOG.error("getDecryptedFileName() No Such Padding "+nspe.getLocalizedMessage());
        } catch (InvalidKeyException ike) {
            LOG.error("getDecryptedFileName() Invalid Key "+ike.getLocalizedMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.error("getDecryptedFileName() ArrayIndexOutOfBoundsException", e);
        } catch (IllegalBlockSizeException e) {
            LOG.error("getDecryptedFileName() IllegalBlockSizeException", e);
        } catch (BadPaddingException e) {
            LOG.error("getDecryptedFileName() BadPaddingException", e);
        } // try/catch
        return name;
    } // getDecryptedFileName()


    protected String getEncryptedFileName(String relativePath, String pathElement) {
        if (encryptionCache.containsKey(relativePath+getSeparator()+pathElement)) {
            return encryptionCache.get(relativePath+getSeparator()+pathElement);
        } // if
        try {
            Cipher encrypter = SecurityUtils.getCipher(getCipherSpec(), Cipher.ENCRYPT_MODE, getCredentials(relativePath));
            byte[] bytes = encrypter.doFinal(getEncodedFileName(relativePath, pathElement));
            StringBuilder result = new StringBuilder();
            int index = 0;
            int bc = 0;
            // System.out.print("E: ");
            for (int i = 0; i<bytes.length; i++) {
                for (int mask = 128; mask>0; mask = mask>>1) {
                    int bit = ((bytes[i]&mask)==0) ? 0 : 1;
                    // System.out.print(bit);
                    index = (index<<1)+bit;
                    bc++;
                    if (bc==bits) {
                        char code = CODES[index];
                        result.append(code);
                        bc = 0;
                        index = 0;
                    } // if
                } // for
                // System.out.print(" "+bytes[i]+" ");
            } // for
            index = index<<(bits-bc);
            char code = CODES[index];
            result.append(code);
            String resultString = result.toString();
            // System.out.println("");
            encryptionCache.put(relativePath+getSeparator()+pathElement, resultString);
            pathElement = resultString;
        } catch (NoSuchAlgorithmException nsae) {
            LOG.error("getEncryptedFileName() No Such Algorhithm "+nsae.getLocalizedMessage());
        } catch (NoSuchPaddingException nspe) {
            LOG.error("getEncryptedFileName() No Such Padding "+nspe.getLocalizedMessage());
        } catch (InvalidKeyException ike) {
            LOG.error("getEncryptedFileName() Invalid Key "+ike.getLocalizedMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.error("getEncryptedFileName("+pathElement+") ArrayIndexOutOfBoundsException", e);
        } catch (IllegalBlockSizeException e) {
            LOG.error("getEncryptedFileName("+pathElement+") IllegalBlockSizeException", e);
        } catch (BadPaddingException e) {
            LOG.error("getEncryptedFileName("+pathElement+") BadPaddingException", e);
        } // try/catch
        return pathElement;
    } // getEncryptedFileName()


    protected String getFileName(String relativePath) {
        String[] pathElements = relativePath.split("\\"+getSeparator());
        String path = "";
        String originalPath = "";
        for (String pathElement : pathElements) {
            if (StringUtils.isNotEmpty(pathElement)) {
                String encodedString = getEncryptedFileName(originalPath, pathElement);
                String checkBack = getDecryptedFileName(originalPath, encodedString);
                // log.warn("getFile() "+pathElement+" == "+checkBack+"?");
                if (!checkBack.equals(pathElement)) {
                    LOG.error("getFileName("+originalPath+") "+pathElement+" != "+checkBack+" in "+relativePath);
                    throw new RuntimeException("getFileName() "+pathElement+" != "+checkBack);
                } // if
                path += getSeparator()+encodedString;
                originalPath += getSeparator()+pathElement;
            } // if
        } // for
        LOG.debug("getFilename() {} -> {}", relativePath, path);
        if (path.length()>253) {
            LOG.warn("getFileName() long path {} for {}", path.length(), relativePath);
        } // if
        return path;
    } // getFileName()


    protected String getMetaDataFileName(String relativePath) {
        StringBuilder result = new StringBuilder(getLastPathElement(JFSConfig.getInstance().getEncryptionPassPhrase(), relativePath));
        result.reverse();
        if (result.length()>8) {
            result.delete(0, result.length()-8);
        } // if
        result.append(".mt");
        return result.toString();
    } // getMetaDataFileName()


    protected String getMetaDataPath(String relativePath) {
        return relativePath+getSeparator()+getMetaDataFileName(relativePath);
    } // getMetaDataPath()

} // AbstractDisguiseStorageAccess
