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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.java;

import me.theentropyshard.crlauncher.cosmic.mods.Mod;
import me.theentropyshard.crlauncher.cosmic.mods.ModLoader;
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.QuiltMod;
import me.theentropyshard.crlauncher.cosmic.mods.fabric.FabricMod;
import me.theentropyshard.crlauncher.cosmic.mods.puzzle.PuzzleMod;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModInstaller;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ListUtils;
import me.theentropyshard.crlauncher.utils.StreamUtils;
import me.theentropyshard.crlauncher.utils.json.Json;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class JavaModsLoader extends Worker<Void, Mod> {
    private final Path modsDir;
    private final Path disabledModsDir;
    private final List<Mod> mods;
    private final ModLoader loader;
    private final JavaModsTableModel tableModel;

    public JavaModsLoader(Instance instance, JavaModsTableModel tableModel) {
        super("loading " + instance.getModLoader().getName() + " mods");

        this.modsDir = instance.getCurrentModsDir();
        this.disabledModsDir = instance.getCurrentDisabledModsDir();
        this.mods = instance.getCurrentMods();
        this.loader = instance.getModLoader();
        this.tableModel = tableModel;
    }

    @Override
    protected Void work() throws Exception {
        this.loadActiveMods();
        this.loadInactiveMods();

        return null;
    }

    private void loadActiveMods() throws IOException {
        this.loadMods(this.modsDir, true);
    }

    private void loadInactiveMods() throws IOException {
        this.loadMods(this.disabledModsDir, false);
    }

    private void loadMods(Path modsDir, boolean active) throws IOException {
        FileUtils.createDirectoryIfNotExists(modsDir);

        for (Path modFile : FileUtils.list(modsDir)) {
            if (!Files.isRegularFile(modFile)) {
                continue;
            }

            this.loadMod(modFile, active);
        }
    }

    private void loadMod(Path modFile, boolean active) throws IOException {
        String fileName = modFile.getFileName().toString();

        try (ZipFile file = new ZipFile(modFile.toFile())) {
            String modInfoFile = ModInstaller.getModInfoFile(this.loader);
            FileHeader fileHeader = file.getFileHeader(modInfoFile);

            if (fileHeader == null) {
                Log.warn(modFile + " does not contain '" + modInfoFile + "'");
                this.loadInvalidMod(fileName, active);
            } else {
                String json = StreamUtils.readToString(file.getInputStream(fileHeader));
                this.loadValidMod(json, fileName, active);
            }
        }
    }

    private void loadInvalidMod(String fileName, boolean active) {
        Mod mod = new Mod(UUID.randomUUID().toString(), fileName, "<unknown>", "<unknown>");
        mod.setActive(active);
        mod.setFileName(fileName);

        if (ListUtils.search(this.mods, m -> m.getId().equals(mod.getId())) == null) {
            this.mods.add(mod);
        } else {
            return;
        }

        this.publish(mod);
    }

    private void loadValidMod(String json, String fileName, boolean active) {
        Mod mod = this.getModFromJson(json);

        if (mod == null) {
            return;
        }

        mod.setFileName(fileName);
        mod.setActive(active);

        if (ListUtils.search(this.mods, m -> m.getId().equals(mod.getId())) == null) {
            this.mods.add(mod);
        } else {
            return;
        }

        this.publish(mod);
    }

    private Mod getModFromJson(String json) {
        return switch (this.loader) {
            case VANILLA -> null;
            case FABRIC -> Json.parse(json, FabricMod.class).toMod();
            case QUILT -> Json.parse(json, QuiltMod.class).toMod();
            case PUZZLE -> Json.parse(json, PuzzleMod.class).toMod();
        };
    }

    @Override
    protected void process(List<Mod> chunks) {
        for (Mod mod : chunks) {
            this.tableModel.addMod(mod);
        }
    }
}
