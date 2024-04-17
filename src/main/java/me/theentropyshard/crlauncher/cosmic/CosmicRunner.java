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
import me.theentropyshard.crlauncher.cosmic.launcher.CosmicLauncher;
import me.theentropyshard.crlauncher.cosmic.launcher.CosmicLauncherFactory;
import me.theentropyshard.crlauncher.cosmic.launcher.LaunchType;
import me.theentropyshard.crlauncher.cosmic.mods.fabric.FabricMod;
import me.theentropyshard.crlauncher.cosmic.mods.jar.JarMod;
import me.theentropyshard.crlauncher.cosmic.version.Version;
import me.theentropyshard.crlauncher.cosmic.version.VersionManager;
import me.theentropyshard.crlauncher.gui.dialogs.CRDownloadDialog;
import me.theentropyshard.crlauncher.instance.InstanceType;
import me.theentropyshard.crlauncher.instance.OldInstance;
import me.theentropyshard.crlauncher.instance.OldInstanceManager;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.TimeUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

            OldInstanceManager oldInstanceManager = CRLauncher.getInstance().getInstanceManager();
            Path saveDirPath = oldInstanceManager.getCosmicDir(this.oldInstance);

            Path versionsDir = CRLauncher.getInstance().getWorkDir().resolve("versions");
            Path clientPath = versionsDir.resolve(version.getId()).resolve(version.getId() + ".jar").toAbsolutePath();

            this.oldInstance.setLastTimePlayed(LocalDateTime.now());

            CosmicLauncher launcher;

            if (this.oldInstance.getType() == InstanceType.VANILLA) {
                clientPath = this.applyJarMods(version, versionsDir);

                launcher = CosmicLauncherFactory.getLauncher(
                        LaunchType.VANILLA,
                        saveDirPath,
                        saveDirPath,
                        clientPath
                );
            } else if (this.oldInstance.getType() == InstanceType.FABRIC) {
                this.updateFabricMods();

                launcher = CosmicLauncherFactory.getLauncher(
                        LaunchType.FABRIC,
                        saveDirPath,
                        saveDirPath,
                        clientPath,
                        oldInstanceManager.getFabricModsDir(this.oldInstance)
                );
            } else if (this.oldInstance.getType() == InstanceType.QUILT) {
                launcher = CosmicLauncherFactory.getLauncher(
                        LaunchType.QUILT,
                        saveDirPath,
                        saveDirPath,
                        clientPath,
                        oldInstanceManager.getQuiltModsDir(this.oldInstance)
                );
            } else {
                throw new IllegalArgumentException("Unknown instance type: " + this.oldInstance.getType());
            }

            long start = System.currentTimeMillis();

            int exitCode = launcher.launch(LOG::info);

            long end = System.currentTimeMillis();

            LOG.info("Cosmic Reach process finished with exit code {}", exitCode);

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
        } finally {
            if (this.clientCopyTmp != null && Files.exists(this.clientCopyTmp)) {
                try {
                    Files.delete(this.clientCopyTmp);
                } catch (IOException e) {
                    LOG.error("Unable to delete temporary client", e);
                }
            }
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

    private void updateFabricMods() throws IOException {
        OldInstanceManager oldInstanceManager = CRLauncher.getInstance().getInstanceManager();
        List<FabricMod> fabricMods = this.oldInstance.getFabricMods();

        if (fabricMods != null && !fabricMods.isEmpty() && fabricMods.stream().anyMatch(FabricMod::isActive)) {
            Path modsDir = oldInstanceManager.getFabricModsDir(this.oldInstance);
            Path disabledModsDir = oldInstanceManager.getCosmicDir(this.oldInstance).resolve("disabledfabricmods");

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
}
