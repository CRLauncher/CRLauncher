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

package me.theentropyshard.crlauncher.gui.view.crmm.modview;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GameVersionLabel extends JLabel {
    public GameVersionLabel(String text) {
        super(text);

        this.setBackground(UIManager.getColor("InstanceItem.pressedColor"));
        this.setBorder(new EmptyBorder(1, 5, 1, 5));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(this.getBackground());
        g2d.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), this.getHeight(), this.getHeight());

        super.paintComponent(g);
    }
}
