package me.theentropyshard.crlauncher.cosmic.mods.puzzle;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.maven.QuiltMavenArtifact;
import me.theentropyshard.crlauncher.github.GithubApi;
import me.theentropyshard.crlauncher.github.GithubRelease;
import me.theentropyshard.crlauncher.network.download.DownloadList;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.network.progress.ProgressListener;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ListUtils;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PuzzleManager {

    private final Path versionsDir;
    private final Path depsDir;
    public final String PUZZLE_COSMIC_COMMON_FILE_NAME_FORMAT = "puzzle-loader-cosmic-%s-common.jar";
    public final String PUZZLE_COSMIC_CLIENT_FILE_NAME_FORMAT = "puzzle-loader-cosmic-%s-client.jar";
    public final String PUZZLE_CORE_COMMON_FILE_NAME_FORMAT = "puzzle-loader-core-%s-common.jar";
    public final String PUZZLE_CORE_CLIENT_FILE_NAME_FORMAT = "puzzle-loader-core-%s-client.jar";

    static final String VERSION_MANIFEST_CORE_LOC = "https://raw.githubusercontent.com/PuzzlesHQ/puzzle-loader-core/refs/heads/versioning/versions.json";
    static final String VERSION_MANIFEST_COSMIC_LOC = "https://raw.githubusercontent.com/PuzzlesHQ/puzzle-loader-cosmic/refs/heads/versioning/versions.json";
    public PuzzleManager(Path workDir) {
        this.versionsDir = workDir.resolve("versions");
        this.depsDir = workDir.resolve("deps");
    }

    public void downloadPuzzle(String version, String version2, ProgressListener progressListener) throws IOException {
        FileUtils.createDirectoryIfNotExists(this.versionsDir);
        FileUtils.createDirectoryIfNotExists(this.depsDir);

        if (this.isInstalled(version,version2)) {
            return;
        }

        GithubApi downloader = new GithubApi();
        List<GithubRelease> allReleases = downloader.getAllReleases("PuzzlesHQ", "puzzle-loader-core");
        GithubRelease release = ListUtils.search(allReleases, r -> r.tag_name.equals(version));
        if (release == null) {
            throw new IOException("Puzzle Core " + version + " not found");
        }

        String fileName = "puzzle-loader-core-" + version;
        String fileName2 = "puzzle-loader-cosmic-" + version2;
        Path filePath2;

        DownloadList list = new DownloadList((totalSize, downloadedBytes) -> {
            progressListener.update(totalSize, downloadedBytes, 0, false);
        });
        Path filePath = this.versionsDir.resolve(fileName + "-client.jar");
        {
            HttpDownload loaderDownload = new HttpDownload.Builder()
                    .httpClient(CRLauncher.getInstance().getHttpClient())
                    .saveAs(filePath)
                    .url("https://repo1.maven.org/maven2/dev/puzzleshq/puzzle-loader-core/" + version + "/" + fileName + "-client.jar")
                    // todo add sha 1 to download and verify
                    .build();
            filePath = this.versionsDir.resolve(fileName + "-common.jar");
            list.add(loaderDownload);

            HttpDownload loaderDownload2 = new HttpDownload.Builder()
                    .httpClient(CRLauncher.getInstance().getHttpClient())
                    .saveAs(filePath)
                    .url("https://repo1.maven.org/maven2/dev/puzzleshq/puzzle-loader-core/" + version + "/" + fileName + "-common.jar")
                    // todo add sha 1 to download and verify
                    .build();
            list.add(loaderDownload2);
        }
        {
            filePath2 = this.versionsDir.resolve(fileName2 + "-common.jar");
            HttpDownload cosmicDownload = new HttpDownload.Builder()
                    .httpClient(CRLauncher.getInstance().getHttpClient())
                    .saveAs(filePath2)
                    .url("https://repo1.maven.org/maven2/dev/puzzleshq/puzzle-loader-cosmic/" + version2 + "/" + fileName2 + "-common.jar")
                    // todo add sha 1 to download and verify
                    .build();
            list.add(cosmicDownload);

            filePath2 = this.versionsDir.resolve(fileName2 + "-client.jar");
            cosmicDownload = new HttpDownload.Builder()
                    .httpClient(CRLauncher.getInstance().getHttpClient())
                    .saveAs(filePath2)
                    .url("https://repo1.maven.org/maven2/dev/puzzleshq/puzzle-loader-cosmic/" + version2 + "/" + fileName2 + "-client.jar")
                    // todo add sha 1 to download and verify
                    .build();
            list.add(cosmicDownload);
        }
        for (PuzzleUnknownRepoDependency dependency : getLibraries(version,version2)) {
            QuiltMavenArtifact artifact = dependency.mavenArtifact();

            for (String repo : dependency.baseReposURL()) {
                if(!repo.endsWith("/"))
                    repo = repo +"/";
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
        deps.add(this.versionsDir.resolve(PUZZLE_CORE_COMMON_FILE_NAME_FORMAT.formatted(core)));
        deps.add(this.versionsDir.resolve(PUZZLE_CORE_CLIENT_FILE_NAME_FORMAT.formatted(core)));
        deps.add(this.versionsDir.resolve(PUZZLE_COSMIC_COMMON_FILE_NAME_FORMAT.formatted(cosmic)));
        deps.add(this.versionsDir.resolve(PUZZLE_COSMIC_CLIENT_FILE_NAME_FORMAT.formatted(cosmic)));

        return String.join(File.pathSeparator, deps.stream().map(Path::toString).toList());
    }

    public boolean isInstalled(String core,String cosmic) {

        if(!Files.exists(this.versionsDir.resolve(PUZZLE_CORE_COMMON_FILE_NAME_FORMAT.formatted(core)))||
                !Files.exists(this.versionsDir.resolve(PUZZLE_CORE_CLIENT_FILE_NAME_FORMAT.formatted(core))) ||
                !Files.exists(this.versionsDir.resolve(PUZZLE_COSMIC_COMMON_FILE_NAME_FORMAT.formatted(cosmic))) ||
                !Files.exists(this.versionsDir.resolve(PUZZLE_COSMIC_CLIENT_FILE_NAME_FORMAT.formatted(cosmic))))
         {
            return false;
        }

        for (PuzzleUnknownRepoDependency dependency : getLibraries(core,cosmic)) {
            QuiltMavenArtifact artifact = dependency.mavenArtifact();

            if (!Files.exists(this.depsDir.resolve(artifact.jar()))) {
                return false;
            }
        }

        return true;
    }

    static private List<PuzzleUnknownRepoDependency> pullDeps(String version, URL url) {
        List<PuzzleUnknownRepoDependency> deps = new ArrayList<>();
        List<String> repos = new ArrayList<>();
        try {
            InputStream stream = url.openStream();
            String jsonInfo = new String(stream.readAllBytes());
            stream.close();

            JsonObject obj = JsonValue.readHjson(jsonInfo).asObject();
            JsonObject versionsList = obj.get("versions").asObject();
            JsonObject versionInfo = versionsList.get(version).asObject();
            if (versionInfo == null) {
                versionInfo = versionsList.get(obj.get("latest").asObject().get("*").asString()).asObject();
//                assertTrue(ObjectUtils.allNotNull(versionInfo));
            }
            URL depsUrl = new URL(versionInfo.get("dependencies").asString());

            stream = depsUrl.openStream();
            String jsonDepsInfoString = new String(stream.readAllBytes());
            stream.close();

            JsonObject depsobj = JsonValue.readHjson(jsonDepsInfoString).asObject();
            if(depsobj.get("repos") != null) {
                JsonArray repoList = depsobj.get("repos").asArray();
                for (JsonValue jsonValue : repoList){

                    JsonObject repoObj = jsonValue.asObject();

                    repos.add(repoObj.get("url").asString());
                }

            }
            if (depsobj.get("common") != null) {
                JsonArray commonDepsList = depsobj.get("common").asArray();
                for (JsonValue jsonValue : commonDepsList) {
                    JsonObject depobj = jsonValue.asObject();
                    if (Objects.equals(depobj.get("type").asString(), "implementation")) {

                        deps.add(new PuzzleUnknownRepoDependency(new ArrayList<>(repos),new QuiltMavenArtifact(
                                depobj.get("groupId").asString(),
                                depobj.get("artifactId").asString(),
                                depobj.get("version").asString()
                        )));
                    }
                }
            }

            if (depsobj.get("client") != null) {
                JsonArray commonDepsList = depsobj.get("client").asArray();
                for (JsonValue jsonValue : commonDepsList) {
                    JsonObject depobj = jsonValue.asObject();
                    if (Objects.equals(depobj.get("type").asString(), "implementation")) {
//                        String dep = String.format("%s:%s:%s",
//                                depobj.get("groupId").asString(),
//                                depobj.get("artifactId").asString(),
//                                depobj.get("version").asString()
//                        );
                        deps.add(new PuzzleUnknownRepoDependency(new ArrayList<>(repos),new QuiltMavenArtifact(
                                depobj.get("groupId").asString(),
                                depobj.get("artifactId").asString(),
                                depobj.get("version").asString()
                        )));
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return deps;
    }

    public static List<PuzzleUnknownRepoDependency> getLibraries(String core,String cosmic) {

        List<PuzzleUnknownRepoDependency> dependencies = new ArrayList<>();

        try {
            dependencies.addAll(pullDeps(core,new URL(VERSION_MANIFEST_CORE_LOC)));
            dependencies.addAll(pullDeps(cosmic,new URL(VERSION_MANIFEST_COSMIC_LOC)));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return dependencies;
    }


    public static String getMainClass(String version) {

        return PuzzleProperties.MAIN_CLASS_REWRITE;
    }
}
