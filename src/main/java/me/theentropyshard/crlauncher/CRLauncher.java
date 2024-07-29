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

import me.theentropyshard.crlauncher.cli.Args;
import me.theentropyshard.crlauncher.cosmic.icon.IconManager;
import me.theentropyshard.crlauncher.cosmic.version.VersionManager;
import me.theentropyshard.crlauncher.gui.Gui;
import me.theentropyshard.crlauncher.gui.utils.WindowClosingListener;
import me.theentropyshard.crlauncher.instance.InstanceManager;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.network.UserAgentInterceptor;
import me.theentropyshard.crlauncher.quilt.QuiltManager;
import me.theentropyshard.crlauncher.utils.FileUtils;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
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

    private final ExecutorService taskPool;

    private final Gui gui;

    private volatile boolean shutdown;

    public static JFrame frame;

    public CRLauncher(Args args, Path workDir) {
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

        this.quiltManager = new QuiltManager(this.workDir.resolve("cosmic_quilt"));

        this.taskPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        this.gui = new Gui(BuildConfig.APP_NAME, this.settings.darkTheme);
        this.gui.getFrame().addWindowListener(new WindowClosingListener(e -> CRLauncher.this.shutdown()));

        this.gui.showGui();
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
