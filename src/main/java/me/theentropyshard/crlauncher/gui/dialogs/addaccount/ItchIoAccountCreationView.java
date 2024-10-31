package me.theentropyshard.crlauncher.gui.dialogs.addaccount;

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.cosmic.account.AccountManager;
import me.theentropyshard.crlauncher.cosmic.account.ItchIoAccount;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.gui.view.accountsview.AccountItem;
import me.theentropyshard.crlauncher.gui.view.accountsview.AccountsView;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;

public class ItchIoAccountCreationView extends JPanel {
    public static final String PLACEHOLDER = "gui.addAccountDialog.itch.textFieldPlaceholder";
    public static final String ADD_BUTTON = "gui.addAccountDialog.addButton";
    public static final String KEY_EMPTY_MESSAGE = "messages.gui.addAccountDialog.itch.apiKeyCannotBeEmpty";
    public static final String ACCOUNT_EXISTS = "gui.addAccountDialog.accountExists";
    private final JTextField usernameField;

    public ItchIoAccountCreationView(AddAccountDialog dialog, AccountsView accountsView) {
        Language language = CRLauncher.getInstance().getLanguage();

        this.usernameField = new JTextField();
        this.usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
            language.getString(ItchIoAccountCreationView.PLACEHOLDER));
        this.usernameField.setPreferredSize(new Dimension(250, 26));

        this.add(this.usernameField);

        JButton button = new JButton(language.getString(ItchIoAccountCreationView.ADD_BUTTON));
        button.addActionListener(e -> {
            String text = this.usernameField.getText();
            if (text.isEmpty()) {
                MessageBox.showErrorMessage(CRLauncher.frame, language.getString(ItchIoAccountCreationView.KEY_EMPTY_MESSAGE));

                return;
            }

            new Worker<ItchIoAccount, Void>("authenticating Itch.io account") {
                @Override
                protected ItchIoAccount work() {
                    ItchIoAccount account = new ItchIoAccount(text);

                    account.authenticate();

                    return account;
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
                        String message = language.getString(ItchIoAccountCreationView.ACCOUNT_EXISTS);
                        MessageBox.showErrorMessage(CRLauncher.frame,
                            message.replace("$$ACCOUNT_NAME$$", account.getUsername()));

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
