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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.quilt;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.github.GithubReleaseResponse;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.network.HttpRequest;
import me.theentropyshard.crlauncher.utils.json.Json;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class QuiltVersionsLoaderWorker extends SwingWorker<List<GithubReleaseResponse>, Void> {
    private final JComboBox<GithubReleaseResponse> versionsCombo;

    public QuiltVersionsLoaderWorker(JComboBox<GithubReleaseResponse> versionsCombo, Instance instance) {
        this.versionsCombo = versionsCombo;
    }

    @Override
    protected List<GithubReleaseResponse> doInBackground() throws Exception {
        try (HttpRequest request = new HttpRequest(CRLauncher.getInstance().getHttpClient())) {
            String string = request.asString("https://codeberg.org/api/v1/repos/CRModders/cosmic-quilt/releases");

            return new ArrayList<>(List.of(Json.parse(string, GithubReleaseResponse[].class)));
        }
    }

    @Override
    protected void done() {
        List<GithubReleaseResponse> releases;
        try {
            releases = this.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        for (GithubReleaseResponse release : releases) {
            this.versionsCombo.addItem(release);
        }
    }
}
