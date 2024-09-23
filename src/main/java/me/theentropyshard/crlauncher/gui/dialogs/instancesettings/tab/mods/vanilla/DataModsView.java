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
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.utils.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DataModsView extends JPanel {
    private final DataModsTableModel dataModsTableModel;
    private final JTable dataModsTable;
    private final JButton deleteDataModButton;

    public DataModsView(Instance instance) {
        super(new BorderLayout());

        this.dataModsTableModel = new DataModsTableModel();

        this.dataModsTable = new JTable(this.dataModsTableModel);
        this.dataModsTable.getTableHeader().setEnabled(false);
        this.dataModsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dataModsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = DataModsView.this.dataModsTable.getSelectedRow();

                if (selectedRow == -1) {
                    return;
                }

                DataModsView.this.deleteDataModButton.setEnabled(true);
            }
        });

        Path dataModsDir = instance.getDataModsDir();
        new Worker<Void, String>("loading data mods") {
            @Override
            protected Void work() throws Exception {
                if (!Files.exists(dataModsDir)) {
                    return null;
                }

                for (Path dataModDir : FileUtils.list(dataModsDir)) {
                    if (!Files.isDirectory(dataModDir)) {
                        continue;
                    }

                    this.publish(dataModDir.getFileName().toString());
                }

                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String dataModDir : chunks) {
                    DataModsView.this.dataModsTableModel.addRow(dataModDir);
                }
            }
        }.execute();

        JScrollPane scrollPane = new JScrollPane(this.dataModsTable);
        scrollPane.setBorder(null);
        this.add(scrollPane, BorderLayout.CENTER);

        this.deleteDataModButton = new JButton(
            CRLauncher.getInstance().getLanguage().getString("gui.instanceSettingsDialog.modsTab.vanilla.deleteModButton")
        );
        this.deleteDataModButton.addActionListener(e ->

        {
            int selectedRow = this.dataModsTable.getSelectedRow();
            if (selectedRow == -1) {
                return;
            }

            String dataMod = this.dataModsTableModel.dataModAt(selectedRow);
            this.dataModsTableModel.removeRow(selectedRow);

            new Worker<Void, Void>("deleting data mod") {
                @Override
                protected Void work() throws Exception {
                    FileUtils.delete(dataModsDir.resolve(dataMod));

                    return null;
                }
            }.execute();
        });

        this.add(this.deleteDataModButton, BorderLayout.SOUTH);
    }

    public DataModsTableModel getDataModsTableModel() {
        return this.dataModsTableModel;
    }
}
