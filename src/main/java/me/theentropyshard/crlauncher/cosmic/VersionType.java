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

package me.theentropyshard.crlauncher.cosmic;

import java.util.HashMap;
import java.util.Map;

public enum VersionType {
    PRE_ALPHA("pre_alpha", "Pre-Alpha");

    private static final Map<String, VersionType> lookup = new HashMap<>();

    static {
        for (VersionType type : VersionType.values()) {
            lookup.put(type.getJsonName(), type);
        }
    }

    private final String jsonName;
    private final String readableName;

    VersionType(String jsonName, String readableName) {
        this.jsonName = jsonName;
        this.readableName = readableName;
    }

    public static VersionType getByJsonName(String jsonName) {
        VersionType type = lookup.get(jsonName);

        if (type == null) {
            throw new IllegalArgumentException("Unknown version type: " + jsonName);
        }

        return type;
    }

    @Override
    public String toString() {
        return this.jsonName;
    }

    public String getJsonName() {
        return this.jsonName;
    }

    public String getReadableName() {
        return this.readableName;
    }
}
