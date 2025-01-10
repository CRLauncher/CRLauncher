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

package me.theentropyshard.crlauncher.cosmic.version;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.CosmicArchiveDownloader;
import me.theentropyshard.crlauncher.cosmic.ItchDownloader;
import me.theentropyshard.crlauncher.cosmic.itch.ItchVersion;
import me.theentropyshard.crlauncher.network.progress.ProgressListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class VersionManager {
    public static final String REMOTE_VERSIONS = "https://raw.githubusercontent.com/CRModders/CosmicArchive/main/versions.json";

    private Mode mode = Mode.ONLINE;
    private VersionList versionList;

    public VersionManager() {

    }

    public void downloadVersion(Version version, ProgressListener listener) throws IOException {
        if (version instanceof CosmicArchiveVersion cosmicArchiveVersion) {
            CosmicArchiveDownloader downloader = new CosmicArchiveDownloader();
            downloader.downloadVersion(cosmicArchiveVersion, listener);
        } else if (version instanceof ItchVersion itchVersion) {
            ItchDownloader downloader = new ItchDownloader();
            downloader.downloadVersion(itchVersion, listener);
        }
    }

    public void updateVersionList() {
        int option = CRLauncher.getInstance().getSettings().versionsSourceOption;
        this.versionList = switch (this.mode) {
            case ONLINE -> switch (option) {
                case 1 -> new ItchVersionList();
                default -> new CosmicArchiveVersionList(VersionManager.REMOTE_VERSIONS);
            };
            case OFFLINE -> switch (option) {
                case 1 -> new LocalVersionList(CRLauncher.getInstance().getVersionsDir(), "itch", ItchVersion.class);
                default -> new LocalVersionList(CRLauncher.getInstance().getVersionsDir(), "cosmic-archive", CosmicArchiveVersion.class);
            };
        };
    }

    public void load() throws IOException {
        this.updateVersionList();

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

    public boolean isInstalled(Version version) {
        return Files.exists(this.getVersionJar(version));
    }

    public Path getVersionJar(Version version) {
        Path versionsDir = CRLauncher.getInstance().getVersionsDir();

        return versionsDir
            .resolve("bin")
            .resolve(version.getId())
            .resolve("client")
            .resolve(version.getId() + ".jar");
    }

    public Path getCosmicArchiveVersionJson(String id) {
        Path versionsDir = CRLauncher.getInstance().getVersionsDir();

        return versionsDir
            .resolve("cosmic-archive")
            .resolve(id + ".json");
    }

    public Path getItchVersionJson(String id) {
        Path versionsDir = CRLauncher.getInstance().getVersionsDir();

        return versionsDir
            .resolve("itch")
            .resolve(id + ".json");
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
