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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.puzzle;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.cosmic.mods.puzzle.PuzzleMod;
import me.theentropyshard.crlauncher.instance.Instance;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class PuzzleModsTableModel extends AbstractTableModel {
    private static final Class<?>[] COLUMN_CLASSES = {String.class, String.class, String.class, Boolean.class};

    private final List<PuzzleMod> puzzleMods;
    private final String[] columnNames = {"Name", "Version", "Description", "Active"};

    public PuzzleModsTableModel(Instance instance) {
        this.puzzleMods = new ArrayList<>(instance.getPuzzleMods());

        Language language = CRLauncher.getInstance().getLanguage();

        this.columnNames[0] = language.getString("gui.instanceSettingsDialog.modsTab.modsTable.puzzle.modName");
        this.columnNames[1] = language.getString("gui.instanceSettingsDialog.modsTab.modsTable.puzzle.modVersion");
        this.columnNames[2] = language.getString("gui.instanceSettingsDialog.modsTab.modsTable.puzzle.modDescription");
        this.columnNames[3] = language.getString("gui.instanceSettingsDialog.modsTab.modsTable.puzzle.modActive");
    }

    @Override
    public int getRowCount() {
        return this.puzzleMods.size();
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
        return PuzzleModsTableModel.COLUMN_CLASSES[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        PuzzleMod puzzleMod = this.puzzleMods.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> puzzleMod.getName();
            case 1 -> puzzleMod.getVersion();
            case 2 -> puzzleMod.getDescription();
            case 3 -> puzzleMod.isActive();
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

        this.puzzleModAt(rowIndex).setActive((boolean) aValue);
        this.fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 3;
    }

    public void add(PuzzleMod puzzleMod) {
        int index = this.puzzleMods.size();
        this.puzzleMods.add(puzzleMod);
        this.fireTableRowsInserted(index, index);
    }

    public PuzzleMod puzzleModAt(int rowIndex) {
        return this.puzzleMods.get(rowIndex);
    }

    public void removeRow(int rowIndex) {
        this.puzzleMods.remove(rowIndex);
        this.fireTableStructureChanged();
    }
}
