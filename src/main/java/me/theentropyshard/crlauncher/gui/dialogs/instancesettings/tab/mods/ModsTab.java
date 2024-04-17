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

import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.Tab;
import me.theentropyshard.crlauncher.instance.InstanceType;
import me.theentropyshard.crlauncher.instance.OldInstance;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

public class ModsTab extends Tab implements ItemListener {
    private final JComboBox<InstanceType> typeCombo;
    private final JPanel mods;
    private JPanel modsView;

    public ModsTab(OldInstance oldInstance, JDialog dialog) {
        super("Mods", oldInstance, dialog);

        JPanel root = this.getRoot();
        root.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;

        {
            JPanel modLoader = this.getTitledPanel("Mod loader", 1, 1);
            this.typeCombo = new JComboBox<>(InstanceType.values());
            for (int i = 0; i < this.typeCombo.getItemCount(); i++) {
                if (this.typeCombo.getItemAt(i) == oldInstance.getType()) {
                    this.typeCombo.setSelectedIndex(i);
                    break;
                }
            }
            this.typeCombo.addItemListener(this);

            modLoader.add(this.typeCombo);

            gbc.gridy++;
            root.add(modLoader, gbc);
        }

        {
            this.mods = this.getTitledPanel("Mods", 1, 1);
            this.updateModsView();

            this.mods.add(this.modsView);

            gbc.gridy++;
            gbc.weighty = 1;
            root.add(this.mods, gbc);
        }
    }

    private void updateModsView() {
        OldInstance instance = this.getInstance();

        this.modsView = switch (instance.getType()) {
            case VANILLA -> new JarModsView(instance);
            case FABRIC -> new FabricModsView(instance);
            case QUILT -> new QuiltModsView();
        };

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

        this.getInstance().setType((InstanceType) e.getItem());

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
        OldInstance instance = this.getInstance();
        instance.setType((InstanceType) this.typeCombo.getSelectedItem());
    }
}
