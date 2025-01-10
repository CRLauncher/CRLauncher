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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.data;

import me.theentropyshard.crlauncher.cosmic.mods.Mod;
import me.theentropyshard.crlauncher.cosmic.mods.ModLoader;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.CosmicInstance;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.ListUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DataModsLoader extends Worker<Void, Mod> {
    private final Path dataModsDir;
    private final Path disabledDataModsDir;
    private final List<Mod> dataMods;
    private final CosmicInstance instance;
    private final DataModsTableModel tableModel;

    public DataModsLoader(CosmicInstance instance, DataModsTableModel tableModel) {
        super("loading data mods");

        this.dataModsDir = instance.getDataModsDir();
        this.disabledDataModsDir = instance.getDisabledDataModsDir();
        this.dataMods = instance.getDataMods();
        this.instance = instance;
        this.tableModel = tableModel;
    }

    @Override
    protected Void work() throws Exception {
        this.removeNonExistentMods();
        this.loadActiveMods();
        this.loadInactiveMods();

        return null;
    }

    private void removeNonExistentMods() {
        this.dataMods.removeIf(mod -> !Files.exists(this.instance.getModPath(mod, ModLoader.VANILLA)));
    }

    private void loadActiveMods() throws IOException {
        this.loadMods(this.dataModsDir, true);
    }

    private void loadInactiveMods() throws IOException {
        this.loadMods(this.disabledDataModsDir, false);
    }

    private void loadMods(Path modsDir, boolean active) throws IOException {
        FileUtils.createDirectoryIfNotExists(modsDir);

        for (Path dataModDir : FileUtils.list(modsDir)) {
            if (!Files.isDirectory(dataModDir)) {
                continue;
            }

            this.loadMod(dataModDir.getFileName().toString(), active);
        }
    }

    private void loadMod(String dirName, boolean active) {
        Mod mod = new Mod();
        mod.setActive(active);
        mod.setName(dirName);
        mod.setFileName(dirName);

        if (ListUtils.search(this.dataMods, m -> m.getName().equals(mod.getName())) == null) {
            this.dataMods.add(mod);
        } else {
            return;
        }

        this.publish(mod);
    }

    @Override
    protected void process(List<Mod> chunks) {
        for (Mod dataMod : chunks) {
            this.tableModel.addMod(dataMod);
        }
    }
}
