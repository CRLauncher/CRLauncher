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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GsonUtils {
    public static int countStringKeys(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            int count = 0;

            // Iterate through the keys in the JSON object
            for (String key : jsonObject.keySet()) {
                JsonElement value = jsonObject.get(key);

                // Check if the value is a string
                if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                    count++; // Count the key if its value is a string
                }

                // Recursively count keys in nested objects
                count += GsonUtils.countStringKeys(value);
            }
            return count;
        }
        // If it's not a JSON object, return 0
        return 0;
    }
}
