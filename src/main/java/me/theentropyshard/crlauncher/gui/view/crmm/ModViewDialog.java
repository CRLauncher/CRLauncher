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

package me.theentropyshard.crlauncher.gui.view.crmm;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.crmm.ModInfo;
import me.theentropyshard.crlauncher.gui.dialogs.AppDialog;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTab;
import me.theentropyshard.crlauncher.gui.view.crmm.modview.CrmmModView;
import me.theentropyshard.crlauncher.instance.CosmicInstance;

import java.awt.*;

public class ModViewDialog extends AppDialog {
    public ModViewDialog(ModInfo modInfo, CosmicInstance instance, ModsTab modsTab, WorkerSupplier<?, Void> supplier) {
        super(CRLauncher.frame,
            CRLauncher.getInstance().getLanguage().getString("gui.searchCRMMModsDialog.modVersionsDialogTitle") +
                " - " + modInfo.getName());

        //ModVersionsView view = new ModVersionsView(modInfo, instance, modsTab, supplier);
        CrmmModView view = new CrmmModView(modInfo);
        view.setPreferredSize(new Dimension((int) (900 * 1.2), (int) (480 * 1.2)));
        //view.loadVersions();

        this.getDialog().getRootPane().setDefaultButton(view.getHeader().getDownloadButton());

        this.setContent(view);
        this.center(0);

        this.setVisible(true);
    }
}
