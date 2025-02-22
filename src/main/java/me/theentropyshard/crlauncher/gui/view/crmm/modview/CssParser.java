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

package me.theentropyshard.crlauncher.gui.view.crmm.modview;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple parser that splits CSS file into separate rules
 */
public class CssParser {
    public static List<String> parse(String input) {
        List<String> rules = new ArrayList<>();
        StringBuilder rule = new StringBuilder();

        char[] chars = input.toCharArray();

        boolean comment = false;

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '/' && chars[i + 1] == '*') {
                comment = true;
            }

            if (chars[i] == '*' && chars[i + 1] == '/') {
                comment = false;
            }

            if (comment) {
                continue;
            }

            rule.append(chars[i]);

            if (chars[i] == '}') {
                rules.add(rule.toString());
                rule.setLength(0);
            }
        }

        return rules;
    }
}
