/*
 * Copyright (C) 2025, Martin Goellnitz
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
package jfs.sync.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import jfs.sync.util.SecurityUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * Test the few security methods.
 *
 */
public class SecurityUtilsTest {

    private static final String PASSWORD = "password";

    private static final String CIPHIER_NAME = "AES";

    @Test
    public void testPasswordCipher() throws Exception {
        String data = "This is just a bunch of text to test with.";
        byte[] plain = data.getBytes("UTF-8");
        Assert.assertEquals(plain.length, 42, "Unexpected plaintext bytes count.");

        Cipher encrypt = SecurityUtils.getPasswordCipher(CIPHIER_NAME, false, PASSWORD);
        ByteArrayOutputStream intermediate = new ByteArrayOutputStream(plain.length);
        CipherOutputStream outputStream = new CipherOutputStream(intermediate, encrypt);
        outputStream.write(plain);
        outputStream.flush();
        outputStream.close();
        byte[] encrypted = intermediate.toByteArray();
        Assert.assertEquals(encrypted.length, 48, "Unexpected cryptotext length.");

        Cipher decrypt = SecurityUtils.getPasswordCipher(CIPHIER_NAME, true, PASSWORD);
        CipherInputStream inputStream = new CipherInputStream(new ByteArrayInputStream(encrypted), decrypt);
        byte[] result = inputStream.readAllBytes();
        Assert.assertEquals(result.length, 42, "Unexpected plaintext length.");
    } // testPasswordCipher()

} // SecurityUtilsTest
