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

import com.google.gson.annotations.SerializedName;

public class ProjectFile {
    private String id;

    @SerializedName("isPrimary")
    private boolean primary;

    private String name;
    private long size;
    private String type;
    private String url;

    @SerializedName("sha1_hash")
    private String sha1;

    @SerializedName("sha512_hash")
    private String sha512;

    public ProjectFile() {

    }

    @Override
    public String toString() {
        return "ProjectFile{" +
            "id='" + this.id + '\'' +
            ", primary=" + this.primary +
            ", name='" + this.name + '\'' +
            ", size=" + this.size +
            ", type='" + this.type + '\'' +
            ", url='" + this.url + '\'' +
            ", sha1='" + this.sha1 + '\'' +
            ", sha512='" + this.sha512 + '\'' +
            '}';
    }

    public String getId() {
        return this.id;
    }

    public boolean isPrimary() {
        return this.primary;
    }

    public String getName() {
        return this.name;
    }

    public long getSize() {
        return this.size;
    }

    public String getType() {
        return this.type;
    }

    public String getUrl() {
        return this.url;
    }

    public String getSha1() {
        return this.sha1;
    }

    public String getSha512() {
        return this.sha512;
    }
}