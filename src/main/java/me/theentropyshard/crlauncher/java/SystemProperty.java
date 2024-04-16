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

package me.theentropyshard.crlauncher.java;

public class SystemProperty {
    private final String prefix;
    private final String name;

    public SystemProperty(String prefix, String name) {
        this.prefix = prefix;
        this.name = name;
    }

    public String get() {
        return this.get(null);
    }

    public String get(String def) {
        String p = System.getProperty(this.prefix + "." + this.name);

        if (p == null) {
            return def;
        }

        return p;
    }

    public String asJvmArg(Object value) {
        return "-D" + this.prefix + "." + this.name + "=" + value;
    }
}
