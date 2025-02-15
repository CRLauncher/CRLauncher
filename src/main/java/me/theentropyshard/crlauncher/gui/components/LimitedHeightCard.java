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

package me.theentropyshard.crlauncher.gui.components;

import java.awt.*;

public class LimitedHeightCard extends Card {
    private final int maxHeight;

    public LimitedHeightCard(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public LimitedHeightCard(int maxHeight, int border, int arc, Color defaultColor, Color hoveredColor, Color pressedColor) {
        super(border, arc, defaultColor, hoveredColor, pressedColor);

        this.maxHeight = maxHeight;
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension maximumSize = super.getMaximumSize();

        return new Dimension(maximumSize.width, this.maxHeight);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize();

        return new Dimension(preferredSize.width, this.maxHeight);
    }
}
