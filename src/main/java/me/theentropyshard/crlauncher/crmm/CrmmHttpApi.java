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

package me.theentropyshard.crlauncher.crmm;

import me.theentropyshard.crlauncher.crmm.model.mod.SearchModsResponse;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectResponse;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectVersion;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectVersionResponse;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectVersionsResponse;
import retrofit2.http.*;

import java.util.Map;

public interface CrmmHttpApi {
    @GET("search")
    SearchModsResponse search(@QueryMap Map<String, String> query);

    @GET("project/{slug}")
    ProjectResponse getProject(@Path("slug") String slug, @Query("includeVersions") boolean includeVersions, @Query("featuredOnly") boolean featuredOnly);

    @GET("project/{slug}/version")
    ProjectVersionsResponse getProjectVersions(@Path("slug") String slug);

    @GET("project/{slug}/version/latest")
    ProjectVersionResponse getLatestVersion(@Path("slug") String slug, @Query("loader") String loader);

    @POST("version-files/update")
    Map<String, ProjectVersion> getLatestVersions(@Body HashesBody body);
}
