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

package me.theentropyshard.crlauncher.gui.utils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CardLayoutPanel extends JPanel {
    private final CardLayout layout;
    private final Map<String, Component> componentMap;

    private String showingName;
    private Component showingComponent;

    public CardLayoutPanel() {
        this(0, 0);
    }

    public CardLayoutPanel(int hgap, int vgap) {
        super(new CardLayout(hgap, vgap));

        this.layout = (CardLayout) this.getLayout();
        this.componentMap = new HashMap<>();
    }

    public void showComponent(String name) {
        if (name == null) {
            return;
        }

        Component c = this.componentMap.get(name);
        if (c == null) {
            return;
        }

        if (c == this.showingComponent) {
            return;
        }

        this.showingName = name;
        this.showingComponent = c;

        this.layout.show(this, name);
    }

    public void addComponent(Component c, String name) {
        this.add(c, name);
        this.componentMap.put(name, c);
    }

    public String getShowingName() {
        return this.showingName;
    }

    public Component getShowingComponent() {
        return this.showingComponent;
    }
}