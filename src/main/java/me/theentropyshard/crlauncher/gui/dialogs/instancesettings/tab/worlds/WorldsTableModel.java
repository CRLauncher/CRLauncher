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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.worlds;

import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.json.Json;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WorldsTableModel extends AbstractTableModel {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");

    private static final String[] COLUMN_NAMES = {"Name", "Seed", "Last played"};
    private static final Class<?>[] COLUMN_CLASSES = {String.class, String.class, String.class};

    private final List<CosmicWorld> worlds;

    public WorldsTableModel(Instance instance) {
        this.worlds = new ArrayList<>();

        new SwingWorker<Void, CosmicWorld>() {
            @Override
            protected Void doInBackground() throws Exception {
                Path worldsDir = instance.getCosmicDir().resolve("worlds");
                List<Path> worldDirs = FileUtils.list(worldsDir);

                for (Path worldDir : worldDirs) {
                    Path worldInfoFile = worldDir.resolve("worldInfo.json");
                    String content = FileUtils.readUtf8(worldInfoFile);

                    CosmicWorld world = Json.parse(content, CosmicWorld.class);

                    Path playerFile = worldDir.resolve("players").resolve("localPlayer.json");
                    if (Files.exists(playerFile)) {
                        BasicFileAttributes attribs = Files.readAttributes(playerFile, BasicFileAttributes.class);
                        OffsetDateTime lastModified = Instant.ofEpochMilli(attribs.lastModifiedTime().toMillis())
                                .atZone(ZoneId.systemDefault()).toOffsetDateTime();
                        world.setLastPlayed(lastModified);
                    }

                    this.publish(world);
                }

                return null;
            }

            @Override
            protected void process(List<CosmicWorld> chunks) {
                WorldsTableModel.this.worlds.addAll(chunks);
            }

            @Override
            protected void done() {
                WorldsTableModel.this.fireTableDataChanged();
            }
        }.execute();
    }

    @Override
    public int getRowCount() {
        return this.worlds.size();
    }

    @Override
    public int getColumnCount() {
        return WorldsTableModel.COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return WorldsTableModel.COLUMN_NAMES[column];
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
            case 2 -> WorldsTableModel.FORMATTER.format(world.getLastPlayed());
            default -> null;
        };
    }

    public void add(CosmicWorld world) {
        int index = this.worlds.size();
        this.worlds.add(world);
        this.fireTableRowsInserted(index, index);
    }

    public void removeRow(int rowIndex) {
        this.worlds.remove(rowIndex);
        this.fireTableStructureChanged();
    }
}
