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

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.CosmicRunner;
import me.theentropyshard.crlauncher.cosmic.Version;
import me.theentropyshard.crlauncher.cosmic.VersionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainView extends JPanel {
    private static final Logger LOG = LogManager.getLogger(MainView.class);

    private final JComboBox<Version> versionCombo;
    private final JButton playButton;
    private final JProgressBar downloadProgress;

    private Version selectedVersion;

    public MainView() {
        this.versionCombo = new JComboBox<>();
        this.versionCombo.setRenderer(new VersionListCellRenderer());
        this.versionCombo.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }

            this.selectedVersion = (Version) e.getItem();
        });

        this.playButton = new JButton("Play");
        this.playButton.setEnabled(false);
        this.playButton.addActionListener(e -> {
            if (this.selectedVersion == null) {
                Gui.showErrorDialog("You must select a version first");
            } else {
                new CosmicRunner(this.selectedVersion).start();
            }
        });

        this.downloadProgress = new JProgressBar();
        this.downloadProgress.setStringPainted(true);
        this.downloadProgress.putClientProperty(FlatClientProperties.PROGRESS_BAR_SQUARE, true);
        this.downloadProgress.setFont(this.downloadProgress.getFont().deriveFont(12.0f));
        this.changeDownloadProgressVisibility(false);

        this.setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel();
        centerPanel.add(this.versionCombo);
        centerPanel.add(this.playButton);

        this.add(centerPanel, BorderLayout.CENTER);
        this.add(this.downloadProgress, BorderLayout.SOUTH);

        this.setPreferredSize(new Dimension(320, 140));
    }

    public void changeDownloadProgressVisibility(boolean v) {
        this.downloadProgress.setVisible(v);
    }

    public void onStartup() {
        new GetVersionsWorker(this).execute();
    }

    public JComboBox<Version> getVersionCombo() {
        return this.versionCombo;
    }

    public JButton getPlayButton() {
        return this.playButton;
    }

    public JProgressBar getDownloadProgress() {
        return this.downloadProgress;
    }

    public Version getSelectedVersion() {
        return this.selectedVersion;
    }

    private static final class GetVersionsWorker extends SwingWorker<List<Version>, Void> {
        private final MainView mainView;

        public GetVersionsWorker(MainView mainView) {
            this.mainView = mainView;
        }

        @Override
        protected List<Version> doInBackground() throws Exception {
            VersionManager versionManager = CRLauncher.getInstance().getVersionManager();

            try {
                return versionManager.getRemoteAvailableVersions();
            } catch (IOException e) {
                LOG.error(e);
            }

            return Collections.emptyList();
        }

        @Override
        protected void done() {
            try {
                this.get().forEach(this.mainView.getVersionCombo()::addItem);
                this.mainView.getPlayButton().setEnabled(true);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
