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

package me.theentropyshard.crlauncher.cosmic.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import me.theentropyshard.crlauncher.cosmic.version.VersionType;

public class VersionTypeTypeAdapter extends TypeAdapter<VersionType> {
    public VersionTypeTypeAdapter() {

    }

    @Override
    public void write(JsonWriter writer, VersionType type) throws IOException {
        writer.value(type.getJsonName());
    }

    @Override
    public VersionType read(JsonReader reader) throws IOException {
        return VersionType.getByJsonName(reader.nextString());
    }
}
