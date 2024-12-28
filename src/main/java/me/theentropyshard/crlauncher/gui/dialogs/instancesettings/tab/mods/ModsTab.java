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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.github.GithubRelease;
import me.theentropyshard.crlauncher.gui.dialogs.crmm.SearchCrmmModsDialog;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.Tab;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.java.fabric.FabricVersionsLoaderWorker;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.java.puzzle.PuzzleVersionsLoaderWorker;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.java.quilt.QuiltVersionsLoaderWorker;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.cosmic.mods.ModLoader;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.OperatingSystem;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class ModsTab extends Tab implements ItemListener {
    private final JComboBox<ModLoader> typeCombo;
    private final JComboBox<GithubRelease> loaderVersionCombo;
    private final JPanel root;
    private final JPanel loaderVersionsPanel;
    private final ModsView modsView;

    private ModLoader lastType;
    private boolean versionsLoaded;

    public ModsTab(Instance instance, JDialog dialog) {
        super(CRLauncher.getInstance().getLanguage()
            .getString("gui.instanceSettingsDialog.modsTab.name"), instance, dialog);

        Language language = CRLauncher.getInstance().getLanguage();

        this.lastType = instance.getModLoader();

        this.root = this.getRoot();
        this.root.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;

        {
            JPanel modLoader = this.getTitledPanel(
                language.getString("gui.instanceSettingsDialog.modsTab.modLoader.borderName"), 1, 1);
            this.typeCombo = new JComboBox<>(ModLoader.values());
            for (int i = 0; i < this.typeCombo.getItemCount(); i++) {
                if (this.typeCombo.getItemAt(i) == instance.getModLoader()) {
                    this.typeCombo.setSelectedIndex(i);
                    break;
                }
            }
            this.typeCombo.addItemListener(this);

            modLoader.add(this.typeCombo);

            gbc.gridy++;
            this.root.add(modLoader, gbc);
        }

        {
            this.loaderVersionCombo = new JComboBox<>();

            this.loaderVersionCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                    if (value instanceof GithubRelease release) {
                        ((JLabel) c).setText(release.name + " (" + release.tag_name + ")");
                    }

                    return c;
                }
            });

            this.loaderVersionsPanel = this.getTitledPanel(
                language.getString("gui.instanceSettingsDialog.modsTab.loaderVersion.borderName"), 1, 1);
            this.loaderVersionsPanel.setVisible(false);
            this.loaderVersionsPanel.add(this.loaderVersionCombo);

            if (this.getInstance().getModLoader() != ModLoader.VANILLA) {
                this.loadModloaderVersions();
                this.loaderVersionsPanel.setVisible(true);
            }

            gbc.gridy++;
            this.root.add(this.loaderVersionsPanel, gbc);
        }

        {
            JPanel modsPanel = this.getTitledPanel(language.getString("gui.instanceSettingsDialog.modsTab.modsTable.borderName"), 1, 1);

            this.modsView = new ModsView(instance);
            modsPanel.add(this.modsView);

            this.updateModsView();

            gbc.gridy++;
            gbc.weighty = 1;
            this.root.add(modsPanel, gbc);
        }

        {
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            JButton openModsFolderButton = new JButton(
                language.getString("gui.instanceSettingsDialog.modsTab.openModsFolder")
            );
            openModsFolderButton.addActionListener(e -> {
                SwingUtils.startWorker(() -> {
                    Path modsDir = switch (instance.getModLoader()) {
                        case VANILLA -> instance.getDataModsDir();
                        case FABRIC -> instance.getFabricModsDir();
                        case QUILT -> instance.getQuiltModsDir();
                        case PUZZLE -> instance.getPuzzleModsDir();
                    };

                    try {
                        FileUtils.createDirectoryIfNotExists(modsDir);
                    } catch (IOException ex) {
                        Log.error("Could not create mods folder: " + modsDir, ex);
                    }

                    OperatingSystem.open(modsDir);
                });
            });
            bottomPanel.add(openModsFolderButton);

            JButton searchModsButton = new JButton(
                language.getString("gui.instanceSettingsDialog.modsTab.searchModsCRMM")
            );
            searchModsButton.addActionListener(e -> {
                new Worker<Void, Void>("searching mods") {
                    @Override
                    protected Void work() throws Exception {
                        new SearchCrmmModsDialog(instance, ModsTab.this);

                        return null;
                    }
                }.execute();
            });
            bottomPanel.add(searchModsButton);

            gbc.gridy++;
            gbc.weighty = 0;
            this.root.add(bottomPanel, gbc);
        }
    }

    private void loadModloaderVersions() {
        Instance instance = this.getInstance();

        if (instance.getModLoader() == ModLoader.VANILLA) {
            this.loaderVersionsPanel.setVisible(false);

            return;
        }

        if (this.versionsLoaded && instance.getModLoader() == this.lastType) {
            return;
        }

        this.loaderVersionCombo.removeAllItems();

        switch (instance.getModLoader()) {
            case FABRIC -> new FabricVersionsLoaderWorker(this.loaderVersionCombo, instance).execute();
            case QUILT -> new QuiltVersionsLoaderWorker(this.loaderVersionCombo, instance).execute();
            case PUZZLE -> new PuzzleVersionsLoaderWorker(this.loaderVersionCombo, instance).execute();
        }

        this.versionsLoaded = true;

        this.loaderVersionsPanel.setVisible(true);
    }

    private void updateModsView() {
        this.loadModloaderVersions();
        this.modsView.update();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() != ItemEvent.SELECTED) {
            return;
        }

        Instance instance = this.getInstance();

        this.lastType = instance.getModLoader();
        instance.setModLoader((ModLoader) e.getItem());

        this.updateModsView();
        this.getRoot().revalidate();
    }

    private JPanel getTitledPanel(String title, int rows, int cols) {
        JPanel panel = new JPanel(new GridLayout(rows, cols));
        panel.setBorder(new TitledBorder(title));

        return panel;
    }

    @Override
    public void save() throws IOException {
        Instance instance = this.getInstance();
        instance.setModLoader((ModLoader) this.typeCombo.getSelectedItem());

        if (instance.getModLoader() == ModLoader.VANILLA) return;

        GithubRelease versionCombo = (GithubRelease) this.loaderVersionCombo.getSelectedItem();
        if (versionCombo == null) {
            Log.warn("Game version combo unavailable");
            return;
        }

        switch (instance.getModLoader()) {
            case FABRIC -> instance.setFabricVersion(versionCombo.tag_name);
            case QUILT -> instance.setQuiltVersion(versionCombo.tag_name);
            case PUZZLE -> instance.setPuzzleVersion(versionCombo.tag_name);
        }
    }

    public JComboBox<GithubRelease> getLoaderVersionCombo() {
        return this.loaderVersionCombo;
    }

    public ModsView getModsView() {
        return this.modsView;
    }
}
