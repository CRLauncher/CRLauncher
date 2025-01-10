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
import me.theentropyshard.crlauncher.cosmic.account.ItchIoAccount;
import me.theentropyshard.crlauncher.cosmic.itch.ItchVersion;
import me.theentropyshard.crlauncher.cosmic.version.VersionManager;
import me.theentropyshard.crlauncher.itch.BuildFile;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.network.progress.ProgressListener;
import me.theentropyshard.crlauncher.network.progress.ProgressNetworkInterceptor;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ListUtils;
import me.theentropyshard.crlauncher.utils.json.Json;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ItchDownloader {
    public ItchDownloader() {

    }

    public void downloadVersion(ItchVersion version, ProgressListener listener) throws IOException {
        CRLauncher launcher = CRLauncher.getInstance();
        ItchIoAccount itchAccount = (ItchIoAccount) launcher.getAccountManager().getCurrentAccount();
        VersionManager versionManager = launcher.getVersionManager();
        Path filePath = versionManager.getVersionJar(version);
        Path zipPath = filePath.getParent().resolve(version.getId() + ".zip");

        Path versionJson = versionManager.getItchVersionJson(version.getId());

        if (!Files.exists(versionJson)) {
            boolean pretty = launcher.getSettings().writePrettyJson;
            FileUtils.writeUtf8(versionJson, pretty ? Json.writePretty(version) : Json.write(version));
        }

        if (!Files.exists(filePath)) {
            OkHttpClient httpClient = launcher.getHttpClient().newBuilder()
                .addNetworkInterceptor(new ProgressNetworkInterceptor(listener))
                .build();

            List<BuildFile> files = version.getFiles();
            BuildFile buildFile = ListUtils.search(files, f -> f.getType().equals("archive"));
            if (buildFile == null) {
                throw new IOException("Could not find archive in Itch build file");
            }
            HttpDownload download = new HttpDownload.Builder()
                .url("https://api.itch.io/builds/" + version.getBuildId() + "/download/archive/default?api_key=" +
                    itchAccount.getItchIoApiKey())
                .expectedSize(buildFile.getSize())
                .httpClient(httpClient)
                .saveAs(zipPath)
                .build();

            download.execute();

            FileHeader jarFileHeader;
            try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
                List<FileHeader> fileHeaders = zipFile.getFileHeaders();
                jarFileHeader = ListUtils.search(fileHeaders, header -> {
                        String fileName = header.getFileName().toLowerCase();
                        return fileName.contains("cosmic") && fileName.contains("reach") && fileName.endsWith(".jar");
                    }
                );
                if (jarFileHeader == null) {
                    throw new IOException("Could not find jar in " + zipPath);
                }
                zipFile.extractFile(jarFileHeader, filePath.getParent().toString());
            }
            FileUtils.delete(zipPath);
            String fileName = jarFileHeader.getFileName();
            FileUtils.renameFile(filePath.getParent().resolve(fileName), version.getId() + ".jar");
        }
    }
}
