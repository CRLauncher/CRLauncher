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

package me.theentropyshard.crlauncher.cosmic.version;

import com.google.gson.annotations.SerializedName;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.network.HttpRequest;
import me.theentropyshard.crlauncher.utils.ListUtils;
import me.theentropyshard.crlauncher.utils.json.Json;

import java.io.IOException;
import java.util.List;

public class CosmicArchiveVersionList extends VersionList {
    private final String url;

    private Version latestVersion;

    public CosmicArchiveVersionList(String url) {
        this.url = url;
    }

    @Override
    public void load() throws IOException {
        try (HttpRequest request = new HttpRequest(CRLauncher.getInstance().getHttpClient())) {
            RawVersionList versionList = Json.parse(request.asString(this.url), RawVersionList.class);

            this.latestVersion = ListUtils.search(versionList.getVersions(), v -> v.getId().equals(versionList.getLatest().getAlpha()));

            versionList.getVersions().forEach(this::addVersion);
        }
    }

    @Override
    public Version getLatestVersion() {
        return this.latestVersion;
    }

    private static final class RawVersionList {
        private Latest latest;
        private List<CosmicArchiveVersion> versions;

        public RawVersionList() {

        }

        public static final class Latest {
            @SerializedName("alpha")
            private String alpha;

            @SerializedName("pre_alpha")
            private String preAlpha;

            public Latest() {

            }

            public String getAlpha() {
                return this.alpha;
            }

            public String getPreAlpha() {
                return this.preAlpha;
            }
        }

        public Latest getLatest() {
            return this.latest;
        }

        public List<? extends Version> getVersions() {
            return this.versions;
        }
    }
}
