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

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.mods.fabric.FabricSystemProperties;
import me.theentropyshard.crlauncher.github.GithubReleaseDownloader;
import me.theentropyshard.crlauncher.github.GithubReleaseResponse;
import me.theentropyshard.crlauncher.gui.Gui;
import me.theentropyshard.crlauncher.gui.dialogs.CRDownloadDialog;
import me.theentropyshard.crlauncher.cosmic.mods.fabric.FabricMod;
import me.theentropyshard.crlauncher.instance.OldInstance;
import me.theentropyshard.crlauncher.instance.OldInstanceManager;
import me.theentropyshard.crlauncher.cosmic.mods.jar.JarMod;
import me.theentropyshard.crlauncher.java.JavaLocator;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.OperatingSystem;
import me.theentropyshard.crlauncher.utils.ResourceUtils;
import me.theentropyshard.crlauncher.utils.TimeUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CosmicRunner extends Thread {
    private static final Logger LOG = LogManager.getLogger(CosmicRunner.class);

    private final OldInstance oldInstance;

    private Path clientCopyTmp;

    public CosmicRunner(OldInstance oldInstance) {
        this.oldInstance = oldInstance;

        this.setName("Cosmic Reach run thread");
    }

    @Override
    public void run() {
        VersionManager versionManager = CRLauncher.getInstance().getVersionManager();

        try {
            Version version = versionManager.getVersion(this.oldInstance.getCrVersion());

            CRDownloadDialog dialog = new CRDownloadDialog();
            SwingUtilities.invokeLater(() -> dialog.setVisible(true));
            versionManager.downloadVersion(version, dialog);
            SwingUtilities.invokeLater(() -> dialog.getDialog().dispose());

            List<String> command = new ArrayList<>();
            command.add(JavaLocator.getJavaPath());

            OldInstanceManager oldInstanceManager = CRLauncher.getInstance().getInstanceManager();
            String saveDirPath = oldInstanceManager.getCosmicDir(this.oldInstance).toString();

            if (OperatingSystem.isWindows()) {
                saveDirPath = saveDirPath.replace("\\", "\\\\");
            }

            Path loaderPath = CRLauncher.getInstance().getWorkDir().resolve("libraries").resolve("CRLoader-0.0.1.jar");

            if (!Files.exists(loaderPath)) {
                ResourceUtils.extractResource("/assets/CRLoader-0.0.1.jar", loaderPath);
            }

            command.add("-javaagent:" + loaderPath + "=" + saveDirPath);

            Path path = this.applyJarMods(version, CRLauncher.getInstance().getWorkDir().resolve("versions"));
            this.applyFabricMods(path, command);

            this.oldInstance.setLastTimePlayed(LocalDateTime.now());
            long start = System.currentTimeMillis();

            int exitCode = this.runGameProcess(oldInstanceManager.getCosmicDir(this.oldInstance), command);
            LOG.info("Cosmic Reach process finished with exit code {}", exitCode);

            long end = System.currentTimeMillis();

            if (this.clientCopyTmp != null && Files.exists(this.clientCopyTmp)) {
                Files.delete(this.clientCopyTmp);
            }

            long timePlayedSeconds = (end - start) / 1000;
            String timePlayed = TimeUtils.getHoursMinutesSeconds(timePlayedSeconds);
            if (!timePlayed.trim().isEmpty()) {
                LOG.info("You played for " + timePlayed + "!");
            }

            this.oldInstance.setTotalPlayedForSeconds(this.oldInstance.getTotalPlayedForSeconds() + timePlayedSeconds);
            this.oldInstance.setLastPlayedForSeconds(timePlayedSeconds);
            this.oldInstance.save();
        } catch (Exception e) {
            LOG.error("Exception occurred while trying to start Cosmic Reach", e);
        }
    }

    private Path applyJarMods(Version version, Path clientsDir) {
        Path originalClientPath = clientsDir.resolve(version.getId()).resolve(version.getId() + ".jar").toAbsolutePath();

        List<JarMod> jarMods = this.oldInstance.getJarMods();

        if (jarMods == null || jarMods.isEmpty() || jarMods.stream().noneMatch(JarMod::isActive)) {
            return originalClientPath;
        } else {
            try {
                OldInstanceManager oldInstanceManager = CRLauncher.getInstance().getInstanceManager();
                Path instanceDir = oldInstanceManager.getInstanceDir(this.oldInstance);
                Path copyOfClient = Files.copy(originalClientPath, instanceDir
                        .resolve(originalClientPath.getFileName().toString() + System.currentTimeMillis() + ".jar"));
                this.clientCopyTmp = copyOfClient;

                List<File> zipFilesToMerge = new ArrayList<>();

                for (JarMod jarMod : jarMods) {
                    if (!jarMod.isActive()) {
                        continue;
                    }

                    zipFilesToMerge.add(Paths.get(jarMod.getFullPath()).toFile());
                }

                try (ZipFile copyZip = new ZipFile(copyOfClient.toFile())) {
                    for (File modFile : zipFilesToMerge) {
                        Path unpackDir = instanceDir.resolve(modFile.getName().replace(".", "_"));
                        try (ZipFile modZip = new ZipFile(modFile)) {
                            if (Files.exists(unpackDir)) {
                                FileUtils.delete(unpackDir);
                            }
                            FileUtils.createDirectoryIfNotExists(unpackDir);

                            modZip.extractAll(unpackDir.toAbsolutePath().toString());
                        }

                        List<Path> modFiles = FileUtils.walk(unpackDir);

                        ZipParameters zipParameters = new ZipParameters();

                        for (Path modFileToAdd : modFiles) {
                            String relative = unpackDir.toAbsolutePath().toUri().relativize(modFileToAdd.toAbsolutePath().toUri()).getPath();
                            zipParameters.setFileNameInZip(relative);
                            copyZip.addFile(modFileToAdd.toFile(), zipParameters);
                        }

                        FileUtils.delete(unpackDir);
                    }
                }

                return copyOfClient;
            } catch (IOException e) {
                LOG.error("Exception while applying jar mods", e);
            }
        }

        return originalClientPath;
    }

    private void applyFabricMods(Path clientPath, List<String> command) throws IOException {
        OldInstanceManager oldInstanceManager = CRLauncher.getInstance().getInstanceManager();

        List<FabricMod> fabricMods = this.oldInstance.getFabricMods();

        if (fabricMods == null || fabricMods.isEmpty() || fabricMods.stream().noneMatch(FabricMod::isActive)) {
            command.add("-jar");
            command.add(clientPath.toString());
        } else {
            Path modsDir = oldInstanceManager.getFabricModsDir(this.oldInstance);
            Path disabledModsDir = oldInstanceManager.getCosmicDir(this.oldInstance).resolve("disabledmods");

            FileUtils.createDirectoryIfNotExists(modsDir);
            FileUtils.createDirectoryIfNotExists(disabledModsDir);

            for (FabricMod mod : fabricMods.stream().filter(Predicate.not(FabricMod::isActive)).toList()) {
                Path filePath = Paths.get(mod.getFilePath());
                if (Files.exists(filePath)) {
                    Files.copy(filePath, disabledModsDir.resolve(filePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    Files.delete(filePath);
                }
            }

            for (FabricMod mod : fabricMods.stream().filter(FabricMod::isActive).toList()) {
                Path filePath = disabledModsDir.resolve(Paths.get(mod.getFilePath()).getFileName());
                if (Files.exists(filePath)) {
                    Files.copy(filePath, modsDir.resolve(filePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    Files.delete(filePath);
                }
            }

            Path fabricLoaderDir = oldInstanceManager.getInstanceDir(this.oldInstance).resolve("fabric_loader");

            Path loaderArchivePath = fabricLoaderDir.resolve("fabric_loader.zip");

            if (!Files.exists(fabricLoaderDir)) {
                FileUtils.createDirectoryIfNotExists(fabricLoaderDir);

                GithubReleaseDownloader downloader = new GithubReleaseDownloader();

                CRDownloadDialog downloadDialog = new CRDownloadDialog();

                downloadDialog.setStage("Downloading Fabric mod loader...");
                SwingUtilities.invokeLater(() -> downloadDialog.setVisible(true));

                GithubReleaseResponse release = downloader.getReleaseResponse("ForwarD-NerN", "CosmicReach-Mod-Loader");

                downloader.downloadLatestRelease(
                        loaderArchivePath,
                        release,
                        0,
                        downloadDialog
                );
                SwingUtilities.invokeLater(() -> downloadDialog.getDialog().dispose());

                try (ZipFile loaderArchive = new ZipFile(loaderArchivePath.toFile())) {
                    loaderArchive.removeFile("launch.bat");
                    loaderArchive.removeFile("launch.sh");

                    loaderArchive.extractAll(fabricLoaderDir.toString());
                }
            }

            command.add(FabricSystemProperties.SKIP_MC_PROVIDER.asJvmArg(true));
            command.add(FabricSystemProperties.GAME_JAR_PATH.asJvmArg(clientPath));

            command.add("-classpath");

            List<String> classpath = new ArrayList<>();
            classpath.add(clientPath.toString());

            Path fabricJar = null;
            for (Path p : FileUtils.list(fabricLoaderDir)) {
                String fileName = p.getFileName().toString();
                if (fileName.contains("fabric") && fileName.contains("modloader") && fileName.endsWith(".jar")) {
                    fabricJar = p;
                }
            }

            if (fabricJar == null) {
                LOG.error("Cannot find fabric modloader jar in {}", fabricLoaderDir);
                Gui.showErrorDialog("Cannot find fabric modloader jar in " + fabricLoaderDir);
                return;
            }

            classpath.add(fabricJar.toString());

            Path depsDir = fabricLoaderDir.resolve("deps");
            if (!Files.exists(depsDir)) {
                LOG.error("Cannot find fabric modloader dependencies in {}", depsDir);
                Gui.showErrorDialog("Cannot find fabric modloader dependencies in " + depsDir);
                return;
            }

            for (Path dep : FileUtils.list(depsDir)) {
                classpath.add(dep.toString());
            }

            command.add(String.join(File.pathSeparator, classpath));

            command.add("net.fabricmc.loader.launch.knot.KnotClient");
        }
    }

    private int runGameProcess(Path dir, List<String> command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(dir.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            LOG.info(line);
        }

        return process.waitFor();
    }
}
