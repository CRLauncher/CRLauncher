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

import me.theentropyshard.crlauncher.github.GithubReleaseResponse;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.Tab;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.fabric.FabricModsView;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.fabric.FabricVersionsLoaderWorker;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.quilt.QuiltModsView;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.quilt.QuiltVersionsLoaderWorker;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.vanilla.JarModsView;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.instance.InstanceType;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Objects;

public class ModsTab extends Tab implements ItemListener {
    private final JComboBox<InstanceType> typeCombo;
    private final JComboBox<GithubReleaseResponse> loaderVersionCombo;
    private final JPanel mods;
    private final JPanel root;
    private final JPanel loaderVersionsPanel;
    private JPanel modsView;

    private InstanceType lastType;
    private boolean versionsLoaded;

    public ModsTab(Instance instance, JDialog dialog) {
        super("Mods", instance, dialog);

        this.lastType = instance.getType();

        this.root = this.getRoot();
        this.root.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;

        {
            JPanel modLoader = this.getTitledPanel("Mod loader", 1, 1);
            this.typeCombo = new JComboBox<>(InstanceType.values());
            for (int i = 0; i < this.typeCombo.getItemCount(); i++) {
                if (this.typeCombo.getItemAt(i) == instance.getType()) {
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

                    if (value instanceof GithubReleaseResponse release) {
                        ((JLabel) c).setText(release.name + " (" + release.tag_name + ")");
                    }

                    return c;
                }
            });

            this.loaderVersionsPanel = this.getTitledPanel("Loader version", 1, 1);
            this.loaderVersionsPanel.setVisible(false);
            this.loaderVersionsPanel.add(this.loaderVersionCombo);

            if (this.getInstance().getType() != InstanceType.VANILLA) {
                this.loadModloaderVersions();
                this.loaderVersionsPanel.setVisible(true);
            }

            gbc.gridy++;
            this.root.add(this.loaderVersionsPanel, gbc);
        }

        {
            this.mods = this.getTitledPanel("Mods", 1, 1);
            this.updateModsView();

            this.mods.add(this.modsView);

            gbc.gridy++;
            gbc.weighty = 1;
            this.root.add(this.mods, gbc);
        }
    }

    private void loadModloaderVersions() {
        Instance instance = this.getInstance();

        if (instance.getType() == InstanceType.VANILLA) {
            this.loaderVersionsPanel.setVisible(false);

            return;
        }

        if (this.versionsLoaded && instance.getType() == this.lastType) {
            return;
        }

        this.loaderVersionCombo.removeAllItems();

        if (instance.getType() == InstanceType.FABRIC) {
            new FabricVersionsLoaderWorker(this.loaderVersionCombo, instance).execute();
        } else if (instance.getType() == InstanceType.QUILT) {
            new QuiltVersionsLoaderWorker(this.loaderVersionCombo, instance).execute();
        }

        this.versionsLoaded = true;

        this.loaderVersionsPanel.setVisible(true);
    }

    private void updateModsView() {
        Instance instance = this.getInstance();

        this.modsView = switch (instance.getType()) {
            case VANILLA -> new JarModsView(instance);
            case FABRIC -> new FabricModsView(instance);
            case QUILT -> new QuiltModsView(instance);
        };

        this.loadModloaderVersions();

        if (this.mods.getComponentCount() > 0) {
            this.mods.remove(0);
        }

        this.mods.add(this.modsView);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() != ItemEvent.SELECTED) {
            return;
        }

        Instance instance = this.getInstance();

        this.lastType = instance.getType();
        instance.setType((InstanceType) e.getItem());

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
        instance.setType((InstanceType) this.typeCombo.getSelectedItem());
        if (instance.getType() == InstanceType.FABRIC) {
            instance.setFabricVersion(
                    ((GithubReleaseResponse) Objects.requireNonNull(this.loaderVersionCombo.getSelectedItem())).tag_name
            );
        } else if (instance.getType() == InstanceType.QUILT) {
            instance.setQuiltVersion(
                    ((GithubReleaseResponse) Objects.requireNonNull(this.loaderVersionCombo.getSelectedItem())).tag_name
            );
        }
    }

    public JComboBox<GithubReleaseResponse> getLoaderVersionCombo() {
        return this.loaderVersionCombo;
    }
}
