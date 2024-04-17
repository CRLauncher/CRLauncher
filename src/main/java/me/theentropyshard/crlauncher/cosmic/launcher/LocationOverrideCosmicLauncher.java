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

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.utils.OperatingSystem;
import me.theentropyshard.crlauncher.utils.ResourceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LocationOverrideCosmicLauncher extends AbstractCosmicLauncher {
    private static final Logger LOG = LogManager.getLogger(LocationOverrideCosmicLauncher.class);

    private static final String CR_LOADER_VERSION = "0.0.1";
    private static final String CR_LOADER_JAR = "CRLoader-" + LocationOverrideCosmicLauncher.CR_LOADER_VERSION + ".jar";

    public LocationOverrideCosmicLauncher(Path runDir, Path gameFilesLocation, Path clientPath) {
        super(runDir, gameFilesLocation, clientPath);
    }

    private void extractLoader(Path path) {
        if (!Files.exists(path)) {
            try {
                ResourceUtils.extractResource("/assets/" + LocationOverrideCosmicLauncher.CR_LOADER_JAR, path);
            } catch (IOException e) {
                LOG.error("Unable to extract {} to {}", LocationOverrideCosmicLauncher.CR_LOADER_JAR, path);
            }
        }
    }

    @Override
    public void buildCommand(List<String> command) {
        super.buildCommand(command);

        Path loaderPath = CRLauncher.getInstance().getWorkDir()
                .resolve("libraries")
                .resolve(LocationOverrideCosmicLauncher.CR_LOADER_JAR);

        this.extractLoader(loaderPath);

        String gameFilesLocation = this.getGameFilesLocation().toString();
        if (OperatingSystem.isWindows()) {
            gameFilesLocation = gameFilesLocation.replace("\\", "\\\\");
        }

        command.add("-javaagent:" + loaderPath + "=" + gameFilesLocation);
    }
}
