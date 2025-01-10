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

package me.theentropyshard.crlauncher.itch;

import me.theentropyshard.crlauncher.itch.model.BuildResponse;
import me.theentropyshard.crlauncher.itch.model.BuildsResponse;
import me.theentropyshard.crlauncher.utils.CallUnwrapAdapter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;
import java.util.Objects;

public class ItchIoApi {
    public static final String BASE_URL = "https://api.itch.io/";

    private final Retrofit retrofit;
    private final ItchHttpApi itchApi;

    public ItchIoApi(OkHttpClient httpClient) {
        this.retrofit = new Retrofit.Builder()
            .baseUrl(ItchIoApi.BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(new CallUnwrapAdapter.Factory())
            .build();

        this.itchApi = this.retrofit.create(ItchHttpApi.class);
    }

    public List<ShortBuild> getBuilds(int uploadId, String apiKey) {
        BuildsResponse builds = Objects.requireNonNull(this.itchApi.getBuilds(uploadId, apiKey));

        return builds.getBuilds();
    }

    public DetailedBuild getBuild(int buildId, String apiKey) {
        BuildResponse buildResponse = Objects.requireNonNull(this.itchApi.getBuild(buildId, apiKey));

        return buildResponse.getBuild();
    }
}
