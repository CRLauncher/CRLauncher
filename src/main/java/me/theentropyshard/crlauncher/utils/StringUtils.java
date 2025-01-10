/*
 * CRLauncher - https://github.com/CRLauncher/CRLauncher
 * Copyright (C) 2024-2025 CRLauncher
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

public final class StringUtils {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    public static String getRandomString(int length) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            builder.append(StringUtils.ALPHABET.charAt(
                    (int) (Math.random() * StringUtils.ALPHABET.length())
            ));
        }

        return builder.toString();
    }

    public static String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static boolean notNullNotEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    public StringUtils() {
        throw new UnsupportedOperationException();
    }
}
