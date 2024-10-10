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

import me.theentropyshard.crlauncher.Args;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.github.GithubApi;
import me.theentropyshard.crlauncher.github.GithubRelease;
import me.theentropyshard.crlauncher.gui.dialogs.ProgressDialog;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.*;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LocationOverrideCosmicLauncher extends AbstractCosmicLauncher {
    private static final SemanticVersion CR_LOADER_VERSION = SemanticVersion.parse("0.1.2");
    private static final String CR_LOADER_JAR = "CRLoader-" + LocationOverrideCosmicLauncher.CR_LOADER_VERSION.toVersionString() + ".jar";
    private static final String CR_LOADER_SHA256 = "5a542329969e0c384101a0f70448011a277409689cfa65a69e72a87c27f43b31";

    public LocationOverrideCosmicLauncher(String javaPath, Path runDir, Path gameFilesLocation, Path clientPath) {
        super(javaPath, runDir, gameFilesLocation, clientPath);
    }

    private void downloadLoader(Path path) throws IOException {
        if (Files.exists(path) && HashUtils.sha256(path).equals(LocationOverrideCosmicLauncher.CR_LOADER_SHA256)) {
            return;
        }

        String loaderVersion = LocationOverrideCosmicLauncher.CR_LOADER_VERSION.toVersionString();

        GithubApi downloader = new GithubApi();
        List<GithubRelease> allReleases = downloader.getAllReleases("CRLauncher", "CRLoader");
        GithubRelease releaseResponse = ListUtils.search(allReleases, r -> r.tag_name.equals("v" + loaderVersion));

        if (releaseResponse == null) {
            throw new IOException("Could not find release v" + loaderVersion);
        }

        ProgressDialog dialog = new ProgressDialog("Downloading CRLoader");
        dialog.setStage("Downloading " + LocationOverrideCosmicLauncher.CR_LOADER_JAR);
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));

        GithubRelease.Asset asset = ListUtils.search(releaseResponse.assets, a -> a.name.equals(LocationOverrideCosmicLauncher.CR_LOADER_JAR));
        downloader.downloadRelease(path, releaseResponse, releaseResponse.assets.indexOf(asset), dialog);

        SwingUtilities.invokeLater(() -> dialog.getDialog().dispose());
    }

    @Override
    public void buildCommand(List<String> command) {
        CRLauncher launcher = CRLauncher.getInstance();
        Args args = launcher.getArgs();

        Path loaderPath;

        String customCRLoaderPath = args.getCustomCRLoaderPath();
        if (customCRLoaderPath != null) {
            loaderPath = Paths.get(customCRLoaderPath).normalize().toAbsolutePath();
        } else {
            loaderPath = launcher.getLibrariesDir().resolve(LocationOverrideCosmicLauncher.CR_LOADER_JAR);

            try {
                this.downloadLoader(loaderPath);
            } catch (IOException e) {
                Log.error("Could not download " + LocationOverrideCosmicLauncher.CR_LOADER_JAR, e);
            }
        }

        String gameFilesLocation = this.getGameFilesLocation().toString();
        if (OperatingSystem.isWindows()) {
            gameFilesLocation = gameFilesLocation.replace("\\", "\\\\");
        }

        this.defineProperty(new SystemProperty("crloader.saveDirPath", gameFilesLocation));

        super.buildCommand(command);

        command.add("-javaagent:" + loaderPath);
    }
}
