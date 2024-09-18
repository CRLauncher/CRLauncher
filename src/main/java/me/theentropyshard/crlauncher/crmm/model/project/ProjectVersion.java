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

package me.theentropyshard.crlauncher.crmm.model.project;

import java.util.List;

public class ProjectVersion {
    private String id;
    private String title;
    private String versionNumber;
    private String slug;
    private String datePublished;
    private boolean featured;
    private int downloads;
    private String changelog;
    private String releaseChannel;
    private List<String> gameVersions;
    private List<String> loaders;
    private ProjectFile primaryFile;
    private List<ProjectFile> files;
    private Author author;
    private List<Object> dependencies;

    public ProjectVersion() {

    }

    public String getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getVersionNumber() {
        return this.versionNumber;
    }

    public String getSlug() {
        return this.slug;
    }

    public String getDatePublished() {
        return this.datePublished;
    }

    public boolean isFeatured() {
        return this.featured;
    }

    public int getDownloads() {
        return this.downloads;
    }

    public String getChangelog() {
        return this.changelog;
    }

    public String getReleaseChannel() {
        return this.releaseChannel;
    }

    public List<String> getGameVersions() {
        return this.gameVersions;
    }

    public List<String> getLoaders() {
        return this.loaders;
    }

    public ProjectFile getPrimaryFile() {
        return this.primaryFile;
    }

    public List<ProjectFile> getFiles() {
        return this.files;
    }

    public Author getAuthor() {
        return this.author;
    }

    public List<Object> getDependencies() {
        return this.dependencies;
    }
}