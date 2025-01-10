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

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.java.JavaLocator;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.ProcessReader;
import me.theentropyshard.crlauncher.utils.SystemProperty;

import java.nio.file.Path;
import java.util.*;

public abstract class AbstractCosmicLauncher implements CosmicLauncher {
    private final String javaPath;
    private final Path runDir;
    private final Path gameFilesLocation;
    private final Path clientPath;
    private final List<String> command;
    private final List<SystemProperty> properties;
    private final Set<String> jvmFlags;
    private final Map<String, String> environment;

    public AbstractCosmicLauncher(String javaPath, Path runDir, Path gameFilesLocation, Path clientPath) {
        this.javaPath = javaPath;
        this.runDir = runDir;
        this.gameFilesLocation = gameFilesLocation;
        this.clientPath = clientPath;
        this.command = new ArrayList<>();
        this.properties = new ArrayList<>();
        this.jvmFlags = new LinkedHashSet<>();
        this.environment = new LinkedHashMap<>();
    }

    public void defineProperty(SystemProperty property) {
        this.properties.add(property);
    }

    public void addJvmFlag(String flag) {
        this.jvmFlags.add(flag);
    }

    public void putEnvironment(String key, String value) {
        this.environment.put(key, value);
    }

    public void buildCommand(List<String> command) {
        command.clear();

        command.add(this.getJavaPath());

        for (SystemProperty property : this.properties) {
            command.add(property.asJvmArg());
        }

        command.addAll(this.jvmFlags);
        this.jvmFlags.clear();
    }

    @Override
    public Process launch(boolean exitAfterLaunch) throws Exception {
        this.buildCommand(this.command);

        Log.info("Running: " + String.join(" ", this.command));

        ProcessBuilder processBuilder = new ProcessBuilder(this.command);
        processBuilder.environment().putAll(this.environment);
        processBuilder.directory(this.runDir.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        if (exitAfterLaunch) {
            CRLauncher.getInstance().shutdown();
        }

        return process;
    }

    public String getJavaPath() {
        return this.javaPath;
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
