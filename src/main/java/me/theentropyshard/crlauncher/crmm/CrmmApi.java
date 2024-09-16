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

package me.theentropyshard.crlauncher.crmm;

import me.theentropyshard.crlauncher.crmm.model.SearchModsResponse;
import me.theentropyshard.crlauncher.utils.CallUnwrapAdapter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CrmmApi {
    public static final String BASE_URL = "https://api.crmm.tech/api/";

    private final Retrofit retrofit;
    private final CrmmHttpApi crmmApi;

    public CrmmApi(OkHttpClient httpClient) {
        this.retrofit = new Retrofit.Builder()
            .baseUrl(CrmmApi.BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(new CallUnwrapAdapter.Factory())
            .build();

        this.crmmApi = this.retrofit.create(CrmmHttpApi.class);
    }

    public SearchModsResponse searchMods(String query) {
        if (query.trim().isEmpty()) {
            return this.crmmApi.searchMods();
        } else {
            return this.crmmApi.searchMods(query);
        }
    }
}
