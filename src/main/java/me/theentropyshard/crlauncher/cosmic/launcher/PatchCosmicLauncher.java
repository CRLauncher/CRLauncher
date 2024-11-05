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

public class PatchCosmicLauncher extends AbstractCosmicLauncher {
    private static final SemanticVersion CR_LOADER_VERSION = SemanticVersion.parse("0.1.4");
    private static final String CR_LOADER_JAR = "CRLoader-" + PatchCosmicLauncher.CR_LOADER_VERSION.toVersionString() + ".jar";
    private static final String CR_LOADER_SHA256 = "7870359e7500e5a52999631528be2c010ab2dfe46d8788e64d42e0ee21440f47";

    private boolean changeSaveLocation;
    private String customWindowTitle;
    private String offlineUsername;
    private boolean appendUsername;
    private boolean patchOfflineAccount;
    private boolean fullscreen;
    private boolean maximized;
    private int windowWidth;
    private int windowHeight;

    public PatchCosmicLauncher(String javaPath, Path runDir, Path gameFilesLocation, Path clientPath) {
        super(javaPath, runDir, gameFilesLocation, clientPath);

        this.setChangeSaveLocation(true);
    }

    private void downloadLoader(Path path) throws IOException {
        if (Files.exists(path) && HashUtils.sha256(path).equals(PatchCosmicLauncher.CR_LOADER_SHA256)) {
            return;
        }

        String loaderVersion = PatchCosmicLauncher.CR_LOADER_VERSION.toVersionString();

        GithubApi downloader = new GithubApi();
        List<GithubRelease> allReleases = downloader.getAllReleases("CRLauncher", "CRLoader");
        GithubRelease releaseResponse = ListUtils.search(allReleases, r -> r.tag_name.equals("v" + loaderVersion));

        if (releaseResponse == null) {
            throw new IOException("Could not find release v" + loaderVersion);
        }

        ProgressDialog dialog = new ProgressDialog("Downloading CRLoader");
        dialog.setStage("Downloading " + PatchCosmicLauncher.CR_LOADER_JAR);
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));

        GithubRelease.Asset asset = ListUtils.search(releaseResponse.assets, a -> a.name.equals(PatchCosmicLauncher.CR_LOADER_JAR));
        downloader.downloadRelease(path, releaseResponse, releaseResponse.assets.indexOf(asset), dialog);

        SwingUtilities.invokeLater(() -> dialog.getDialog().dispose());
    }

    private Path setupLoader() {
        CRLauncher launcher = CRLauncher.getInstance();
        Args args = launcher.getArgs();

        Path loaderPath;

        String customCRLoaderPath = args.getCustomCRLoaderPath();
        if (customCRLoaderPath != null) {
            loaderPath = Paths.get(customCRLoaderPath).normalize().toAbsolutePath();
        } else {
            loaderPath = launcher.getLibrariesDir().resolve(PatchCosmicLauncher.CR_LOADER_JAR);

            try {
                this.downloadLoader(loaderPath);
            } catch (IOException e) {
                Log.error("Could not download " + PatchCosmicLauncher.CR_LOADER_JAR, e);
            }
        }

        return loaderPath;
    }

    @Override
    public void buildCommand(List<String> command) {
        if (this.isChangeSaveLocation()) {
            String gameFilesLocation = this.getGameFilesLocation().toString();

            if (OperatingSystem.isWindows()) {
                gameFilesLocation = gameFilesLocation.replace("\\", "\\\\");
            }

            this.defineProperty(new SystemProperty("crloader.saveDirPath", gameFilesLocation));
        }

        if (this.customWindowTitle != null && !this.customWindowTitle.trim().isEmpty()) {
            this.defineProperty(new SystemProperty("crloader.windowTitle", this.customWindowTitle));
        }

        if (this.offlineUsername != null && !this.offlineUsername.trim().isEmpty()) {
            this.defineProperty(new SystemProperty("crloader.offlineUsername", this.offlineUsername));
        }

        if (this.isPatchOfflineAccount()) {
            this.defineProperty(new SystemProperty("crloader.patchOfflineAccount", true));
        }

        if (this.isAppendUsername()) {
            this.defineProperty(new SystemProperty("crloader.appendUsername", true));
        }

        if (this.isFullscreen()) {
            this.defineProperty(new SystemProperty("crloader.windowFullscreen", true));
        } else if (this.isMaximized()) {
            this.defineProperty(new SystemProperty("crloader.windowMaximized", true));
        } else {
            this.defineProperty(new SystemProperty("crloader.windowWidth", this.windowWidth));
            this.defineProperty(new SystemProperty("crloader.windowHeight", this.windowHeight));
        }

        super.buildCommand(command);

        command.add("-javaagent:" + this.setupLoader());
    }

    public boolean isChangeSaveLocation() {
        return this.changeSaveLocation;
    }

    public void setChangeSaveLocation(boolean changeSaveLocation) {
        this.changeSaveLocation = changeSaveLocation;
    }

    public void setCustomWindowTitle(String customWindowTitle) {
        this.customWindowTitle = customWindowTitle;
    }

    public void setOfflineUsername(String offlineUsername) {
        this.offlineUsername = offlineUsername;
    }

    public boolean isAppendUsername() {
        return this.appendUsername;
    }

    public void setAppendUsername(boolean appendUsername) {
        this.appendUsername = appendUsername;
    }

    public boolean isPatchOfflineAccount() {
        return this.patchOfflineAccount;
    }

    public void setPatchOfflineAccount(boolean patchOfflineAccount) {
        this.patchOfflineAccount = patchOfflineAccount;
    }

    public boolean isFullscreen() {
        return this.fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    public boolean isMaximized() {
        return this.maximized;
    }

    public void setMaximized(boolean maximized) {
        this.maximized = maximized;
    }

    public int getWindowWidth() {
        return this.windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return this.windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }
}
