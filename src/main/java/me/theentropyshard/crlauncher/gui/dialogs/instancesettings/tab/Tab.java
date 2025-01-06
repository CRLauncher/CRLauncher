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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab;

import me.theentropyshard.crlauncher.instance.CosmicInstance;

import javax.swing.*;

public abstract class Tab {
    private final JDialog dialog;
    private final String name;
    private final CosmicInstance instance;
    private final JPanel root;

    public Tab(String name, CosmicInstance instance, JDialog dialog) {
        this.name = name;
        this.instance = instance;
        this.dialog = dialog;
        this.root = new JPanel();
    }

    public void shown() {

    }

    public void hidden() {

    }

    public String getName() {
        return this.name;
    }

    public CosmicInstance getInstance() {
        return this.instance;
    }

    public JDialog getDialog() {
        return this.dialog;
    }

    public JPanel getRoot() {
        return this.root;
    }
}
