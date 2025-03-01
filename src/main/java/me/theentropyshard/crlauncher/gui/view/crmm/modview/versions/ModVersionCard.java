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

package me.theentropyshard.crlauncher.gui.view.crmm.modview.versions;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectFile;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectVersion;
import me.theentropyshard.crlauncher.gui.components.MouseListenerBuilder;
import me.theentropyshard.crlauncher.gui.view.crmm.modview.GameVersionLabel;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.utils.StringUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseListener;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ModVersionCard extends JPanel {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(
        CRLauncher.getInstance().getLanguage().getString("general.time.dateFormat")
    );

    private static final int BORDER_SIZE = 12;
    private static final int ARC_SIZE = 10;

    private Color defaultColor;
    private Color hoveredColor;
    private Color pressedColor;

    private boolean mouseOver;
    private boolean mousePressed;

    public ModVersionCard(ProjectVersion version, FileListener fileListener) {
        super(new MigLayout("insets 0, align left", "[30%]push[20%][10%][10%]push[10%]", "[]"));

        JPanel versionInfoPanel = new JPanel(new GridLayout(2, 1));
        versionInfoPanel.setOpaque(false);
        JLabel versionNumberLabel = new JLabel("<html><b>" + version.getVersionNumber() + "</b></html>");
        JLabel versionTitleLabel = new JLabel(version.getTitle());
        versionInfoPanel.add(versionNumberLabel);
        versionInfoPanel.add(versionTitleLabel);

        this.add(versionInfoPanel);

        JPanel compatibility = new JPanel(new MigLayout("insets 0, gap 5 5", "[][]", "[]"));
        compatibility.setOpaque(false);

        List<String> gameVersions = version.getGameVersions();
        String gameText;
        if (gameVersions.size() == 1) {
            gameText = gameVersions.get(0).replace("-pre-alpha", "");
        } else {
            gameText = gameVersions.get(0).replace("-pre-alpha", "") + "-" +
                gameVersions.get(gameVersions.size() - 1).replace("-pre-alpha", "");
        }
        GameVersionLabel gameVersion = new GameVersionLabel(
            gameText
        );
        compatibility.add(gameVersion);

        for (String loader : version.getLoaders()) {
            StringBuilder formattedLoader = new StringBuilder();

            for (String s : loader.replace("_", " ").split(" ")) {
                formattedLoader.append(StringUtils.capitalize(s)).append(" ");
            }

            compatibility.add(new GameVersionLabel(formattedLoader.toString().trim()));
        }

        this.add(compatibility);

        Language language = CRLauncher.getInstance().getLanguage();

        JButton downloadButton = new JButton(language.getString("gui.searchCRMMModsDialog.downloadButton"));
        downloadButton.addActionListener(e -> {
            fileListener.fileChosen(version.getPrimaryFile());
        });

        JButton otherFilesButtons = new JButton(language.getString("gui.searchCRMMModsDialog.otherFiles"));
        otherFilesButtons.addActionListener(e -> {
            OtherFilesView.showDialog(version.getFiles(), fileListener);
        });

        JLabel published = new JLabel(ModVersionCard.FORMATTER.format(OffsetDateTime.parse(version.getDatePublished())));
        this.add(published);

        JLabel downloads = new JLabel(String.valueOf(version.getDownloads()));
        this.add(downloads);

        JPanel buttonsPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS);
        buttonsPanel.setLayout(boxLayout);
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(downloadButton);
        buttonsPanel.add(otherFilesButtons);

        this.add(buttonsPanel);

        this.setOpaque(false);
        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));

        this.setBorder(new EmptyBorder(
            ModVersionCard.BORDER_SIZE,
            ModVersionCard.BORDER_SIZE,
            ModVersionCard.BORDER_SIZE,
            ModVersionCard.BORDER_SIZE
        ));

        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseListener listener = new MouseListenerBuilder()
            .mouseEntered(e -> {
                this.mouseOver = true;
                this.repaint();
            })
            .mouseExited(e -> {
                this.mouseOver = false;
                this.repaint();
            })
            .mousePressed(e -> {
                this.mousePressed = true;
                this.repaint();
            })
            .mouseReleased(e -> {
                this.mousePressed = false;
                this.repaint();
            })
            .build();

        this.addMouseListener(listener);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        this.paintBackground(g2d);

        super.paintComponent(g2d);
    }

    private void paintBackground(Graphics2D g2d) {
        Color color = this.defaultColor;

        if (this.mouseOver) {
            color = this.hoveredColor;
        }

        if (this.mousePressed) {
            color = this.pressedColor;
        }

        g2d.setColor(color);
        g2d.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), ModVersionCard.ARC_SIZE, ModVersionCard.ARC_SIZE);
    }

    public interface FileListener {
        void fileChosen(ProjectFile file);
    }

    public void setDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    public void setHoveredColor(Color hoveredColor) {
        this.hoveredColor = hoveredColor;
    }

    public void setPressedColor(Color pressedColor) {
        this.pressedColor = pressedColor;
    }
}
