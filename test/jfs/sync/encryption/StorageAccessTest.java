/*
 * Copyright (C) 2025 Martin Goellnitz
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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * Test some generic functions for encoding and encryption along the storage
 * access way.
 */
public class StorageAccessTest {

    private class StorageAccess extends AbstractEncryptedStorageAccess {

        public StorageAccess(boolean shortenPaths) {
            super(shortenPaths);
        }

        @Override
        public String getSeparator() {
            return "/";
        }

        @Override
        public String getCipherSpec() {
            return "Twofish";
        }

        public int getCodeTableSize() {
            return StorageAccess.CODES.length;
        }

        public int getCharacterTableSize() {
            return StorageAccess.FILE_NAME_CHARACTERS.length;
        }

    } // StorageAccess

    @Test
    public void testCharacterEncodings() {
        StorageAccess access = new StorageAccess(false);
        Assert.assertEquals(access.getCharacterTableSize(), 113, "Unexpected character encoding table size");
        Assert.assertEquals(access.getCodeTableSize(), 128, "Unexpected code table size");
        String filename = "Capture d'écran.png";
        String encryptedFileName = access.getEncryptedFileName("", filename);
        Assert.assertEquals(access.getDecryptedFileName("", encryptedFileName), filename, "Problem in filename encryption");
    } //testCharacterEncodings()

} // StorageAccessTest
