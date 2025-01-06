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
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTableModel;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import java.io.IOException;

public class JarModsTableModel extends ModsTableModel {
    private static final Class<?>[] COLUMN_CLASSES = {String.class, Boolean.class};
    private static final double[] WIDTH_PERCENTAGES = {90.0D, 10.0D};

    private final JTable modsTable;
    private final CosmicInstance instance;
    private final String[] columnNames;

    public JarModsTableModel(JTable modsTable, CosmicInstance instance) {
        super(instance.getJarMods());

        this.modsTable = modsTable;
        this.instance = instance;

        Language language = CRLauncher.getInstance().getLanguage();

        this.columnNames = new String[]{
            language.getString("gui.instanceSettingsDialog.modsTab.modsTable.vanilla.modFolder"),
            language.getString("gui.instanceSettingsDialog.modsTab.modsTable.cosmicQuilt.modActive")
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return JarModsTableModel.COLUMN_CLASSES[columnIndex];
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
        Mod dataMod = this.getModAt(rowIndex);

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
        Mod jarMod = this.getModAt(rowIndex);
        jarMod.setActive(isSelected);

        SwingUtils.startWorker(() -> {
            try {
                this.instance.updateMod(
                    jarMod,
                    this.instance.getJarModsDir(),
                    this.instance.getDisabledJarModsDir()
                );
            } catch (IOException e) {
                Log.error("Could not update Jar mod '" + jarMod.getFileName() + "'", e);

                Language language = CRLauncher.getInstance().getLanguage();

                MessageBox.showErrorMessage(
                    CRLauncher.frame,
                    language.getString("messages.gui.instanceSettingsDialog.couldNotUpdateMod")
                        .replace("$$MOD_LOADER$$", "Jar")
                        .replace("$$MOD_ID$$", jarMod.getFileName()) + ": " + e.getMessage()
                );
            }
        });

        this.fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public double[] getTableColumnWidthPercentages() {
        return JarModsTableModel.WIDTH_PERCENTAGES;
    }

    @Override
    public void addMod(Mod mod) {
        super.addMod(mod);

        SwingUtils.setJTableColumnsWidth(this.modsTable, this.getTableColumnWidthPercentages());
    }

    @Override
    public void removeRow(int index) {
        super.removeRow(index);

        SwingUtils.setJTableColumnsWidth(this.modsTable, this.getTableColumnWidthPercentages());
    }
}
