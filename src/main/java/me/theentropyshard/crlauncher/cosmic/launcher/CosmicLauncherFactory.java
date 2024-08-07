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

package me.theentropyshard.crlauncher.cosmic.launcher;

import java.nio.file.Path;

public class CosmicLauncherFactory {
    public static CosmicLauncher getLauncher(LaunchType type,
                                             Path runDir,
                                             Path gameFilesLocation,
                                             Path clientPath) {

        return CosmicLauncherFactory.getLauncher(type, runDir, gameFilesLocation, clientPath, null, null);
    }

    public static CosmicLauncher getLauncher(LaunchType type,
                                             Path runDir,
                                             Path gameFilesLocation,
                                             Path clientPath,
                                             Path modsDir,
                                             String version
    ) {

        if (type == LaunchType.VANILLA) {
            return new VanillaCosmicLauncher(runDir, gameFilesLocation, clientPath);
        } else {
            if (modsDir == null) {
                throw new IllegalArgumentException("Mods dir must not be null when launching with mods");
            }

            if (version == null) {
                throw new IllegalArgumentException("Mod loader version must not be null when launching with mods");
            }

            if (type == LaunchType.FABRIC) {
                return new FabricCosmicLauncher(runDir, gameFilesLocation, clientPath, modsDir, version);
            } else if (type == LaunchType.QUILT) {
                return new QuiltCosmicLauncher(runDir, gameFilesLocation, clientPath, modsDir, version);
            } else if (type == LaunchType.PUZZLE) {
                return new PuzzleCosmicLauncher(runDir, gameFilesLocation, clientPath, modsDir, version);
            }
        }

        throw new IllegalArgumentException("Unknown launch type: " + type);
    }
}
