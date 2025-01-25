/*
 * CRLauncher - https://github.com/CRLauncher/CRLauncher
 * Copyright (C) 2024-2025 CRLauncher
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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.worlds;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.SemanticVersion;
import me.theentropyshard.crlauncher.utils.json.Json;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorldsTableModel extends AbstractTableModel {
    private static final Class<?>[] COLUMN_CLASSES = {String.class, String.class, String.class, String.class};

    private final String[] columnNames = {"Name", "Seed", "Last played", "Created"};
    private final DateTimeFormatter formatter;

    private final List<CosmicWorld> worlds;

    public WorldsTableModel(JTable table, CosmicInstance instance) {
        this.worlds = new ArrayList<>();

        Language language = CRLauncher.getInstance().getLanguage();

        this.columnNames[0] = language.getString("gui.instanceSettingsDialog.worldsTab.table.worldName");
        this.columnNames[1] = language.getString("gui.instanceSettingsDialog.worldsTab.table.worldSeed");
        this.columnNames[2] = language.getString("gui.instanceSettingsDialog.worldsTab.table.lastPlayed");
        this.columnNames[3] = language.getString("gui.instanceSettingsDialog.worldsTab.table.createdAt");

        DateTimeFormatter formatter;

        try {
            formatter = DateTimeFormatter.ofPattern("HH:mm " + language.getString("general.time.dateFormat"));
        } catch (Exception e) {
            formatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
        }

        this.formatter = formatter;

        new WorldLoadWorker(instance, this, table).execute();
    }

    public static OffsetDateTime fromEpochMillis(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    @Override
    public int getRowCount() {
        return this.worlds.size();
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
        return WorldsTableModel.COLUMN_CLASSES[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CosmicWorld world = this.worlds.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> world.getWorldDisplayName();
            case 1 -> world.getWorldSeed();
            case 2 -> this.formatter.format(
                world instanceof UpdatedCosmicWorld updatedWorld ?
                    WorldsTableModel.fromEpochMillis(updatedWorld.getLastPlayedEpochMillis()) : world.getLastPlayed()
            );
            case 3 -> world instanceof UpdatedCosmicWorld updatedWorld ?
                this.formatter.format(
                    WorldsTableModel.fromEpochMillis(updatedWorld.getWorldCreatedEpochMillis())
                ) : "N/A";
            default -> null;
        };
    }

    public void add(CosmicWorld world) {
        int index = this.worlds.size();
        this.worlds.add(world);
        this.fireTableRowsInserted(index, index);
    }

    public CosmicWorld worldAt(int rowIndex) {
        return this.worlds.get(rowIndex);
    }

    public void removeRow(int rowIndex) {
        this.worlds.remove(rowIndex);
        this.fireTableStructureChanged();
    }

    public List<CosmicWorld> getWorlds() {
        return this.worlds;
    }
}
