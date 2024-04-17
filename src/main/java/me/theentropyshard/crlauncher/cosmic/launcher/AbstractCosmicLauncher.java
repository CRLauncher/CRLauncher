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

import me.theentropyshard.crlauncher.cosmic.launcher.CosmicLauncher;
import me.theentropyshard.crlauncher.cosmic.launcher.LogConsumer;
import me.theentropyshard.crlauncher.java.JavaLocator;
import me.theentropyshard.crlauncher.java.SystemProperty;
import me.theentropyshard.crlauncher.utils.ProcessReader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCosmicLauncher implements CosmicLauncher {
    private final Path runDir;
    private final Path gameFilesLocation;
    private final Path clientPath;
    private final List<String> command;
    private final List<SystemProperty> properties;

    public AbstractCosmicLauncher(Path runDir, Path gameFilesLocation, Path clientPath) {
        this.runDir = runDir;
        this.gameFilesLocation = gameFilesLocation;
        this.clientPath = clientPath;
        this.command = new ArrayList<>();
        this.properties = new ArrayList<>();
    }

    public void defineProperty(SystemProperty property) {
        this.properties.add(property);
    }

    public void buildCommand(List<String> command) {
        command.clear();

        command.add(this.getJavaPath());

        for (SystemProperty property : this.properties) {
            command.add(property.asJvmArg());
        }
    }

    @Override
    public int launch(LogConsumer log) throws Exception {
        this.buildCommand(this.command);

        ProcessBuilder processBuilder = new ProcessBuilder(this.command);
        processBuilder.directory(this.runDir.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        new ProcessReader(process).read(log::line);

        return process.waitFor();
    }

    public String getJavaPath() {
        return JavaLocator.getJavaPath();
    }

    public Path getRunDir() {
        return this.runDir;
    }

    public Path getGameFilesLocation() {
        return this.gameFilesLocation;
    }

    public Path getClientPath() {
        return this.clientPath;
    }
}
