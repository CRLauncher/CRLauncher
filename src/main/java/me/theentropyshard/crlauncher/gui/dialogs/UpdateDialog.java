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

package me.theentropyshard.crlauncher.gui.dialogs;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.github.GithubRelease;
import me.theentropyshard.crlauncher.gui.BrowseHyperlinkListener;
import me.theentropyshard.crlauncher.gui.FlatSmoothScrollPaneUI;
import me.theentropyshard.crlauncher.utils.OperatingSystem;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class UpdateDialog extends AppDialog {
    private boolean update;

    public UpdateDialog(GithubRelease release) {
        super(CRLauncher.frame, CRLauncher.getInstance().getLanguage().getString("gui.updateDialog.title"));

        JPanel root = new JPanel(new BorderLayout());
        root.setPreferredSize(new Dimension((int) (960 * 0.85), (int) (540 * 0.85)));

        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");

        ActionMap actionMap = root.getActionMap();
        actionMap.put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UpdateDialog.this.getDialog().dispose();
            }
        });

        Language language = CRLauncher.getInstance().getLanguage();

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JTextPane releaseNamePane = new JTextPane();
        releaseNamePane.addHyperlinkListener(new BrowseHyperlinkListener());
        releaseNamePane.setContentType("text/html");
        releaseNamePane.setEditorKit(new HTMLEditorKit());
        releaseNamePane.setFont(releaseNamePane.getFont().deriveFont(24.0f));
        releaseNamePane.setText(release.name + " - <a style=\"font-size:20\" href=" + release.html_url + "\">" + release.html_url + "</a>");
        releaseNamePane.setEditable(false);
        topPanel.add(releaseNamePane);
        root.add(topPanel, BorderLayout.NORTH);

        Parser parser = Parser.builder().build();
        Node document = parser.parse(release.body);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);

        JTextPane changelogPane = new JTextPane();
        changelogPane.addHyperlinkListener(new BrowseHyperlinkListener());
        changelogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
        changelogPane.setFont(changelogPane.getFont().deriveFont(14.0f));
        changelogPane.setContentType("text/html");
        changelogPane.setEditorKit(new HTMLEditorKit());
        changelogPane.setText(html);
        changelogPane.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(
            changelogPane
        );
        scrollPane.setUI(new FlatSmoothScrollPaneUI());
        root.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
            UIManager.getColor("Component.borderColor")));

        JButton updateNowButton = new JButton(language.getString("gui.updateDialog.updateNowButton"));
        updateNowButton.addActionListener(e -> {
            this.update = true;
            this.getDialog().dispose();
        });
        bottomPanel.add(updateNowButton);

        JButton updateLaterButton = new JButton(language.getString("gui.updateDialog.updateLaterButton"));
        updateLaterButton.addActionListener(e -> this.getDialog().dispose());
        bottomPanel.add(updateLaterButton);

        root.add(bottomPanel, BorderLayout.SOUTH);

        this.getDialog().getRootPane().setDefaultButton(updateNowButton);

        this.setContent(root);
        this.center(0);
        this.setVisible(true);
    }

    public static boolean show(GithubRelease release) {
        return new UpdateDialog(release).isUpdate();
    }

    public boolean isUpdate() {
        return this.update;
    }
}
