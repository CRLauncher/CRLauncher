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
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LoadVersionsWorker extends SwingWorker<List<Version>, Void> {


    private final CosmicVersionsTableModel model;
    private final AddInstanceDialog dialog;
    private final JTable table;
    private final boolean forceNetwork;

    public LoadVersionsWorker(CosmicVersionsTableModel model, AddInstanceDialog dialog, JTable table, boolean forceNetwork) {
        this.model = model;
        this.dialog = dialog;
        this.table = table;
        this.forceNetwork = forceNetwork;
    }

    @Override
    protected List<Version> doInBackground() throws Exception {
        return CRLauncher.getInstance().getVersionManager().getRemoteVersions(this.forceNetwork);
    }

    @Override
    protected void done() {
        List<Version> versions;

        try {
            versions = this.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.error(e);

            return;
        }

        boolean showAmountOfTime = CRLauncher.getInstance().getSettings().showAmountOfTime;

        for (Version version : versions) {
            Instant instant = Instant.ofEpochSecond(version.getReleaseTime());
            OffsetDateTime releaseTime = OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));
            String releasedDate = Version.FORMATTER.format(releaseTime) + (showAmountOfTime ?
                " (" + this.getAgoFromNow(releaseTime) + ")" : "");

            Object[] rowData = {
                version.getId(),
                releasedDate,
                version.getType()
            };

            this.model.addRow(rowData);
        }

        TableRowSorter<CosmicVersionsTableModel> rowSorter = new TableRowSorter<>(this.model);

        this.dialog.getPreAlphasBox().addActionListener(e -> rowSorter.sort());

        rowSorter.setRowFilter(RowFilter.orFilter(Arrays.asList(
            new VersionTypeRowFilter(this.dialog.getPreAlphasBox(), VersionType.PRE_ALPHA)
        )));

        this.table.setRowSorter(rowSorter);

        this.dialog.getAddButton().setEnabled(true);

        if (showAmountOfTime) {
            SwingUtils.setJTableColumnsWidth(this.table, 55, 30, 15);
        } else {
            SwingUtils.setJTableColumnsWidth(this.table, 70, 15, 15);
        }
    }

    private String getAgoFromNow(Temporal temporal) {
        OffsetDateTime now = OffsetDateTime.now();

        long years = ChronoUnit.YEARS.between(temporal, now);
        if (years == 0) {
            long months = ChronoUnit.MONTHS.between(temporal, now);
            if (months == 0) {
                long weeks = ChronoUnit.WEEKS.between(temporal, now);
                if (weeks == 0) {
                    int days = (int) ChronoUnit.DAYS.between(temporal, now);
                    switch (days) {
                        case 0:
                            return "today";
                        case 1:
                            return "yesterday";
                        default:
                            return days + " days ago";
                    }
                } else {
                    if (weeks == 1) {
                        return "1 week ago";
                    } else {
                        return weeks + " weeks ago";
                    }
                }
            } else {
                if (months == 1) {
                    return "1 month ago";
                } else {
                    return months + " months ago";
                }
            }
        } else {
            if (years == 1) {
                return "1 year ago";
            } else {
                return years + " years ago";
            }
        }
    }
}
