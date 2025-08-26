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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.cosmic.mods.Mod;
import me.theentropyshard.crlauncher.cosmic.mods.ModLoader;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.data.DataModsTableModel;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.java.JavaModsTableModel;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.nio.file.Path;

public class ModsView extends JPanel {
    private final CosmicInstance instance;
    private final JComboBox<String> modpackCombo;
    private final JTable modsTable;
    private final JButton addModButton;
    private final JButton deleteModButton;

    private ModsTableModel modsTableModel;

    public ModsView(CosmicInstance instance) {
        super(new BorderLayout());

        this.instance = instance;

        Language language = CRLauncher.getInstance().getLanguage();

        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel modpackLabel = new JLabel(language.getString("gui.instanceSettingsDialog.modsTab.modpack"));
        topPanel.add(modpackLabel, BorderLayout.WEST);

        this.modpackCombo = new JComboBox<>(new String[]{language.getString("gui.instanceSettingsDialog.modsTab.customModpack")});
        topPanel.add(this.modpackCombo, BorderLayout.CENTER);

        this.add(topPanel, BorderLayout.NORTH);

        this.modsTable = new JTable();
        this.modsTable.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtils.setJTableColumnsWidth(ModsView.this.modsTable, ModsView.this.modsTableModel.getTableColumnWidthPercentages());
            }
        });

        JScrollPane scrollPane = new JScrollPane(
            this.modsTable,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        this.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 2));

        this.addModButton = new JButton(this.getAddModButtonName());
        this.addModButton.addActionListener(e -> {
            ModInstaller.pickMod(instance, this.modsTableModel);
        });
        bottomPanel.add(this.addModButton);

        this.deleteModButton = new JButton(this.getDeleteModButtonName());
        this.deleteModButton.addActionListener(e -> {
            int selectedRow = this.modsTable.getSelectedRow();
            if (selectedRow == -1) {
                return;
            }

            Mod mod = this.modsTableModel.getModAt(selectedRow);
            this.modsTableModel.removeRow(selectedRow);

            ModLoader loader = instance.getModLoader();

            instance.getMods(loader).removeIf(m -> m.getFileName().equals(mod.getFileName()));

            Path modFile = instance.getModPath(mod, loader);

            try {
                FileUtils.delete(modFile);
            } catch (IOException ex) {
                Log.error("Exception while trying to delete " + loader.getName() + " Mod", ex);
            }
        });
        bottomPanel.add(this.deleteModButton);

        this.add(bottomPanel, BorderLayout.SOUTH);
    }

    public void update() {
        this.addModButton.setText(this.getAddModButtonName());
        this.deleteModButton.setText(this.getDeleteModButtonName());

        ModsTableModel tableModel = switch (this.instance.getModLoader()) {
            case VANILLA -> new DataModsTableModel(this.modsTable, this.instance);
            case FABRIC -> new JavaModsTableModel(this.modsTable, this.instance, ModLoader.FABRIC);
            case QUILT -> new JavaModsTableModel(this.modsTable, this.instance, ModLoader.QUILT);
            case PUZZLE -> new JavaModsTableModel(this.modsTable, this.instance, ModLoader.PUZZLE);
        };

        this.setModsTableModel(tableModel);

        SwingUtils.setJTableColumnsWidth(this.modsTable, tableModel.getTableColumnWidthPercentages());
    }

    private String getAddModButtonName() {
        Language language = CRLauncher.getInstance().getLanguage();

        String parentKey = "gui.instanceSettingsDialog.modsTab.modsTable";

        return switch (this.instance.getModLoader()) {
            case VANILLA -> language.getString(parentKey + ".vanilla.addModButton");
            case FABRIC -> language.getString(parentKey + ".fabric.addModButton");
            case QUILT -> language.getString(parentKey + ".cosmicQuilt.addModButton");
            case PUZZLE -> language.getString(parentKey + ".puzzle.addModButton");
        };
    }

    private String getDeleteModButtonName() {
        Language language = CRLauncher.getInstance().getLanguage();

        String parentKey = "gui.instanceSettingsDialog.modsTab.modsTable";

        return switch (this.instance.getModLoader()) {
            case VANILLA -> language.getString(parentKey + ".vanilla.deleteModButton");
            case FABRIC -> language.getString(parentKey + ".fabric.deleteModButton");
            case QUILT -> language.getString(parentKey + ".cosmicQuilt.deleteModButton");
            case  PUZZLE -> language.getString(parentKey + ".puzzle.deleteModButton");
        };
    }

    public JComboBox<String> getModpackCombo() {
        return this.modpackCombo;
    }

    public JTable getModsTable() {
        return this.modsTable;
    }

    public JButton getAddModButton() {
        return this.addModButton;
    }

    public JButton getDeleteModButton() {
        return this.deleteModButton;
    }

    public ModsTableModel getModsTableModel() {
        return this.modsTableModel;
    }

    public void setModsTableModel(ModsTableModel modsTableModel) {
        this.modsTableModel = modsTableModel;
        this.modsTable.setModel(modsTableModel);
    }
}
