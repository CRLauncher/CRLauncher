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
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.network.progress.ProgressListener;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.json.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class VersionManager {
    public static final String REMOTE_VERSIONS = "https://raw.githubusercontent.com/CRModders/CosmicArchive/main/versions.json";

    private Mode mode = Mode.ONLINE;
    private VersionList versionList;

    public VersionManager() {

    }

    public void downloadVersion(Version version, ProgressListener listener) throws IOException {
        CosmicDownloader downloader = new CosmicDownloader();
        downloader.downloadVersion(version, listener);
    }

    public void load() throws IOException {
        this.versionList = switch (this.mode) {
            case ONLINE -> new RemoteVersionList(VersionManager.REMOTE_VERSIONS);
            case OFFLINE -> new LocalVersionList(CRLauncher.getInstance().getVersionsDir());
        };
        this.versionList.load();
    }

    public Version getVersion(String id) {
        return this.versionList.getVersionById(id);
    }

    public Version getLatest() {
        return this.versionList.getLatestVersion();
    }

    public boolean isLoaded() {
        return this.versionList != null && !this.versionList.isEmpty();
    }

    public boolean isLatest(Version version) {
        return version.getId().equals(this.versionList.getLatestVersion().getId());
    }

    public Path getVersionPath(String id, String ext) {
        Path versionsDir = CRLauncher.getInstance().getVersionsDir();

        return versionsDir
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

    public List<Version> getVersions() {
        return this.versionList.getVersions();
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public enum Mode {
        ONLINE,
        OFFLINE
    }
}
