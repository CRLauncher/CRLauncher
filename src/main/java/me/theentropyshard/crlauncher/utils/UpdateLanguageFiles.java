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

package me.theentropyshard.crlauncher.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.theentropyshard.crlauncher.utils.json.Json;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UpdateLanguageFiles {
    public static void main(String[] args) throws IOException {
        Gson gson = new Gson();

        Path langDir = Paths.get("src", "main", "resources", "lang");

        for (Path file : FileUtils.list(langDir)) {
            JsonObject englishObject = gson.fromJson(FileUtils.readUtf8(langDir.resolve("en_US.json")), JsonObject.class);

            String fileName = file.getFileName().toString();

            if (fileName.equals("en_US.json") || fileName.equals("ru_RU.json") || !fileName.endsWith(".json")) {
                continue;
            }

            UpdateLanguageFiles.copyJsonValues(gson.fromJson(FileUtils.readUtf8(file), JsonObject.class), englishObject);

            FileUtils.writeUtf8(file, Json.writePretty(englishObject));
        }
    }

    private static void copyJsonValues(JsonObject source, JsonObject target) {
        for (String key : target.keySet()) {
            if (source.has(key)) {
                JsonElement sourceValue = source.get(key);

                if (sourceValue.isJsonObject()) {
                    UpdateLanguageFiles.copyJsonValues(sourceValue.getAsJsonObject(), target.getAsJsonObject(key));
                } else {
                    target.add(key, sourceValue);
                }
            }
        }
    }
}
