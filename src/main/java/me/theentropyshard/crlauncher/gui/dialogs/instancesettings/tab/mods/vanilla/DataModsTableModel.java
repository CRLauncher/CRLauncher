package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.vanilla;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class DataModsTableModel extends AbstractTableModel {
    private final List<String> dataMods;

    public DataModsTableModel() {
        this.dataMods = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return this.dataMods.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public String getColumnName(int column) {
        return "Mod folder";
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return this.dataMods.get(rowIndex);
    }

    public void addRow(String dataMod) {
        int index = this.dataMods.size();
        this.dataMods.add(dataMod);
        this.fireTableRowsInserted(index, index);
    }

    public String dataModAt(int rowIndex) {
        return this.dataMods.get(rowIndex);
    }

    public void removeRow(int rowIndex) {
        this.dataMods.remove(rowIndex);
        this.fireTableStructureChanged();
    }
}
