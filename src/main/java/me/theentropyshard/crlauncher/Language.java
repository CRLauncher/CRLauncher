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

package me.theentropyshard.crlauncher;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.json.Json;

public class Language {
    private final JsonObject languageObject;
    private final String name;
    private final String displayName;

    public Language(String json) {
        this.languageObject = Json.parse(json, JsonObject.class);
        this.name = this.languageObject.get("metaInfo").getAsJsonObject().get("name").getAsString();
        this.displayName = this.languageObject.get("metaInfo").getAsJsonObject().get("displayName").getAsString();
    }

    public String getString(String key) {
        String[] parts = key.split("\\.");

        JsonElement stringElement = this.languageObject.get(parts[0]);

        for (int i = 1; i < parts.length && stringElement != null; i++) {
            stringElement = stringElement.getAsJsonObject().get(parts[i]);
        }

        if (stringElement == null) {
            Log.warn("Cannot find translation for " + key);

            return key;
        }

        return stringElement.getAsString();
    }

    @Override
    public String toString() {
        return this.displayName;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
