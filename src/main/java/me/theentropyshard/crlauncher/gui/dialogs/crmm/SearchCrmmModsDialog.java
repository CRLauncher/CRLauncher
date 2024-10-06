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

package me.theentropyshard.crlauncher.gui.dialogs.crmm;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.gui.dialogs.AppDialog;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.mods.ModsTab;
import me.theentropyshard.crlauncher.gui.view.crmm.CrmmModsView;
import me.theentropyshard.crlauncher.instance.Instance;

import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SearchCrmmModsDialog extends AppDialog {
    public SearchCrmmModsDialog(Instance instance, ModsTab modsTab) {
        super(CRLauncher.frame, CRLauncher.getInstance().getLanguage()
            .getString("gui.searchCRMMModsDialog.title"));

        CrmmModsView modsView = new CrmmModsView(instance, modsTab);
        modsView.setPreferredSize(new Dimension((int) (900 * 1.2), (int) (480 * 1.2)));
        modsView.setBorder(new EmptyBorder(10, 10, 10, 10));

        this.setContent(modsView);
        this.center(0);
        this.setVisible(true);
    }
}
