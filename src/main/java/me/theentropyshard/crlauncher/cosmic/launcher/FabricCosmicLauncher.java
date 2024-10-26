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
import me.theentropyshard.crlauncher.cosmic.mods.fabric.FabricProperties;
import me.theentropyshard.crlauncher.github.GithubApi;
import me.theentropyshard.crlauncher.github.GithubRelease;
import me.theentropyshard.crlauncher.gui.dialogs.ProgressDialog;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ListUtils;
import net.lingala.zip4j.ZipFile;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FabricCosmicLauncher extends ModdedPatchCosmicLauncher {
    
    private final String version;

    public FabricCosmicLauncher(String javaPath, Path runDir, Path gameFilesLocation, Path clientPath, Path modsDir, String version) {
        super(javaPath, runDir, gameFilesLocation, clientPath, modsDir);

        this.version = version;
    }

    private void downloadFabricLoader(Path loaderDir, String version) {
        try {
            if (Files.exists(loaderDir) && FileUtils.countFiles(loaderDir) != 0) {
                return;
            }

            Path loaderArchivePath = loaderDir.resolve("fabric_loader_%s.zip".formatted(version));

            FileUtils.createDirectoryIfNotExists(loaderDir);

            ProgressDialog downloadDialog = new ProgressDialog("Downloading Fabric");
            downloadDialog.setStage("Downloading Fabric mod loader...");

            SwingUtilities.invokeLater(() -> downloadDialog.setVisible(true));

            GithubApi downloader = new GithubApi();

            List<GithubRelease> allReleases =
                    downloader.getAllReleases("ForwarD-Nern", "CosmicReach-Mod-Loader");
            GithubRelease release = ListUtils.search(allReleases, resp -> resp.tag_name.equals(version));

            if (release == null) {
                Log.error("Could find version " + version);

                SwingUtilities.invokeLater(() -> downloadDialog.getDialog().dispose());

                return;
            }

            downloader.downloadRelease(
                    loaderArchivePath,
                    release,
                    0,
                    downloadDialog
            );

            SwingUtilities.invokeLater(() -> downloadDialog.getDialog().dispose());

            try (ZipFile loaderArchive = new ZipFile(loaderArchivePath.toFile())) {
                loaderArchive.removeFile("launch.bat");
                loaderArchive.removeFile("launch.sh");

                loaderArchive.extractAll(loaderDir.toString());
            }
        } catch (IOException e) {
            Log.error("Exception while downloading Fabric loader", e);
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
            Log.error("Exception while listing files in " + loaderDir, e);
        }

        if (fabricJar == null) {
            Log.error("Cannot find fabric modloader jar in " + loaderDir);
            MessageBox.showErrorMessage(CRLauncher.frame, "Cannot find fabric modloader jar in " + loaderDir);

            return null;
        }

        return fabricJar;
    }

    private List<String> resolveDependencies(Path loaderDir) {
        List<String> classpath = new ArrayList<>();

        Path depsDir = loaderDir.resolve("deps");
        if (!Files.exists(depsDir)) {
            Log.error("Cannot find fabric modloader dependencies in " + depsDir);
            MessageBox.showErrorMessage(CRLauncher.frame,
                    "Cannot find fabric modloader dependencies in " + depsDir);

            return classpath;
        }

        try {
            for (Path dep : FileUtils.list(depsDir)) {
                classpath.add(dep.toString());
            }
        } catch (IOException e) {
            Log.error("Cannot list files in " + depsDir, e);
        }

        return classpath;
    }

    @Override
    public void buildCommand(List<String> command) {
        this.defineProperty(FabricProperties.SKIP_MC_PROVIDER.copy(true));
        this.defineProperty(FabricProperties.GAME_JAR_PATH.copy(this.getClientPath()));
        this.defineProperty(FabricProperties.MODS_FOLDER.copy(this.getModsDir()));

        super.buildCommand(command);

        Path fabricLoaderDir = CRLauncher.getInstance().getModloadersDir().resolve("fabric-loader-%s".formatted(this.version));
        this.downloadFabricLoader(fabricLoaderDir, this.version);

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
