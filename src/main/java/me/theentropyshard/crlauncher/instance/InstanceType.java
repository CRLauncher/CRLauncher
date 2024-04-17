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

package me.theentropyshard.crlauncher.instance;

import java.util.HashMap;
import java.util.Map;

public enum InstanceType {
    VANILLA("vanilla", "Vanilla"),
    FABRIC("fabric", "Fabric"),
    QUILT("quilt", "Cosmic Quilt");

    private static final Map<String, InstanceType> lookup = new HashMap<>();

    static {
        for (InstanceType type : InstanceType.values()) {
            lookup.put(type.getType(), type);
        }
    }

    private final String type;
    private final String name;

    InstanceType(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public static InstanceType getByType(String jsonType) {
        InstanceType type = lookup.get(jsonType);

        if (type == null) {
            throw new IllegalArgumentException("Unknown instance type: " + jsonType);
        }

        return type;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }
}
