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

package me.theentropyshard.crlauncher.gui.console;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.gui.FlatSmoothScrollPaneUI;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.OperatingSystem;
import me.theentropyshard.crlauncher.utils.Pair;
import me.theentropyshard.crlauncher.utils.TextSearch;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LauncherConsole {
    private static final int DEFAULT_X = 80;
    private static final int DEFAULT_Y = 80;
    private static final int INITIAL_WIDTH = 960;
    private static final int INITIAL_HEIGHT = 540;
    private static final int INITIAL_FONT_SIZE = 14;
    public static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, LauncherConsole.INITIAL_FONT_SIZE);

    public static final String SCROLL_DOWN = "gui.console.scrollDown";
    public static final String COPY = "gui.console.copyButton";
    public static final String CLEAR = "gui.console.clearButton";
    public static final String TITLE = "gui.console.title";

    // TODO thats dumb but uhh
    public static final String SEARCH_BUTTON = "gui.instanceSettingsDialog.gameLogTab.find";

    private final JCheckBox scrollDown;
    public static LauncherConsole instance;
    private final JTextPane textPane;
    private final SimpleAttributeSet attrs;
    private final JFrame frame;
    private final JScrollPane scrollPane;
    private final JButton copyButton;
    private final JButton clearButton;
    private final JTextField searchField;
    private final JButton searchButton;

    private String lastWordSearched;
    private List<Pair<Integer, Integer>> searchIndices;
    private int searchPairIndex;

    public LauncherConsole() {
        Language language = CRLauncher.getInstance().getLanguage();

        this.textPane = new NoWrapJTextPane() {
            @Override
            protected void paintComponent(Graphics g) {
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                super.paintComponent(g);
            }
        };
        this.textPane.setFont(LauncherConsole.FONT);
        ((DefaultCaret) this.textPane.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        this.textPane.setEditable(false);
        //this.textPane.getDocument().addDocumentListener(new ConsoleLinesLimiter(1000));

        this.attrs = new SimpleAttributeSet();

        this.scrollPane = new JScrollPane(
            this.textPane,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        );
        this.scrollPane.setUI(new FlatSmoothScrollPaneUI());

        JPanel root = new JPanel(new BorderLayout());
        root.setPreferredSize(new Dimension(LauncherConsole.INITIAL_WIDTH, LauncherConsole.INITIAL_HEIGHT));
        root.add(this.scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
        root.add(bottomPanel, BorderLayout.SOUTH);

        JPanel leftButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(leftButtonsPanel);

        this.searchField = new JTextField();
        this.searchField.setPreferredSize(new Dimension(250, this.searchField.getPreferredSize().height));
        leftButtonsPanel.add(this.searchField);

        this.searchButton = new JButton(language.getString(LauncherConsole.SEARCH_BUTTON));
        this.searchButton.addActionListener(e -> {
            String searchText = this.searchField.getText();

            if (searchText.equals(this.lastWordSearched)) {
                this.performSearch();
            } else {
                this.lastWordSearched = searchText;
                this.searchPairIndex = 0;

                new Worker<List<Pair<Integer, Integer>>, Void>("searching text") {
                    @Override
                    protected List<Pair<Integer, Integer>> work() throws Exception {
                        String areaText = LauncherConsole.this.textPane.getText();
                        String searchText = LauncherConsole.this.searchField.getText();

                        TextSearch textSearch = TextSearch.create();

                        return textSearch.findOccurrences(areaText, searchText);
                    }

                    @Override
                    protected void done() {
                        try {
                            LauncherConsole.this.searchIndices = this.get();
                        } catch (InterruptedException | ExecutionException ex) {
                            Log.error("Unexpected error", ex);

                            return;
                        }

                        LauncherConsole.this.performSearch();
                    }
                }.execute();
            }
        });
        leftButtonsPanel.add(this.searchButton);

        JPanel rightButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(rightButtonsPanel);

        this.scrollDown = new JCheckBox(language.getString(LauncherConsole.SCROLL_DOWN));
        this.scrollDown.setSelected(CRLauncher.getInstance().getSettings().consoleScrollDown);
        this.scrollDown.addActionListener(e -> {
            CRLauncher.getInstance().getSettings().consoleScrollDown = this.scrollDown.isSelected();
            this.scrollToBottom();
        });

        rightButtonsPanel.add(this.scrollDown);

        this.copyButton = new JButton(language.getString(LauncherConsole.COPY));
        this.copyButton.addActionListener(e -> {
            OperatingSystem.copyToClipboard(this.textPane.getText());
        });
        rightButtonsPanel.add(this.copyButton);

        this.clearButton = new JButton(language.getString(LauncherConsole.CLEAR));
        this.clearButton.addActionListener(e -> {
            this.textPane.setText("");
            this.searchIndices = null;
            this.searchPairIndex = 0;
            this.lastWordSearched = "";
        });
        rightButtonsPanel.add(this.clearButton);

        this.frame = new JFrame(language.getString(LauncherConsole.TITLE));
        this.frame.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        this.frame.add(root, BorderLayout.CENTER);
        this.frame.pack();
        this.frame.setLocation(LauncherConsole.DEFAULT_X, LauncherConsole.DEFAULT_Y);
    }

    private void performSearch() {
        if (this.searchIndices != null && this.searchIndices.size() != 0) {
            int pairIndex = (this.searchPairIndex++) % this.searchIndices.size();
            Pair<Integer, Integer> indices = this.searchIndices.get(pairIndex);
            LauncherConsole.this.textPane.requestFocus();
            LauncherConsole.this.textPane.select(indices.getLeft(), indices.getRight());
            LauncherConsole.this.textPane.repaint();
        }
    }

    private void scrollToBottom() {
        if (this.scrollDown.isSelected()) {
            JScrollBar scrollBar = this.scrollPane.getVerticalScrollBar();
            scrollBar.setValue(scrollBar.getMaximum());
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
        SwingUtilities.invokeLater(() -> {
            StyleConstants.setForeground(this.attrs, c);
        });

        return this;
    }

    public LauncherConsole setBold(boolean bold) {
        SwingUtilities.invokeLater(() -> {
            StyleConstants.setBold(this.attrs, bold);
        });

        return this;
    }

    public void write(String line) {
        SwingUtilities.invokeLater(() -> {
            Document document = this.textPane.getDocument();

            try {
                document.insertString(document.getLength(), line, this.attrs);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            this.scrollToBottom();
        });
    }

    public void reloadLanguage() {
        Language language = CRLauncher.getInstance().getLanguage();

        this.frame.setTitle(language.getString(LauncherConsole.TITLE));
        this.scrollDown.setText(language.getString(LauncherConsole.SCROLL_DOWN));
        this.copyButton.setText(language.getString(LauncherConsole.COPY));
        this.clearButton.setText(language.getString(LauncherConsole.CLEAR));
        this.searchButton.setText(language.getString(LauncherConsole.SEARCH_BUTTON));
    }
}
