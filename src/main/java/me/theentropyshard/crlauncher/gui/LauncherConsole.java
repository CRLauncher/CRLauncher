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

package me.theentropyshard.crlauncher.gui;

import com.formdev.flatlaf.ui.FlatScrollPaneUI;
import me.theentropyshard.crlauncher.BuildConfig;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.utils.OperatingSystem;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowListener;

public class LauncherConsole {
    private static final int DEFAULT_X = 80;
    private static final int DEFAULT_Y = 80;
    private static final int INITIAL_WIDTH = 480;
    private static final int INITIAL_HEIGHT = 280;
    private static final int INITIAL_FONT_SIZE = 14;
    private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, LauncherConsole.INITIAL_FONT_SIZE);

    private final JCheckBox scrollDown;
    public static LauncherConsole instance;
    private final JTextPane textPane;
    private final SimpleAttributeSet attrs;
    private final JFrame frame;

    public LauncherConsole() {
        this.textPane = new JTextPane() {
            @Override
            protected void paintComponent(Graphics g) {
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                super.paintComponent(g);
            }
        };
        this.textPane.setPreferredSize(new Dimension(LauncherConsole.INITIAL_WIDTH, LauncherConsole.INITIAL_HEIGHT));
        this.textPane.setFont(LauncherConsole.FONT);
        this.textPane.setEditorKit(new WrapEditorKit());
        this.textPane.setEditable(false);

        this.attrs = new SimpleAttributeSet();

        JScrollPane scrollPane = new JScrollPane(
            this.textPane,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setUI(new FlatScrollPaneUI() {
            @Override
            protected MouseWheelListener createMouseWheelListener() {
                if (this.isSmoothScrollingEnabled()) {
                    return new SmoothScrollMouseWheelListener(this.scrollpane.getVerticalScrollBar());
                } else {
                    return super.createMouseWheelListener();
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(bottomPanel, BorderLayout.SOUTH);

        this.scrollDown = new JCheckBox("Scroll down");
        this.scrollDown.setSelected(CRLauncher.getInstance().getSettings().consoleScrollDown);
        this.scrollDown.addActionListener(e -> {
            CRLauncher.getInstance().getSettings().consoleScrollDown = this.scrollDown.isSelected();
            this.scrollToBottom();
        });

        bottomPanel.add(this.scrollDown);

        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> {
            OperatingSystem.copyToClipboard(this.textPane.getText());
        });
        bottomPanel.add(copyButton);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            this.textPane.setText("");
        });
        bottomPanel.add(clearButton);

        this.frame = new JFrame(BuildConfig.APP_NAME + " console");
        this.frame.add(panel, BorderLayout.CENTER);
        this.frame.pack();
        this.frame.setLocation(LauncherConsole.DEFAULT_X, LauncherConsole.DEFAULT_Y);
    }

    private void scrollToBottom() {
        if (this.scrollDown.isSelected()) {
            this.textPane.setCaretPosition(this.textPane.getDocument().getLength());
        }
    }

    public JFrame getFrame() {
        return this.frame;
    }

    public void setVisible(boolean visibility) {
        this.frame.setVisible(visibility);
    }

    public void addWindowListener(WindowListener listener) {
        this.frame.addWindowListener(listener);
    }

    public LauncherConsole setColor(Color c) {
        StyleConstants.setForeground(this.attrs, c);

        return this;
    }

    public LauncherConsole setBold(boolean bold) {
        StyleConstants.setBold(this.attrs, bold);

        return this;
    }

    public void write(String line) {
        Document document = this.textPane.getDocument();

        try {
            document.insertString(document.getLength(), line, this.attrs);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        this.scrollToBottom();
    }

    private static final class WrapEditorKit extends StyledEditorKit {
        private final ViewFactory viewFactory;

        public WrapEditorKit() {
            this.viewFactory = new WrapColumnFactory();
        }

        @Override
        public ViewFactory getViewFactory() {
            return this.viewFactory;
        }
    }

    private static final class WrapColumnFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();

            return switch (kind) {
                case AbstractDocument.ContentElementName -> new WrapLabelView(elem);
                case AbstractDocument.ParagraphElementName -> new ParagraphView(elem);
                case AbstractDocument.SectionElementName -> new BoxView(elem, View.Y_AXIS);
                case StyleConstants.ComponentElementName -> new ComponentView(elem);
                case StyleConstants.IconElementName -> new IconView(elem);
                default -> new LabelView(elem);
            };
        }
    }

    private static final class WrapLabelView extends LabelView {
        public WrapLabelView(Element element) {
            super(element);
        }

        public float getMinimumSpan(int axis) {
            return switch (axis) {
                case View.X_AXIS -> 0;
                case View.Y_AXIS -> super.getMinimumSpan(axis);
                default -> throw new IllegalArgumentException("Invalid axis: " + axis);
            };
        }
    }
}
