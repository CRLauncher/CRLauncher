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

import com.formdev.flatlaf.FlatClientProperties;
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

import java.util.List;
import java.util.concurrent.ExecutionException;

public class SearchCrmmDataModsView extends SearchCrmmModsView {
    public SearchCrmmDataModsView(Instance instance, ModsTab modsTab) {
        super(instance, modsTab);

        this.getSearchField().putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search data mods...");
    }

    @Override
    public void searchMods() {
        new Worker<List<Datapack>, Void>("searching data mods") {
            @Override
            protected List<Datapack> work() {
                CrmmApi crmmApi = CRLauncher.getInstance().getCrmmApi();
                SearchDatapacksResponse searchDatapacksResponse = crmmApi.searchDataMods(SearchCrmmDataModsView.this.getSearchField().getText());

                return searchDatapacksResponse.getDatapacks();
            }

            @Override
            protected void done() {
                SearchCrmmDataModsView.this.getModCardsPanel().removeAll();

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
                        new ModVersionsDialog(modInfo, SearchCrmmDataModsView.this.getInstance(), SearchCrmmDataModsView.this.getModsTab(),
                            (versionsView, version) -> {
                            return new DataModDownloadWorkerSupplier(
                                SearchCrmmDataModsView.this.getInstance(), SearchCrmmDataModsView.this.getModsTab()
                            ).getWorker(versionsView, version);
                            });
                    }));
                    SearchCrmmDataModsView.this.getModCardsPanel().add(card);
                }

                SearchCrmmDataModsView.this.getModCardsPanel().revalidate();
            }
        }.execute();
    }
}
