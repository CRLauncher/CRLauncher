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

package me.theentropyshard.crlauncher.crmm.model.mod;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchModsResponse {
    private int estimatedTotalHits;

    @SerializedName("hits")
    private List<Mod> mods;

    private int limit;
    private int offset;
    private long processingTimeMs;
    private String query;

    public SearchModsResponse() {

    }

    @Override
    public String toString() {
        return "SearchModsResponse{" +
            "estimatedTotalHits=" + this.estimatedTotalHits +
            ", mods=" + this.mods +
            ", limit=" + this.limit +
            ", offset=" + this.offset +
            ", processingTimeMs=" + this.processingTimeMs +
            ", query='" + this.query + '\'' +
            '}';
    }

    public int getEstimatedTotalHits() {
        return this.estimatedTotalHits;
    }

    public List<Mod> getMods() {
        return this.mods;
    }

    public int getLimit() {
        return this.limit;
    }

    public int getOffset() {
        return this.offset;
    }

    public long getProcessingTimeMs() {
        return this.processingTimeMs;
    }

    public String getQuery() {
        return this.query;
    }
}
