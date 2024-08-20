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

package me.theentropyshard.crlauncher.cosmic.mods.puzzle;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.maven.MavenArtifact;
import me.theentropyshard.crlauncher.github.GithubReleaseDownloader;
import me.theentropyshard.crlauncher.github.GithubReleaseResponse;
import me.theentropyshard.crlauncher.network.download.DownloadList;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.network.progress.ProgressListener;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ListUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PuzzleManager {
    private static final List<PuzzleDependency> LIBRARIES = List.of(
        new PuzzleDependency("https://repo.spongepowered.org/repository/maven-public/", new MavenArtifact("org.spongepowered", "mixin", "0.8.5")),
        new PuzzleDependency("https://jitpack.io/", new MavenArtifact("com.github.PuzzleLoader", "access_manipulators", "1.0.1")),
        new PuzzleDependency("https://repo1.maven.org/maven2/", new MavenArtifact("org.ow2.asm", "asm", "9.6")),
        new PuzzleDependency("https://repo1.maven.org/maven2/", new MavenArtifact("org.ow2.asm", "asm-tree", "9.6")),
        new PuzzleDependency("https://repo1.maven.org/maven2/", new MavenArtifact("org.ow2.asm", "asm-util", "9.6")),
        new PuzzleDependency("https://repo1.maven.org/maven2/", new MavenArtifact("org.ow2.asm", "asm-analysis", "9.6")),
        new PuzzleDependency("https://repo1.maven.org/maven2/", new MavenArtifact("org.ow2.asm", "asm-commons", "9.6"))
    );

    private final Path versionsDir;
    private final Path depsDir;

    public PuzzleManager(Path workDir) {
        this.versionsDir = workDir.resolve("versions");
        this.depsDir = workDir.resolve("deps");
    }

    public void downloadPuzzle(String version, ProgressListener progressListener) throws IOException {
        FileUtils.createDirectoryIfNotExists(this.versionsDir);
        FileUtils.createDirectoryIfNotExists(this.depsDir);

        if (this.isInstalled(version)) {
            return;
        }

        GithubReleaseDownloader downloader = new GithubReleaseDownloader();
        List<GithubReleaseResponse> allReleases = downloader.getAllReleases("PuzzleLoader", "PuzzleLoader");
        GithubReleaseResponse release = ListUtils.search(allReleases, r -> r.tag_name.equals(version));
        if (release == null) {
            throw new IOException("Puzzle Loader " + version + " not found");
        }

        String fileName = "PuzzleLoader-" + version + ".jar";
        Path filePath = this.versionsDir.resolve(fileName);

        DownloadList list = new DownloadList((totalSize, downloadedBytes) -> {
            progressListener.update(totalSize, downloadedBytes, 0, false);
        });

        HttpDownload loaderDownload = new HttpDownload.Builder()
            .httpClient(CRLauncher.getInstance().getHttpClient())
            .saveAs(filePath)
            .url("https://jitpack.io/com/github/PuzzleLoader/PuzzleLoader/" + version + "/" + fileName)
            // todo add sha 1 to download and verify
            .build();

        list.add(loaderDownload);

        for (PuzzleDependency dependency : PuzzleManager.LIBRARIES) {
            MavenArtifact artifact = dependency.mavenArtifact();

            HttpDownload libDownload = new HttpDownload.Builder()
                .httpClient(CRLauncher.getInstance().getHttpClient())
                .saveAs(this.depsDir.resolve(artifact.jar()))
                .url(dependency.baseRepoURL() + artifact.url())
                // todo add sha 1 to download and verify
                .build();

            list.add(libDownload);
        }

        list.downloadAll();
    }

    public String getClasspath(String version) throws IOException {
        List<Path> deps = FileUtils.list(this.depsDir);
        deps.add(this.versionsDir.resolve("PuzzleLoader-" + version + ".jar"));

        return String.join(File.pathSeparator, deps.stream().map(Path::toString).toList());
    }

    public boolean isInstalled(String version) {
        Path loaderFile = this.versionsDir.resolve("PuzzleLoader-" + version + ".jar");
        if (!Files.exists(loaderFile)) {
            return false;
        }

        for (PuzzleDependency dependency : PuzzleManager.LIBRARIES) {
            MavenArtifact artifact = dependency.mavenArtifact();

            if (!Files.exists(this.depsDir.resolve(artifact.jar()))) {
                return false;
            }
        }

        return true;
    }
}
