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
import java.util.List;

public class VanillaCosmicLauncher extends PatchCosmicLauncher {
    public VanillaCosmicLauncher(String javaPath, Path runDir, Path gameFilesLocation, Path clientPath) {
        super(javaPath, runDir, gameFilesLocation, clientPath);
    }

    @Override
    public void buildCommand(List<String> command) {
        super.buildCommand(command);
        command.add("-javaagent:" + super.setupLoader());

        command.add("-jar");
        command.add(this.getClientPath().toString());
    }
}
