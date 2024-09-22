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

package me.theentropyshard.crlauncher.crmm;

import java.util.List;

public class ModInfo {
    private String iconUrl;
    private String name;
    private String description;
    private String author;
    private String datePublished;
    private String dateUpdated;
    private String downloads;
    private String followers;
    private List<String> featuredCategories;
    private List<String> loaders;
    private String slug;

    public ModInfo(String iconUrl, String name, String description, String author, String datePublished, String dateUpdated, String downloads, String followers, List<String> featuredCategories, List<String> loaders, String slug) {
        this.iconUrl = iconUrl;
        this.name = name;
        this.description = description;
        this.author = author;
        this.datePublished = datePublished;
        this.dateUpdated = dateUpdated;
        this.downloads = downloads;
        this.followers = followers;
        this.featuredCategories = featuredCategories;
        this.loaders = loaders;
        this.slug = slug;
    }

    public String getIconUrl() {
        return this.iconUrl;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getDatePublished() {
        return this.datePublished;
    }

    public String getDateUpdated() {
        return this.dateUpdated;
    }

    public String getDownloads() {
        return this.downloads;
    }

    public String getFollowers() {
        return this.followers;
    }

    public List<String> getFeaturedCategories() {
        return this.featuredCategories;
    }

    public List<String> getLoaders() {
        return this.loaders;
    }

    public String getSlug() {
        return this.slug;
    }
}
