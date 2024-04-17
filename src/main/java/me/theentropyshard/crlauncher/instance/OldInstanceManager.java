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

import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OldInstanceManager {
    private final Path workDir;
    private final List<OldInstance> oldInstances;
    private final Map<String, OldInstance> instancesByName;

    public OldInstanceManager(Path workDir) {
        this.workDir = workDir;
        this.oldInstances = new ArrayList<>();
        this.instancesByName = new HashMap<>();
    }

    public void load() throws IOException {
        if (!Files.exists(this.workDir)) {
            FileUtils.createDirectoryIfNotExists(this.workDir);
        }

        List<Path> paths = FileUtils.list(this.workDir);

        for (Path path : paths) {
            if (!Files.isDirectory(path)) {
                continue;
            }

            Path instanceFile = path.resolve("instance.json");
            if (!Files.exists(instanceFile)) {
                continue;
            }

            OldInstance oldInstance = Json.parse(FileUtils.readUtf8(instanceFile), OldInstance.class);
            if (oldInstance.getDirName() == null) {
                oldInstance.setDirName(oldInstance.getName());
            }

            this.oldInstances.add(oldInstance);
            this.instancesByName.put(oldInstance.getName(), oldInstance);
        }
    }

    private void createDirName(OldInstance oldInstance) {
        if (Files.exists(this.getInstanceDir(oldInstance))) {
            oldInstance.setDirName(oldInstance.getDirName() + "_");
            if (!Files.exists(this.getInstanceDir(oldInstance))) {
                return;
            }

            this.createDirName(oldInstance);
        }
    }

    public void reload() throws IOException {
        this.oldInstances.clear();
        this.instancesByName.clear();
        this.load();
    }

    public void save(OldInstance oldInstance) throws IOException {
        Path instanceDir = this.getInstanceDir(oldInstance);
        FileUtils.createDirectoryIfNotExists(instanceDir);

        Path instanceFile = instanceDir.resolve("instance.json");
        FileUtils.writeUtf8(instanceFile, Json.write(oldInstance));
    }

    public void createInstance(String name, String groupName, String minecraftVersion) throws IOException {
        OldInstance oldInstance = new OldInstance(name, groupName, minecraftVersion);

        oldInstance.setDirName(oldInstance.getName());
        this.createDirName(oldInstance);

        Path instanceDir = this.getInstanceDir(oldInstance);
        if (Files.exists(instanceDir)) {
            throw new IOException("Instance dir '" + instanceDir + "' already exists");
        }

        this.oldInstances.add(oldInstance);
        this.instancesByName.put(name, oldInstance);

        FileUtils.createDirectoryIfNotExists(instanceDir);
        Path cosmicDir = instanceDir.resolve("cosmic-reach");
        FileUtils.createDirectoryIfNotExists(cosmicDir);
        Path jarModsDir = this.getInstanceJarModsDir(oldInstance);
        FileUtils.createDirectoryIfNotExists(jarModsDir);
        Path instanceFile = instanceDir.resolve("instance.json");
        FileUtils.writeUtf8(instanceFile, Json.write(oldInstance));
    }

    public void removeInstance(String name) throws IOException {
        OldInstance oldInstance = this.getInstanceByName(name);
        if (oldInstance == null) {
            return;
        }

        Path instanceDir = this.getInstanceDir(oldInstance);
        if (Files.exists(instanceDir)) {
            FileUtils.delete(instanceDir);
        }

        this.oldInstances.remove(oldInstance);
        this.instancesByName.remove(name);
    }

    public boolean instanceExists(String name) {
        OldInstance oldInstance = this.getInstanceByName(name);
        if (oldInstance == null) {
            return false;
        }

        Path instanceDir = this.getInstanceDir(oldInstance);
        if (Files.exists(instanceDir)) {
            return true;
        } else {
            this.oldInstances.remove(oldInstance);
            this.instancesByName.remove(name);
            return false;
        }
    }

    public Path getInstanceDir(OldInstance oldInstance) {
        return this.workDir.resolve(oldInstance.getDirName());
    }

    public Path getCosmicDir(OldInstance oldInstance) {
        return this.workDir.resolve(oldInstance.getDirName()).resolve("cosmic-reach");
    }

    public Path getInstanceJarModsDir(OldInstance oldInstance) {
        return this.workDir.resolve(oldInstance.getDirName()).resolve("jarmods");
    }

    public Path getFabricModsDir(OldInstance oldInstance) {
        return this.getCosmicDir(oldInstance).resolve("fabric_mods");
    }

    public Path getQuiltModsDir(OldInstance instance) {
        return this.getCosmicDir(instance).resolve("quilt_mods");
    }

    public OldInstance getInstanceByName(String name) {
        return this.instancesByName.get(name);
    }

    public List<OldInstance> getInstances() {
        return this.oldInstances;
    }
}
