/*
 * Copyright (C) 2010-2013, Martin Goellnitz
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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public final class SecurityUtils {

    private static final BouncyCastleProvider PROVIDER = new BouncyCastleProvider();


    private SecurityUtils() {
    }


    public static Cipher getCipher(String cipherName, int cipherMode, byte[] credentials) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException {
        SecretKeySpec keySpec = new SecretKeySpec(credentials, cipherName);
        Cipher cipher = Cipher.getInstance(cipherName, PROVIDER);
        cipher.init(cipherMode, keySpec);
        return cipher;
    } // getCipher()


    public static Cipher getPasswordCipher(String cipherName, int cipherMode, String password, String salt)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchPaddingException,
            InvalidKeyException {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        PBEParameterSpec paramSpec = new PBEParameterSpec(salt.getBytes(), password.length());
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance(cipherName);
        SecretKey pbeKey = keyFac.generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance(cipherName, PROVIDER);
        cipher.init(cipherMode, pbeKey, paramSpec);
        return cipher;
    } // getPasswordCipher()

} // SecurityUtils()
