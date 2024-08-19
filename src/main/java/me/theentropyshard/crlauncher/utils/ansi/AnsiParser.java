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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnsiParser {
    private static final String ANSI_COLOR_REGEX = "\\u001B\\[(\\d+;)?\\d+m";

    public static List<AnsiPart> parseAnsiString(String input) {
        List<AnsiPart> parts = new ArrayList<>();
        Pattern pattern = Pattern.compile(AnsiParser.ANSI_COLOR_REGEX);
        Matcher matcher = pattern.matcher(input);

        AnsiColor currentColor = AnsiColor.NONE;

        int lastEnd = 0;

        while (matcher.find()) {
            // Append text before the current match
            if (lastEnd < matcher.start()) {
                String textSegment = input.substring(lastEnd, matcher.start());
                if (!textSegment.isEmpty()) {
                    parts.add(new AnsiPart(currentColor, textSegment));
                }
            }

            // Update the current color based on the ANSI code
            currentColor = AnsiColor.of(matcher.group());

            lastEnd = matcher.end();
        }

        // Append any remaining text after the last match
        if (lastEnd < input.length()) {
            String textSegment = input.substring(lastEnd);
            if (!textSegment.isEmpty()) {
                parts.add(new AnsiPart(currentColor, textSegment));
            }
        }

        return parts;
    }
}
