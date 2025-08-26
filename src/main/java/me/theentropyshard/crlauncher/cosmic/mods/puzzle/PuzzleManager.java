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

package me.theentropyshard.crlauncher.cosmic.mods.puzzle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.maven.QuiltMavenArtifact;
import me.theentropyshard.crlauncher.github.GithubApi;
import me.theentropyshard.crlauncher.github.GithubRelease;
import me.theentropyshard.crlauncher.network.HttpRequest;
import me.theentropyshard.crlauncher.network.download.DownloadList;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.network.progress.ProgressListener;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ListUtils;
import me.theentropyshard.crlauncher.utils.json.Json;

public class PuzzleManager {
    private static final String PUZZLE_COSMIC_COMMON_FILE_NAME_FORMAT = "puzzle-loader-cosmic-%s-common.jar";
    private static final String PUZZLE_COSMIC_CLIENT_FILE_NAME_FORMAT = "puzzle-loader-cosmic-%s-client.jar";
    private static final String PUZZLE_CORE_COMMON_FILE_NAME_FORMAT = "puzzle-loader-core-%s-common.jar";
    private static final String PUZZLE_CORE_CLIENT_FILE_NAME_FORMAT = "puzzle-loader-core-%s-client.jar";
    private static final String VERSION_MANIFEST_CORE_LOC = "https://raw.githubusercontent.com/PuzzlesHQ/puzzle-loader-core/refs/heads/versioning/versions.json";
    private static final String VERSION_MANIFEST_COSMIC_LOC = "https://raw.githubusercontent.com/PuzzlesHQ/puzzle-loader-cosmic/refs/heads/versioning/versions.json";

    private final Path versionsDir;
    private final Path depsDir;

    public PuzzleManager(Path workDir) {
        this.versionsDir = workDir.resolve("versions");
        this.depsDir = workDir.resolve("deps");
    }

    public void downloadPuzzle(String version, String version2, ProgressListener progressListener) throws IOException {
        FileUtils.createDirectoryIfNotExists(this.versionsDir);
        FileUtils.createDirectoryIfNotExists(this.depsDir);

        if (this.isInstalled(version, version2)) {
            return;
        }

        GithubApi downloader = new GithubApi();
        List<GithubRelease> allReleases = downloader.getAllReleases("PuzzlesHQ", "puzzle-loader-core");
        GithubRelease release = ListUtils.search(allReleases, r -> r.tag_name.equals(version));
        if (release == null) {
            throw new IOException("Puzzle Core " + version + " not found");
        }

        String coreFileName = "puzzle-loader-core-" + version;
        String cosmicFileName = "puzzle-loader-cosmic-" + version2;
        Path filePath2;

        DownloadList list = new DownloadList((totalSize, downloadedBytes) -> {
            progressListener.update(totalSize, downloadedBytes, 0, false);
        });
        Path filePath = this.versionsDir.resolve(coreFileName + "-client.jar");

        HttpDownload loaderDownload = new HttpDownload.Builder()
            .httpClient(CRLauncher.getInstance().getHttpClient())
            .saveAs(filePath)
            .url("https://repo1.maven.org/maven2/dev/puzzleshq/puzzle-loader-core/" + version + "/" + coreFileName + "-client.jar")
            // todo add sha 1 to download and verify
            .build();
        filePath = this.versionsDir.resolve(coreFileName + "-common.jar");
        list.add(loaderDownload);

        HttpDownload loaderDownload2 = new HttpDownload.Builder()
            .httpClient(CRLauncher.getInstance().getHttpClient())
            .saveAs(filePath)
            .url("https://repo1.maven.org/maven2/dev/puzzleshq/puzzle-loader-core/" + version + "/" + coreFileName + "-common.jar")
            // todo add sha 1 to download and verify
            .build();
        list.add(loaderDownload2);

        filePath2 = this.versionsDir.resolve(cosmicFileName + "-common.jar");
        HttpDownload cosmicDownload = new HttpDownload.Builder()
            .httpClient(CRLauncher.getInstance().getHttpClient())
            .saveAs(filePath2)
            .url("https://repo1.maven.org/maven2/dev/puzzleshq/puzzle-loader-cosmic/" + version2 + "/" + cosmicFileName + "-common.jar")
            // todo add sha 1 to download and verify
            .build();
        list.add(cosmicDownload);

        filePath2 = this.versionsDir.resolve(cosmicFileName + "-client.jar");
        cosmicDownload = new HttpDownload.Builder()
            .httpClient(CRLauncher.getInstance().getHttpClient())
            .saveAs(filePath2)
            .url("https://repo1.maven.org/maven2/dev/puzzleshq/puzzle-loader-cosmic/" + version2 + "/" + cosmicFileName + "-client.jar")
            // todo add sha 1 to download and verify
            .build();
        list.add(cosmicDownload);

        for (PuzzleUnknownRepoDependency dependency : PuzzleManager.getLibraries(version, version2)) {
            QuiltMavenArtifact artifact = dependency.mavenArtifact();

            for (String repo : dependency.baseReposURL()) {
                if (!repo.endsWith("/")) {
                    repo = repo + "/";
                }

                HttpDownload libDownload = new HttpDownload.Builder()
                    .httpClient(CRLauncher.getInstance().getHttpClient())
                    .saveAs(this.depsDir.resolve(artifact.jar()))
                    .url(repo + artifact.url())
                    // todo add sha 1 to download and verify
                    .build();

                list.add(libDownload);
            }
        }

        list.downloadAll();
    }

