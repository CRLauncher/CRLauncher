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

package me.theentropyshard.crlauncher.cosmic.launcher;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.github.GithubReleaseDownloader;
import me.theentropyshard.crlauncher.github.GithubReleaseResponse;
import me.theentropyshard.crlauncher.gui.dialogs.ProgressDialog;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.network.download.DownloadList;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.maven.MavenArtifact;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ListUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PuzzleCosmicLauncher extends ModdedLocationOverrideCosmicLauncher {
    private static final List<PuzzleDependency> LIBRARIES = List.of(
        new PuzzleDependency("https://repo.spongepowered.org/repository/maven-public/", new MavenArtifact("org.spongepowered", "mixin", "0.8.5")),
        new PuzzleDependency("https://jitpack.io/", new MavenArtifact("com.github.PuzzleLoader", "access_manipulators", "1.0.1")),
        new PuzzleDependency("https://repo1.maven.org/maven2/", new MavenArtifact("org.ow2.asm", "asm", "9.6")),
        new PuzzleDependency("https://repo1.maven.org/maven2/", new MavenArtifact("org.ow2.asm", "asm-tree", "9.6")),
        new PuzzleDependency("https://repo1.maven.org/maven2/", new MavenArtifact("org.ow2.asm", "asm-util", "9.6")),
        new PuzzleDependency("https://repo1.maven.org/maven2/", new MavenArtifact("org.ow2.asm", "asm-analysis", "9.6")),
        new PuzzleDependency("https://repo1.maven.org/maven2/", new MavenArtifact("org.ow2.asm", "asm-commons", "9.6"))
    );

    private final String version;

    private record PuzzleDependency(String baseRepoURL, MavenArtifact mavenArtifact) {

    }

    public PuzzleCosmicLauncher(String javaPath, Path runDir, Path gameFilesLocation, Path clientPath, Path modsDir, String version) {
        super(javaPath, runDir, gameFilesLocation, clientPath, modsDir);

        this.version = version;
    }

    private String downloadPuzzle() throws IOException {
        GithubReleaseDownloader downloader = new GithubReleaseDownloader();
        List<GithubReleaseResponse> allReleases = downloader.getAllReleases("PuzzleLoader", "PuzzleLoader");
        GithubReleaseResponse release = ListUtils.search(allReleases, r -> r.tag_name.equals(this.version));
        if (release == null) {
            throw new IOException("Puzzle Loader " + this.version + " not found");
        }

        List<String> classpath = new ArrayList<>();
        classpath.add(this.getClientPath().toString());

        Path puzzleDir = CRLauncher.getInstance().getCosmicDir().resolve("puzzle");
        Path downloadDir = puzzleDir.resolve(this.version);
        FileUtils.createDirectoryIfNotExists(downloadDir);
        String fileName = "PuzzleLoader-" + this.version + ".jar";
        Path filePath = downloadDir.resolve(fileName);
        classpath.add(filePath.toString());

        ProgressDialog dialog = new ProgressDialog("Downloading Puzzle");
        dialog.setStage("Downloading Puzzle " + this.version);

        DownloadList list = new DownloadList((totalSize, downloadedBytes) -> {
            dialog.update(totalSize, downloadedBytes, 0, false);
        });

        HttpDownload download = new HttpDownload.Builder()
            .httpClient(CRLauncher.getInstance().getHttpClient())
            .saveAs(filePath)
            .url("https://jitpack.io/com/github/PuzzleLoader/PuzzleLoader/" + this.version + "/" + fileName)
            // todo add sha 1 to download and verify
            .build();

        list.add(download);

        Path libsDir = downloadDir.resolve("libs");
        FileUtils.createDirectoryIfNotExists(libsDir);
        for (PuzzleDependency pDep : PuzzleCosmicLauncher.LIBRARIES) {
            MavenArtifact dep = pDep.mavenArtifact();
            String mavenJar = dep.jar();

            Path libPath = libsDir.resolve(mavenJar);
            HttpDownload libDownload = new HttpDownload.Builder()
                .httpClient(CRLauncher.getInstance().getHttpClient())
                .saveAs(libPath)
                .url(pDep.baseRepoURL() + dep.url())
                // todo add sha 1 to download and verify
                .build();

            list.add(libDownload);
        }

        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
        list.downloadAll();
        FileUtils.list(libsDir).forEach(path -> classpath.add(path.toString()));
        SwingUtilities.invokeLater(() -> dialog.getDialog().dispose());

        return String.join(File.pathSeparator, classpath);
    }

    @Override
    public void buildCommand(List<String> command) {
        super.buildCommand(command);

        String classpath;
        try {
            classpath = this.downloadPuzzle();
        } catch (IOException e) {
            Log.error("Could not download Puzzle", e);

            return;
        }

        command.add("-classpath");

        command.add(classpath);

        command.add("com.github.puzzle.loader.launch.Piece");
    }
}
