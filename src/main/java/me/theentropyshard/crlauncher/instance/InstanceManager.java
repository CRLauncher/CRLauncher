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

import me.theentropyshard.crlauncher.java.JavaLocator;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.StringUtils;
import me.theentropyshard.crlauncher.utils.ZipUtils;
import me.theentropyshard.crlauncher.utils.json.Json;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstanceManager {


    private final Path workDir;
    private final List<CosmicInstance> instances;
    private final Map<String, CosmicInstance> instancesByName;

    public InstanceManager(Path workDir) {
        this.workDir = workDir;
        this.instances = new ArrayList<>();
        this.instancesByName = new HashMap<>();
    }

    public void load() throws IOException {
        List<Path> paths = FileUtils.list(this.workDir);

        for (Path path : paths) {
            if (!Files.isDirectory(path)) {
                continue;
            }

            this.loadInstance(path);
        }
    }

    private CosmicInstance loadInstance(Path instanceDir) throws IOException {
        Path instanceFile = instanceDir.resolve("instance.json");

        if (!Files.exists(instanceFile)) {
            return null;
        }

        CosmicInstance instance = Json.parse(FileUtils.readUtf8(instanceFile), CosmicInstance.class);
        instance.setWorkDir(instanceDir);

        this.cacheInstance(instance);

        return instance;
    }

    public void reload() throws IOException {
        this.uncacheAll();
        this.load();
    }

    private void cacheInstance(CosmicInstance instance) {
        if (this.instancesByName.containsKey(instance.getName())) {
            return;
        }

        this.instances.add(instance);
        this.instancesByName.put(instance.getName(), instance);
    }

    private void uncacheInstance(CosmicInstance instance) {
        if (!this.instancesByName.containsKey(instance.getName())) {
            return;
        }

        this.instances.remove(instance);
        this.instancesByName.remove(instance.getName());
    }

    private void uncacheAll() {
        this.instances.clear();
        this.instancesByName.clear();
    }

    private Path findFreeName(String suggestion) {
        Path path = this.workDir.resolve(suggestion);

        if (Files.exists(path)) {
            suggestion = suggestion + "_";
            path = this.workDir.resolve(suggestion);

            if (!Files.exists(path)) {
                return path;
            }

            return this.findFreeName(suggestion);
        }

        return path;
    }

    private Path getInstanceWorkDir(String suggestedName, String cosmicVersion) {
        String cleanName = FileUtils.sanitizeFileName(suggestedName);

        if (cleanName.isEmpty()) {
            cleanName = "instance" + cosmicVersion;
        }

        Path freeName;

        try {
            freeName = this.findFreeName(cleanName);
        } catch (StackOverflowError | Exception e) {
            Log.warn("Unable to find free name for instance: " + e.getMessage());

            freeName = this.workDir.resolve(StringUtils.getRandomString(10));
        }

        return freeName;
    }

    public CosmicInstance createInstance(String name, String groupName, String cosmicVersion, boolean autoUpdate) throws
        IOException,
        InstanceAlreadyExistsException {

        if (this.instancesByName.containsKey(name)) {
            throw new InstanceAlreadyExistsException(name);
        }

        CosmicInstance instance = new CosmicInstance(name, groupName, cosmicVersion);
        instance.setWorkDir(this.getInstanceWorkDir(name, cosmicVersion));
        instance.setAutoUpdateToLatest(autoUpdate);
        instance.setJavaPath(JavaLocator.getJavaPath());

        this.cacheInstance(instance);

        FileUtils.createDirectoryIfNotExists(instance.getWorkDir());
        FileUtils.createDirectoryIfNotExists(instance.getCosmicDir());
        FileUtils.createDirectoryIfNotExists(instance.getJarModsDir());

        Path modsDir = instance.getCosmicDir().resolve("mods");
        FileUtils.createDirectoryIfNotExists(modsDir);

        instance.save();

        return instance;
    }

    public void removeInstance(String name) throws IOException {
        CosmicInstance instance = this.getInstanceByName(name);

        if (instance == null) {
            return;
        }

        FileUtils.delete(instance.getWorkDir());

        this.uncacheInstance(instance);
    }

    public boolean renameInstance(CosmicInstance instance, String newName) throws IOException {
        this.uncacheInstance(instance);

        Path newInstanceDir = this.getInstanceWorkDir(newName, instance.getCosmicVersion());

        Files.move(instance.getWorkDir(), newInstanceDir, StandardCopyOption.REPLACE_EXISTING);

        instance.setWorkDir(newInstanceDir);

        instance.setName(newName);

        this.cacheInstance(instance);

        return false;
    }

    public InstanceImportResult importInstance(Path file) throws IOException {
        try (ZipFile zipFile = new ZipFile(file.toFile())) {
            List<FileHeader> fileHeaders = zipFile.getFileHeaders();
            if (fileHeaders.isEmpty()) {
                return new InstanceImportResult(
                    InstanceImportStatus.BAD_FILE,
                    "empty zip"
                );
            }

            String fileName = ZipUtils.findTopLevelDirectory(fileHeaders);
            if (fileName == null) {
                return new InstanceImportResult(
                    InstanceImportStatus.BAD_FILE,
                    "cannot find top level directory in zip"
                );
            }

            if (Files.exists(this.workDir.resolve(fileName))) {
                return new InstanceImportResult(
                    InstanceImportStatus.INSTANCE_EXISTS,
                    fileName
                );
            }

            zipFile.extractAll(this.workDir.toString());

            Path instanceDir = this.workDir.resolve(fileName);
            CosmicInstance instance = this.loadInstance(instanceDir);
            if (instance == null) {
                FileUtils.delete(instanceDir);

                return new InstanceImportResult(
                    InstanceImportStatus.BAD_FILE,
                    "no instance.json in zip"
                );
            }

            return new InstanceImportResult(InstanceImportStatus.SUCCESS, instance);
        }
    }

    public static final class InstanceImportResult {
        private final InstanceImportStatus status;
        private final Object message;

        public InstanceImportResult(InstanceImportStatus status, Object message) {
            this.status = status;
            this.message = message;
        }

        public InstanceImportStatus getStatus() {
            return this.status;
        }

        public Object getMessage() {
            return this.message;
        }
    }

    public enum InstanceImportStatus {
        SUCCESS,
        BAD_FILE,
        INSTANCE_EXISTS
    }

    public CosmicInstance getInstanceByName(String name) {
        return this.instancesByName.get(name);
    }

    public List<CosmicInstance> getInstances() {
        return this.instances;
    }
}
