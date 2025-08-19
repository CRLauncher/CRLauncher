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

package me.theentropyshard.crlauncher.java;

import net.lingala.zip4j.ZipFile;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.network.progress.ProgressListener;
import me.theentropyshard.crlauncher.network.progress.ProgressNetworkInterceptor;
import me.theentropyshard.crlauncher.utils.OperatingSystem;

public class JavaManager {
    private static final Map<JavaVersion, Map<OperatingSystem, String>> JRE_DOWNLOADS = Map.of(
        JavaVersion.VERSION_17, Map.of(
            OperatingSystem.WINDOWS, "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.6%2B10/OpenJDK17U-jre_x64_windows_hotspot_17.0.6_10.zip",
            OperatingSystem.LINUX, "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.6%2B10/OpenJDK17U-jre_x64_linux_hotspot_17.0.6_10.tar.gz",
            OperatingSystem.MACOS, "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.6%2B10/OpenJDK17U-jre_x64_mac_hotspot_17.0.6_10.tar.gz"
        ),
        JavaVersion.VERSION_24, Map.of(
            OperatingSystem.WINDOWS, "https://github.com/adoptium/temurin24-binaries/releases/download/jdk-24.0.2%2B12/OpenJDK24U-jre_x64_windows_hotspot_24.0.2_12.zip",
            OperatingSystem.LINUX, "https://github.com/adoptium/temurin24-binaries/releases/download/jdk-24.0.2%2B12/OpenJDK24U-jre_x64_linux_hotspot_24.0.2_12.tar.gz",
            OperatingSystem.MACOS, "https://github.com/adoptium/temurin24-binaries/releases/download/jdk-24.0.2%2B12/OpenJDK24U-jre_x64_mac_hotspot_24.0.2_12.tar.gz"
        )
    );

    private final Path workDir;

    private final Map<JavaVersion, String> localJREs;

    public JavaManager(Path workDir) {
        this.workDir = workDir;

        this.localJREs = new HashMap<>();

        try {
            this.loadLocalJREs();
        } catch (IOException e) {
            Log.error("Could not load local JREs from " + this.workDir);
        }
    }

    private Path getJavaPath(JavaVersion version) {
        Path jreDir = this.workDir.resolve(version.dirName);

        if (OperatingSystem.isMacOS()) {
            jreDir =  jreDir.resolve("Contents").resolve("Home");
        }

        return jreDir.resolve("bin").resolve(OperatingSystem.getCurrent().getJavaExecutableName());
    }

    private void loadLocalJREs() throws IOException {
        for (JavaVersion version : JavaVersion.values()) {
            Path javaExecutable = this.getJavaPath(version);

            if (Files.exists(javaExecutable)) {
                this.localJREs.put(version, javaExecutable.toString());
            }
        }
    }

    public String getJavaExecutable(JavaVersion version) {
        if (this.isJREInstalled(version)) {
            return this.localJREs.get(version);
        }

        return "";
    }

    public boolean isJREInstalled(JavaVersion version) {
        return this.localJREs.containsKey(version);
    }

    private String getArchiveExtension() {
        return switch (OperatingSystem.getCurrent()) {
            case WINDOWS -> ".zip";
            case LINUX, MACOS -> ".tar.gz";
            case UNKNOWN -> throw new RuntimeException();
        };
    }

    public void installJRE(JavaVersion version, ProgressListener listener) throws Exception {
        listener.setStage("Downloading " + version.getDirName());

        Path archiveFile = this.workDir.resolve(version.getDirName() + this.getArchiveExtension());

        OkHttpClient client = CRLauncher.getInstance().getHttpClient().newBuilder()
            .addNetworkInterceptor(new ProgressNetworkInterceptor(listener))
            .build();

        HttpDownload download = new HttpDownload.Builder()
            .httpClient(client)
            .saveAs(archiveFile)
            .url(JavaManager.JRE_DOWNLOADS.get(version).get(OperatingSystem.getCurrent()))
            .build();

        download.execute();

        listener.setStage("Extracting JRE");

        if (OperatingSystem.getCurrent() == OperatingSystem.WINDOWS) {
            try (ZipFile zipFile = new ZipFile(archiveFile.toFile())) {
                zipFile.extractAll(this.workDir.toString());
            }
        } else {
            ProcessBuilder builder = new ProcessBuilder("tar", "-xf", archiveFile.toString());
            builder.directory(this.workDir.toFile());
            int code = builder.start().waitFor();

            if (code != 0) {
                throw new RuntimeException("Could not extract " + archiveFile);
            }
        }

        this.localJREs.put(version, this.getJavaPath(version).toString());
    }

    public List<String> getLocalJREs() {
        return new ArrayList<>(this.localJREs.values());
    }

    public enum JavaVersion {
        VERSION_17("jdk-17.0.6+10-jre"),
        VERSION_24("jdk-24.0.2+12-jre");

        private final String dirName;

        JavaVersion(String dirName) {
            this.dirName = dirName;
        }

        public String getDirName() {
            return this.dirName;
        }
    }
}
