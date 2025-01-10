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

package me.theentropyshard.crlauncher.gui.view.crmm.navbar;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashSet;
import java.util.Set;

public class NavBar extends JPanel {
    private final Set<TabListener> tabListeners;
    private final ButtonGroup buttonGroup;

    private int tabCounter;

    public NavBar() {
        super(new GridLayout(1, 5, 3, 3));

        this.tabListeners = new LinkedHashSet<>();
        this.buttonGroup = new ButtonGroup();
    }

    public void addItem(String text) {
        NavBarButton button = new NavBarButton(text, this.tabCounter);
        button.addActionListener(e -> {
            for (TabListener listener : this.tabListeners) {
                listener.onTabSelected(button.getId());
            }
        });
        if (this.buttonGroup.getButtonCount() == 0) {
            button.setSelected(true);
        }
        this.buttonGroup.add(button);
        this.add(button);
        this.tabCounter++;
    }

    public void addTabListener(TabListener listener) {
        this.tabListeners.add(listener);
    }

    public interface TabListener {
        void onTabSelected(int tab);
    }
}
