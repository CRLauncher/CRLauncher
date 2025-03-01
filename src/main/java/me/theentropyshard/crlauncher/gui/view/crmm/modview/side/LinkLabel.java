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

package me.theentropyshard.crlauncher.gui.view.crmm.modview.side;

import me.theentropyshard.crlauncher.gui.components.MouseListenerBuilder;
import me.theentropyshard.crlauncher.utils.OperatingSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;

public class LinkLabel extends JLabel {
    public LinkLabel(String name, String link) {
        this.setText("<html>" + name + "</html>");

        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseListener listener = new MouseListenerBuilder()
            .mouseEntered(e -> {
                this.setText("<html><u>" + name + "</u></html>");
            })
            .mouseExited(e -> {
                this.setText("<html>" + name + "</html>");
            })
            .mouseClicked(e -> {
                OperatingSystem.browse(link);
            })
            .build();

        this.addMouseListener(listener);
    }
}
