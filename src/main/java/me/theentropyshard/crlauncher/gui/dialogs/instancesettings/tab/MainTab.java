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

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.cosmic.version.Version;
import me.theentropyshard.crlauncher.cosmic.version.VersionManager;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MainTab extends Tab {
    private final JComboBox<String> versionsCombo;
    private final JTextField windowTitleField;
    private final String oldWindowTitle;

    public MainTab(Instance instance, JDialog dialog) {
        super(CRLauncher.getInstance().getLanguage()
            .getString("gui.instanceSettingsDialog.mainTab.name"), instance, dialog);

        JPanel root = this.getRoot();
        root.setLayout(new GridBagLayout());

        Language language = CRLauncher.getInstance().getLanguage();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        {
            JPanel crVersionSettings = new JPanel(new GridLayout(0, 1));
            crVersionSettings.setBorder(new TitledBorder(
                language.getString("gui.instanceSettingsDialog.mainTab.cosmicReachVersion.borderName")
            ));

            this.versionsCombo = new JComboBox<>();
            this.versionsCombo.addItemListener(e -> {
                if (e.getStateChange() != ItemEvent.SELECTED) {
                    return;
                }

                String crVersion = String.valueOf(e.getItem());
                instance.setCosmicVersion(crVersion);
            });
            crVersionSettings.add(this.versionsCombo);

            JCheckBox updateToLatestAutomatically = new JCheckBox(
                language.getString("gui.instanceSettingsDialog.mainTab.cosmicReachVersion.autoUpdateToLatest")
            );
            updateToLatestAutomatically.setSelected(instance.isAutoUpdateToLatest());
            updateToLatestAutomatically.addActionListener(e -> {
                instance.setAutoUpdateToLatest(!instance.isAutoUpdateToLatest());
            });
            crVersionSettings.add(updateToLatestAutomatically);

            gbc.gridy++;
            root.add(crVersionSettings, gbc);
        }

        {
            this.oldWindowTitle = instance.getCustomWindowTitle();

            this.windowTitleField = new JTextField(instance.getCustomWindowTitle());
            this.windowTitleField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                language.getString("gui.instanceSettingsDialog.mainTab.otherSettings.textFieldPlaceholder"));

            JPanel otherSettings = new JPanel(new GridLayout(1, 1));
            otherSettings.setBorder(new TitledBorder(
                language.getString("gui.instanceSettingsDialog.mainTab.otherSettings.borderName")
            ));

            otherSettings.add(this.windowTitleField);

            gbc.gridy++;
            gbc.weighty = 1;
            root.add(otherSettings, gbc);
        }

        new Worker<List<String>, Void>("getting remote versions") {
            @Override
            protected List<String> work() throws Exception {
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
                    Log.error("Could not get versions", e);

                    MessageBox.showErrorMessage(
                            CRLauncher.frame,
                            language.getString("messages.gui.instanceSettingsDialog.couldNotLoadVersions") +
                                ": " + e.getMessage()
                    );

                    return;
                }

                String cosmicVersion = instance.getCosmicVersion();
                versions.forEach(MainTab.this.versionsCombo::addItem);
                MainTab.this.versionsCombo.setSelectedItem(cosmicVersion);
            }
        }.execute();
    }

    @Override
    public void save() throws IOException {
        String windowTitle = this.windowTitleField.getText();

        if (Objects.equals(this.oldWindowTitle, windowTitle)) {
            return;
        }

        this.getInstance().setCustomWindowTitle(windowTitle);
    }
}
