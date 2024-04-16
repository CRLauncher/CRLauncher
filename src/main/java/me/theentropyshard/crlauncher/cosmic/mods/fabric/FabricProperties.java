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

package me.theentropyshard.crlauncher.cosmic.mods.fabric;

import me.theentropyshard.crlauncher.java.SystemProperty;

public final class FabricProperties {
    public static final String MAIN_CLASS = "net.fabricmc.loader.launch.knot.KnotClient";

    public static final SystemProperty SKIP_MC_PROVIDER = new SystemProperty("fabric.skipMcProvider");
    public static final SystemProperty GAME_JAR_PATH = new SystemProperty("fabric.gameJarPath");
    public static final SystemProperty GAME_VERSION = new SystemProperty("fabric.gameVersion");
    public static final SystemProperty MODS_FOLDER = new SystemProperty("fabric.modsFolder");

    private FabricProperties() {
        throw new UnsupportedOperationException();
    }
}
