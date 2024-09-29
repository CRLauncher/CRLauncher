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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.fabric;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.cosmic.mods.fabric.FabricMod;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FabricModsTableModel extends AbstractTableModel {
    private static final Class<?>[] COLUMN_CLASSES = {String.class, String.class, String.class, Boolean.class};

    private final List<FabricMod> fabricMods;
    private final Instance instance;
    private final String[] columnNames = {"Name", "Version", "Description", "Active"};

    public FabricModsTableModel(Instance instance) {
        this.fabricMods = new ArrayList<>(instance.getFabricMods());
        this.instance = instance;

        Language language = CRLauncher.getInstance().getLanguage();

        this.columnNames[0] = language.getString("gui.instanceSettingsDialog.modsTab.modsTable.fabric.modName");
        this.columnNames[1] = language.getString("gui.instanceSettingsDialog.modsTab.modsTable.fabric.modVersion");
        this.columnNames[2] = language.getString("gui.instanceSettingsDialog.modsTab.modsTable.fabric.modDescription");
        this.columnNames[3] = language.getString("gui.instanceSettingsDialog.modsTab.modsTable.fabric.modActive");
    }

    @Override
    public int getRowCount() {
        return this.fabricMods.size();
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
        return FabricModsTableModel.COLUMN_CLASSES[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FabricMod fabricMod = this.fabricMods.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> fabricMod.getName();
            case 1 -> fabricMod.getVersion();
            case 2 -> fabricMod.getDescription();
            case 3 -> fabricMod.isActive();
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex != 3) {
            return;
        }

        if (!(aValue instanceof Boolean)) {
            return;
        }

        boolean isSelected = (Boolean) aValue;
        FabricMod fabricMod = this.fabricModAt(rowIndex);
        fabricMod.setActive(isSelected);

        SwingUtils.startWorker(() -> {
            try {
                this.instance.updateMod(
                    fabricMod,
                    this.instance.getFabricModsDir(),
                    this.instance.getDisabledFabricModsDir(),
                    isSelected
                );
            } catch (IOException e) {
                Language language = CRLauncher.getInstance().getLanguage();

                MessageBox.showErrorMessage(
                    CRLauncher.frame,
                    language.getString("messages.gui.instanceSettingsDialog.couldNotUpdateMod")
                        .replace("$$MOD_LOADER$$", "Fabric")
                        .replace("$$MOD_ID$$", fabricMod.getId())
                );

                Log.error("Could not update Fabric mod '" + fabricMod.getId() + "': " + e.getMessage());
            }
        });

        this.fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 3;
    }

    public void add(FabricMod fabricMod) {
        int index = this.fabricMods.size();
        this.fabricMods.add(fabricMod);
        this.fireTableRowsInserted(index, index);
    }

    public FabricMod fabricModAt(int rowIndex) {
        return this.fabricMods.get(rowIndex);
    }

    public void removeRow(int rowIndex) {
        this.fabricMods.remove(rowIndex);
        this.fireTableStructureChanged();
    }
}
