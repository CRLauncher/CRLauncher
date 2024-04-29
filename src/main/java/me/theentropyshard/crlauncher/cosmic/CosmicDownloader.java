package me.theentropyshard.crlauncher.cosmic;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.version.Version;
import me.theentropyshard.crlauncher.cosmic.version.VersionManager;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.network.progress.ProgressListener;
import me.theentropyshard.crlauncher.network.progress.ProgressNetworkInterceptor;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.HashUtils;
import me.theentropyshard.crlauncher.utils.json.Json;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CosmicDownloader {
    public CosmicDownloader() {

    }

    public void downloadVersion(Version version, ProgressListener listener) throws IOException {
        VersionManager versionManager = CRLauncher.getInstance().getVersionManager();
        Path filePath = versionManager.getVersionPath(version);

        Path versionJson = filePath.getParent().resolve(version.getId() + ".json");

        if (!Files.exists(versionJson)) {
            FileUtils.writeUtf8(versionJson, Json.write(version));
        }

        if (!Files.exists(filePath) || !HashUtils.sha256(filePath).equals(version.getSha256())) {
            if (Files.exists(filePath)) {
                FileUtils.delete(filePath);
            }

            OkHttpClient httpClient = CRLauncher.getInstance().getHttpClient().newBuilder()
                    .addNetworkInterceptor(new ProgressNetworkInterceptor(listener))
                    .build();

            HttpDownload download = new HttpDownload.Builder()
                    .url(version.getUrl())
                    .expectedSize(version.getSize())
                    .httpClient(httpClient)
                    .saveAs(filePath)
                    .build();

            download.execute();
        }
    }
}
