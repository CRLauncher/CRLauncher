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
import me.theentropyshard.crlauncher.crmm.model.project.Project;
import me.theentropyshard.crlauncher.gui.BrowseHyperlinkListener;
import me.theentropyshard.crlauncher.gui.components.Card;
import me.theentropyshard.crlauncher.utils.ResourceUtils;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
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
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModDescriptionCard extends Card {
    private final JTextPane changelogPane;
    private String html;

    public ModDescriptionCard(Project project) {
        this.setLayout(new BorderLayout());

        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "rss");

        ActionMap actionMap = this.getActionMap();
        actionMap.put("rss", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ModDescriptionCard.this.updateStyleSheet(ModDescriptionCard.this.html);
            }
        });

        List<Extension> extensions = new ArrayList<>();
        extensions.add(TablesExtension.create());
        extensions.add(ImageAttributesExtension.create());

        Parser parser = Parser.builder().extensions(extensions).build();
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
        this.html = renderer.render(parser.parse(project.getDescription()));

        Document htmlDocument = Jsoup.parse(this.html);
        Elements imageElements = Collector.collect(new Evaluator.Tag("img"), htmlDocument);
        for (Element img : imageElements) {
            Attributes attributes = img.attributes();
            attributes.add("width", "600");
        }

        this.html = htmlDocument.toString();

        this.changelogPane = new JTextPane();
        this.changelogPane.setBorder(new EmptyBorder(-15, 0, 0, 0));
        this.changelogPane.addHyperlinkListener(new BrowseHyperlinkListener());
        this.changelogPane.setContentType("text/html");
        this.updateStyleSheet(this.html);
        this.changelogPane.setEditable(false);
        this.changelogPane.setBackground(this.getDefaultColor());

        this.add(this.changelogPane, BorderLayout.CENTER);
    }

    private void updateStyleSheet(String html) {
        HTMLEditorKit kit = new HTMLEditorKit() {
            @Override
            public javax.swing.text.Document createDefaultDocument() {
                StyleSheet styleSheet = new StyleSheet();

                try {
                    String file = CRLauncher.getInstance().getSettings().darkTheme ? "mod-view-dark.css" : "mod-view-light.css";

                    List<String> rules = CssParser.parse(ResourceUtils.readToString("/themes/" + file));

                    rules.forEach(styleSheet::addRule);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                HTMLDocument doc = new HTMLDocument(styleSheet);
                doc.setParser(this.getParser());
                doc.setAsynchronousLoadPriority(4);
                doc.setTokenThreshold(100);

                return doc;
            }
        };
        this.changelogPane.setEditorKit(kit);
        this.changelogPane.setDocument(kit.createDefaultDocument());
        this.changelogPane.setText("<html><body><div style='width:750px'>" + html + "</body></html>");
        this.revalidate();
    }
}
