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

package me.theentropyshard.crlauncher.gui.dialogs.addaccount;

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.account.Account;
import me.theentropyshard.crlauncher.cosmic.account.AccountManager;
import me.theentropyshard.crlauncher.cosmic.account.OfflineAccount;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.view.accountsview.AccountItem;
import me.theentropyshard.crlauncher.gui.view.accountsview.AccountsView;

import javax.swing.*;
import java.awt.*;

public class OfflineAccountCreationView extends JPanel {
    private final JTextField usernameField;

    public OfflineAccountCreationView(AddAccountDialog dialog, AccountsView accountsView) {
        this.usernameField = new JTextField();
        this.usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter desired username");
        this.usernameField.setPreferredSize(new Dimension(250, 26));

        this.add(this.usernameField);

        JButton button = new JButton("Add");
        button.addActionListener(e -> {
            String text = this.usernameField.getText();
            if (text.isEmpty()) {
                MessageBox.showErrorMessage(CRLauncher.frame, "Enter a username");

                return;
            }

            AccountManager accountManager = CRLauncher.getInstance().getAccountManager();

            Account account = new OfflineAccount(text);
            if (!accountManager.canCreateAccount(account.getUsername())) {
                MessageBox.showErrorMessage(CRLauncher.frame, "Account with username '" + account.getUsername() + "' already exists");

                return;
            }

            accountManager.saveAccount(account);
            accountsView.addAccountItem(new AccountItem(account));

            dialog.getDialog().dispose();
        });
        this.add(button);
    }
}
