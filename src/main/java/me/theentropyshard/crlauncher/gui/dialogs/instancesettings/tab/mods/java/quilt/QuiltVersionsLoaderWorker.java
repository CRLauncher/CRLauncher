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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.java.quilt;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.github.GithubRelease;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ToggleableItemListener;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.network.HttpRequest;
import me.theentropyshard.crlauncher.utils.json.Json;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class QuiltVersionsLoaderWorker extends Worker<List<GithubRelease>, Void> {
    private final JComboBox<GithubRelease> versionsCombo;
    private final ToggleableItemListener listener;
    private final Instance instance;

    public QuiltVersionsLoaderWorker(JComboBox<GithubRelease> versionsCombo, ToggleableItemListener listener, Instance instance) {
        super("loading Cosmic Quilt versions");

        this.versionsCombo = versionsCombo;
        this.listener = listener;
        this.instance = instance;
    }

    @Override
    protected List<GithubRelease> work() throws Exception {
        try (HttpRequest request = new HttpRequest(CRLauncher.getInstance().getHttpClient())) {
            String string = request.asString("https://codeberg.org/api/v1/repos/CRModders/cosmic-quilt/releases");
            List<GithubRelease> versionArray = new ArrayList<>(List.of(Json.parse(string, GithubRelease[].class)));

            if (this.instance.getQuiltVersion() == null)
                this.instance.setQuiltVersion(versionArray.getFirst().tag_name);

            return versionArray;
        }
    }

    @Override
    protected void done() {
        List<GithubRelease> releases;
        try {
            releases = this.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        this.listener.setActive(false);

        for (GithubRelease release : releases) {
            this.versionsCombo.addItem(release);
        }

        this.listener.setActive(true);

        for (int i = 0; i < this.versionsCombo.getItemCount(); i++) {
            GithubRelease release = this.versionsCombo.getItemAt(i);

            if (release.tag_name.equals(this.instance.getQuiltVersion())) {
                this.versionsCombo.setSelectedIndex(i);

                break;
            }
        }
    }
}
