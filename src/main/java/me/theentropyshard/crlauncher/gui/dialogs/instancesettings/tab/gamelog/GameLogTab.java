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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.gamelog;

import com.formdev.flatlaf.ui.FlatScrollPaneBorder;
import com.google.gson.JsonObject;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.language.Language;
import me.theentropyshard.crlauncher.gui.BrowseHyperlinkListener;
import me.theentropyshard.crlauncher.gui.FlatSmoothScrollPaneUI;
import me.theentropyshard.crlauncher.gui.console.LauncherConsole;
import me.theentropyshard.crlauncher.gui.console.NoWrapJTextPane;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.Tab;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.instance.Instance;
import me.theentropyshard.crlauncher.language.LanguageSection;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.mclogs.McLogsApi;
import me.theentropyshard.crlauncher.mclogs.model.LimitsResponse;
import me.theentropyshard.crlauncher.mclogs.model.PasteResponse;
import me.theentropyshard.crlauncher.utils.FileUtils;
import me.theentropyshard.crlauncher.utils.OperatingSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class GameLogTab extends Tab {

    private final JTextPane logArea;
    private final JButton uploadButton;
    private final JButton copyFileButton;
    private final JButton copyTextButton;
    private final JButton clearButton;

    public GameLogTab(Instance instance, JDialog dialog) {
        super(CRLauncher.getInstance().getLanguage().getString("gui.instanceSettingsDialog.gameLogTab.name"), instance, dialog);

        Language language = CRLauncher.getInstance().getLanguage();
        LanguageSection section = language.getSection("gui.instanceSettingsDialog.gameLogTab");

        JPanel root = this.getRoot();
        root.setBorder(new EmptyBorder(3, 3, 3, 3));
        root.setLayout(new BorderLayout());

        // TODO: make search actually work
        JPanel topSearchPanel = new JPanel(new BorderLayout());
        topSearchPanel.setBorder(new EmptyBorder(0, 4, 3, 4));

        JLabel searchLabel = new JLabel(section.getString("search") + ": ");
        topSearchPanel.add(searchLabel, BorderLayout.WEST);

        JTextField searchField = new JTextField();
        topSearchPanel.add(searchField, BorderLayout.CENTER);

        JButton searchButton = new JButton(section.getString("find"));
        searchButton.addActionListener(e -> {
            new Worker<Void, Void>("searching text") {
                @Override
                protected Void work() throws Exception {
                    // TODO: improve text search

                    String text = searchField.getText();
                    String areaText = GameLogTab.this.logArea.getText();
                    int index = areaText.indexOf(text);

                    if (index == -1) {
                        return null;
                    }

                    int line = GameLogTab.findLine(areaText, index);
                    int i = index - line + 1;

                    SwingUtilities.invokeLater(() -> {
                        GameLogTab.this.logArea.requestFocus();
                        GameLogTab.this.logArea.select(i, i + text.length());
                        GameLogTab.this.logArea.repaint();
                    });

                    return null;
                }
            }.execute();
        });
        topSearchPanel.add(searchButton, BorderLayout.EAST);

        root.add(topSearchPanel, BorderLayout.NORTH);

        // TODO: try making also colored
        this.logArea = new NoWrapJTextPane() {
            @Override
            protected void paintComponent(Graphics g) {
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                super.paintComponent(g);
            }
        };
        this.logArea.getCaret().setSelectionVisible(true);
        this.logArea.setFont(LauncherConsole.FONT);
        ((DefaultCaret) this.logArea.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        this.logArea.setEditable(false);
        this.logArea.setBackground(UIManager.getColor("TextField.background"));

        JScrollPane scrollPane = new JScrollPane(
            this.logArea,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        );
        scrollPane.setUI(new FlatSmoothScrollPaneUI());
        scrollPane.setBorder(new FlatScrollPaneBorder());
        root.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomButtonsPanel.setBorder(new EmptyBorder(2, 0, 0, 0));

        this.uploadButton = new JButton(section.getString("upload"));
        this.uploadButton.addActionListener(e -> {
            this.uploadButton.setText(section.getString("uploading") + "...");

            SwingUtils.startWorker(() -> {
                String log;

                Path logFile = this.getInstance().getCosmicDir().resolve("errorLogLatest.txt");
                try {
                    log = FileUtils.readUtf8(logFile);
                } catch (IOException ex) {
                    Log.error("Could not read file: " + logFile, ex);
                    MessageBox.showErrorMessage(CRLauncher.frame, section.getString("failed") + ": " + ex.getMessage());

                    return;
                }

                McLogsApi api = CRLauncher.getInstance().getMcLogsApi();

                LimitsResponse limits = api.getLimits();
                int maxLength = limits.getMaxLength();

                if (log.getBytes(StandardCharsets.UTF_8).length > maxLength) {
                    MessageBox.showWarningMessage(CRLauncher.frame, section.getString("largeLog"));
                }

                PasteResponse pasteResponse = api.pasteLog(log);

                if (pasteResponse.isSuccess()) {
                    OperatingSystem.copyToClipboard(pasteResponse.getUrl());

                    JTextPane messagePane = new JTextPane();
                    messagePane.setContentType("text/html");
                    messagePane.setEditable(false);
                    messagePane.setEditorKit(new HTMLEditorKit());
                    messagePane.setText("<html>" + section.getString("successMessage").replace(
                        "$$LINK$$",
                        "<a href=\"" + pasteResponse.getUrl() + "\">" + pasteResponse.getUrl() + "</a>"
                    ) + "</html>");
                    messagePane.addHyperlinkListener(new BrowseHyperlinkListener());


                    MessageBox.showPlainMessage(CRLauncher.frame, section.getString("successTitle"), messagePane);
                } else {
                    Log.error("Could not upload log: " + pasteResponse.getError());
                    MessageBox.showErrorMessage(CRLauncher.frame, section.getString("failed") + ": " + pasteResponse.getError());
                }

                SwingUtilities.invokeLater(() -> {
                    this.uploadButton.setText(section.getString("upload"));
                });
            });
        });
        bottomButtonsPanel.add(this.uploadButton);

        this.copyFileButton = new JButton(section.getString("copyFile"));
        Path logFile = this.getInstance().getCosmicDir().resolve("errorLogLatest.txt");
        this.copyFileButton.addActionListener(e -> {
            SwingUtils.startWorker(() -> {
                OperatingSystem.copyToClipboard(this.getInstance().getCosmicDir().resolve("errorLogLatest.txt"));
            });
        });
        bottomButtonsPanel.add(this.copyFileButton);

        this.copyTextButton = new JButton(section.getString("copyText"));
        this.copyTextButton.addActionListener(e -> {
            SwingUtils.startWorker(() -> {
                OperatingSystem.copyToClipboard(this.logArea.getText());
            });
        });
        bottomButtonsPanel.add(this.copyTextButton);

        this.clearButton = new JButton(section.getString("clear"));
        this.clearButton.addActionListener(e -> {
            SwingUtils.startWorker(() -> {
                try {
                    FileUtils.delete(this.getInstance().getCosmicDir().resolve("errorLogLatest.txt"));
                } catch (IOException ex) {
                    Log.error("Could not delete log file", ex);
                }

                SwingUtilities.invokeLater(() -> {
                    this.logArea.setText("");
                    this.copyFileButton.setEnabled(false);
                });
            });
        });
        bottomButtonsPanel.add(this.clearButton);

        if (!Files.exists(logFile)) {
            this.toggleButtons(false);
        }

        root.add(bottomButtonsPanel, BorderLayout.SOUTH);

        new GameLogLoader(this).execute();
    }

    public void appendLine(String line) {
        Document doc = this.logArea.getDocument();

        try {
            doc.insertString(doc.getLength(), line, null);
        } catch (BadLocationException e) {
            Log.error("Could not append line to game log area", e);
        }
    }

    public void toggleButtons(boolean enabled) {
        this.uploadButton.setEnabled(enabled);
        this.copyFileButton.setEnabled(enabled);
        this.copyTextButton.setEnabled(enabled);
        this.clearButton.setEnabled(enabled);
    }

    @Override
    public void save() throws IOException {

    }

    public static int findLine(String text, int index) {
        String[] lines = text.split("\n");
        int charCount = 0;

        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            charCount += lines[lineNumber].length() + 1; // Add line length and 1 for the newline character
            if (charCount > index) {
                return lineNumber + 1; // Return the line number (1-based index)
            }
        }

        return -1;
    }
}
