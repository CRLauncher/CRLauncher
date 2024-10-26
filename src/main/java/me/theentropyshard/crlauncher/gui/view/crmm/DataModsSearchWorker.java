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

package me.theentropyshard.crlauncher.gui.view.crmm;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.crmm.CrmmApi;
import me.theentropyshard.crlauncher.crmm.ModInfo;
import me.theentropyshard.crlauncher.crmm.model.datapack.Datapack;
import me.theentropyshard.crlauncher.crmm.model.datapack.SearchDatapacksResponse;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTab;
import me.theentropyshard.crlauncher.gui.utils.MouseClickListener;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DataModsSearchWorker extends Worker<List<Datapack>, Void> {
    private final String query;
    private final JPanel modCardsPanel;
    private final Instance instance;
    private final ModsTab modsTab;
    private final DataModType modType;

    public DataModsSearchWorker(String query, JPanel modCardsPanel, Instance instance, ModsTab modsTab, DataModType modType) {
        super("searching data mods");

        this.query = query;
        this.modCardsPanel = modCardsPanel;
        this.instance = instance;
        this.modsTab = modsTab;
        this.modType = modType;
    }

    @Override
    protected List<Datapack> work() {
        CrmmApi crmmApi = CRLauncher.getInstance().getCrmmApi();

        SearchDatapacksResponse searchDatapacksResponse = switch (this.modType) {
            case DATAMOD -> crmmApi.searchDataMods(this.query);
            case RESOURCE_PACK -> crmmApi.searchResourcePacks(this.query);
            case SHADER -> crmmApi.searchShaders(this.query);
            case MODPACK -> crmmApi.searchModpacks(this.query);
        };

        return searchDatapacksResponse.getDatapacks();
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected void done() {
        this.modCardsPanel.removeAll();

        List<Datapack> datapacks = null;
        try {
            datapacks = this.get();
        } catch (InterruptedException | ExecutionException ex) {
            Log.error(ex);
        }

        if (datapacks == null) {
            return;
        }

        for (Datapack datapack : datapacks) {
            ModInfo modInfo = datapack.toModInfo();
            ModCard card = new ModCard(modInfo);
            card.addMouseListener(new MouseClickListener(e -> {
                new ModVersionsDialog(modInfo, this.instance, this.modsTab,
                    new DataModDownloadWorkerSupplier(
                    )
                );
            }));
            this.modCardsPanel.add(card);
        }

        this.modCardsPanel.revalidate();
    }
}
