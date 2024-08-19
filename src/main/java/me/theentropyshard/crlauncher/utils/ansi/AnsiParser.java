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
    public static List<AnsiPart> splitAnsiString(String inputString) {
        List<AnsiPart> result = new ArrayList<>();
        Pattern ansiEscape = Pattern.compile("(\033\\[[0-9;]*m)");
        Matcher matcher = ansiEscape.matcher(inputString);

        int lastEnd = 0;
        while (matcher.find()) {
            if (lastEnd < matcher.start()) {
                result.add(new AnsiPart(AnsiColor.of(""), inputString.substring(lastEnd, matcher.start())));
            }
            result.add(new AnsiPart(AnsiColor.of(matcher.group(1)), matcher.group(0)));
            lastEnd = matcher.end();
        }

        if (lastEnd < inputString.length()) {
            result.add(new AnsiPart(AnsiColor.of(""), inputString.substring(lastEnd)));
        }

        return result;
    }
}
