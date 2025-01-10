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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.java.fabric;

import me.theentropyshard.crlauncher.github.GithubApi;
import me.theentropyshard.crlauncher.github.GithubRelease;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ToggleableItemListener;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.CosmicInstance;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FabricVersionsLoaderWorker extends Worker<List<GithubRelease>, Void> {
    private final JComboBox<GithubRelease> versionsCombo;
    private final ToggleableItemListener listener;
    private final CosmicInstance instance;

    public FabricVersionsLoaderWorker(JComboBox<GithubRelease> versionsCombo, ToggleableItemListener listener, CosmicInstance instance) {
        super("loading Fabric versions");

        this.versionsCombo = versionsCombo;
        this.listener = listener;
        this.instance = instance;
    }

    @Override
    protected List<GithubRelease> work() throws Exception {
        List<GithubRelease> versionArray = new GithubApi().getAllReleases("ForwarD-Nern", "CosmicReach-Mod-Loader");

        if (this.instance.getFabricVersion() == null) {
            this.instance.setFabricVersion(versionArray.get(0).tag_name);
        }

        return versionArray;
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
            if (release.tag_name.equals(this.instance.getFabricVersion())) {
                this.versionsCombo.setSelectedIndex(i);
                break;
            }
        }
    }
}
