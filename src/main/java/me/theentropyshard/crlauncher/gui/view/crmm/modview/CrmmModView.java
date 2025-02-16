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

import me.theentropyshard.crlauncher.crmm.model.project.Project;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CrmmModView extends JPanel {
    private final CrmmModViewHeader header;
    private final CrmmModSideView sideView;
    private final CrmmModInfoView infoView;

    public CrmmModView(Project project) {
        super(new BorderLayout());

        this.setBorder(new EmptyBorder(10, 10, 10, 10));

        this.header = new CrmmModViewHeader(project);
        this.add(this.header, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new MigLayout("fill, insets 0, gap 10 10", "[25%][75%]", "[top]"));
        centerPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        this.sideView = new CrmmModSideView(project);
        centerPanel.add(this.sideView, "growx");

        this.infoView = new CrmmModInfoView();
        centerPanel.add(this.infoView, "growx");

        this.add(centerPanel, BorderLayout.CENTER);
    }

    public CrmmModViewHeader getHeader() {
        return this.header;
    }
}
