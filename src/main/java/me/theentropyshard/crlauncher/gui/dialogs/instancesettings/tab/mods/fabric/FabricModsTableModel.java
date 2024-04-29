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

import me.theentropyshard.crlauncher.cosmic.mods.fabric.FabricMod;
import me.theentropyshard.crlauncher.instance.Instance;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class FabricModsTableModel extends AbstractTableModel {
    private static final String[] COLUMN_NAMES = {"Name", "Version", "Description", "Active"};
    private static final Class<?>[] COLUMN_CLASSES = {String.class, String.class, String.class, Boolean.class};

    private final List<FabricMod> fabricMods;

    public FabricModsTableModel(Instance oldInstance) {
        if (oldInstance.getFabricMods() == null) {
            this.fabricMods = new ArrayList<>();
        } else {
            this.fabricMods = new ArrayList<>(oldInstance.getFabricMods());
        }
    }

    @Override
    public int getRowCount() {
        return this.fabricMods.size();
    }

    @Override
    public int getColumnCount() {
        return FabricModsTableModel.COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return FabricModsTableModel.COLUMN_NAMES[column];
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
        this.fabricModAt(rowIndex).setActive(isSelected);

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
