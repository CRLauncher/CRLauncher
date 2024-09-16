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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.puzzle;

import me.theentropyshard.crlauncher.github.GithubApi;
import me.theentropyshard.crlauncher.github.GithubRelease;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PuzzleVersionsLoaderWorker extends Worker<List<GithubRelease>, Void> {
    private final JComboBox<GithubRelease> versionsCombo;
    private final Instance instance;

    public PuzzleVersionsLoaderWorker(JComboBox<GithubRelease> versionsCombo, Instance instance) {
        super("loading Puzzle versions");

        this.versionsCombo = versionsCombo;
        this.instance = instance;
    }

    @Override
    protected List<GithubRelease> work() throws Exception {
        return new GithubApi().getAllReleases("PuzzleLoader", "PuzzleLoader");
    }

    @Override
    protected void done() {
        List<GithubRelease> releases;
        try {
            releases = this.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        for (GithubRelease release : releases) {
            this.versionsCombo.addItem(release);
        }

        for (int i = 0; i < this.versionsCombo.getItemCount(); i++) {
            GithubRelease release = this.versionsCombo.getItemAt(i);
            if (release.tag_name.equals(this.instance.getPuzzleVersion())) {
                this.versionsCombo.setSelectedIndex(i);
                break;
            }
        }
    }
}
