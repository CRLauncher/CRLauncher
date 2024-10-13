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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.vanilla;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.cosmic.mods.vanilla.DataMod;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataModsTableModel extends AbstractTableModel {
    private static final Class<?>[] COLUMN_CLASSES = {String.class, Boolean.class};

    private final List<DataMod> dataMods;
    private final String[] columnNames = {"Name", "Active"};
    private final Instance instance;

    public DataModsTableModel(Instance instance) {
        this.instance = instance;
        this.dataMods = new ArrayList<>();

        Language language = CRLauncher.getInstance().getLanguage();

        this.columnNames[0] = language.getString("gui.instanceSettingsDialog.modsTab.modsTable.vanilla.modFolder");
        this.columnNames[1] = language.getString("gui.instanceSettingsDialog.jarModsTab.modActive");
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    @Override
    public int getRowCount() {
        return this.dataMods.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return DataModsTableModel.COLUMN_CLASSES[columnIndex];
    }

    @Override
    public String getColumnName(int column) {
        return this.columnNames[column];
    }

    @Override
    public int getColumnCount() {
        return this.columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DataMod dataMod = this.dataMods.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> dataMod.getName();
            case 1 -> dataMod.isActive();
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
        DataMod dataMod = this.dataModAt(rowIndex);
        dataMod.setActive(isSelected);

        SwingUtils.startWorker(() -> {
            try {
                this.instance.updateMod(
                    dataMod,
                    this.instance.getDataModsDir(),
                    this.instance.getDisabledDataModsDir(),
                    isSelected
                );
            } catch (IOException e) {
                Log.error("Could not update data mod '" + dataMod.getFileName() + "'", e);

                Language language = CRLauncher.getInstance().getLanguage();

                MessageBox.showErrorMessage(
                    CRLauncher.frame,
                    language.getString("messages.gui.instanceSettingsDialog.couldNotUpdateMod")
                        .replace("$$MOD_LOADER$$", "data")
                        .replace("$$MOD_ID$$", dataMod.getFileName()) + ": " + e.getMessage()
                );
            }
        });

        this.fireTableCellUpdated(rowIndex, columnIndex);
    }

    public void addDataMod(DataMod dataMod) {
        int index = this.dataMods.size();
        this.dataMods.add(dataMod);
        this.fireTableRowsInserted(index, index);
    }

    public DataMod dataModAt(int rowIndex) {
        return this.dataMods.get(rowIndex);
    }

    public void removeRow(int rowIndex) {
        this.dataMods.remove(rowIndex);
        this.fireTableStructureChanged();
    }
}
