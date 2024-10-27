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
import me.theentropyshard.crlauncher.cosmic.version.VersionManager;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

public class CosmicVersionsTableModel extends AbstractTableModel {
    private static final Class<?>[] COLUMN_CLASSES = {String.class, String.class, String.class};
    private static final double[] WIDTH_PERCENTAGES = {70, 15, 15};
    private static final double[] WIDTH_PERCENTAGES_WITH_TIME = {55, 30, 15};

    private final List<Version> versions;
    private final AddInstanceDialog dialog;
    private final JTable table;
    private final String[] columnNames;

    private DateTimeFormatter formatter;
    private boolean showInstalled;

    public CosmicVersionsTableModel(AddInstanceDialog dialog, JTable table) {
        this.dialog = dialog;
        this.table = table;
        this.versions = new ArrayList<>();

        Language language = CRLauncher.getInstance().getLanguage();

        this.columnNames = new String[]{
            language.getString("gui.addInstanceDialog.table.version"),
            language.getString("gui.addInstanceDialog.table.dateReleased"),
            language.getString("gui.addInstanceDialog.table.versionType")
        };

        SwingUtils.startWorker(() -> {
            try {
                this.formatter = DateTimeFormatter.ofPattern(language.getString("general.time.dateFormat"));
            } catch (Exception e) {
                this.formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            }

            this.load(false);
        });
    }

    public void load(boolean forceNetwork) {
        new LoadVersionsWorker(this, this.dialog, this.table, forceNetwork).execute();
    }

    public void reload(boolean forceNetwork) {
        int rowCount = this.getRowCount();

        this.versions.clear();

        if (rowCount != 0) {
            this.fireTableRowsDeleted(0, rowCount - 1);
        }

        this.load(forceNetwork);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return CosmicVersionsTableModel.COLUMN_CLASSES[columnIndex];
    }

    @Override
    public String getColumnName(int column) {
        return this.columnNames[column];
    }

    @Override
    public int getRowCount() {
        return this.versions.size();
    }

    @Override
    public int getColumnCount() {
        return this.columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Version version = this.versions.get(rowIndex);

        VersionManager versionManager = CRLauncher.getInstance().getVersionManager();

        if (!versionManager.isInstalled(version) && this.showInstalled) {
            return null;
        }

        return switch (columnIndex) {
            case 0 -> {
                Language language = CRLauncher.getInstance().getLanguage();

                if (versionManager.isLatest(version)) {
                    yield version.getId() + " " + language.getString("gui.addInstanceDialog.latestVersion");
                }

                yield version.getId();
            }
            case 1 -> {
                boolean showAmountOfTime = CRLauncher.getInstance().getSettings().showAmountOfTime;

                Instant instant = Instant.ofEpochSecond(version.getReleaseTime());
                OffsetDateTime releaseTime = OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));

                yield this.formatter.format(releaseTime) + (showAmountOfTime ?
                    " (" + CosmicVersionsTableModel.getAgoFromNow(releaseTime) + ")" : "");
            }
            case 2 -> version.getType();
            default -> null;
        };
    }

    public double[] getTableColumnWidthPercentages() {
        return CRLauncher.getInstance().getSettings().showAmountOfTime ?
            CosmicVersionsTableModel.WIDTH_PERCENTAGES_WITH_TIME : CosmicVersionsTableModel.WIDTH_PERCENTAGES;
    }

    public Version getVersion(int rowIndex) {
        return this.versions.get(rowIndex);
    }

    public void addVersion(Version version) {
        int index = this.versions.size();
        this.versions.add(version);
        this.fireTableRowsInserted(index, index);

        SwingUtils.setJTableColumnsWidth(this.table, this.getTableColumnWidthPercentages());
    }

    public void removeRow(int index) {
        this.versions.remove(index);
        this.fireTableStructureChanged();

        SwingUtils.setJTableColumnsWidth(this.table, this.getTableColumnWidthPercentages());
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

    public void showInstalled(boolean selected) {
        this.showInstalled = selected;
    }
}
