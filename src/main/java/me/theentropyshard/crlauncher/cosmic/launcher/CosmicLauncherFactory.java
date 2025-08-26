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

package me.theentropyshard.crlauncher.cosmic.launcher;

import java.nio.file.Path;

public class CosmicLauncherFactory {
    public static CosmicLauncher getLauncher(String javaPath,
                                             LaunchType type,
                                             Path runDir,
                                             Path gameFilesLocation,
                                             Path clientPath) {

        return CosmicLauncherFactory.getLauncher(javaPath, type, runDir, gameFilesLocation, clientPath, null, null, null);
    }

    public static CosmicLauncher getLauncher(String javaPath,
                                             LaunchType type,
                                             Path runDir,
                                             Path gameFilesLocation,
                                             Path clientPath,
                                             Path modsDir,
                                             String version) {

        return CosmicLauncherFactory.getLauncher(javaPath, type, runDir, gameFilesLocation, clientPath, modsDir, version, null);
    }

    public static CosmicLauncher getLauncher(String javaPath,
                                             LaunchType type,
                                             Path runDir,
                                             Path gameFilesLocation,
                                             Path clientPath,
                                             Path modsDir,
                                             String version,
                                             String version2
    ) {

        if (type == LaunchType.VANILLA) {
            return new VanillaCosmicLauncher(javaPath, runDir, gameFilesLocation, clientPath);
        } else {
            if (modsDir == null) {
                throw new IllegalArgumentException("Mods dir must not be null when launching with mods");
            }

            if (version == null) {
                throw new IllegalArgumentException("Mod loader version must not be null when launching with mods");
            }

            if (type == LaunchType.FABRIC) {
                return new FabricCosmicLauncher(javaPath, runDir, gameFilesLocation, clientPath, modsDir, version);
            } else if (type == LaunchType.QUILT) {
                return new QuiltCosmicLauncher(javaPath, runDir, gameFilesLocation, clientPath, modsDir, version);
            } else if (type == LaunchType.PUZZLE) {
                if (version2 == null) {
                    throw new IllegalArgumentException("Mod loader version must not be null when launching with mods");
                }

                return new PuzzleCosmicLauncher(javaPath, runDir, gameFilesLocation, clientPath, modsDir, version, version2);
            }
        }

        throw new IllegalArgumentException("Unknown launch type: " + type);
    }
}
