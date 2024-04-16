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

import me.theentropyshard.crlauncher.cosmic.mods.fabric.FabricProperties;
import me.theentropyshard.crlauncher.github.GithubReleaseDownloader;
import me.theentropyshard.crlauncher.gui.Gui;
import me.theentropyshard.crlauncher.gui.dialogs.CRDownloadDialog;
import me.theentropyshard.crlauncher.utils.FileUtils;
import net.lingala.zip4j.ZipFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FabricCosmicLauncher extends ModdedCosmicLauncher {
    private static final Logger LOG = LogManager.getLogger(FabricCosmicLauncher.class);

    public FabricCosmicLauncher(Path runDir, Path gameFilesLocation, Path clientPath, Path modsDir) {
        super(runDir, gameFilesLocation, clientPath, modsDir);
    }

    private void downloadFabricLoader(Path loaderDir) {
        try {
            Path loaderArchivePath = loaderDir.resolve("fabric_loader.zip");

            if (!Files.exists(loaderDir)) {
                FileUtils.createDirectoryIfNotExists(loaderDir);

                CRDownloadDialog downloadDialog = new CRDownloadDialog();
                downloadDialog.setStage("Downloading Fabric mod loader...");

                SwingUtilities.invokeLater(() -> downloadDialog.setVisible(true));

                GithubReleaseDownloader downloader = new GithubReleaseDownloader();
                downloader.downloadLatestRelease(
                        loaderArchivePath,
                        downloader.getReleaseResponse("ForwarD-NerN", "CosmicReach-Mod-Loader"),
                        0,
                        downloadDialog
                );

                SwingUtilities.invokeLater(() -> downloadDialog.getDialog().dispose());

                try (ZipFile loaderArchive = new ZipFile(loaderArchivePath.toFile())) {
                    loaderArchive.removeFile("launch.bat");
                    loaderArchive.removeFile("launch.sh");

                    loaderArchive.extractAll(loaderDir.toString());
                }
            }
        } catch (IOException e) {
            LOG.error("Exception while downloading Fabric loader", e);
        }
    }

    private Path findFabricJar(Path loaderDir) {
        Path fabricJar = null;

        try {
            for (Path p : FileUtils.list(loaderDir)) {
                String fileName = p.getFileName().toString();
                if (fileName.contains("fabric") && fileName.contains("modloader") && fileName.endsWith(".jar")) {
                    fabricJar = p;
                }
            }
        } catch (IOException e) {
            LOG.error("Exception while listing files in {}", loaderDir, e);
        }

        if (fabricJar == null) {
            LOG.error("Cannot find fabric modloader jar in {}", loaderDir);
            Gui.showErrorDialog("Cannot find fabric modloader jar in " + loaderDir);

            return null;
        }

        return fabricJar;
    }

    private List<String> resolveDependencies(Path loaderDir) {
        List<String> classpath = new ArrayList<>();

        Path depsDir = loaderDir.resolve("deps");
        if (!Files.exists(depsDir)) {
            LOG.error("Cannot find fabric modloader dependencies in {}", depsDir);
            Gui.showErrorDialog("Cannot find fabric modloader dependencies in " + depsDir);

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
        this.defineProperty(FabricProperties.SKIP_MC_PROVIDER.copy(true));
        this.defineProperty(FabricProperties.GAME_JAR_PATH.copy(this.getClientPath()));

        super.buildCommand(command);

        Path fabricLoaderDir = this.getGameFilesLocation().resolve("fabric");
        this.downloadFabricLoader(fabricLoaderDir);

        command.add("-classpath");

        List<String> classpath = new ArrayList<>();
        classpath.add(this.getClientPath().toString());

        Path fabricJar = this.findFabricJar(fabricLoaderDir);

        if (fabricJar == null) {
            return;
        } else {
            classpath.add(fabricJar.toString());
        }

        classpath.addAll(this.resolveDependencies(fabricLoaderDir));

        command.add(String.join(File.pathSeparator, classpath));
        command.add(FabricProperties.MAIN_CLASS);
    }
}
