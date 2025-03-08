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

package me.theentropyshard.crlauncher.gui.view.crmm.modview.gallery;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import me.theentropyshard.crlauncher.gui.components.Card;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GalleryImageItem extends Card {
    public GalleryImageItem(GalleryImageInfo info) {
        this.setLayout(new MigLayout("wrap, flowy", "[center]", "[center][bottom]"));
        this.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel imageLabel = new JLabel(new ImageIcon(info.getThumbnail()));
        this.add(imageLabel);

        JLabel textLabel = new JLabel(info.getTitle());
        String path = "/assets/images/star_" + (info.isFeatured() ? "full" : "outline") + ".svg";
        textLabel.setIcon(new FlatSVGIcon(GalleryImageItem.class.getResource(path)));
        textLabel.setHorizontalTextPosition(JLabel.LEADING);
        this.add(textLabel);
    }
}
