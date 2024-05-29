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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.worlds;

import me.theentropyshard.crlauncher.instance.Instance;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WorldsTableModel extends AbstractTableModel {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");

    private static final String[] COLUMN_NAMES = {"Name", "Seed", "Last played"};
    private static final Class<?>[] COLUMN_CLASSES = {String.class, String.class, String.class};

    private final List<CosmicWorld> worlds;

    public WorldsTableModel(Instance instance) {
        this.worlds = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return this.worlds.size();
    }

    @Override
    public int getColumnCount() {
        return WorldsTableModel.COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return WorldsTableModel.COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return WorldsTableModel.COLUMN_CLASSES[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CosmicWorld world = this.worlds.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> world.getWorldDisplayName();
            case 1 -> world.getWorldSeed();
            case 2 -> WorldsTableModel.FORMATTER.format(world.getLastPlayed());
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex != 0) {
            return;
        }

        if (!(aValue instanceof String)) {
            return;
        }

        String newWorldName = String.valueOf(aValue);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; //columnIndex == 0;
    }

    public void add(CosmicWorld world) {
        int index = this.worlds.size();
        this.worlds.add(world);
        this.fireTableRowsInserted(index, index);
    }

    public void removeRow(int rowIndex) {
        this.worlds.remove(rowIndex);
        this.fireTableStructureChanged();
    }
}
