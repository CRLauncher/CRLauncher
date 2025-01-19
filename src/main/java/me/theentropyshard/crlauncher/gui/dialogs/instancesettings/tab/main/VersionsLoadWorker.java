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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.main;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.cosmic.version.Version;
import me.theentropyshard.crlauncher.cosmic.version.VersionManager;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.ListUtils;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class VersionsLoadWorker extends Worker<List<Version>, Void> {
    private final CosmicInstance instance;
    private final MainTab mainTab;
    private final CompletionListener listener;
    private final boolean forceNetwork;

    public VersionsLoadWorker(CosmicInstance instance, MainTab mainTab) {
        this(instance, mainTab, () -> {}, false);
    }

    public VersionsLoadWorker(CosmicInstance instance, MainTab mainTab, CompletionListener listener, boolean forceNetwork) {
        super("getting remote versions");

        this.instance = instance;
        this.mainTab = mainTab;
        this.listener = listener;
        this.forceNetwork = forceNetwork;
    }

    public interface CompletionListener {
        void onComplete();
    }

    @Override
    protected List<Version> work() throws Exception {
        VersionManager versionManager = CRLauncher.getInstance().getVersionManager();

        if (!versionManager.isLoaded() || this.forceNetwork) {
            versionManager.setMode(VersionManager.Mode.ONLINE);
            versionManager.load();
        }

        return versionManager.getVersions();
    }

    @Override
    protected void done() {
        Language language = CRLauncher.getInstance().getLanguage();

        List<Version> versions;
        try {
            versions = this.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.error("Could not get versions", e);

            MessageBox.showErrorMessage(
                CRLauncher.frame,
                language.getString("messages.gui.instanceSettingsDialog.couldNotLoadVersions") +
                    ": " + e.getMessage()
            );

            return;
        }

        this.mainTab.setVersions(versions);

        String cosmicVersion = this.instance.getCosmicVersion();
        Version currentVersion = ListUtils.search(versions, v -> v.getId().equals(cosmicVersion));

        CRLauncher launcher = CRLauncher.getInstance();
        Settings settings = launcher.getSettings();
        VersionManager versionManager = launcher.getVersionManager();

        for (Version version : versions) {
            if (!versionManager.isInstalled(version) && settings.showOnlyInstalledVersions) {
                continue;
            }

            this.mainTab.getVersionsCombo().addItem(version);
        }

        this.mainTab.setPreviousValue(currentVersion);

        if (currentVersion != null) {
            this.mainTab.getVersionsCombo().setSelectedItem(currentVersion);
        }

        this.listener.onComplete();
    }
}
