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

package me.theentropyshard.crlauncher.crmm.model.project;

import java.util.List;

public class Project {
    private String id;
    private String teamId;
    private String orgId;
    private String name;
    private String icon;
    private String status;
    private String summary;
    private String description;
    private List<String> type;
    private List<String> categories;
    private List<String> featuredCategories;
    private String licenseId;
    private String licenseName;
    private String licenseUrl;
    private String dateUpdated;
    private String datePublished;
    private int downloads;
    private int followers;
    private String slug;
    private String visibility;
    private String issueTrackerUrl;
    private String projectSourceUrl;
    private String projectWikiUrl;
    private String discordInviteUrl;
    private String clientSide;
    private String serverSide;
    private List<String> loaders;
    private List<String> gameVersions;
    private List<GalleryImage> gallery;
    private List<Member> members;
    private Object organisation;

    public Project() {

    }

    public String getId() {
        return this.id;
    }

    public String getTeamId() {
        return this.teamId;
    }

    public String getOrgId() {
        return this.orgId;
    }

    public String getName() {
        return this.name;
    }

    public String getIcon() {
        return this.icon;
    }

    public String getStatus() {
        return this.status;
    }

    public String getSummary() {
        return this.summary;
    }

    public String getDescription() {
        return this.description;
    }

    public List<String> getType() {
        return this.type;
    }

    public List<String> getCategories() {
        return this.categories;
    }

    public List<String> getFeaturedCategories() {
        return this.featuredCategories;
    }

    public String getLicenseId() {
        return this.licenseId;
    }

    public String getLicenseName() {
        return this.licenseName;
    }

    public String getLicenseUrl() {
        return this.licenseUrl;
    }

    public String getDateUpdated() {
        return this.dateUpdated;
    }

    public String getDatePublished() {
        return this.datePublished;
    }

    public int getDownloads() {
        return this.downloads;
    }

    public int getFollowers() {
        return this.followers;
    }

    public String getSlug() {
        return this.slug;
    }

    public String getVisibility() {
        return this.visibility;
    }

    public String getIssueTrackerUrl() {
        return this.issueTrackerUrl;
    }

    public String getProjectSourceUrl() {
        return this.projectSourceUrl;
    }

    public String getProjectWikiUrl() {
        return this.projectWikiUrl;
    }

    public String getDiscordInviteUrl() {
        return this.discordInviteUrl;
    }

    public String getClientSide() {
        return this.clientSide;
    }

    public String getServerSide() {
        return this.serverSide;
    }

    public List<String> getLoaders() {
        return this.loaders;
    }

    public List<String> getGameVersions() {
        return this.gameVersions;
    }

    public List<GalleryImage> getGallery() {
        return this.gallery;
    }

    public List<Member> getMembers() {
        return this.members;
    }

    public Object getOrganisation() {
        return this.organisation;
    }
}