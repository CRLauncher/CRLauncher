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

package me.theentropyshard.crlauncher.utils.json;

import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import me.theentropyshard.crlauncher.cosmic.account.Account;
import me.theentropyshard.crlauncher.cosmic.account.AccountStorage;
import me.theentropyshard.crlauncher.cosmic.account.gson.AccountDeserializer;
import me.theentropyshard.crlauncher.cosmic.account.gson.AccountStorageDeserializer;
import me.theentropyshard.crlauncher.cosmic.gson.VersionPhaseTypeAdapter;
import me.theentropyshard.crlauncher.cosmic.gson.VersionTypeTypeAdapter;
import me.theentropyshard.crlauncher.cosmic.version.VersionPhase;
import me.theentropyshard.crlauncher.cosmic.mods.ModLoader;
import me.theentropyshard.crlauncher.cosmic.version.VersionType;
import me.theentropyshard.crlauncher.utils.json.type.InstanceTypeTypeAdapter;
import me.theentropyshard.crlauncher.utils.json.type.LocalDateTimeTypeAdapter;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

public final class Json {
    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .disableJdkUnsafe()
        //
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
        .registerTypeAdapter(VersionPhase.class, new VersionPhaseTypeAdapter())
        .registerTypeAdapter(VersionType.class, new VersionTypeTypeAdapter())
        .registerTypeAdapter(ModLoader.class, new InstanceTypeTypeAdapter())
        .registerTypeAdapter(AccountStorage.class, new AccountStorageDeserializer())
        .registerTypeAdapter(Account.class, new AccountDeserializer())
        //
        .create();

    private static final Gson PRETTY_GSON = Json.GSON.newBuilder()
        .setFormattingStyle(FormattingStyle.PRETTY.withIndent(" ".repeat(4)))
        .create();

    public static <T> T parse(String json, Type clazz) {
        return Json.GSON.fromJson(json, clazz);
    }

    public static <T> T parse(String json, Class<T> clazz) {
        return Json.GSON.fromJson(json, clazz);
    }

    public static <T> T parse(JsonElement element, Type clazz) {
        return Json.GSON.fromJson(element, clazz);
    }

    public static <T> T parse(JsonElement element, Class<T> clazz) {
        return Json.GSON.fromJson(element, clazz);
    }

    public static String write(Object o) {
        return Json.GSON.toJson(o);
    }

    public static String writePretty(Object o) {
        return Json.PRETTY_GSON.toJson(o);
    }

    private Json() {
        throw new UnsupportedOperationException();
    }
}
