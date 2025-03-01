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

import me.theentropyshard.crlauncher.crmm.model.project.Project;
import me.theentropyshard.crlauncher.gui.view.crmm.modview.side.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class CrmmModSideView extends JPanel {
    public CrmmModSideView(Project project) {
        this.setLayout(new MigLayout("fill, insets 0, gap 10 10", "[fill]", "[top][top][top][top][top]"));

        this.add(new CompatibilityCard(project), "grow, wrap");

        if (project.hasLinks()) {
            this.add(new LinksCard(project), "grow, wrap");
        }

        this.add(new FeaturedVersionsCard(project), "grow, wrap");
        this.add(new CreatorsCard(project), "grow, wrap");
        this.add(new DetailsCard(project), "grow");
    }
}
