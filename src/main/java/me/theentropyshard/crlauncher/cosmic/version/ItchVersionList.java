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

package me.theentropyshard.crlauncher.cosmic.version;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.account.Account;
import me.theentropyshard.crlauncher.cosmic.account.ItchIoAccount;
import me.theentropyshard.crlauncher.cosmic.itch.ItchVersion;
import me.theentropyshard.crlauncher.itch.DetailedBuild;
import me.theentropyshard.crlauncher.itch.ItchIoApi;
import me.theentropyshard.crlauncher.itch.ShortBuild;

import java.io.IOException;
import java.util.List;

public class ItchVersionList extends VersionList {
    private static final int COSMIC_REACH_UPLOAD_ID = 9891067;

    private Version latestVersion;

    @Override
    public void load() throws IOException {
        Account currentAccount = CRLauncher.getInstance().getAccountManager().getCurrentAccount();
        ItchIoApi itchIoApi = CRLauncher.getInstance().getItchIoApi();
        List<ShortBuild> builds = itchIoApi.getBuilds(ItchVersionList.COSMIC_REACH_UPLOAD_ID, ((ItchIoAccount) currentAccount).getItchIoApiKey());
        builds.forEach(build -> {
            ItchVersion version = new ItchVersion();
            version.setBuildId(build.getBuildId());
            version.setVersion(build.getVersion());
            version.setUserVersion(build.getUserVersion());
            version.setCreatedAt(build.getCreatedAt());
            version.setUpdatedAt(build.getUpdatedAt());
            version.setParentBuildId(build.getParentBuildId());
            this.addVersion(version);
        });
        this.latestVersion = this.getVersions().get(0);
    }

    @Override
    public Version getVersionById(String id) {
        Account currentAccount = CRLauncher.getInstance().getAccountManager().getCurrentAccount();
        ItchVersion version = (ItchVersion) super.getVersionById(id);

        if (version == null) {
            return null;
        }

        if (version.getFiles() == null) {
            ItchIoApi itchIoApi = CRLauncher.getInstance().getItchIoApi();
            DetailedBuild build = itchIoApi.getBuild(version.getBuildId(), ((ItchIoAccount) currentAccount).getItchIoApiKey());
            version.setFiles(build.getFiles());
        }

        return version;
    }

    @Override
    public Version getLatestVersion() {
        return this.getVersionById(this.latestVersion.getId());
    }
}
