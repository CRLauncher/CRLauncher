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

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.gui.components.Card;
import me.theentropyshard.crlauncher.language.LanguageSection;

import javax.swing.*;
import java.awt.*;

public class TabsCard extends Card {
    public TabsCard(Runnable onDescriptionClick, Runnable onGalleryClick, Runnable onChangelogClick, Runnable onVersionsClick) {
        this.setLayout(new FlowLayout(FlowLayout.LEFT));

        LanguageSection section = CRLauncher.getInstance().getLanguage().getSection("gui.searchCRMMModsDialog.modViewDialog.infoView.tabsCard");

        ButtonGroup buttonGroup = new ButtonGroup();

        ModInfoTabButton descriptionButton = new ModInfoTabButton(section.getString("description"), buttonGroup, onDescriptionClick);
        descriptionButton.setSelected(true);
        this.add(descriptionButton);

        ModInfoTabButton galleryButton = new ModInfoTabButton(section.getString("gallery"), buttonGroup, onGalleryClick);
        this.add(galleryButton);

        ModInfoTabButton changelogButton = new ModInfoTabButton(section.getString("changelog"), buttonGroup, onChangelogClick);
        this.add(changelogButton);

        ModInfoTabButton versionsButton = new ModInfoTabButton(section.getString("versions"), buttonGroup, onVersionsClick);
        this.add(versionsButton);
    }
}
