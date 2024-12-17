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

package me.theentropyshard.crlauncher.itch;

import com.google.gson.annotations.SerializedName;

public class ShortBuild {
    @SerializedName("id")
    private int buildId;

    @SerializedName("parent_build_id")
    private int parentBuildId;

    private int version;

    @SerializedName("user_version")
    private String userVersion;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public ShortBuild() {

    }

    public int getBuildId() {
        return this.buildId;
    }

    public void setBuildId(int buildId) {
        this.buildId = buildId;
    }

    public int getParentBuildId() {
        return this.parentBuildId;
    }

    public void setParentBuildId(int parentBuildId) {
        this.parentBuildId = parentBuildId;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getUserVersion() {
        return this.userVersion;
    }

    public void setUserVersion(String userVersion) {
        this.userVersion = userVersion;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
