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

package me.theentropyshard.crlauncher.gui.dialogs;

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.network.progress.ProgressListener;
import me.theentropyshard.crlauncher.utils.MathUtils;

import javax.swing.*;
import java.awt.*;

public class ProgressDialog extends AppDialog implements ProgressListener {
    private final JProgressBar progressBar;
    private final JLabel stageLabel;

    public ProgressDialog(String title) {
        super(CRLauncher.frame, title);

        JPanel root = new JPanel(new BorderLayout());
        root.setPreferredSize(new Dimension(450, 270));

        JPanel centerPanel = new JPanel();
        this.stageLabel = new JLabel("Downloading client...");
        centerPanel.add(this.stageLabel);
        root.add(centerPanel, BorderLayout.CENTER);

        this.progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        this.progressBar.putClientProperty(FlatClientProperties.PROGRESS_BAR_SQUARE, true);
        this.progressBar.setFont(this.progressBar.getFont().deriveFont(12.0f));
        this.progressBar.setStringPainted(true);
        root.add(this.progressBar, BorderLayout.SOUTH);

        this.setResizable(false);
        this.setContent(root);
        this.center(0);
    }

    @Override
    public void update(long contentLength, long downloadedBytes, long bytesThisTime, boolean done) {
        if (SwingUtilities.isEventDispatchThread()) {
            this.updateProgress(contentLength, downloadedBytes);
        } else {
            SwingUtilities.invokeLater(() -> {
                this.updateProgress(contentLength, downloadedBytes);
            });
        }
    }

    private void updateProgress(long contentLength, long downloadedBytes) {
        this.progressBar.setMinimum(0);
        this.progressBar.setMaximum((int) contentLength);
        this.progressBar.setValue((int) downloadedBytes);

        double downloaded = MathUtils.round(downloadedBytes / 1024.0D / 1024.0D, 2);
        double total = MathUtils.round(contentLength / 1024.0D / 1024.0D, 2);

        this.progressBar.setString(String.format("%.2f MiB / %.2f MiB", downloaded, total));
    }

    @Override
    public void setStage(String stage) {
        this.stageLabel.setText(stage);
    }

    public JProgressBar getProgressBar() {
        return this.progressBar;
    }
}
