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

package me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt;

import com.google.gson.annotations.SerializedName;
import me.theentropyshard.crlauncher.cosmic.mods.Mod;

import java.util.List;
import java.util.Map;

public class QuiltMod {
    @SerializedName("quilt_loader")
    private QuiltLoader quiltLoader;

    public Mod toMod() {
        return new Mod(this.getId(), this.getName(), this.getVersion(), this.getDescription());
    }

    public String getName() {
        return this.quiltLoader.metadata.name;
    }

    public String getVersion() {
        return this.quiltLoader.version;
    }

    public String getDescription() {
        return this.quiltLoader.metadata.description;
    }

    public String getId() {
        return this.quiltLoader.id;
    }

    public static final class QuiltLoader {
        private String id;
        private String version;
        private Metadata metadata;
    }

    private static class Metadata {
        private String name;
        private String description;
    }
}