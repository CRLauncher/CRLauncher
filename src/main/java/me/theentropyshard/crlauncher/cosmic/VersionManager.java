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

package me.theentropyshard.crlauncher.cosmic;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.gui.Gui;
import me.theentropyshard.crlauncher.network.HttpRequest;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.network.progress.ProgressListener;
import me.theentropyshard.crlauncher.network.progress.ProgressNetworkInterceptor;
import me.theentropyshard.crlauncher.utils.HashUtils;
import me.theentropyshard.crlauncher.utils.Json;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class VersionManager {
    private static final String REMOTE_VERSIONS = "https://raw.githubusercontent.com/CRLauncher/MetaInfo/master/versions.json";

    private final Path workDir;

    public VersionManager(Path workDir) {
        this.workDir = workDir;
    }

    public void downloadVersion(Version version, ProgressListener listener) throws IOException {
        Path filePath = this.workDir.resolve(version.getId() + ".jar");

        if (Files.exists(filePath) && HashUtils.murmur3(filePath).equals(version.getHash())) {
            return;
        }

        OkHttpClient httpClient = CRLauncher.getInstance().getHttpClient().newBuilder()
                .addNetworkInterceptor(new ProgressNetworkInterceptor(listener))
                .build();

        HttpDownload download = new HttpDownload.Builder()
                .url(version.getUrl())
                .expectedSize(version.getSize())
                .httpClient(httpClient)
                .saveAs(filePath)
                .build();

        Gui.instance.getMainView().changeDownloadProgressVisibility(true);
        download.execute();
        Gui.instance.getMainView().changeDownloadProgressVisibility(false);
    }

    public Path getVersionPath(Version version) {
        return this.workDir.resolve(version.getId() + ".jar");
    }

    public List<Version> getRemoteAvailableVersions() throws IOException {
        try (HttpRequest request = new HttpRequest(CRLauncher.getInstance().getHttpClient())) {
            VersionList versionList = Json.parse(request.asString(VersionManager.REMOTE_VERSIONS), VersionList.class);

            return versionList.getVersions();
        }
    }

    public List<Version> getLocalAvailableVersions() throws IOException {
        return Collections.emptyList();
    }
}
