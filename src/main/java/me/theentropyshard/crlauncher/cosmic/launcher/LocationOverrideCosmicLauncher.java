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
import me.theentropyshard.crlauncher.gui.dialogs.CRDownloadDialog;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.HashUtils;
import me.theentropyshard.crlauncher.utils.ListUtils;
import me.theentropyshard.crlauncher.utils.OperatingSystem;
import me.theentropyshard.crlauncher.utils.ResourceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LocationOverrideCosmicLauncher extends AbstractCosmicLauncher {
    

    private static final String CR_LOADER_VERSION = "0.0.1";
    private static final String CR_LOADER_JAR = "CRLoader-" + LocationOverrideCosmicLauncher.CR_LOADER_VERSION + ".jar";
    private static final String CR_LOADER_SHA256 = "213128b5e80280af873f1b91cbc4a44515294dc191bb0f46b297c982904dcdbe";

    public LocationOverrideCosmicLauncher(Path runDir, Path gameFilesLocation, Path clientPath) {
        super(runDir, gameFilesLocation, clientPath);
    }

    private void downloadLoader(Path path) throws IOException {
        if (Files.exists(path) && HashUtils.sha256(path).equals(LocationOverrideCosmicLauncher.CR_LOADER_SHA256)) {
            return;
        }

        GithubReleaseDownloader downloader = new GithubReleaseDownloader();
        List<GithubReleaseResponse> allReleases = downloader.getAllReleases("CRLauncher", "CRLoader");
        GithubReleaseResponse releaseResponse = ListUtils.search(allReleases, r -> r.tag_name.equals("v" + LocationOverrideCosmicLauncher.CR_LOADER_VERSION));
        if (releaseResponse == null) {
            throw new IOException("Could not find release v" + LocationOverrideCosmicLauncher.CR_LOADER_VERSION);
        }

        CRDownloadDialog dialog = new CRDownloadDialog();
        dialog.setStage("Downloading " + LocationOverrideCosmicLauncher.CR_LOADER_JAR);
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));

        GithubReleaseResponse.Asset asset = ListUtils.search(releaseResponse.assets, a -> a.name.equals(LocationOverrideCosmicLauncher.CR_LOADER_JAR));
        downloader.downloadRelease(path, releaseResponse, releaseResponse.assets.indexOf(asset), dialog);

        SwingUtilities.invokeLater(() -> dialog.getDialog().dispose());
    }

    @Override
    public void buildCommand(List<String> command) {
        super.buildCommand(command);

        Path loaderPath = CRLauncher.getInstance().getLibrariesDir().resolve(LocationOverrideCosmicLauncher.CR_LOADER_JAR);
        try {
            this.downloadLoader(loaderPath);
        } catch (IOException e) {
            Log.error("Could not download " + LocationOverrideCosmicLauncher.CR_LOADER_JAR, e);
        }

        String gameFilesLocation = this.getGameFilesLocation().toString();
        if (OperatingSystem.isWindows()) {
            gameFilesLocation = gameFilesLocation.replace("\\", "\\\\");
        }

        command.add("-javaagent:" + loaderPath + "=" + gameFilesLocation);
    }
}
