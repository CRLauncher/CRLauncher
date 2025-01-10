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

package me.theentropyshard.crlauncher.gui.dialogs.addaccount;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.gui.dialogs.AppDialog;
import me.theentropyshard.crlauncher.gui.view.accountsview.AccountsView;

import javax.swing.*;
import java.awt.*;

public class AddAccountDialog extends AppDialog {
    public static final String TITLE = "gui.addAccountDialog.title";
    public static final String OFFLINE_TAB_NAME = "gui.addAccountDialog.offline.tabName";
    public static final String ITCH_TAB_NAME = "gui.addAccountDialog.itch.tabName";
    private final OfflineAccountCreationView offlineView;
    private final ItchIoAccountCreationView itchView;

    public AddAccountDialog(AccountsView accountsView) {
        super(CRLauncher.frame, CRLauncher.getInstance().getLanguage().getString(AddAccountDialog.TITLE));

        JPanel root = new JPanel(new BorderLayout());

        JTabbedPane viewSelector = new JTabbedPane(JTabbedPane.TOP);
        viewSelector.putClientProperty("JTabbedPane.tabAreaAlignment", "fill");

        Language language = CRLauncher.getInstance().getLanguage();

        this.offlineView = new OfflineAccountCreationView(this, accountsView);
        viewSelector.addTab(language.getString(AddAccountDialog.OFFLINE_TAB_NAME), this.offlineView);

        this.itchView = new ItchIoAccountCreationView(this, accountsView);
        viewSelector.addTab(language.getString(AddAccountDialog.ITCH_TAB_NAME), this.itchView);

        root.add(viewSelector, BorderLayout.CENTER);
        //root.setPreferredSize(new Dimension(450, 270));

        this.setResizable(false);
        this.setContent(root);
        this.center(0);
        this.setVisible(true);
    }
}
