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

import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.RegexUtils;
import me.theentropyshard.crlauncher.utils.SemanticVersion;
import me.theentropyshard.crlauncher.utils.json.Json;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorldLoadWorker extends Worker<Void, CosmicWorld> {
    private final CosmicInstance instance;
    private final WorldsTableModel tableModel;
    private final JTable table;

    public WorldLoadWorker(CosmicInstance instance, WorldsTableModel tableModel, JTable table) {
        super("loading worlds");

        this.instance = instance;
        this.tableModel = tableModel;
        this.table = table;
    }

    @Override
    protected Void work() throws Exception {
        SemanticVersion cosmicVersion = this.parseVersion();

        Path worldsDir = this.instance.getCosmicDir().resolve("worlds");

        if (!Files.exists(worldsDir)) {
            return null;
        }

        List<Path> worldDirs = FileUtils.list(worldsDir);

        for (Path worldDir : worldDirs) {
            if (!Files.isDirectory(worldDir)) {
                continue;
            }

            Path worldInfoFile = worldDir.resolve("worldInfo.json");

            if (!Files.exists(worldInfoFile)) {
                Log.warn("File does not exist: " + worldInfoFile + ", skipping");

                continue;
            }

            try {
                this.loadWorld(worldInfoFile, worldDir, cosmicVersion);
            } catch (IOException e) {
                Log.warn("Could not load world: " + e.getMessage());
            }
        }

        return null;
    }

    private SemanticVersion parseVersion() {
        String version = this.instance.getCosmicVersion();

        SemanticVersion cosmicVersion = SemanticVersion.parse(version);

        if (cosmicVersion == null) {
            Log.warn("Could not parse game version " + version + " of instance " + this.instance.getName() +
                ", trying to extract with regex");

            Matcher matcher = RegexUtils.THREE_DIGITS.matcher(version);
            if (matcher.find()) {
                String parsedVersion = matcher.group(0);
                cosmicVersion = SemanticVersion.parse(parsedVersion);
                Log.info("Successfully extracted game version with regex: " + parsedVersion);
            }
        }

        if (cosmicVersion == null) {
            Log.warn("Could not extract game version with regex, defaulting to 0.1.33");

            cosmicVersion = new SemanticVersion(0, 1, 33);
        }

        return cosmicVersion;
    }

    private void loadWorld(Path worldInfoFile, Path worldDir, SemanticVersion cosmicVersion) throws IOException {
        String content;

        if (cosmicVersion.getMinor() < 2 && cosmicVersion.getPatch() < 40) {
            content = FileUtils.read(worldInfoFile, Charset.defaultCharset());
        } else {
            content = FileUtils.readUtf8(worldInfoFile);
        }

        CosmicWorld world;

        if (cosmicVersion.getMinor() < 2 && cosmicVersion.getPatch() < 34) {
            world = Json.parse(content, CosmicWorld.class);

            Path playerFile = worldDir.resolve("players").resolve("localPlayer.json");
            if (Files.exists(playerFile)) {
                BasicFileAttributes attribs = Files.readAttributes(playerFile, BasicFileAttributes.class);
                world.setLastPlayed(WorldsTableModel.fromEpochMillis(attribs.lastModifiedTime().toMillis()));
            }
        } else {
            world = Json.parse(content, UpdatedCosmicWorld.class);
        }

        world.setWorldDir(worldDir);

        this.publish(world);
    }

    @Override
    protected void process(List<CosmicWorld> chunks) {
        this.tableModel.getWorlds().addAll(chunks);
    }

    @Override
    protected void done() {
        this.tableModel.fireTableDataChanged();
        SwingUtils.setJTableColumnsWidth(this.table, 45, 25, 15, 15);
    }
}
