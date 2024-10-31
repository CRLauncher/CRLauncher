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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.jar;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.cosmic.mods.Mod;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModInstaller;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

public class JarModsView extends JPanel {
    private final JTable jarModsTable;
    private final JarModsTableModel jarModsTableModel;

    public JarModsView(Instance instance) {
        super(new BorderLayout());

        this.jarModsTable = new JTable();
        this.jarModsTableModel = new JarModsTableModel(this.jarModsTable, instance);

        this.jarModsTable.setModel(this.jarModsTableModel);
        this.jarModsTable.getTableHeader().setEnabled(false);
        this.jarModsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.jarModsTable.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtils.setJTableColumnsWidth(JarModsView.this.jarModsTable, JarModsView.this.jarModsTableModel.getTableColumnWidthPercentages());
            }
        });

        JScrollPane scrollPane = new JScrollPane(this.jarModsTable);
        scrollPane.setBorder(null);
        this.add(scrollPane, BorderLayout.CENTER);

        Language language = CRLauncher.getInstance().getLanguage();

        JButton addJarMod = new JButton(language.getString("gui.instanceSettingsDialog.jarModsTab.addModButton"));
        addJarMod.addActionListener(e -> {
            ModInstaller.pickJarMod(instance, this.jarModsTableModel);
        });

        JButton deleteModButton = new JButton(language.getString("gui.instanceSettingsDialog.jarModsTab.deleteModButton"));
        deleteModButton.addActionListener(e -> {
            int selectedRow = this.jarModsTable.getSelectedRow();

            if (selectedRow == -1) {
                return;
            }

            Mod jarMod = this.jarModsTableModel.getModAt(selectedRow);
            this.jarModsTableModel.removeRow(selectedRow);
            instance.getJarMods().remove(jarMod);

            try {
                FileUtils.delete(instance.getJarModPath(jarMod));
            } catch (IOException ex) {
                Log.error("Exception while trying to delete jar Mod", ex);
            }
        });

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        buttonsPanel.add(addJarMod);
        buttonsPanel.add(deleteModButton);

        this.add(buttonsPanel, BorderLayout.SOUTH);
    }
}
