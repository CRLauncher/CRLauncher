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
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.CosmicQuiltProperties;
import me.theentropyshard.crlauncher.gui.dialogs.CRDownloadDialog;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.maven.MavenDownloader;
import me.theentropyshard.crlauncher.network.download.DownloadList;
import me.theentropyshard.crlauncher.network.download.DownloadListener;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.utils.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class QuiltCosmicLauncher extends ModdedCosmicLauncher {
    private static final Logger LOG = LogManager.getLogger(QuiltCosmicLauncher.class);

    private final String version;

    public QuiltCosmicLauncher(Path runDir, Path gameFilesLocation, Path clientPath, Path modsDir, String version) {
        super(runDir, gameFilesLocation, clientPath, modsDir);

        this.version = version;
    }

    private void downloadCosmicQuilt(Path quiltDir, String version) {
        try {
            if (Files.exists(quiltDir) && FileUtils.countFiles(quiltDir) != 0) {
                return;
            }

            CRDownloadDialog dialog = new CRDownloadDialog();

            SwingUtilities.invokeLater(() -> dialog.setVisible(true));

            DownloadList downloadList = new DownloadList((totalSize, downloadedBytes) -> {
                SwingUtilities.invokeLater(() -> dialog.update(totalSize, downloadedBytes, 0, false));
            });

            List<HttpDownload> downloads = new ArrayList<>();

            dialog.setStage("Collecting Cosmic Quilt...");
            MavenDownloader.downloadRelease(version, quiltDir.resolve("deps"),
                    quiltDir.resolve("cosmic-quilt-%s.jar".formatted(version)), downloads);

            downloadList.addAll(downloads);

            dialog.setStage("Downloading Cosmic Quilt...");
            downloadList.downloadAll();

            SwingUtilities.invokeLater(() -> dialog.getDialog().dispose());
        } catch (IOException e) {
            LOG.error("Exception while downloading Cosmic Quilt", e);
        }
    }

    private List<String> resolveDependencies(Path loaderDir) {
        List<String> classpath = new ArrayList<>();

        Path depsDir = loaderDir.resolve("deps");
        if (!Files.exists(depsDir)) {
            LOG.error("Cannot find Cosmic Quilt dependencies in {}", depsDir);
            MessageBox.showErrorMessage(CRLauncher.frame, "Cannot find Cosmic Quilt dependencies in " + depsDir);

            return classpath;
        }

        try {
            for (Path dep : FileUtils.list(depsDir)) {
                classpath.add(dep.toString());
            }
        } catch (IOException e) {
            LOG.error("Cannot list files in {}", depsDir, e);
        }

        return classpath;
    }

    @Override
    public void buildCommand(List<String> command) {
        this.defineProperty(CosmicQuiltProperties.LAUNCH_DIR.copy(this.getGameFilesLocation()));
        this.defineProperty(CosmicQuiltProperties.GAME_JAR_PATH.copy(this.getClientPath()));
        this.defineProperty(CosmicQuiltProperties.MODS_FOLDER.copy(this.getModsDir()));

        super.buildCommand(command);

        Path cosmicQuiltDir = CRLauncher.getInstance().getCosmicDir().resolve("cosmic_quilt_%s".formatted(this.version));
        this.downloadCosmicQuilt(cosmicQuiltDir, this.version);

        command.add("-classpath");

        List<String> classpath = new ArrayList<>();
        classpath.add(String.valueOf(cosmicQuiltDir.resolve("cosmic-quilt-%s.jar".formatted(this.version))));
        classpath.addAll(this.resolveDependencies(cosmicQuiltDir));

        command.add(String.join(File.pathSeparator, classpath));
        command.add(CosmicQuiltProperties.MAIN_CLASS);
    }
}
