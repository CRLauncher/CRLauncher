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

package me.theentropyshard.crlauncher.gui.view.devlog;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.gui.FlatSmoothScrollPaneUI;
import me.theentropyshard.crlauncher.gui.components.MouseListenerBuilder;
import me.theentropyshard.crlauncher.gui.utils.ScrollablePanel;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.network.HttpRequest;
import org.commonmark.Extension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DevlogPostView extends JPanel {
    private final JLabel postTitleLabel;
    private final DevlogPane devlogPane;

    public DevlogPostView(Runnable onCrossClicked) {
        super(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            public void scrollRectToVisible(Rectangle aRect) {

            }
        };

        JPanel topPanel = new JPanel(new BorderLayout());

        this.postTitleLabel = new JLabel();
        this.postTitleLabel.setFont(this.postTitleLabel.getFont().deriveFont(20.0f));

        JPanel topLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topLeftPanel.add(this.postTitleLabel);
        topPanel.add(topLeftPanel, BorderLayout.WEST);

        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        FlatSVGIcon crossIcon = new FlatSVGIcon(DevlogPostView.class.getResource("/assets/images/cross.svg"))
            .derive(16, 16);

        FlatSVGIcon iconDefault = new FlatSVGIcon(crossIcon).setColorFilter(new FlatSVGIcon.ColorFilter(color -> UIManager.getColor("InstanceItem.defaultColor")));
        FlatSVGIcon iconHovered = new FlatSVGIcon(crossIcon).setColorFilter(new FlatSVGIcon.ColorFilter(color -> UIManager.getColor("InstanceItem.hoveredColor")));
        FlatSVGIcon iconPressed = new FlatSVGIcon(crossIcon).setColorFilter(new FlatSVGIcon.ColorFilter(color -> UIManager.getColor("InstanceItem.pressedColor")));

        JLabel crossLabel = new JLabel(iconDefault);
        crossLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        crossLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
        MouseListener listener = new MouseListenerBuilder()
            .mouseClicked(e -> {
                onCrossClicked.run();
            })
            .mouseEntered(e -> {
                crossLabel.setIcon(iconHovered);
            })
            .mouseExited(e -> {
                crossLabel.setIcon(iconDefault);
            })
            .mousePressed(e -> {
                crossLabel.setIcon(iconPressed);
            })
            .mouseReleased(e -> {
                crossLabel.setIcon(iconHovered);
            })
            .build();
        crossLabel.addMouseListener(listener);
        topRightPanel.add(crossLabel);
        topPanel.add(topRightPanel, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);

        this.devlogPane = new DevlogPane();

        panel.add(this.devlogPane, BorderLayout.CENTER);

        JPanel borderPanel = new ScrollablePanel(new BorderLayout());
        borderPanel.add(panel, BorderLayout.PAGE_START);

        JScrollPane scrollPane = new JScrollPane(borderPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setUI(new FlatSmoothScrollPaneUI());

        this.add(scrollPane, BorderLayout.CENTER);
    }

    public void load(PostInfo postInfo) {
        this.postTitleLabel.setText(postInfo.getTitle() + " - Loading...");
        this.devlogPane.setText("");

        new PostLoadWorker(postInfo, this.postTitleLabel, this.devlogPane).execute();
    }
}
