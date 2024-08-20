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

package me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.maven;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.network.HttpRequest;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MavenDownloader {
    public static final String QUILT_LOADER_DOWNLOAD = "https://jitpack.io/org/codeberg/CRModders/cosmic-quilt/%s/%s";
    public static final String[] MAVEN_REPOSITORIES = {
            "https://maven.quiltmc.org/repository/release/",
            "https://maven.fabricmc.net/",
            "https://repo.spongepowered.org/maven/",
            "https://jitpack.io/",
    };

    public static List<MavenArtifact> downloadRelease(String version, Path saveDir, Path cqPath, List<HttpDownload> downloads) throws IOException {
        String jarUrl = MavenDownloader.QUILT_LOADER_DOWNLOAD.formatted(version, "cosmic-quilt-%s.jar".formatted(version));

        HttpDownload cqDownload = new HttpDownload.Builder()
                .saveAs(cqPath)
                .httpClient(CRLauncher.getInstance().getHttpClient())
                .url(jarUrl)
                .build();

        downloads.add(cqDownload);

        Map<String, MavenArtifact> deps = new HashMap<>();

        try (HttpRequest request = new HttpRequest(CRLauncher.getInstance().getHttpClient())) {
            String pomURL = MavenDownloader.QUILT_LOADER_DOWNLOAD.formatted(version, "cosmic-quilt-%s.pom".formatted(version));
            String pomContent = request.asString(pomURL);

            MavenDownloader.getDependencies(deps, pomContent, null, 0);
            for (MavenArtifact mavenArtifact : deps.values()) {
                MavenDownloader.downloadDependencies(mavenArtifact, MavenDownloader.MAVEN_REPOSITORIES, downloads, saveDir);
            }
        }

        return new ArrayList<>(deps.values());
    }

    private static void getDependencies(Map<String, MavenArtifact> dependencies, String pom, String search, int depth) throws IOException {
        if (pom == null || depth > 9) return;
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(pom)));
            NodeList dependencyList = document.getElementsByTagName("dependency");
            for (int i = 0; i < dependencyList.getLength(); i++) {
                Element dependency = (Element) dependencyList.item(i);
                String groupId = dependency.getElementsByTagName("groupId").item(0).getTextContent();
                String artifactId = dependency.getElementsByTagName("artifactId").item(0).getTextContent();
                if (search != null && !artifactId.equalsIgnoreCase(search)) continue;
                Node optionalNode = dependency.getElementsByTagName("optional").item(0);
                if (optionalNode != null && optionalNode.getTextContent().equalsIgnoreCase("true")) continue;
                if (dependency.getElementsByTagName("version").item(0) != null) {
                    String version = dependency.getElementsByTagName("version").item(0).getTextContent();
                    if (version.equals("${project.version}")) { // replace with parent version
                        NodeList parentList = document.getElementsByTagName("parent");
                        if (parentList != null && parentList.getLength() > 0) {
                            Element parent = (Element) parentList.item(0);
                            version = parent.getElementsByTagName("version").item(0).getTextContent();
                        }
                    }
                    if (version.matches("\\$\\{.*}")) continue;
                    Node scope = dependency.getElementsByTagName("scope").item(0);
                    if ((scope == null || (!scope.getTextContent().equalsIgnoreCase("test") ||
                            artifactId.equals("slf4j-api"))) && (!dependencies.containsKey(artifactId) ||
                            MavenDownloader.compare(dependencies.get(artifactId).version(), version) == 1) &&
                            !artifactId.equalsIgnoreCase("cosmicreach")) {
                        dependencies.put(artifactId, new MavenArtifact(groupId, artifactId, version));
                        for (String repository : MavenDownloader.MAVEN_REPOSITORIES) {
                            try (HttpRequest request = new HttpRequest(CRLauncher.getInstance().getHttpClient())) {
                                String url = repository + groupId.replace('.', '/') +
                                        "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom";
                                String nextPom = request.asString(url);
                                if (nextPom != null && request.code() / 100 == 2) {
                                    getDependencies(dependencies, nextPom, null, depth + 1);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    NodeList parentList = document.getElementsByTagName("parent");
                    if (parentList.getLength() > 0) {
                        Element parent = (Element) parentList.item(0);
                        String parentGroupId = parent.getElementsByTagName("groupId").item(0).getTextContent();
                        String parentArtifactId = parent.getElementsByTagName("artifactId").item(0).getTextContent();
                        String parentVersion = parent.getElementsByTagName("version").item(0).getTextContent();
                        for (String repository : MavenDownloader.MAVEN_REPOSITORIES) {
                            try (HttpRequest request = new HttpRequest(CRLauncher.getInstance().getHttpClient())) {
                                String url = repository +
                                        parentGroupId.replace('.', '/') + "/" + parentArtifactId + "/" +
                                        parentVersion + "/" + parentArtifactId + "-" + parentVersion + ".pom";
                                String parentPom = request.asString(url);
                                if (parentPom != null && request.code() / 100 == 2) {
                                    getDependencies(dependencies, parentPom, artifactId, depth + 1);
                                }
                            }
                        }
                    }
                }
            }

        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new IOException("Failed to parse POM", e);
        }
    }

    private static void downloadDependencies(MavenArtifact mavenArtifact, String[] repositories, List<HttpDownload> downloads, Path saveDir) throws IOException {
        if (mavenArtifact == null) {
            return;
        }

        String groupId = mavenArtifact.groupId();
        String artifactId = mavenArtifact.artifactId();
        String version = mavenArtifact.version();
        for (String repository : repositories) {
            String pomURL = repository + groupId.replace('.', '/') + "/" + artifactId +
                    "/" + version + "/" + mavenArtifact.pom();

            try (HttpRequest request = new HttpRequest(CRLauncher.getInstance().getHttpClient())) {
                request.asString(pomURL);

                if (request.code() / 100 != 2) {
                    continue;
                }

                HttpDownload download = new HttpDownload.Builder()
                        .url(repository + groupId.replace('.', '/') + "/" + artifactId +
                                "/" + version + "/" + mavenArtifact.jar())
                        .saveAs(saveDir.resolve(mavenArtifact.jar()))
                        .httpClient(CRLauncher.getInstance().getHttpClient())
                        .build();

                downloads.add(download);

                break;
            }
        }
    }

    public static int compare(String o1, String o2) {
        // Split version numbers into parts
        String[] v1Parts = o1.split("\\.");
        String[] v2Parts = o2.split("\\.");

        int length = Math.max(v1Parts.length, v2Parts.length);
        for (int i = 0; i < length; i++) {
            int v1Part = i < v1Parts.length && !v1Parts[i].replaceAll("\\D+", "").isEmpty() ? Integer.parseInt(v1Parts[i].replaceAll("\\D+", "")) : 0;
            int v2Part = i < v2Parts.length && !v2Parts[i].replaceAll("\\D+", "").isEmpty() ? Integer.parseInt(v2Parts[i].replaceAll("\\D+", "")) : 0;
            if (v1Part < v2Part)
                return 1;
            if (v1Part > v2Part)
                return -1;
        }

        // If versions are identical, compare trailing non-digit characters
        String v1Suffix = v1Parts[v1Parts.length - 1].replaceAll("\\d+", "");
        String v2Suffix = v2Parts[v2Parts.length - 1].replaceAll("\\d+", "");
        return v2Suffix.compareTo(v1Suffix);
    }
}