    public String getClasspath(String core, String cosmic) throws IOException {
        List<Path> deps = FileUtils.list(this.depsDir);
        deps.add(this.versionsDir.resolve(PuzzleManager.PUZZLE_CORE_COMMON_FILE_NAME_FORMAT.formatted(core)));
        deps.add(this.versionsDir.resolve(PuzzleManager.PUZZLE_CORE_CLIENT_FILE_NAME_FORMAT.formatted(core)));
        deps.add(this.versionsDir.resolve(PuzzleManager.PUZZLE_COSMIC_COMMON_FILE_NAME_FORMAT.formatted(cosmic)));
        deps.add(this.versionsDir.resolve(PuzzleManager.PUZZLE_COSMIC_CLIENT_FILE_NAME_FORMAT.formatted(cosmic)));

        return String.join(File.pathSeparator, deps.stream().map(Path::toString).toList());
    }

    public boolean isInstalled(String core, String cosmic) {
        if (!Files.exists(this.versionsDir.resolve(PuzzleManager.PUZZLE_CORE_COMMON_FILE_NAME_FORMAT.formatted(core))) ||
            !Files.exists(this.versionsDir.resolve(PuzzleManager.PUZZLE_CORE_CLIENT_FILE_NAME_FORMAT.formatted(core))) ||
            !Files.exists(this.versionsDir.resolve(PuzzleManager.PUZZLE_COSMIC_COMMON_FILE_NAME_FORMAT.formatted(cosmic))) ||
            !Files.exists(this.versionsDir.resolve(PuzzleManager.PUZZLE_COSMIC_CLIENT_FILE_NAME_FORMAT.formatted(cosmic)))) {

            return false;
        }

        for (PuzzleUnknownRepoDependency dependency : PuzzleManager.getLibraries(core, cosmic)) {
            QuiltMavenArtifact artifact = dependency.mavenArtifact();

            if (!Files.exists(this.depsDir.resolve(artifact.jar()))) {
                return false;
            }
        }

        return true;
    }

    static private List<PuzzleUnknownRepoDependency> pullDeps(String version, String url) {
        List<PuzzleUnknownRepoDependency> deps = new ArrayList<>();
        List<String> repos = new ArrayList<>();
        try {
            String jsonInfo;
            try (HttpRequest request = new HttpRequest(CRLauncher.getInstance().getHttpClient())) {
                jsonInfo = request.asString(url);
            }

            JsonObject obj = Json.parse(jsonInfo, JsonObject.class);
            JsonObject versionsList = obj.getAsJsonObject("versions");
            JsonObject versionInfo = versionsList.getAsJsonObject(version);

            if (versionInfo == null) {
                versionInfo = versionsList.getAsJsonObject(obj.getAsJsonObject("latest").get("*").getAsString());
            }

            String jsonDepsInfoString;
            try (HttpRequest request = new HttpRequest(CRLauncher.getInstance().getHttpClient())) {
                jsonDepsInfoString = request.asString(versionInfo.get("dependencies").getAsString());
            }

            JsonObject depsobj = Json.parse(jsonDepsInfoString, JsonObject.class);
            if (depsobj.has("repos")) {
                JsonArray repoList = depsobj.getAsJsonArray("repos");

                for (JsonElement jsonValue : repoList) {
                    JsonObject repoObj = jsonValue.getAsJsonObject();
                    repos.add(repoObj.get("url").getAsString());
                }
            }

            PuzzleManager.collectDeps(depsobj, repos, "common", deps);
            PuzzleManager.collectDeps(depsobj, repos, "client", deps);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return deps;
    }

    private static void collectDeps(JsonObject depsobj, List<String> repos, String classifier, List<PuzzleUnknownRepoDependency> depsList) {
        if (depsobj.has(classifier)) {
            JsonArray commonDepsList = depsobj.getAsJsonArray(classifier);
            for (JsonElement element : commonDepsList) {
                JsonObject depobj = element.getAsJsonObject();
                if ("implementation".equals(depobj.get("type").getAsString())) {
                    depsList.add(new PuzzleUnknownRepoDependency(new ArrayList<>(repos), new QuiltMavenArtifact(
                        depobj.get("groupId").getAsString(),
                        depobj.get("artifactId").getAsString(),
                        depobj.get("version").getAsString()
                    )));
                }
            }
        }
    }

    public static List<PuzzleUnknownRepoDependency> getLibraries(String core, String cosmic) {
        List<PuzzleUnknownRepoDependency> dependencies = new ArrayList<>();
        dependencies.addAll(PuzzleManager.pullDeps(core, PuzzleManager.VERSION_MANIFEST_CORE_LOC));
        dependencies.addAll(PuzzleManager.pullDeps(cosmic, PuzzleManager.VERSION_MANIFEST_COSMIC_LOC));

        return dependencies;
    }
}
