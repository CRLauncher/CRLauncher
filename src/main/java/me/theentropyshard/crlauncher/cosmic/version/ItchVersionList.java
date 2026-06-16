/*
 * CRLauncher - https://github.com/CRLauncher/CRLauncher
 * Copyright (C) 2024-2026 CRLauncher
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

import java.io.IOException;
import java.util.List;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.account.ItchIoAccount;
import me.theentropyshard.crlauncher.cosmic.itch.ItchVersion;
import me.theentropyshard.crlauncher.itch.DetailedBuild;
import me.theentropyshard.crlauncher.itch.ShortBuild;

public class ItchVersionList extends VersionList {
    private static final int COSMIC_REACH_UPLOAD_ID = 9891067;

    private Version latestVersion;

    @Override
    public void load() throws IOException {
        List<ShortBuild> builds = this.getBuilds();

        if (builds.isEmpty()) {
            return;
        }

        for (ShortBuild build : builds) {
            this.addVersion(new ItchVersion(build));
        }

        this.latestVersion = this.getVersions().get(0);
    }

    @Override
    public Version getVersionById(String id) {
        ItchVersion version = (ItchVersion) super.getVersionById(id);

        if (version == null) {
            return null;
        }

        if (version.getFiles() == null) {
            version.setFiles(this.getDetailedBuild(version.getBuildId()).getFiles());
        }

        return version;
    }

    @Override
    public Version getLatestVersion() {
        return this.getVersionById(this.latestVersion.getId());
    }

    private List<ShortBuild> getBuilds() {
        CRLauncher launcher = CRLauncher.getInstance();
        ItchIoAccount account = (ItchIoAccount) launcher.getAccountManager().getCurrentAccount();

        return launcher.getItchIoApi().getBuilds(ItchVersionList.COSMIC_REACH_UPLOAD_ID, account.getItchIoApiKey());
    }

    private DetailedBuild getDetailedBuild(int buildId) {
        CRLauncher launcher = CRLauncher.getInstance();
        ItchIoAccount account = (ItchIoAccount) launcher.getAccountManager().getCurrentAccount();

        return launcher.getItchIoApi().getBuild(buildId, account.getItchIoApiKey());
    }
}
