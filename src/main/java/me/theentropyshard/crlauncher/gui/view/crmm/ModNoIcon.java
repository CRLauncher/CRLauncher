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

package me.theentropyshard.crlauncher.gui.view.crmm;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import me.theentropyshard.crlauncher.CRLauncher;

import javax.swing.*;
import java.awt.*;

public class ModNoIcon {
    public static final FlatSVGIcon INSTANCE = new FlatSVGIcon(
        ModNoIcon.class.getResource("/assets/images/mod_default_icon.svg")
    ).derive(64, 64);

    public static final Icon INSTANCE_DARK = new FlatSVGIcon(
        ModNoIcon.INSTANCE
    ).setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.BLACK));

    public static Icon getInstance() {
        return CRLauncher.getInstance().getGui().isDarkTheme() ? ModNoIcon.INSTANCE : ModNoIcon.INSTANCE_DARK;
    }
}
