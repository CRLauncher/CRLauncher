/*
 * CRLauncher - https://github.com/CRLauncher/CRLauncher
 * Copyright (C) 2024 CRLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.theentropyshard.crlauncher.utils;

import org.apache.commons.codec.digest.MurmurHash3;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtils {
    public static String sha1(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            byte[] dataBytes = new byte[1024];

            int numRead;
            while ((numRead = inputStream.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, numRead);
            }

            byte[] mdBytes = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : mdBytes) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException("SHA-1 algorithm is not available in your JRE", ex);
        }
    }

    public static String murmur3(Path file) throws IOException {
        long[] twoLongs = MurmurHash3.hash128x64(Files.readAllBytes(file));

        byte[] bytes = new byte[Long.BYTES * 2];

        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.putLong(twoLongs[0]);
        byteBuffer.putLong(twoLongs[1]);

        return new BigInteger(bytes).abs().toString(16);
    }

    private HashUtils() {
        throw new UnsupportedOperationException();
    }
}
