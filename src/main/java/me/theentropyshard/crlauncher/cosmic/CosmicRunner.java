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
import me.theentropyshard.crlauncher.gui.Gui;
import me.theentropyshard.crlauncher.utils.MathUtils;
import me.theentropyshard.crlauncher.utils.OperatingSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CosmicRunner extends Thread {
    private static final Logger LOG = LogManager.getLogger(CosmicRunner.class);

    private final Version version;

    public CosmicRunner(Version version) {
        this.version = version;

        this.setName("Cosmic Reach run thread");
    }

    @Override
    public void run() {
        try {
            VersionManager versionManager = CRLauncher.getInstance().getVersionManager();

            versionManager.downloadVersion(this.version,
                    ((contentLength, totalBytesRead, bytesReadThisTime, done) -> {
                        JProgressBar downloadProgress = Gui.instance.getMainView().getDownloadProgress();
                        downloadProgress.setMinimum(0);
                        downloadProgress.setMaximum((int) contentLength);
                        downloadProgress.setValue((int) totalBytesRead);
                        downloadProgress.setString(MathUtils.round(totalBytesRead / 1024.0D / 1024.0D, 2) +
                                " MiB / " + MathUtils.round(contentLength / 1024.0D / 1024.0D, 2) + " MiB");
                    }));

            List<String> command = new ArrayList<>();
            command.add(this.getJavaPath());
            command.add("-jar");
            command.add(versionManager.getVersionPath(this.version).toAbsolutePath().toString());

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                LOG.info(line);
            }
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private String getJavaPath() {
        String exeName = OperatingSystem.isWindows() ? "javaw.exe" : "java";

        return Paths.get(System.getProperty("java.home"), "bin", exeName).toString();
    }
}
