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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.jarmods;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.cosmic.mods.jar.JarMod;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JarModsTableModel extends AbstractTableModel {
    private static final Class<?>[] COLUMN_CLASSES = {String.class, Boolean.class};

    private final Instance instance;
    private final List<JarMod> jarMods;
    private final String[] columnNames = {"Name", "Active"};

    public JarModsTableModel(Instance instance) {
        this.instance = instance;

        if (instance.getJarMods() == null) {
            this.jarMods = new ArrayList<>();
        } else {
            this.jarMods = new ArrayList<>(instance.getJarMods());
        }

        Language language = CRLauncher.getInstance().getLanguage();

        this.columnNames[0] = language.getString("gui.instanceSettingsDialog.jarModsTab.modName");
        this.columnNames[1] = language.getString("gui.instanceSettingsDialog.jarModsTab.modActive");
    }

    @Override
    public int getRowCount() {
        return this.jarMods.size();
    }

    @Override
    public int getColumnCount() {
        return this.columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return this.columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return JarModsTableModel.COLUMN_CLASSES[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        JarMod jarMod = this.jarMods.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> jarMod.getName();
            case 1 -> jarMod.isActive();
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex != 1) {
            return;
        }

        if (!(aValue instanceof Boolean)) {
            return;
        }

        boolean isSelected = (Boolean) aValue;
        JarMod jarMod = this.jarModAt(rowIndex);
        jarMod.setActive(isSelected);

        SwingUtils.startWorker(() -> {
            try {
                this.instance.updateMod(
                    jarMod,
                    this.instance.getJarModsDir(),
                    this.instance.getDisabledJarModsDir(),
                    isSelected
                );
            } catch (IOException e) {
                Log.error("Could not update Jar mod '" + jarMod.getId() + "'", e);

                Language language = CRLauncher.getInstance().getLanguage();

                MessageBox.showErrorMessage(
                    CRLauncher.frame,
                    language.getString("messages.gui.instanceSettingsDialog.couldNotUpdateMod")
                        .replace("$$MOD_LOADER$$", "Jar")
                        .replace("$$MOD_ID$$", jarMod.getId().toString()) + ": " + e.getMessage()
                );
            }
        });

        this.fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    public void add(JarMod jarMod) {
        int index = this.jarMods.size();
        this.jarMods.add(jarMod);
        this.fireTableRowsInserted(index, index);
    }

    public JarMod jarModAt(int rowIndex) {
        return this.jarMods.get(rowIndex);
    }

    public void removeRow(int rowIndex) {
        this.jarMods.remove(rowIndex);
        this.fireTableStructureChanged();
    }
}
