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

package me.theentropyshard.crlauncher.cosmic.account.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.theentropyshard.crlauncher.cosmic.account.Account;
import me.theentropyshard.crlauncher.cosmic.account.ItchIoAccount;
import me.theentropyshard.crlauncher.cosmic.account.OfflineAccount;

import java.lang.reflect.Type;

public class AccountDeserializer implements JsonDeserializer<Account> {
    public AccountDeserializer() {

    }

    @Override
    public Account deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) {
        JsonObject root = element.getAsJsonObject();

        if (root.has("itchIoApiKey")) {
            return ctx.deserialize(root, ItchIoAccount.class);
        } else {
            return ctx.deserialize(root, OfflineAccount.class);
        }
    }
}
