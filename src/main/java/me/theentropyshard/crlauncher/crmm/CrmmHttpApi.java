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

import me.theentropyshard.crlauncher.crmm.model.datapack.SearchDatapacksResponse;
import me.theentropyshard.crlauncher.crmm.model.mod.SearchModsResponse;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectResponse;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectVersionResponse;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CrmmHttpApi {
    @GET("search?type=mod")
    SearchModsResponse searchMods();

    @GET("search?type=mod")
    SearchModsResponse searchMods(@Query("q") String query);

    @GET("search?type=datamod")
    SearchDatapacksResponse searchDatapacks();

    @GET("search?type=datamod")
    SearchDatapacksResponse searchDatapacks(@Query("q") String query);

    @GET("project/{slug}")
    ProjectResponse getProject(@Path("slug") String slug);

    @GET("project/{slug}/version")
    ProjectVersionResponse getProjectVersions(@Path("slug") String slug);
}
