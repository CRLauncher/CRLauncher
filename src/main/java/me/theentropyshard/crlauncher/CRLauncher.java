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

package me.theentropyshard.crlauncher;

import me.theentropyshard.crlauncher.cosmic.account.AccountManager;
import me.theentropyshard.crlauncher.cosmic.icon.IconManager;
import me.theentropyshard.crlauncher.cosmic.mods.puzzle.PuzzleManager;
import me.theentropyshard.crlauncher.cosmic.version.VersionManager;
import me.theentropyshard.crlauncher.github.GithubReleaseDownloader;
import me.theentropyshard.crlauncher.github.GithubReleaseResponse;
import me.theentropyshard.crlauncher.gui.Gui;
import me.theentropyshard.crlauncher.gui.dialogs.ProgressDialog;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.WindowClosingListener;
import me.theentropyshard.crlauncher.instance.InstanceManager;
import me.theentropyshard.crlauncher.java.JavaLocator;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.network.UserAgentInterceptor;
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.QuiltManager;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ListUtils;
import me.theentropyshard.crlauncher.utils.SemanticVersion;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

import javax.swing.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CRLauncher {


    public static final String USER_AGENT = BuildConfig.APP_NAME + "/" + BuildConfig.APP_VERSION;

    public static final int WIDTH = 960;
    public static final int HEIGHT = 540;

    private final Args args;
    private final Path workDir;

    private final Path cosmicDir;
    private final Path librariesDir;
    private final Path instancesDir;
    private final Path versionsDir;

    private final Path settingsFile;
    private final Settings settings;

    private final OkHttpClient httpClient;

    private final VersionManager versionManager;
    private final InstanceManager instanceManager;
    private final IconManager iconManager;
    private final QuiltManager quiltManager;
    private final PuzzleManager puzzleManager;
    private final AccountManager accountManager;

    private final ExecutorService taskPool;

    private final Gui gui;

    private volatile boolean shutdown;

    public static JFrame frame;

    public CRLauncher(Args args, String[] rawArgs, Path workDir) {
        this.args = args;
        this.workDir = workDir;

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        if (args.hasUnknownOptions()) {
            Log.warn("Unknown options: " + args.getUnknownOptions());
        }

        CRLauncher.setInstance(this);

        this.librariesDir = this.workDir.resolve("libraries");

        this.cosmicDir = this.workDir.resolve("cosmic-reach");
        this.instancesDir = this.cosmicDir.resolve("instances");
        this.versionsDir = this.cosmicDir.resolve("versions");
        this.createDirectories();

        this.settingsFile = this.workDir.resolve("settings.json");
        this.settings = Settings.load(this.settingsFile);

        this.httpClient = new OkHttpClient.Builder()
            .addNetworkInterceptor(new UserAgentInterceptor(CRLauncher.USER_AGENT))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
            .build();

        this.versionManager = new VersionManager(this.versionsDir);

        this.accountManager = new AccountManager(this.cosmicDir);
        try {
            this.accountManager.load();
        } catch (IOException e) {
            Log.error("Unable to load accounts", e);
        }

        this.instanceManager = new InstanceManager(this.instancesDir);
        try {
            this.instanceManager.load();
        } catch (IOException e) {
            Log.error("Unable to load instances", e);
        }

        Path iconsDir = this.cosmicDir.resolve("icons");
        this.iconManager = new IconManager(iconsDir);
        try {
            FileUtils.createDirectoryIfNotExists(iconsDir);
            this.iconManager.loadIcons();
            this.iconManager.saveBuiltinIcons();
        } catch (IOException e) {
            Log.error("Unable to load icons", e);
        }

        this.quiltManager = new QuiltManager(this.cosmicDir.resolve("cosmic-quilt"));
        this.puzzleManager = new PuzzleManager(this.cosmicDir.resolve("puzzle"));

        this.taskPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        this.gui = new Gui(BuildConfig.APP_NAME, this.settings.darkTheme);
        this.gui.getFrame().addWindowListener(new WindowClosingListener(e -> CRLauncher.this.shutdown()));

        if (this.settings.checkUpdatesStartup) {
            this.taskPool.execute(() -> {
                Log.info("Checking for updates...");

                try {
                    GithubReleaseDownloader downloader = new GithubReleaseDownloader();
                    GithubReleaseResponse release = downloader.getLatestRelease("CRLauncher", "CRLauncher");
                    SemanticVersion latestVersion = SemanticVersion.parse(release.tag_name.substring(1));
                    SemanticVersion currentVersion = SemanticVersion.parse(BuildConfig.APP_VERSION);

                    if (latestVersion.isGreaterThan(currentVersion)) {
                        String link = "https://github.com/CRLauncher/CRLauncher/releases/tag/" + release.tag_name;
                        String baseText = "New version is available! (" + latestVersion.toVersionString() + "). Download here: ";

                        Log.info(baseText + link);

                        JTextPane message = new JTextPane();
                        message.setContentType("text/html");
                        message.setText(baseText + "<a href=\"" + link + "\">" + link + "</a><br>" +
                            "Update now?");
                        message.setEditable(false);

                        boolean updateNow = MessageBox.showConfirmMessage(CRLauncher.frame, "Update", message);
                        if (updateNow) {
                            Log.info("Updating now");

                            String fileExtension;

                            if (CRLauncher.isJar()) {
                                fileExtension = ".jar";
                            } else if (CRLauncher.isExe()) {
                                fileExtension = ".exe";
                            } else {
                                throw new RuntimeException("Updating launcher while running not in jar " +
                                    "and not in exe is not supported. (Running in an IDE?)");
                            }

                            Path tmpDir = this.getWorkDir().resolve("tmp");
                            FileUtils.createDirectoryIfNotExists(tmpDir);
                            Path newLauncherFile = tmpDir.resolve(BuildConfig.APP_NAME + fileExtension);

                            FileUtils.delete(newLauncherFile);

                            GithubReleaseResponse.Asset asset = ListUtils.search(release.assets, a -> a.name.endsWith(fileExtension));

                            ProgressDialog dialog = new ProgressDialog("Updating CRLauncher to " + latestVersion.toVersionString());

                            SwingUtilities.invokeLater(() -> dialog.setVisible(true));
                            Log.info("Downloading new version");
                            downloader.downloadRelease(newLauncherFile, release, release.assets.indexOf(asset), dialog);
                            SwingUtilities.invokeLater(() -> dialog.getDialog().dispose());

                            Path currentPath = Paths.get(URI.create(Args.class.getProtectionDomain().getCodeSource().getLocation().toString()));

                            List<String> arguments = new ArrayList<>();
                            arguments.add(JavaLocator.getJavaPath());
                            arguments.add("-classpath");
                            arguments.add(newLauncherFile.toString());
                            arguments.add("me.theentropyshard.crlauncher.Updater");
                            arguments.add(currentPath.toString());
                            arguments.add(newLauncherFile.toString());
                            arguments.addAll(Arrays.asList(rawArgs));

                            Log.info("Starting new version with command: " + arguments);

                            ProcessBuilder builder = new ProcessBuilder(arguments);
                            builder.start();

                            this.shutdown();
                        } else {
                            Log.info("Not updating");
                        }
                    } else {
                        Log.info("No updates are available");
                    }
                } catch (IOException e) {
                    Log.error("Could not check for updates", e);
                }
            });
        }

        this.gui.showGui();
    }

    public static boolean isExe() {
        Path path = Paths.get(URI.create(Args.class.getProtectionDomain().getCodeSource().getLocation().toString()));

        return path.toString().endsWith(".exe");
    }

    public static boolean isJar() {
        Path path = Paths.get(URI.create(Args.class.getProtectionDomain().getCodeSource().getLocation().toString()));

        return path.toString().endsWith(".jar");
    }

    private void createDirectories() {
        try {
            FileUtils.createDirectoryIfNotExists(this.workDir);
            FileUtils.createDirectoryIfNotExists(this.librariesDir);
            FileUtils.createDirectoryIfNotExists(this.instancesDir);
            FileUtils.createDirectoryIfNotExists(this.versionsDir);
        } catch (IOException e) {
            Log.error("Unable to create launcher directories", e);
        }
    }

    public void doTask(Runnable r) {
        this.taskPool.submit(r);
    }

    public void shutdown() {
        if (this.shutdown) {
            return;
        }

        this.shutdown = true;

        this.taskPool.shutdown();

        try {
            this.accountManager.save();
        } catch (IOException e) {
            Log.error("Exception while saving accounts", e);
        }

        this.instanceManager.getInstances().forEach(instance -> {
            try {
                instance.save();
            } catch (IOException e) {
                Log.error("Exception while saving instance '" + instance + "'", e);
            }
        });

        this.settings.lastInstanceGroup = String.valueOf(this.gui.getPlayView().getModel().getSelectedItem());

        this.settings.save(this.settingsFile);

        System.exit(0);
    }

    private static CRLauncher instance;

    public static CRLauncher getInstance() {
        return CRLauncher.instance;
    }

    private static void setInstance(CRLauncher instance) {
        CRLauncher.instance = instance;
    }

    public OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    public VersionManager getVersionManager() {
        return this.versionManager;
    }

    public IconManager getIconManager() {
        return this.iconManager;
    }

    public QuiltManager getQuiltManager() {
        return this.quiltManager;
    }

    public PuzzleManager getPuzzleManager() {
        return this.puzzleManager;
    }

    public AccountManager getAccountManager() {
        return this.accountManager;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public Args getArgs() {
        return this.args;
    }

    public Path getWorkDir() {
        return this.workDir;
    }

    public Path getCosmicDir() {
        return this.cosmicDir;
    }

    public Path getLibrariesDir() {
        return this.librariesDir;
    }

    public Path getInstancesDir() {
        return this.instancesDir;
    }

    public Path getVersionsDir() {
        return this.versionsDir;
    }

    public InstanceManager getInstanceManager() {
        return this.instanceManager;
    }

    public Gui getGui() {
        return this.gui;
    }
}
