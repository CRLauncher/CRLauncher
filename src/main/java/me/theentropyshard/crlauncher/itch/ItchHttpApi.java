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
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ItchHttpApi {
    @GET("uploads/{id}/builds")
    BuildsResponse getBuilds(@Path("id") int uploadId, @Query("api_key") String apiKey);

    @GET("builds/{id}")
    BuildResponse getBuild(@Path("id") int buildId, @Query("api_key") String apiKey);
}
