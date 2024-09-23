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
import me.theentropyshard.crlauncher.Language;
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

        Language language = CRLauncher.getInstance().getLanguage();
        this.getSearchField().putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
            language.getString("gui.searchCRMMModsDialog.searchDatamods"));
    }

    @Override
    public void searchMods() {
        new DataModsSearchWorker(
            this.getSearchField(), this.getModCardsPanel(), this.getInstance(), this.getModsTab(),
            DataModType.DATAMOD
        ).execute();
    }
}
