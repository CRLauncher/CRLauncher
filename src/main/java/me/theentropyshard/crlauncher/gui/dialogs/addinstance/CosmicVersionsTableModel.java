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

package me.theentropyshard.crlauncher.gui.dialogs.addinstance;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CosmicVersionsTableModel extends DefaultTableModel {
    private final AddInstanceDialog dialog;
    private final JTable table;

    public CosmicVersionsTableModel(AddInstanceDialog dialog, JTable table) {
        super(new Object[][]{}, CosmicVersionsTableModel.getColumnNames());

        this.dialog = dialog;
        this.table = table;

        this.load(false);
    }

    private static Object[] getColumnNames() {
        Language language = CRLauncher.getInstance().getLanguage();

        return new Object[]{
            language.getString("gui.addInstanceDialog.table.version"),
            language.getString("gui.addInstanceDialog.table.dateReleased"),
            language.getString("gui.addInstanceDialog.table.versionType")
        };
    }

    public void load(boolean forceNetwork) {
        new LoadVersionsWorker(this, this.dialog, this.table, forceNetwork).execute();
    }

    public void reload(boolean forceNetwork) {
        int rowCount = this.getRowCount();

        this.dataVector.clear();

        if (rowCount != 0) {
            this.fireTableRowsDeleted(0, rowCount - 1);
        }

        this.load(forceNetwork);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
