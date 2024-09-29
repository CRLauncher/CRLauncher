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

package me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt;

import com.google.gson.annotations.SerializedName;
import me.theentropyshard.crlauncher.cosmic.mods.IMod;

import java.util.List;
import java.util.Map;

public class QuiltMod implements IMod {
    public String filePath;
    public boolean active;

    @SerializedName("schema_version")
    public int schemaVersion;

    @SerializedName("quilt_loader")
    public QuiltLoader quiltLoader;

    public String mixin;

    @Override
    public String getName() {
        return this.quiltLoader.metadata.name;
    }

    @Override
    public String getVersion() {
        return this.quiltLoader.version;
    }

    @Override
    public String getDescription() {
        return this.quiltLoader.metadata.description;
    }

    @Override
    public String getFilePath() {
        return this.filePath;
    }

    @Override
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public static final class QuiltLoader {
        public String group;
        public String id;
        public String version;
        public String intermediate_mappings;
        public Metadata metadata;
        public Map<String, String> entrypoints;
        public List<Depend> depends;

        public static class Contact {
            public String homepage;
            public String issues;
            public String sources;
            public String wiki;
        }

        public static class Depend {
            public String id;
            public String versions;
        }

        public static class Metadata {
            public String name;
            public String description;
            public Map<String, String> contributors;
            public String license;
            public Contact contact;
            public String icon;
        }
    }
}
