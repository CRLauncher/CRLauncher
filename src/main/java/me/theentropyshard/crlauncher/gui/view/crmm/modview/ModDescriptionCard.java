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
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Collector;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ModDescriptionCard extends Card {
    public ModDescriptionCard(Project project) {
        this.setLayout(new BorderLayout());

        List<Extension> extensions = new ArrayList<>();
        extensions.add(TablesExtension.create());

        Parser parser = Parser.builder().extensions(extensions).build();
        Node document = parser.parse(project.getDescription());
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
        String html = renderer.render(document);

        Document htmlDocument = Jsoup.parse(html);
        Elements imgs = Collector.collect(new Evaluator.Tag("img"), htmlDocument);
        for (Element img : imgs) {
            Attributes attributes = img.attributes();
            attributes.add("style", "max-width: 600;");
            attributes.add("height", "300");
        }
        System.out.println(htmlDocument);
        html = htmlDocument.toString();

        JTextPane changelogPane = new JTextPane();
        changelogPane.setBorder(new EmptyBorder(-15, 0, 0, 0));
        changelogPane.addHyperlinkListener(new BrowseHyperlinkListener());
        changelogPane.setFont(changelogPane.getFont().deriveFont(14.0f));
        changelogPane.setContentType("text/html");
        changelogPane.setEditorKit(new HTMLEditorKit());
        changelogPane.setText("<html><body><div style='width:750px'>" +
            html
            + "</div></body></html>");
        changelogPane.setEditable(false);
        changelogPane.setBackground(this.getDefaultColor());

        this.add(changelogPane, BorderLayout.CENTER);
    }
}
