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

package me.theentropyshard.crlauncher.utils.ansi;

public class AnsiPart {
    private final AnsiColor color;
    private final String text;

    AnsiPart(AnsiColor color, String text) {
        this.color = color;
        this.text = text;
    }

    @Override
    public String toString() {
        return this.color.getCode() + this.text;
    }

    public AnsiColor getColor() {
        return this.color;
    }

    public String getText() {
        return this.text;
    }
}