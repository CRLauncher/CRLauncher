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

package me.theentropyshard.crlauncher.utils.ansi;

import java.util.HashMap;
import java.util.Map;

public enum AnsiColor {
    RESET("\u001B[0m"),
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m"),

    BRIGHT_BLACK("\u001B[90m"),
    BRIGHT_RED("\u001B[91m"),
    BRIGHT_GREEN("\u001B[92m"),
    BRIGHT_YELLOW("\u001B[93m"),
    BRIGHT_BLUE("\u001B[94m"),
    BRIGHT_PURPLE("\u001B[95m"),
    BRIGHT_CYAN("\u001B[96m"),
    BRIGHT_WHITE("\u001B[97m"),

    BG_BLACK("\u001B[40m"),
    BG_RED("\u001B[41m"),
    BG_GREEN("\u001B[42m"),
    BG_YELLOW("\u001B[43m"),
    BG_BLUE("\u001B[44m"),
    BG_PURPLE("\u001B[45m"),
    BG_CYAN("\u001B[46m"),
    BG_WHITE("\u001B[47m"),

    BRIGHT_BG_BLACK("\u001B[100m"),
    BRIGHT_BG_RED("\u001B[101m"),
    BRIGHT_BG_GREEN("\u001B[102m"),
    BRIGHT_BG_YELLOW("\u001B[103m"),
    BRIGHT_BG_BLUE("\u001B[104m"),
    BRIGHT_BG_PURPLE("\u001B[105m"),
    BRIGHT_BG_CYAN("\u001B[106m"),
    BRIGHT_BG_WHITE("\u001B[107m"),

    NONE("");

    private static final Map<String, AnsiColor> lookup = new HashMap<>();

    static {
        for (AnsiColor color : AnsiColor.values()) {
            lookup.put(color.getCode(), color);
        }
    }

    private final String code;

    AnsiColor(String code) {
        this.code = code;
    }

    public static AnsiColor of(String code) {
        AnsiColor color = lookup.get(code);

        if (color == null) {
            throw new IllegalArgumentException("Unknown ANSI code");
        }

        return color;
    }

    public String getCode() {
        return this.code;
    }

    public static String stripAnsiCodes(String input) {
        return input.replaceAll("\u001B\\[[;\\d]*m", "");
    }
}