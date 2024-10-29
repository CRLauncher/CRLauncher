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
import java.util.Map;

public class Modpack {
    public static final int MINIMUM_SUPPORTED_REVISION = 2;

    @SerializedName("spec_revision")
    private int specRevision;

    private String name;
    private String version;
    private String description;

    /**
     * Possible values: "vanilla", "cosmic-quilt", "puzzle"
     */
    private String loader;
    private List<File> files;
    private Map<String, String> dependencies;

    @SerializedName("base_url")
    private String baseUrl;

    public Modpack() {

    }

    public int getSpecRevision() {
        return this.specRevision;
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public String getDescription() {
        return this.description;
    }

    public String getLoader() {
        return this.loader;
    }

    public List<File> getFiles() {
        return this.files;
    }

    public Map<String, String> getDependencies() {
        return this.dependencies;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public static final class File {
        private String type;
        private String id;
        private String version;

        @SerializedName("sha512_hash")
        private String sha512;

        private Env env;

        @SerializedName("download_url")
        private String downloadUrl;

        public File() {

        }

        public String getType() {
            return this.type;
        }

        public String getId() {
            return this.id;
        }

        public String getVersion() {
            return this.version;
        }

        public String getSha512() {
            return this.sha512;
        }

        public Env getEnv() {
            return this.env;
        }

        public String getDownloadUrl() {
            return this.downloadUrl;
        }
    }

    public static final class Env {
        /**
         * Possible values: "required", "optional"
         */
        private String client;

        /**
         * Possible values: "required", "optional"
         */
        private String server;

        public Env() {

        }

        public String getClient() {
            return this.client;
        }

        public String getServer() {
            return this.server;
        }
    }
}
