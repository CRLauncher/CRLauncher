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
import me.theentropyshard.crlauncher.cosmic.version.Version;
import me.theentropyshard.crlauncher.cosmic.version.VersionType;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LoadVersionsWorker extends Worker<List<Version>, Void> {


    private final CosmicVersionsTableModel model;
    private final AddInstanceDialog dialog;
    private final JTable table;
    private final boolean forceNetwork;

    private DateTimeFormatter formatter;

    public LoadVersionsWorker(CosmicVersionsTableModel model, AddInstanceDialog dialog, JTable table, boolean forceNetwork) {
        super("loading Cosmic Reach versions");

        this.model = model;
        this.dialog = dialog;
        this.table = table;
        this.forceNetwork = forceNetwork;
    }

    @Override
    protected List<Version> work() throws Exception {
        Language language = CRLauncher.getInstance().getLanguage();

        try {
            this.formatter = DateTimeFormatter.ofPattern(language.getString("general.time.dateFormat"));
        } catch (Exception e) {
            this.formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        }

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
            String releasedDate = this.formatter.format(releaseTime) + (showAmountOfTime ?
                " (" + LoadVersionsWorker.getAgoFromNow(releaseTime) + ")" : "");

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

    public static String getAgoFromNow(Temporal temporal) {
        Language language = CRLauncher.getInstance().getLanguage();
        String ago = language.getString("general.time.ago");

        OffsetDateTime now = OffsetDateTime.now();

        int years = (int) ChronoUnit.YEARS.between(temporal, now);
        if (years == 0) {
            int months = (int) ChronoUnit.MONTHS.between(temporal, now);
            if (months == 0) {
                int weeks = (int) ChronoUnit.WEEKS.between(temporal, now);
                if (weeks == 0) {
                    int days = (int) ChronoUnit.DAYS.between(temporal, now);
                    return switch (days) {
                        case 0 -> language.getString("general.time.units.today");
                        case 1 -> language.getString("general.time.units.yesterday");
                        case 2, 3, 4 -> days + " " + language.getString("general.time.units.days234") + " " + ago;
                        default -> days + " " + language.getString("general.time.units.days") + " " + ago;
                    };
                } else {
                    return switch (weeks) {
                        case 1 -> weeks + " " + language.getString("general.time.units.week1") + " " + ago;
                        case 2, 3, 4 -> weeks + " " + language.getString("general.time.units.weeks234") + " " + ago;
                        default -> weeks + " " + language.getString("general.time.units.weeks") + " " + ago;
                    };
                }
            } else {
                return switch (months) {
                    case 1 -> months + " " + language.getString("general.time.units.month1") + " " + ago;
                    case 2, 3, 4 -> months + " " + language.getString("general.time.units.months234") + " " + ago;
                    default -> months + " " + language.getString("general.time.units.months") + " " + ago;
                };
            }
        } else {
            return switch (years) {
                case 1 -> years + " " + language.getString("general.time.units.year1") + " " + ago;
                case 2, 3, 4 -> years + " " + language.getString("general.time.units.years234") + " " + ago;
                default -> years + " " + language.getString("general.time.units.years") + " " + ago;
            };
        }
    }
}
