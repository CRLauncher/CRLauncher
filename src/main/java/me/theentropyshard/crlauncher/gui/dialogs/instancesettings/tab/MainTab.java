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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.version.Version;
import me.theentropyshard.crlauncher.cosmic.version.VersionManager;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.instance.Instance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainTab extends Tab {
    private static final Logger LOG = LogManager.getLogger(MainTab.class);

    public MainTab(Instance instance, JDialog dialog) {
        super("Main", instance, dialog);

        JPanel root = this.getRoot();
        root.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        JPanel crVersionSettings = new JPanel(new GridLayout(0, 1));
        JComboBox<String> versionsCombo = new JComboBox<>();
        versionsCombo.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }

            String crVersion = String.valueOf(e.getItem());
            instance.setCosmicVersion(crVersion);
        });
        crVersionSettings.add(versionsCombo);
        crVersionSettings.setBorder(new TitledBorder("Cosmic Reach version"));

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                VersionManager versionManager = CRLauncher.getInstance().getVersionManager();
                List<Version> remoteVersions = versionManager.getRemoteVersions(false);

                return remoteVersions.stream().map(Version::getId).toList();
            }

            @Override
            protected void done() {
                List<String> versions;
                try {
                    versions = this.get();
                } catch (InterruptedException | ExecutionException e) {
                    LOG.error("Could not get versions", e);

                    MessageBox.showErrorMessage(
                            CRLauncher.frame,
                            "Could not get versions: " + e.getMessage()
                    );

                    return;
                }

                String cosmicVersion = instance.getCosmicVersion();
                versions.forEach(versionsCombo::addItem);
                versionsCombo.setSelectedItem(cosmicVersion);
            }
        }.execute();

        gbc.gridy++;
        gbc.weighty = 1;
        root.add(crVersionSettings, gbc);
    }

    @Override
    public void save() throws IOException {

    }
}
