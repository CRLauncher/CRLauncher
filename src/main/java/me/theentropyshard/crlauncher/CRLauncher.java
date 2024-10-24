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
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.QuiltManager;
import me.theentropyshard.crlauncher.cosmic.mods.puzzle.PuzzleManager;
import me.theentropyshard.crlauncher.cosmic.version.VersionManager;
import me.theentropyshard.crlauncher.crmm.CrmmApi;
import me.theentropyshard.crlauncher.github.GithubApi;
import me.theentropyshard.crlauncher.github.GithubRelease;
import me.theentropyshard.crlauncher.gui.Gui;
import me.theentropyshard.crlauncher.gui.dialogs.ProgressDialog;
import me.theentropyshard.crlauncher.gui.dialogs.UpdateDialog;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.WindowClosingListener;
import me.theentropyshard.crlauncher.instance.InstanceManager;
import me.theentropyshard.crlauncher.java.JavaLocator;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.mclogs.McLogsApi;
import me.theentropyshard.crlauncher.network.UserAgentInterceptor;
import me.theentropyshard.crlauncher.utils.*;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

import javax.swing.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
    private final Path languagesDir;
    private final Path modloadersDir;

    private final Path settingsFile;
    private final Settings settings;

    private final OkHttpClient httpClient;
    private final CrmmApi crmmApi;
    private final McLogsApi mcLogsApi;

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
    public static String[] rawArgs;

    private final Map<String, Language> languages;

    public CRLauncher(Args args, String[] rawArgs, Path workDir) {
        this.args = args;
        this.workDir = workDir;

        CRLauncher.rawArgs = rawArgs;

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        if (args.hasUnknownOptions()) {
            Log.warn("Unknown options: " + args.getUnknownOptions());
        }

        CRLauncher.setInstance(this);

        this.settingsFile = this.workDir.resolve("settings.json");
        this.settings = Settings.load(this.settingsFile);

        this.librariesDir = this.workDir.resolve("libraries");

        this.cosmicDir = this.workDir.resolve("cosmic-reach");

        if (this.settings.overrideInstancesPath && StringUtils.notNullNotEmpty(this.settings.instancesDirPath)) {
            this.instancesDir = Paths.get(this.settings.instancesDirPath).toAbsolutePath();
        } else {
            this.instancesDir = this.cosmicDir.resolve("instances");
        }

        if (this.settings.overrideVersionsPath && StringUtils.notNullNotEmpty(this.settings.versionsDirPath)) {
            this.versionsDir = Paths.get(this.settings.versionsDirPath).toAbsolutePath();
        } else {
            this.versionsDir = this.cosmicDir.resolve("versions");
        }

        if (this.settings.overrideModloadersPath && StringUtils.notNullNotEmpty(this.settings.modloadersDirPath)) {
            this.modloadersDir = Paths.get(this.settings.modloadersDirPath).toAbsolutePath();
        } else {
            this.modloadersDir = this.cosmicDir.resolve("modloaders");
        }

        this.languagesDir = this.workDir.resolve("languages");
        this.createDirectories();

        this.languages = new LinkedHashMap<>();

        for (String lang : new String[]{"de_DE", "en_US", "fil_PH", "hr_HR", "ru_RU", "tl_PH"}) {
            String resourcePath = "/lang/" + lang + ".json";

            String json = null;

            try {
                json = ResourceUtils.readToString(resourcePath);
            } catch (IOException e) {
                Log.warn("Cannot load " + resourcePath + ": " + e.getMessage());
            }

            if (json == null) {
                continue;
            }

            Language language = new Language(json);
            this.languages.put(language.getName(), language);
        }

        try {
            for (Path languageJsonFile : FileUtils.list(this.languagesDir)) {
                try {
                    String json = FileUtils.readUtf8(languageJsonFile);

                    Language language = new Language(json);
                    String name = language.getName();

                    if (this.languages.containsKey(name)) {
                        Log.warn("Duplicate language is in 'languages' folder, it won't be loaded! Duplicated name: " + name);

                        continue;
                    }

                    this.languages.put(name, language);
                } catch (IOException e) {
                    Log.error("Could not load custom languages", e);
                }
            }
        } catch (IOException e) {
            Log.error("Could not list '" + this.languagesDir + "'", e);
        }

        Language language = this.getLanguage();
        UIManager.put("OptionPane.yesButtonText", language.getString("gui.general.yes"));
        UIManager.put("OptionPane.noButtonText", language.getString("gui.general.no"));
        UIManager.put("OptionPane.okButtonText", language.getString("gui.general.ok"));
        UIManager.put("OptionPane.cancelButtonText", language.getString("gui.general.cancel"));

        this.httpClient = new OkHttpClient.Builder()
            .addNetworkInterceptor(new UserAgentInterceptor(CRLauncher.USER_AGENT))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
            .build();

        this.crmmApi = new CrmmApi(this.httpClient);
        this.mcLogsApi = new McLogsApi(this.httpClient);

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

        this.quiltManager = new QuiltManager(this.modloadersDir.resolve("cosmic-quilt"));
        this.puzzleManager = new PuzzleManager(this.modloadersDir.resolve("puzzle"));

        this.taskPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        this.gui = new Gui(BuildConfig.APP_NAME, this.settings.darkTheme);
        this.gui.getFrame().addWindowListener(new WindowClosingListener(e -> CRLauncher.this.shutdown()));

        if (this.settings.checkUpdatesStartup) {
            this.taskPool.execute(() -> {
                CRLauncher.checkForUpdates(false);
            });
        }

        this.gui.showGui();
    }

    public static void checkForUpdates(boolean showDialogIfNoUpdates) {
        Log.info("Checking for updates...");

        try {
            GithubApi githubApi = new GithubApi();
            GithubRelease release = githubApi.getLatestRelease("CRLauncher", "CRLauncher");
            SemanticVersion latestVersion = SemanticVersion.parse(release.tag_name.substring(1));
            SemanticVersion currentVersion = SemanticVersion.parse(BuildConfig.APP_VERSION);

            if (latestVersion.isHigherThan(currentVersion)) {
                String baseText = "New version is available! (" + latestVersion.toVersionString() + "). Download here: ";
                String link = "https://github.com/CRLauncher/CRLauncher/releases/tag/" + release.tag_name;

                Log.info(baseText + link);

                boolean updateNow = UpdateDialog.show(release);

                if (updateNow) {
                    CRLauncher.runUpdater(githubApi, release, latestVersion);
                } else {
                    Log.info("Not updating");
                }
            } else {
                Log.info("No updates are available");

                if (!showDialogIfNoUpdates) {
                    return;
                }

                Language language = CRLauncher.getInstance().getLanguage();

                MessageBox.showPlainMessage(CRLauncher.frame,
                    language.getString("gui.updateDialog.title"),
                    language.getString("gui.settingsView.other.noUpdatesAvailable"));
            }
        } catch (IOException e) {
            Log.error("Could not check for updates", e);
        }
    }

    public static void runUpdater(GithubApi githubApi, GithubRelease release, SemanticVersion latestVersion) throws IOException {
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

        Path tmpDir = CRLauncher.getInstance().getWorkDir().resolve("tmp");
        FileUtils.createDirectoryIfNotExists(tmpDir);

        Path newLauncherFile = tmpDir.resolve(BuildConfig.APP_NAME + fileExtension);
        FileUtils.delete(newLauncherFile);

        ProgressDialog dialog = new ProgressDialog("Updating CRLauncher to " + latestVersion.toVersionString());
        dialog.setStage("Downloading launcher...");
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));

        Log.info("Downloading new version");
        GithubRelease.Asset asset = ListUtils.search(release.assets, a -> a.name.endsWith(fileExtension));
        githubApi.downloadRelease(newLauncherFile, release, release.assets.indexOf(asset), dialog);

        SwingUtilities.invokeLater(() -> dialog.getDialog().dispose());

        Path currentPath = Paths.get(URI.create(Args.class.getProtectionDomain().getCodeSource().getLocation().toString()));

        List<String> arguments = new ArrayList<>();
        arguments.add(JavaLocator.getJavaPath());
        arguments.add("-classpath");
        arguments.add(newLauncherFile.toString());
        arguments.add("me.theentropyshard.crlauncher.Updater");
        arguments.add(currentPath.toString());
        arguments.add(newLauncherFile.toString());
        arguments.addAll(Arrays.asList(CRLauncher.rawArgs));

        Log.info("Starting new version with command: " + arguments);

        ProcessBuilder builder = new ProcessBuilder(arguments);
        builder.start();

        CRLauncher.getInstance().shutdown();
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
            FileUtils.createDirectoryIfNotExists(this.languagesDir);
            FileUtils.createDirectoryIfNotExists(this.modloadersDir);
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

    public Language getLanguage() {
        Language language = this.languages.get(this.settings.language);

        if (language == null) {
            return this.languages.get("English");
        }

        return language;
    }

    public Map<String, Language> getLanguages() {
        return this.languages;
    }

    private static void setInstance(CRLauncher instance) {
        CRLauncher.instance = instance;
    }

    public OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    public CrmmApi getCrmmApi() {
        return this.crmmApi;
    }

    public McLogsApi getMcLogsApi() {
        return this.mcLogsApi;
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

    public Path getModloadersDir() {
        return this.modloadersDir;
    }

    public InstanceManager getInstanceManager() {
        return this.instanceManager;
    }

    public Gui getGui() {
        return this.gui;
    }
}
