package me.theentropyshard.crlauncher.gui.dialogs.addaccount;

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.account.AccountManager;
import me.theentropyshard.crlauncher.cosmic.account.ItchIoAccount;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.view.accountsview.AccountItem;
import me.theentropyshard.crlauncher.gui.view.accountsview.AccountsView;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;

public class ItchIoAccountCreationView extends JPanel {
    private final JTextField usernameField;

    public ItchIoAccountCreationView(AddAccountDialog dialog, AccountsView accountsView) {
        this.usernameField = new JTextField();
        this.usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter Itch.io API key");
        this.usernameField.setPreferredSize(new Dimension(250, 26));

        this.add(this.usernameField);

        JButton button = new JButton("Add");
        button.addActionListener(e -> {
            String text = this.usernameField.getText();
            if (text.isEmpty()) {
                MessageBox.showErrorMessage(CRLauncher.frame, "API key cannot be empty");

                return;
            }

            new SwingWorker<ItchIoAccount, Void>() {
                @Override
                protected ItchIoAccount doInBackground() {
                    try {
                        ItchIoAccount account = new ItchIoAccount(text);

                        account.authenticate();

                        return account;
                    } catch (Exception e) {
                        MessageBox.showErrorMessage(CRLauncher.frame, "Unexpected error: " + e.getMessage());
                        Log.error(e);
                    }

                    return null;
                }

                @Override
                protected void done() {
                    ItchIoAccount account = null;
                    try {
                        account = this.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        MessageBox.showErrorMessage(CRLauncher.frame, "Unexpected error: " + ex.getMessage());
                        Log.error(ex);
                    }

                    if (account == null) {
                        return;
                    }

                    AccountManager accountManager = CRLauncher.getInstance().getAccountManager();
                    if (!accountManager.canCreateAccount(account.getUsername())) {
                        MessageBox.showErrorMessage(CRLauncher.frame, "Account with name '" + account.getUsername() + "' already exists");

                        return;
                    }

                    accountManager.saveAccount(account);
                    accountsView.addAccountItem(new AccountItem(account));
                }
            }.execute();

            dialog.getDialog().dispose();
        });
        this.add(button);
    }
}
