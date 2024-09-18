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
