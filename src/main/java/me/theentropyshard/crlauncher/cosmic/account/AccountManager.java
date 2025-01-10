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

package me.theentropyshard.crlauncher.cosmic.account;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.json.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AccountManager {
    private final Path accountsFile;

    private AccountStorage accountStorage;

    public AccountManager(Path workDir) {
        this.accountsFile = workDir.resolve("accounts.json");

        try {
            FileUtils.createFileIfNotExists(this.accountsFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Account getCurrentAccount() {
        return this.accountStorage.getSelectedAccount();
    }

    public boolean canCreateAccount(String username) {
        return !this.accountStorage.accountExists(username);
    }

    public void load() throws IOException {
        if (Files.size(this.accountsFile) == 0L) {
            this.accountStorage = new AccountStorage();

            return;
        }

        this.accountStorage = Json.parse(FileUtils.readUtf8(this.accountsFile), AccountStorage.class);

        if (this.accountStorage == null) {
            throw new IOException("Could not load account storage from '" + this.accountsFile + "'");
        }
    }

    public boolean saveAccount(Account account) {
        if (!this.canCreateAccount(account.getUsername())) {
            return false;
        }

        this.accountStorage.addAccount(account);

        try {
            this.save();

            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void selectAccount(Account account) {
        this.accountStorage.setSelected(account.getUsername());
    }

    public void save() throws IOException {
        Settings settings = CRLauncher.getInstance().getSettings();
        String json = settings.writePrettyJson ? Json.writePretty(this.accountStorage) : Json.write(this.accountStorage);
        FileUtils.writeUtf8(this.accountsFile, json);
    }

    public List<Account> getAccounts() {
        return this.accountStorage.getAccountList();
    }

    public void removeAccount(Account account) throws IOException {
        this.accountStorage.removeAccount(account);

        this.save();
    }

    public AccountStorage getAccountStorage() {
        return this.accountStorage;
    }
}
