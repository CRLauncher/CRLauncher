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
import me.theentropyshard.crlauncher.cosmic.version.Version;
import me.theentropyshard.crlauncher.cosmic.version.VersionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LoadVersionsWorker extends SwingWorker<List<Version>, Void> {
    private static final Logger LOG = LogManager.getLogger(LoadVersionsWorker.class);

    private final CRVersionsTableModel model;
    private final AddInstanceDialog dialog;
    private final JTable table;

    public LoadVersionsWorker(CRVersionsTableModel model, AddInstanceDialog dialog, JTable table) {
        this.model = model;
        this.dialog = dialog;
        this.table = table;
    }

    @Override
    protected List<Version> doInBackground() throws Exception {
        return CRLauncher.getInstance().getVersionManager().getRemoteVersions();
    }

    @Override
    protected void done() {
        List<Version> versions;

        try {
            versions = this.get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error(e);

            return;
        }

        for (Version version : versions) {
            Instant instant = Instant.ofEpochSecond(version.getReleaseTime());
            Object[] rowData = {
                    version.getId(),
                    Version.FORMATTER.format(OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"))),
                    version.getType()
            };

            this.model.addRow(rowData);
        }

        TableRowSorter<CRVersionsTableModel> rowSorter = new TableRowSorter<>(this.model);

        this.dialog.getPreAlphasBox().addActionListener(e -> rowSorter.sort());

        rowSorter.setRowFilter(RowFilter.orFilter(Arrays.asList(
                new VersionTypeRowFilter(this.dialog.getPreAlphasBox(), VersionType.PRE_ALPHA)
        )));

        this.table.setRowSorter(rowSorter);

        this.dialog.getAddButton().setEnabled(true);
    }
}
