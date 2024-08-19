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

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public enum AnsiColor {
    RESET("\u001B[0m", null),
    BLACK("\u001B[30m", new Color(0, 0, 0)),
    RED("\u001B[31m", new Color(170, 0, 0)),
    GREEN("\u001B[32m", new Color(0, 170, 0)),
    YELLOW("\u001B[33m", new Color(193, 156, 0)),
    BLUE("\u001B[34m", new Color(0, 0, 170)),
    MAGENTA("\u001B[35m", new Color(170, 0, 170)),
    CYAN("\u001B[36m", new Color(0, 170, 170)),
    WHITE("\u001B[37m", new Color(170, 170, 170)),

    BRIGHT_BLACK("\u001B[90m", new Color(85, 85, 85)),
    BRIGHT_RED("\u001B[91m", new Color(255, 85, 85)),
    BRIGHT_GREEN("\u001B[92m", new Color(85, 255, 85)),
    BRIGHT_YELLOW("\u001B[93m", new Color(255, 255, 85)),
    BRIGHT_BLUE("\u001B[94m", new Color(85, 85, 255)),
    BRIGHT_MAGENTA("\u001B[95m", new Color(255, 85, 255)),
    BRIGHT_CYAN("\u001B[96m", new Color(85, 255, 255)),
    BRIGHT_WHITE("\u001B[97m", new Color(255, 255, 255)),

    /*BG_BLACK("\u001B[40m"),
    BG_RED("\u001B[41m"),
    BG_GREEN("\u001B[42m"),
    BG_YELLOW("\u001B[43m"),
    BG_BLUE("\u001B[44m"),
    BG_MAGENTA("\u001B[45m"),
    BG_CYAN("\u001B[46m"),
    BG_WHITE("\u001B[47m"),

    BRIGHT_BG_BLACK("\u001B[100m"),
    BRIGHT_BG_RED("\u001B[101m"),
    BRIGHT_BG_GREEN("\u001B[102m"),
    BRIGHT_BG_YELLOW("\u001B[103m"),
    BRIGHT_BG_BLUE("\u001B[104m"),
    BRIGHT_BG_MAGENTA("\u001B[105m"),
    BRIGHT_BG_CYAN("\u001B[106m"),
    BRIGHT_BG_WHITE("\u001B[107m"),*/

    NONE("", null);

    private static final Map<String, AnsiColor> lookup = new HashMap<>();

    static {
        for (AnsiColor color : AnsiColor.values()) {
            lookup.put(color.getCode(), color);
        }
    }

    private final String code;
    private final Color color;

    AnsiColor(String code, Color color) {
        this.code = code;
        this.color = color;
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

    public Color getColor() {
        return this.color;
    }

    public static String stripAnsiCodes(String input) {
        return input.replaceAll("\u001B\\[[;\\d]*m", "");
    }
}