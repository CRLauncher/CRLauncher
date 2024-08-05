package me.theentropyshard.crlauncher.cosmic.account;

import com.google.gson.annotations.SerializedName;

public class ItchProfile {
    public static final int COSMIC_REACH_ITCH_GAME_ID = 2557309;

    private String username;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("cover_url")
    private String coverUrl;

    private String url;

    private long id;

    public ItchProfile() {

    }

    @Override
    public String toString() {
        return "ItchProfile{" +
            "username='" + this.username + '\'' +
            ", displayName='" + this.displayName + '\'' +
            ", coverUrl='" + this.coverUrl + '\'' +
            ", url='" + this.url + '\'' +
            ", id=" + this.id +
            '}';
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCoverUrl() {
        return this.coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /*public void read(Json json, JsonValue jsonData) {
        JsonValue api_key = jsonData.get("api_key");
        JsonValue issuer = api_key.get("issuer");
        if (2557309 != issuer.getInt("game_id"))
            throw new YarHarrFiddleDeeDeeException("Invalid game id!");
        JsonValue user = jsonData.get("user");
        this.username = user.getString("username");
        this.display_name = user.getString("display_name");
        this.cover_url = user.getString("cover_url");
        this.url = user.getString("url");
        this.id = user.getLong("id");
    }*/
}
