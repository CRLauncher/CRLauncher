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

package me.theentropyshard.crlauncher.crm;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Modpack {
    private String name;
    private String description;
    private String version;

    @SerializedName("spec_revision")
    private int specRevision;

    @SerializedName("base_url")
    private String baseUrl;

    @SerializedName("game_version")
    private String gameVersion;

    private List<Mod> mods;

    public Modpack() {

    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getSpecRevision() {
        return this.specRevision;
    }

    public void setSpecRevision(int specRevision) {
        this.specRevision = specRevision;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getGameVersion() {
        return this.gameVersion;
    }

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public List<Mod> getMods() {
        return this.mods;
    }

    public void setMods(List<Mod> mods) {
        this.mods = mods;
    }

    public static final class Mod {
        private String name;
        private String id;
        private String url;

        @SerializedName("sha512_hash")
        private String sha512;

        private String version;

        @SerializedName("mod_type")
        private String modType;

        public Mod() {

        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return this.id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUrl() {
            return this.url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getSha512() {
            return this.sha512;
        }

        public void setSha512(String sha512) {
            this.sha512 = sha512;
        }

        public String getVersion() {
            return this.version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getModType() {
            return this.modType;
        }

        public void setModType(String modType) {
            this.modType = modType;
        }
    }
}
