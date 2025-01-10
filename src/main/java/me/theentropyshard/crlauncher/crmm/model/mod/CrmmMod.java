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

package me.theentropyshard.crlauncher.crmm.model.mod;

import me.theentropyshard.crlauncher.crmm.ModInfo;

import java.util.List;

public class CrmmMod {
    private String id;
    private String slug;
    private String name;
    private String summary;
    private List<String> type;
    private String icon;
    private int downloads;
    private int followers;
    private String dateUpdated;
    private String datePublished;
    private List<String> categories;
    private List<String> featuredCategories;
    private List<String> gameVersions;
    private List<String> loaders;
    private String author;

    public CrmmMod() {

    }

    public ModInfo toModInfo() {
        return new ModInfo(
            this.icon, this.name, this.summary, this.author, this.datePublished, this.dateUpdated,
            String.valueOf(this.downloads), String.valueOf(this.followers), this.featuredCategories, this.loaders, this.slug);
    }

    @Override
    public String toString() {
        return "Mod{" +
            "id='" + this.id + '\'' +
            ", slug='" + this.slug + '\'' +
            ", name='" + this.name + '\'' +
            ", summary='" + this.summary + '\'' +
            ", type=" + this.type +
            ", icon='" + this.icon + '\'' +
            ", downloads=" + this.downloads +
            ", followers=" + this.followers +
            ", dateUpdated='" + this.dateUpdated + '\'' +
            ", datePublished='" + this.datePublished + '\'' +
            ", categories=" + this.categories +
            ", featuredCategories=" + this.featuredCategories +
            ", gameVersions=" + this.gameVersions +
            ", loaders=" + this.loaders +
            ", author='" + this.author + '\'' +
            '}';
    }

    public String getId() {
        return this.id;
    }

    public String getSlug() {
        return this.slug;
    }

    public String getName() {
        return this.name;
    }

    public String getSummary() {
        return this.summary;
    }

    public List<String> getType() {
        return this.type;
    }

    public String getIcon() {
        return this.icon;
    }

    public int getDownloads() {
        return this.downloads;
    }

    public int getFollowers() {
        return this.followers;
    }

    public String getDateUpdated() {
        return this.dateUpdated;
    }

    public String getDatePublished() {
        return this.datePublished;
    }

    public List<String> getCategories() {
        return this.categories;
    }

    public List<String> getFeaturedCategories() {
        return this.featuredCategories;
    }

    public List<String> getGameVersions() {
        return this.gameVersions;
    }

    public List<String> getLoaders() {
        return this.loaders;
    }

    public String getAuthor() {
        return this.author;
    }
}