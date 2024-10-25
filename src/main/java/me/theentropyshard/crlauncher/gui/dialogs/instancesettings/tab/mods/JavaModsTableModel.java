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

import com.google.gson.JsonObject;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.cosmic.mods.Mod;
import me.theentropyshard.crlauncher.cosmic.mods.ModLoader;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import java.io.IOException;

public class JavaModsTableModel extends ModsTableModel {
    private static final Class<?>[] COLUMN_CLASSES = {String.class, String.class, String.class, Boolean.class};
    private static final double[] WIDTH_PERCENTAGES = {30.0D, 10.0D, 50.0D, 10.0D};

    private final JTable modsTable;
    private final Instance instance;
    private final ModLoader loader;
    private final String[] columnNames;

    public JavaModsTableModel(JTable modsTable, Instance instance, ModLoader loader) {
        super(instance.getMods(loader));

        this.modsTable = modsTable;
        this.instance = instance;
        this.loader = loader;

        Language language = CRLauncher.getInstance().getLanguage();
        JsonObject section = language.getSection("gui.instanceSettingsDialog.modsTab.modsTable.cosmicQuilt");

        this.columnNames = new String[]{
            language.getString(section, "modName"),
            language.getString(section, "modVersion"),
            language.getString(section, "modDescription"),
            language.getString(section, "modActive")
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Mod mod = this.getModAt(rowIndex);

        return switch (columnIndex) {
            case 0 -> mod.getName();
            case 1 -> mod.getVersion();
            case 2 -> mod.getDescription();
            case 3 -> mod.isActive();
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
        Mod mod = this.getModAt(rowIndex);
        mod.setActive(isSelected);

        SwingUtils.startWorker(() -> {
            try {
                this.instance.updateMod(
                    mod,
                    this.instance.getModsDir(this.loader),
                    this.instance.getDisabledModsDir(this.loader)
                );
            } catch (IOException e) {
                Language language = CRLauncher.getInstance().getLanguage();

                MessageBox.showErrorMessage(
                    CRLauncher.frame,
                    language.getString("messages.gui.instanceSettingsDialog.couldNotUpdateMod")
                        .replace("$$MOD_LOADER$$", this.loader.getName())
                        .replace("$$MOD_ID$$", mod.getId())
                );

                Log.error("Could not update " + this.loader.getName() + " mod '" + mod.getId() + "': " + e.getMessage());
            }
        });

        this.fireTableCellUpdated(rowIndex, columnIndex);
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
        return JavaModsTableModel.COLUMN_CLASSES[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 3;
    }

    @Override
    public double[] getTableColumnWidthPercentages() {
        return JavaModsTableModel.WIDTH_PERCENTAGES;
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
