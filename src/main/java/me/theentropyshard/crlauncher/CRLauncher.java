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

import me.theentropyshard.crlauncher.cosmic.VersionManager;
import me.theentropyshard.crlauncher.gui.Gui;
import me.theentropyshard.crlauncher.network.UserAgentInterceptor;
import me.theentropyshard.crlauncher.utils.FileUtils;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class CRLauncher {
    private static final Logger LOG = LogManager.getLogger(CRLauncher.class);

    public static final String NAME = "CRLauncher";
    public static final String VERSION = "0.0.1";
    public static final String USER_AGENT = CRLauncher.NAME + "/" + CRLauncher.VERSION;

    private final Args args;
    private final Path workDir;

    private final OkHttpClient httpClient;

    private final VersionManager versionManager;

    public CRLauncher(Args args, Path workDir) {
        this.args = args;
        this.workDir = workDir;

        instance = this;

        try {
            FileUtils.createDirectoryIfNotExists(this.workDir);
        } catch (IOException e) {
            LOG.error(e);
            System.exit(1);
        }

        this.httpClient = CRLauncher.createHttpClient(CRLauncher.USER_AGENT);

        this.versionManager = new VersionManager(this.workDir.resolve("versions"));

        Gui gui = new Gui();
        gui.show();
    }

    private static OkHttpClient createHttpClient(String userAgent) {
        return new OkHttpClient.Builder()
                .addNetworkInterceptor(new UserAgentInterceptor(userAgent))
                .connectTimeout(60L, TimeUnit.SECONDS)
                .readTimeout(60L, TimeUnit.SECONDS)
                .writeTimeout(60L, TimeUnit.SECONDS)
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .build();
    }

    public Args getArgs() {
        return this.args;
    }

    public Path getWorkDir() {
        return this.workDir;
    }

    public OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    private static CRLauncher instance;

    public static CRLauncher getInstance() {
        return instance;
    }

    public VersionManager getVersionManager() {
        return this.versionManager;
    }
}
