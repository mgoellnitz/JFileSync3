/*
 * Copyright (C) 2010-2025, Martin Goellnitz
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
package jfs.sync.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Basic security methods collected in a utility class.
 */
public final class SecurityUtils {

    private static final BouncyCastleProvider PROVIDER = new BouncyCastleProvider();

    private SecurityUtils() {
    }

    /**
     * Convenience method to create a cipher based on byte[] credentials as
     * symmetric key base.
     *
     * @param cipherName name of the algprithm to be used
     * @param decrypt return cipher in decrypt mode if set to true
     * @param credentials credentials to be used as a key
     * @return cipher to be password streams
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     */
    public static Cipher getCipher(String cipherName, boolean decrypt, byte[] credentials)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        int cipherMode = decrypt ? Cipher.DECRYPT_MODE : Cipher.ENCRYPT_MODE;
        SecretKeySpec keySpec = new SecretKeySpec(credentials, cipherName);
        Cipher cipher = Cipher.getInstance(cipherName, PROVIDER);
        cipher.init(cipherMode, keySpec);
        return cipher;
    } // getCipher()

    /**
     *
     * Convenience method to create a cipher based on a given textual password.
     * Currently not used in this project and not necessarily working.
     *
     * @param cipherName name of the algprithm to be used
     * @param decrypt return cipher in decrypt mode if set to true
     * @param password textual password to derive symmetric key from
     * @param salt additional salt for key derivation
     * @return cipher to be password streams
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeySpecException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     */
    public static Cipher getPasswordCipher(String cipherName, boolean decrypt, String password)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchPaddingException,
            InvalidKeyException, UnsupportedEncodingException {
        int cipherMode = decrypt ? Cipher.DECRYPT_MODE : Cipher.ENCRYPT_MODE;
        byte[] salt = new StringBuilder(password).reverse().toString().getBytes("UTF-8");
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 16, 256);
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1", PROVIDER);
        SecretKey intermediate = keyFac.generateSecret(keySpec);
        SecretKey secretKey = new SecretKeySpec(intermediate.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance(cipherName, PROVIDER);
        cipher.init(cipherMode, secretKey);
        return cipher;
    } // getPasswordCipher()

    public static void main(String[] args) {
        System.out.println(PROVIDER.getName());
        for (String key : PROVIDER.stringPropertyNames()) {
            if (key.startsWith("Cipher")) {
                System.out.println("\t" + key + "\t" + PROVIDER.getProperty(key));
            }
        }
    }

} // SecurityUtils
