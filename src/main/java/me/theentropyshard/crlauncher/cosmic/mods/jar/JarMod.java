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

package me.theentropyshard.crlauncher.cosmic.mods.jar;

import me.theentropyshard.crlauncher.cosmic.mods.Mod;

import java.util.UUID;

public class JarMod implements Mod {
    private boolean active;
    private String fileName;
    private UUID id;
    private String name;

    public JarMod() {

    }

    public JarMod(boolean active, String fileName, UUID id, String name) {
        this.active = active;
        this.fileName = fileName;
        this.id = id;
        this.name = name;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Jar mods don't have version");
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Jar mods don't have description");
    }

    public void setName(String name) {
        this.name = name;
    }
}
