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

package me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt;

import com.google.gson.reflect.TypeToken;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.network.download.DownloadList;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.network.progress.ProgressListener;
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.maven.MavenArtifact;
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.maven.MavenDownloader;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.json.Json;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class QuiltManager {
    private static final String COSMIC_QUILT_FILE_NAME = "cosmic-quilt-%s.jar";
    private static final String DEPS_FILE = "deps.json";

    private final Path versionsDir;
    private final Path depsDir;

    public QuiltManager(Path workDir) {
        this.versionsDir = workDir.resolve("versions");
        this.depsDir = workDir.resolve("deps");
    }

    public void downloadCosmicQuilt(String version, ProgressListener listener) throws IOException {
        FileUtils.createDirectoryIfNotExists(this.versionsDir);
        FileUtils.createDirectoryIfNotExists(this.depsDir);

        if (this.checkIfInstalled(version)) {
            return;
        }

        DownloadList downloadList = new DownloadList((totalSize, downloadedBytes) -> {
            listener.update(totalSize, downloadedBytes, 0, false);
        });

        List<HttpDownload> downloads = new ArrayList<>();

        Path versionDir = this.versionsDir.resolve(version);
        FileUtils.createDirectoryIfNotExists(versionDir);

        String cosmicQuiltJar = QuiltManager.COSMIC_QUILT_FILE_NAME.formatted(version);

        List<MavenArtifact> deps = MavenDownloader.downloadRelease(version, this.depsDir, versionDir.resolve(cosmicQuiltJar), downloads);
        deps.removeIf(mavenArtifact -> mavenArtifact.artifactId().equals("quilt-loader-dependencies"));

        downloadList.addAll(downloads);
        downloadList.downloadAll();

        boolean pretty = CRLauncher.getInstance().getSettings().writePrettyJson;
        FileUtils.writeUtf8(versionDir.resolve(QuiltManager.DEPS_FILE), pretty ? Json.writePretty(deps) : Json.write(deps));
    }

    private boolean checkIfInstalled(String version) throws IOException {
        Path versionDir = this.versionsDir.resolve(version);
        if (!Files.exists(versionDir)) {
            return false;
        }

        Path depsFile = versionDir.resolve(QuiltManager.DEPS_FILE);
        if (!Files.exists(depsFile)) {
            return false;
        }

        Path cosmicQuilt = this.versionsDir.resolve(version).resolve(QuiltManager.COSMIC_QUILT_FILE_NAME.formatted(version));
        if (!Files.exists(cosmicQuilt)) {
            return false;
        }

        List<MavenArtifact> deps = Json.parse(FileUtils.readUtf8(depsFile), new TypeToken<List<MavenArtifact>>() {}.getType());
        for (MavenArtifact dep : deps) {
            Path depFile = this.depsDir.resolve(dep.jar());

            if (depFile.getFileName().toString().contains("quilt-loader-dependencies")) {
                FileUtils.delete(depFile);
            }

            if (!Files.exists(depFile)) {
                return false;
            }
        }

        return true;
    }

    public String getClasspathFor(String version) throws IOException {
        List<String> classpath = new ArrayList<>();

        Path versionDir = this.versionsDir.resolve(version);
        classpath.add(versionDir.resolve(QuiltManager.COSMIC_QUILT_FILE_NAME.formatted(version)).toString());

        Path depsFile = versionDir.resolve(QuiltManager.DEPS_FILE);
        List<MavenArtifact> deps = Json.parse(FileUtils.readUtf8(depsFile), new TypeToken<List<MavenArtifact>>() {}.getType());
        for (MavenArtifact dep : deps) {
            Path depFile = this.depsDir.resolve(dep.jar());
            classpath.add(depFile.toString());
        }

        return String.join(File.pathSeparator, classpath);
    }
}
