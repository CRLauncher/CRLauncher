/*
 * CRLauncher - https://github.com/CRLauncher/CRLauncher
 * Copyright (C) 2024-2025 CRLauncher
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
import me.theentropyshard.crlauncher.cosmic.account.Account;
import me.theentropyshard.crlauncher.cosmic.account.ItchIoAccount;
import me.theentropyshard.crlauncher.cosmic.launcher.*;
import me.theentropyshard.crlauncher.cosmic.mods.Mod;
import me.theentropyshard.crlauncher.cosmic.version.Version;
import me.theentropyshard.crlauncher.cosmic.version.VersionManager;
import me.theentropyshard.crlauncher.gui.console.LauncherConsole;
import me.theentropyshard.crlauncher.gui.components.InstanceItem;
import me.theentropyshard.crlauncher.gui.dialogs.ProgressDialog;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.cosmic.mods.ModLoader;
import me.theentropyshard.crlauncher.java.JavaLocator;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ProcessReader;
import me.theentropyshard.crlauncher.utils.SystemProperty;
import me.theentropyshard.crlauncher.utils.TimeUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class CosmicRunner extends Thread {
    public static final String[] FLAG_SET_1 = {
        "-XX:+UnlockExperimentalVMOptions", "-XX:+UnlockDiagnosticVMOptions", "-XX:+AlwaysPreTouch",
        "-XX:+DisableExplicitGC", "-XX:+UseNUMA", "-XX:NmethodSweepActivity=1", "-XX:ReservedCodeCacheSize=400M",
        "-XX:NonNMethodCodeHeapSize=12M", "-XX:ProfiledCodeHeapSize=194M", "-XX:NonProfiledCodeHeapSize=194M",
        "-XX:-DontCompileHugeMethods", "-XX:MaxNodeLimit=240000", "-XX:NodeLimitFudgeFactor=8000", "-XX:+UseVectorCmov",
        "-XX:+PerfDisableSharedMem", "-XX:+UseFastUnorderedTimeStamps", "-XX:+UseCriticalJavaThreadPriority",
        "-XX:ThreadPriorityPolicy=1", "-XX:AllocatePrefetchStyle=3", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=37",
        "-XX:+PerfDisableSharedMem", "-XX:G1HeapRegionSize=16M", "-XX:G1NewSizePercent=23", "-XX:G1ReservePercent=20",
        "-XX:SurvivorRatio=32", "-XX:G1MixedGCCountTarget=3", "-XX:G1HeapWastePercent=20",
        "-XX:InitiatingHeapOccupancyPercent=10", "-XX:G1RSetUpdatingPauseTimePercent=0", "-XX:MaxTenuringThreshold=1",
        "-XX:G1SATBBufferEnqueueingThresholdPercent=30", "-XX:G1ConcMarkStepDurationMillis=5.0",
        "-XX:G1ConcRSHotCardLimit=16", "-XX:G1ConcRefinementServiceIntervalMillis=150", "-XX:GCTimeRatio=99",
        "-XX:+UseLargePages", "-XX:LargePageSizeInBytes=2m"
    };

    public static final String[] FLAG_SET_2 = {
        "-XX:+UnlockExperimentalVMOptions", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=37", "-XX:+PerfDisableSharedMem",
        "-XX:G1HeapRegionSize=16M", "-XX:G1NewSizePercent=23", "-XX:G1ReservePercent=20", "-XX:SurvivorRatio=32",
        "-XX:G1MixedGCCountTarget=3", "-XX:G1HeapWastePercent=20", "-XX:InitiatingHeapOccupancyPercent=10",
        "-XX:G1RSetUpdatingPauseTimePercent=0", "-XX:MaxTenuringThreshold=1", "-XX:G1SATBBufferEnqueueingThresholdPercent=30",
        "-XX:G1ConcMarkStepDurationMillis=5.0", "-XX:G1ConcRSHotCardLimit=16",
        "-XX:G1ConcRefinementServiceIntervalMillis=150", "-XX:GCTimeRatio=99"
    };

    public static final String ITCH_IO_API_KEY_ENV_KEY = "ITCHIO_API_KEY";

    private final CosmicInstance instance;
    private final InstanceItem item;

    private Process process;
    private Path clientCopyTmp;

    public CosmicRunner(CosmicInstance instance, InstanceItem item) {
        this.instance = instance;
        this.item = item;

        this.setName("Cosmic Reach run thread");
    }

    @Override
    public synchronized void start() {
        if (this.instance.isRunning()) {
            return;
        }

        this.instance.setRunning(true);
        this.item.setEnabled(false);

        super.start();
    }

    @Override
    public void run() {
        VersionManager versionManager = CRLauncher.getInstance().getVersionManager();

        try {
            if (!versionManager.isLoaded()) {
                versionManager.load();
            }

            Log.info("Starting instance \"" + this.instance.getName() + "\"");
            Log.info("Cosmic Reach version: " + this.instance.getCosmicVersion());

            this.updateCosmicVersion();

            Log.info("Mod Loader: " + this.instance.getModLoader());

            Version version = versionManager.getVersion(this.instance.getCosmicVersion());

            Log.info("Versions source: " + (CRLauncher.getInstance().getSettings().versionsSourceOption == 1 ? "Itch" : "Cosmic Archive"));

            if (version == null) {
                SwingUtilities.invokeLater(() -> {
                    String s = CRLauncher.getInstance().getLanguage().getString("messages.gui.progressDialog.nonexistentVersion");
                    String message = s
                        .replace("$$VERSION_ID$$", this.instance.getCosmicVersion())
                        .replace("$$VERSION_LIST$$",
                            switch (CRLauncher.getInstance().getSettings().versionsSourceOption) {
                                case 1 -> "Itch";
                                default -> "Cosmic Archive";
                            });

                    Log.error(message);
                    MessageBox.showErrorMessage(CRLauncher.frame, message);
                });

                return;
            } else {
                Log.info("Downloading/verifying Cosmic Reach");

                ProgressDialog dialog = new ProgressDialog("Downloading Cosmic Reach");
                SwingUtilities.invokeLater(() -> dialog.setVisible(true));
                versionManager.downloadVersion(version, dialog);
                dialog.getDialog().dispose();
            }

            Path saveDirPath = this.instance.getCosmicDir();
            Log.info("Working directory: " + saveDirPath);

            this.instance.setLastTimePlayed(LocalDateTime.now());

            String javaPath = this.instance.getJavaPath();
            if (javaPath == null || javaPath.isEmpty()) {
                javaPath = JavaLocator.getJavaPath();
                this.instance.setJavaPath(javaPath);
            }

            Log.info("Java path: " + javaPath);

            Path clientPath = this.applyJarMods(version);

            CosmicLauncher launcher;

            if (this.instance.getModLoader() == ModLoader.VANILLA) {
                launcher = CosmicLauncherFactory.getLauncher(
                    javaPath,
                    LaunchType.VANILLA,
                    saveDirPath,
                    saveDirPath,
                    clientPath
                );
            } else {
                launcher = switch (this.instance.getModLoader()) {
                    case FABRIC -> CosmicLauncherFactory.getLauncher(
                        javaPath,
                        LaunchType.FABRIC,
                        saveDirPath,
                        saveDirPath,
                        clientPath,
                        this.instance.getFabricModsDir(),
                        this.instance.getFabricVersion()
                    );
                    case QUILT -> CosmicLauncherFactory.getLauncher(
                        javaPath,
                        LaunchType.QUILT,
                        saveDirPath,
                        saveDirPath,
                        clientPath,
                        this.instance.getQuiltModsDir(),
                        this.instance.getQuiltVersion()
                    );
                    case PUZZLE -> CosmicLauncherFactory.getLauncher(
                        javaPath,
                        LaunchType.PUZZLE,
                        saveDirPath,
                        saveDirPath,
                        clientPath,
                        this.instance.getPuzzleModsDir(),
                        this.instance.getPuzzleVersion()
                    );
                    default -> throw new IllegalArgumentException("Unknown instance type: " + this.instance.getModLoader());
                };
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

            if (launcher instanceof PatchCosmicLauncher patchLauncher) {
                patchLauncher.defineProperty(new SystemProperty("file.encoding", "utf-8"));
                patchLauncher.defineProperty(new SystemProperty("console.encoding", "utf-8"));

                patchLauncher.setCustomWindowTitle(this.instance.getCustomWindowTitle());

                Account currentAccount = CRLauncher.getInstance().getAccountManager().getCurrentAccount();

                if (currentAccount != null) {
                    patchLauncher.putEnvironment("COSMIC_REACH_OFFLINE_DISPLAY_NAME", currentAccount.getUsername());
                    patchLauncher.setOfflineUsername(currentAccount.getUsername());
                    patchLauncher.setPatchOfflineAccount(CRLauncher.getInstance().getSettings().patchOfflineAccount);
                    patchLauncher.setAppendUsername(CRLauncher.getInstance().getSettings().appendUsername);
                }

                if (currentAccount instanceof ItchIoAccount itch) {
                    patchLauncher.putEnvironment(CosmicRunner.ITCH_IO_API_KEY_ENV_KEY, itch.getItchIoApiKey());
                }

                if (this.instance.isFullscreen()) {
                    patchLauncher.setFullscreen(true);
                } else if (this.instance.isMaximized()) {
                    patchLauncher.setMaximized(true);
                } else {
                    patchLauncher.setWindowWidth(this.instance.getCosmicWindowWidth());
                    patchLauncher.setWindowHeight(this.instance.getCosmicWindowHeight());
                }

                int currentFlagsOption = this.instance.getCurrentFlagsOption();

                if (currentFlagsOption == 0) {
                    Set<String> customJvmFlags = this.instance.getCustomJvmFlags();
                    if (customJvmFlags != null && !customJvmFlags.isEmpty()) {
                        for (String customJvmFlag : customJvmFlags) {
                            if (customJvmFlag.isEmpty()) {
                                continue;
                            }

                            patchLauncher.addJvmFlag(customJvmFlag);
                        }
                    }
                } else {
                    String[] flags = CosmicRunner.getBuiltinFlags(currentFlagsOption);
                    for (String customJvmFlag : flags) {
                        if (customJvmFlag.isEmpty()) {
                            continue;
                        }

                        patchLauncher.addJvmFlag(customJvmFlag);
                    }
                }
            }

            long start = System.currentTimeMillis();

            int exitCode = this.startProcess(launcher, launchOption == 3);

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

            String exitMessage = "Cosmic Reach process finished with exit code " + exitCode;
            if (exitCode == 0) {
                Log.info(exitMessage);
            } else {
                Log.error(exitMessage);
            }

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
            this.instance.setRunning(false);
            this.item.setEnabled(true);

            if (this.clientCopyTmp != null && Files.exists(this.clientCopyTmp)) {
                try {
                    Files.delete(this.clientCopyTmp);
                } catch (IOException e) {
                    Log.error("Unable to delete temporary client", e);
                }
            }
        }
    }

    private int startProcess(CosmicLauncher launcher, boolean exitAfterLaunch) throws Exception {
        this.process = launcher.launch(exitAfterLaunch);

        String userHome = System.getProperty("user.home");

        new ProcessReader(this.process).read(line -> {
            if (line.contains(userHome)) {
                line = line.replace(userHome, "<UserHome>");
            }

            ModLoader type = this.instance.getModLoader();
            if (type == ModLoader.VANILLA || type == ModLoader.FABRIC) {
                Log.cosmicReachVanilla(line);
            } else {
                Log.cosmicReachModded(line);
            }
        });

        return this.process.waitFor();
    }

    public void stopGame() {
        if (this.process == null || !this.process.isAlive()) {
            return;
        }

        this.process.destroy();

        Log.info("Destroyed Cosmic Reach process for instance " + this.instance.getName());
    }

    private void updateCosmicVersion() {
        VersionManager versionManager = CRLauncher.getInstance().getVersionManager();

        if (this.instance.isAutoUpdateToLatest()) {
            if (!versionManager.isLoaded()) {
                try {
                    versionManager.load();
                } catch (IOException e) {
                    Log.error("Could not load game versions, not updating instance to latest");

                    return;
                }
            }

            Version latest = versionManager.getLatest();

            if (latest == null) {
                Log.warn("Latest version returned by version manager is null");

                return;
            }

            this.instance.setCosmicVersion(latest.getId());

            Log.info("Updated Cosmic Reach version to " + latest.getId());
        }
    }

    private Path applyJarMods(Version version) {
        Path originalClientPath = CRLauncher.getInstance().getVersionManager().getVersionJar(version);

        List<Mod> jarMods = this.instance.getJarMods();

        if (jarMods == null || jarMods.isEmpty() || jarMods.stream().noneMatch(Mod::isActive)) {
            return originalClientPath;
        } else {
            try {
                Log.info("Creating modified client...");
                this.clientCopyTmp = Files.copy(originalClientPath, this.instance.getWorkDir()
                    .resolve(originalClientPath.getFileName().toString() + System.currentTimeMillis() + ".jar"));

                Log.info("Collecting jar mods...");
                List<File> zipFilesToMerge = FileUtils.list(this.instance.getJarModsDir(), Files::isRegularFile)
                    .stream()
                    .map(Path::toFile)
                    .toList();

                Log.info("Merging modified files...");
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

                Log.info("Successfully applied jar mods");

                return this.clientCopyTmp;
            } catch (IOException e) {
                Log.error("Exception while applying jar mods", e);
            }
        }

        return originalClientPath;
    }

    public static String[] getBuiltinFlags(int option) {
        return switch (option) {
            case 1 -> CosmicRunner.FLAG_SET_1;
            case 2 -> CosmicRunner.FLAG_SET_2;
            default -> CosmicRunner.FLAG_SET_1;
        };
    }
}
