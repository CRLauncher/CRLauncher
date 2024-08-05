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
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.cosmic.launcher.CosmicLauncher;
import me.theentropyshard.crlauncher.cosmic.launcher.CosmicLauncherFactory;
import me.theentropyshard.crlauncher.cosmic.launcher.LaunchType;
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.QuiltMod;
import me.theentropyshard.crlauncher.cosmic.mods.fabric.FabricMod;
import me.theentropyshard.crlauncher.cosmic.mods.jar.JarMod;
import me.theentropyshard.crlauncher.cosmic.version.Version;
import me.theentropyshard.crlauncher.cosmic.version.VersionList;
import me.theentropyshard.crlauncher.cosmic.version.VersionManager;
import me.theentropyshard.crlauncher.gui.LauncherConsole;
import me.theentropyshard.crlauncher.gui.dialogs.CRDownloadDialog;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.instance.InstanceType;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.TimeUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CosmicRunner extends Thread {


    private final Instance instance;

    private Path clientCopyTmp;

    public CosmicRunner(Instance instance) {
        this.instance = instance;

        this.setName("Cosmic Reach run thread");
    }

    @Override
    public void run() {
        VersionManager versionManager = CRLauncher.getInstance().getVersionManager();

        this.updateCosmicVersion();

        try {
            Version version = versionManager.getVersion(this.instance.getCosmicVersion());

            CRDownloadDialog dialog = new CRDownloadDialog();
            SwingUtilities.invokeLater(() -> dialog.setVisible(true));
            versionManager.downloadVersion(version, dialog);
            dialog.getDialog().dispose();

            Path saveDirPath = this.instance.getCosmicDir();

            Path versionsDir = CRLauncher.getInstance().getVersionsDir();
            Path clientPath = versionsDir.resolve(version.getId()).resolve(version.getId() + ".jar").toAbsolutePath();

            this.instance.setLastTimePlayed(LocalDateTime.now());

            CosmicLauncher launcher;

            if (this.instance.getType() == InstanceType.VANILLA) {
                clientPath = this.applyJarMods(version, versionsDir);

                launcher = CosmicLauncherFactory.getLauncher(
                    LaunchType.VANILLA,
                    saveDirPath,
                    saveDirPath,
                    clientPath
                );
            } else if (this.instance.getType() == InstanceType.FABRIC) {
                this.updateFabricMods();

                launcher = CosmicLauncherFactory.getLauncher(
                    LaunchType.FABRIC,
                    saveDirPath,
                    saveDirPath,
                    clientPath,
                    this.instance.getFabricModsDir(),
                    this.instance.getFabricVersion()
                );
            } else if (this.instance.getType() == InstanceType.QUILT) {
                this.updateQuiltMods();

                launcher = CosmicLauncherFactory.getLauncher(
                    LaunchType.QUILT,
                    saveDirPath,
                    saveDirPath,
                    clientPath,
                    this.instance.getQuiltModsDir(),
                    this.instance.getQuiltVersion()
                );
            } else {
                throw new IllegalArgumentException("Unknown instance type: " + this.instance.getType());
            }

            Settings settings = CRLauncher.getInstance().getSettings();

            int launchOption = settings.whenCRLaunchesOption;

            boolean consoleWasOpen = LauncherConsole.instance.getFrame().isVisible();

            switch (launchOption) {
                case 1 -> CRLauncher.frame.setVisible(false);
                case 2 -> {
                    CRLauncher.frame.setVisible(false);
                    LauncherConsole.instance.setVisible(false);
                }
            }

            long start = System.currentTimeMillis();

            int exitCode = launcher.launch(Log::info);

            long end = System.currentTimeMillis();

            int exitsOption = settings.whenCRExitsOption;
            if (exitsOption == 0) {
                switch (launchOption) {
                    case 1 -> CRLauncher.frame.setVisible(true);
                    case 2 -> {
                        if (consoleWasOpen) {
                            LauncherConsole.instance.setVisible(true);
                        }
                        CRLauncher.frame.setVisible(true);
                    }
                }
            }

            Log.info("Cosmic Reach process finished with exit code " + exitCode);

            long timePlayedSeconds = (end - start) / 1000;
            String timePlayed = TimeUtils.getHoursMinutesSeconds(timePlayedSeconds);
            if (!timePlayed.trim().isEmpty()) {
                Log.info("You played for " + timePlayed + "!");
            }

            this.instance.updatePlaytime(timePlayedSeconds);
            this.instance.save();

            if (exitCode == 0 && exitsOption == 1) {
                CRLauncher.getInstance().shutdown();
            }
        } catch (Exception e) {
            Log.error("Exception occurred while trying to start Cosmic Reach", e);
        } finally {
            if (this.clientCopyTmp != null && Files.exists(this.clientCopyTmp)) {
                try {
                    Files.delete(this.clientCopyTmp);
                } catch (IOException e) {
                    Log.error("Unable to delete temporary client", e);
                }
            }
        }
    }

    private void updateCosmicVersion() {
        VersionManager versionManager = CRLauncher.getInstance().getVersionManager();

        if (this.instance.isAutoUpdateToLatest()) {
            VersionList versionList = versionManager.getVersionList();

            if (versionList == null) {
                try {
                    versionManager.loadRemoteVersions();
                    versionList = versionManager.getVersionList();
                } catch (IOException e) {
                    Log.error("Could not load remote versions, no auto-update performed", e);

                    return;
                }
            }

            this.instance.setCosmicVersion(versionList.getLatest().getPreAlpha());
        }
    }

    private Path applyJarMods(Version version, Path clientsDir) {
        Path originalClientPath = clientsDir.resolve(version.getId()).resolve(version.getId() + ".jar").toAbsolutePath();

        List<JarMod> jarMods = this.instance.getJarMods();

        if (jarMods == null || jarMods.isEmpty() || jarMods.stream().noneMatch(JarMod::isActive)) {
            return originalClientPath;
        } else {
            try {
                this.clientCopyTmp = Files.copy(originalClientPath, this.instance.getWorkDir()
                    .resolve(originalClientPath.getFileName().toString() + System.currentTimeMillis() + ".jar"));

                List<File> zipFilesToMerge = new ArrayList<>();

                for (JarMod jarMod : jarMods) {
                    if (!jarMod.isActive()) {
                        continue;
                    }

                    zipFilesToMerge.add(Paths.get(jarMod.getFullPath()).toFile());
                }

                try (ZipFile copyZip = new ZipFile(this.clientCopyTmp.toFile())) {
                    for (File modFile : zipFilesToMerge) {
                        Path unpackDir = this.instance.getWorkDir().resolve(modFile.getName().replace(".", "_"));
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

                return this.clientCopyTmp;
            } catch (IOException e) {
                Log.error("Exception while applying jar mods", e);
            }
        }

        return originalClientPath;
    }

    private void updateFabricMods() throws IOException {
        List<FabricMod> fabricMods = this.instance.getFabricMods();

        if (!fabricMods.isEmpty()) {
            Path modsDir = this.instance.getFabricModsDir();
            Path disabledModsDir = this.instance.getDisabledFabricModsDir();

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
        }
    }

    private void updateQuiltMods() throws IOException {
        List<QuiltMod> quiltMods = this.instance.getQuiltMods();

        if (!quiltMods.isEmpty()) {
            Path modsDir = this.instance.getQuiltModsDir();
            Path disabledModsDir = this.instance.getDisabledQuiltModsDir();

            FileUtils.createDirectoryIfNotExists(modsDir);
            FileUtils.createDirectoryIfNotExists(disabledModsDir);

            for (QuiltMod mod : quiltMods.stream().filter(Predicate.not(q -> q.active)).toList()) {
                Path filePath = Paths.get(mod.filePath);
                if (Files.exists(filePath)) {
                    Files.copy(filePath, disabledModsDir.resolve(filePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    Files.delete(filePath);
                }
            }

            for (QuiltMod mod : quiltMods.stream().filter(q -> q.active).toList()) {
                Path filePath = disabledModsDir.resolve(Paths.get(mod.filePath).getFileName());
                if (Files.exists(filePath)) {
                    Files.copy(filePath, modsDir.resolve(filePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    Files.delete(filePath);
                }
            }
        }
    }
}
