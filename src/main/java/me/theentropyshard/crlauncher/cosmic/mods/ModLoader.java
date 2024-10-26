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

package me.theentropyshard.crlauncher.cosmic.mods;

import java.util.HashMap;
import java.util.Map;

public enum ModLoader {
    VANILLA("vanilla", "Vanilla"),
    FABRIC("fabric", "Fabric"),
    QUILT("quilt", "Cosmic Quilt"),
    PUZZLE("puzzle", "Puzzle");

    private static final Map<String, ModLoader> lookup = new HashMap<>();

    static {
        for (ModLoader type : ModLoader.values()) {
            ModLoader.lookup.put(type.getLoader(), type);
        }
    }

    private final String loader;
    private final String name;

    ModLoader(String loader, String name) {
        this.loader = loader;
        this.name = name;
    }

    public static ModLoader getByName(String jsonName) {
        ModLoader type = ModLoader.lookup.get(jsonName);

        if (type == null) {
            throw new IllegalArgumentException("Unknown mod loader: " + jsonName);
        }

        return type;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String getLoader() {
        return this.loader;
    }

    public String getName() {
        return this.name;
    }
}
