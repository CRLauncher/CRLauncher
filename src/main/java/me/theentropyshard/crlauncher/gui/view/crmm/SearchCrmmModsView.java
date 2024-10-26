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
import me.theentropyshard.crlauncher.crmm.model.mod.CrmmMod;
import me.theentropyshard.crlauncher.crmm.model.mod.SearchModsResponse;
import me.theentropyshard.crlauncher.gui.FlatSmoothScrollPaneUI;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTab;
import me.theentropyshard.crlauncher.gui.utils.MouseClickListener;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SearchCrmmModsView extends JPanel {
    private final JPanel modCardsPanel;
    private final Instance instance;
    private final ModsTab modsTab;

    public SearchCrmmModsView(Instance instance, ModsTab modsTab) {
        super(new BorderLayout());

        this.instance = instance;
        this.modsTab = modsTab;

        this.modCardsPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        this.modCardsPanel.setBorder(new EmptyBorder(0, 3, 0, 10));
        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.add(this.modCardsPanel, BorderLayout.PAGE_START);

        JScrollPane modCardsScrollPane = new JScrollPane(borderPanel);
        modCardsScrollPane.setUI(new FlatSmoothScrollPaneUI());
        modCardsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.add(modCardsScrollPane, BorderLayout.CENTER);
    }

    public void clear() {
        this.modCardsPanel.removeAll();
        this.modCardsPanel.revalidate();
    }

    public void searchMods(String query) {
        this.clear();

        new Worker<List<CrmmMod>, Void>("searching mods") {
            @Override
            protected List<CrmmMod> work() {
                CrmmApi crmmApi = CRLauncher.getInstance().getCrmmApi();
                SearchModsResponse searchModsResponse = crmmApi.searchMods(query);

                return searchModsResponse.getMods();
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                List<CrmmMod> crmmMods = null;
                try {
                    crmmMods = this.get();
                } catch (InterruptedException | ExecutionException ex) {
                    Log.error(ex);
                }

                if (crmmMods == null) {
                    return;
                }

                for (CrmmMod crmmMod : crmmMods) {
                    ModInfo modInfo = crmmMod.toModInfo();
                    ModCard card = new ModCard(modInfo);
                    card.addMouseListener(new MouseClickListener(e -> {
                        new ModVersionsDialog(modInfo, SearchCrmmModsView.this.instance, SearchCrmmModsView.this.modsTab,
                            new ModDownloadWorkerSupplier());
                    }));
                    SearchCrmmModsView.this.modCardsPanel.add(card);
                }

                SearchCrmmModsView.this.modCardsPanel.revalidate();
            }
        }.execute();
    }

    public Instance getInstance() {
        return this.instance;
    }

    public ModsTab getModsTab() {
        return this.modsTab;
    }

    public JPanel getModCardsPanel() {
        return this.modCardsPanel;
    }
}
