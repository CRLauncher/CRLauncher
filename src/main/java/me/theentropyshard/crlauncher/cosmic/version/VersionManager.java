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

package me.theentropyshard.crlauncher.cosmic.version;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.CosmicDownloader;
import me.theentropyshard.crlauncher.network.HttpRequest;
import me.theentropyshard.crlauncher.network.progress.ProgressListener;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.json.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class VersionManager {
    public static final String REMOTE_VERSIONS = "https://raw.githubusercontent.com/CRModders/CosmicArchive/main/versions.json";

    private final Path workDir;
    private final Map<String, Version> remoteVersions;

    private VersionList versionList;

    public VersionManager(Path workDir) {
        this.workDir = workDir;
        this.remoteVersions = new LinkedHashMap<>();
    }

    public void downloadVersion(Version version, ProgressListener listener) throws IOException {
        CosmicDownloader downloader = new CosmicDownloader();
        downloader.downloadVersion(version, listener);
    }

    public void loadRemoteVersions() throws IOException {
        try (HttpRequest request = new HttpRequest(CRLauncher.getInstance().getHttpClient())) {
            this.versionList = Json.parse(request.asString(VersionManager.REMOTE_VERSIONS), VersionList.class);

            for (Version version : this.versionList.getVersions()) {
                this.remoteVersions.put(version.getId(), version);
            }
        }
    }

    public Path getVersionPath(String id, String ext) {
        return this.workDir
            .resolve(id)
            .resolve("client")
            .resolve(id + "." + ext);
    }

    public Path getVersionPath(Version version, String ext) {
        return this.getVersionPath(version.getId(), ext);
    }

    public Path getVersionJar(Version version) {
        return this.getVersionPath(version, "jar");
    }

    public Path getVersionJson(String id) {
        return this.getVersionPath(id, "json");
    }

    public Version getVersion(String id) throws IOException {
        Path versionJson = this.getVersionJson(id);
        if (Files.exists(versionJson)) {
            return Json.parse(FileUtils.readUtf8(versionJson), Version.class);
        }

        if (this.remoteVersions.isEmpty()) {
            this.loadRemoteVersions();
        }

        return this.remoteVersions.get(id);
    }

    public List<Version> getRemoteVersions(boolean forceNetwork) throws IOException {
        if (this.remoteVersions.isEmpty() || forceNetwork) {
            this.remoteVersions.clear();
            this.loadRemoteVersions();
        }

        return new ArrayList<>(this.remoteVersions.values());
    }

    public List<Version> getLocalAvailableVersions() throws IOException {
        return Collections.emptyList();
    }

    public VersionList getVersionList() {
        return this.versionList;
    }
}
