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

import me.theentropyshard.crlauncher.cosmic.mods.Mod;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public abstract class ModsTableModel extends AbstractTableModel {
    private final List<Mod> mods;

    public ModsTableModel(List<Mod> mods) {
        this.mods = new ArrayList<>(mods);
    }

    @Override
    public int getRowCount() {
        return this.mods.size();
    }

    @Override
    public abstract int getColumnCount();

    @Override
    public abstract Object getValueAt(int rowIndex, int columnIndex);

    @Override
    public abstract String getColumnName(int column);

    @Override
    public abstract Class<?> getColumnClass(int columnIndex);

    @Override
    public abstract void setValueAt(Object aValue, int rowIndex, int columnIndex);

    @Override
    public abstract boolean isCellEditable(int rowIndex, int columnIndex);

    public abstract double[] getTableColumnWidthPercentages();

    public void addMod(Mod mod) {
        int index = this.mods.size();
        this.mods.add(mod);
        this.fireTableRowsInserted(index, index);
    }

    public Mod getModAt(int index) {
        return this.mods.get(index);
    }

    public void removeRow(int index) {
        this.mods.remove(index);
        this.fireTableStructureChanged();
    }
}
