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
import me.theentropyshard.crlauncher.gui.BrowseHyperlinkListener;
import me.theentropyshard.crlauncher.gui.components.Card;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;

public class ModDescriptionCard extends Card {
    public ModDescriptionCard(Project project) {
        this.setLayout(new BorderLayout());

        Parser parser = Parser.builder().build();
        Node document = parser.parse(project.getDescription());
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);

        JTextPane changelogPane = new JTextPane();
        changelogPane.setBorder(new EmptyBorder(-15, 0, 0, 0));
        changelogPane.addHyperlinkListener(new BrowseHyperlinkListener());
        changelogPane.setFont(changelogPane.getFont().deriveFont(14.0f));
        changelogPane.setContentType("text/html");
        changelogPane.setEditorKit(new HTMLEditorKit());
        changelogPane.setText(html);
        changelogPane.setEditable(false);
        changelogPane.setBackground(this.getDefaultColor());

        this.add(changelogPane, BorderLayout.CENTER);
    }
}
