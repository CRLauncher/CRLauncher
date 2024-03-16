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

package me.theentropyshard.crlauncher.gui.playview;

import me.theentropyshard.crlauncher.gui.View;

import javax.swing.*;
import java.awt.*;

public class PlayViewHeader extends View {
    private final JComboBox<String> instanceGroups;

    public static PlayViewHeader instance;

    public PlayViewHeader() {
        PlayViewHeader.instance = this;
        JPanel root = this.getRoot();

        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel instanceLabel = new JLabel("Instance group:");
        leftSide.add(instanceLabel);

        this.instanceGroups = new JComboBox<>();
        leftSide.add(this.instanceGroups);

        root.add(leftSide, BorderLayout.WEST);
    }

    public JComboBox<String> getInstanceGroups() {
        return this.instanceGroups;
    }
}
