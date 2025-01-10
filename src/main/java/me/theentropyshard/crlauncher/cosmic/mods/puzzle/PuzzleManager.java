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

package me.theentropyshard.crlauncher.cosmic.mods.puzzle;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.maven.MavenArtifact;
import me.theentropyshard.crlauncher.github.GithubApi;
import me.theentropyshard.crlauncher.github.GithubRelease;
import me.theentropyshard.crlauncher.network.download.DownloadList;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.network.progress.ProgressListener;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ListUtils;
import me.theentropyshard.crlauncher.utils.SemanticVersion;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
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

    private static final List<PuzzleDependency> LIBRARIES_2_0_0 = List.of(
        new PuzzleDependency("https://maven.fabricmc.net/", new MavenArtifact("net.fabricmc", "sponge-mixin", "0.15.3+mixin.0.8.7")),
        new PuzzleDependency("https://jitpack.io/", new MavenArtifact("com.github.PuzzleLoader", "access_manipulators", "1.0.1")),
        new PuzzleDependency("https://repo1.maven.org/maven2/", new MavenArtifact("org.ow2.asm", "asm", "9.6")),
        new PuzzleDependency("https://repo1.maven.org/maven2/", new MavenArtifact("org.ow2.asm", "asm-tree", "9.6")),
        new PuzzleDependency("https://repo1.maven.org/maven2/", new MavenArtifact("org.ow2.asm", "asm-util", "9.6")),
        new PuzzleDependency("https://repo1.maven.org/maven2/", new MavenArtifact("org.ow2.asm", "asm-analysis", "9.6")),
        new PuzzleDependency("https://repo1.maven.org/maven2/", new MavenArtifact("org.ow2.asm", "asm-commons", "9.6"))
    );

    private static final SemanticVersion VERSION_2_0_0 = new SemanticVersion(2, 0, 0);
    private static final SemanticVersion VERSION_2_1_15 = new SemanticVersion(2, 1, 15);

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

        GithubApi downloader = new GithubApi();
        List<GithubRelease> allReleases = downloader.getAllReleases("PuzzleLoader", "PuzzleLoader");
        GithubRelease release = ListUtils.search(allReleases, r -> r.tag_name.equals(version));
        if (release == null) {
            throw new IOException("Puzzle Loader " + version + " not found");
        }

        String fileName = PuzzleManager.getClientName(version);
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

        for (PuzzleDependency dependency : PuzzleManager.getLibraries(version)) {
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
        deps.add(this.versionsDir.resolve(PuzzleManager.getClientName(version)));

        return String.join(File.pathSeparator, deps.stream().map(Path::toString).toList());
    }

    public boolean isInstalled(String version) {
        Path loaderFile = this.versionsDir.resolve(PuzzleManager.getClientName(version));
        if (!Files.exists(loaderFile)) {
            return false;
        }

        for (PuzzleDependency dependency : PuzzleManager.getLibraries(version)) {
            MavenArtifact artifact = dependency.mavenArtifact();

            if (!Files.exists(this.depsDir.resolve(artifact.jar()))) {
                return false;
            }
        }

        return true;
    }

    public static List<PuzzleDependency> getLibraries(String version) {
        SemanticVersion puzzleVersion = SemanticVersion.parse(version);

        if (puzzleVersion == null) {
            throw new RuntimeException("Could not parse semantic version: " + version);
        }

        if (puzzleVersion.isHigherThan(PuzzleManager.VERSION_2_1_15)) {
            return Collections.emptyList();
        }

        if (puzzleVersion.isLowerThan(PuzzleManager.VERSION_2_0_0)) {
            return PuzzleManager.LIBRARIES;
        } else {
            return PuzzleManager.LIBRARIES_2_0_0;
        }
    }

    public static String getClientName(String version) {
        SemanticVersion puzzleVersion = SemanticVersion.parse(version);

        if (puzzleVersion == null) {
            throw new RuntimeException("Could not parse semantic version: " + version);
        }

        if (puzzleVersion.isLowerThan(PuzzleManager.VERSION_2_0_0)) {
            return "PuzzleLoader-" + version + ".jar";
        } else {
            return "PuzzleLoader-" + version + "-client.jar";
        }
    }

    public static String getMainClass(String version) {
        SemanticVersion puzzleVersion = SemanticVersion.parse(version);

        if (puzzleVersion == null) {
            throw new RuntimeException("Could not parse semantic version: " + version);
        }

        if (puzzleVersion.isLowerThan(PuzzleManager.VERSION_2_0_0)) {
            return PuzzleProperties.MAIN_CLASS;
        } else {
            return PuzzleProperties.MAIN_CLASS_2_0_0;
        }
    }
}
