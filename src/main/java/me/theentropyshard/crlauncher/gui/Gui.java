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

import com.formdev.flatlaf.FlatLightLaf;
import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;

public class Gui {
    private final JFrame frame;
    private final MainView mainView;

    public static Gui instance;

    public Gui() {
        JDialog.setDefaultLookAndFeelDecorated(true);
        JFrame.setDefaultLookAndFeelDecorated(true);
        FlatLightLaf.setup();

        instance = this;

        this.mainView = new MainView();

        this.frame = new JFrame(CRLauncher.NAME);
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.add(this.mainView, BorderLayout.CENTER);
        this.frame.pack();
        SwingUtils.centerWindow(this.frame, 0);
    }

    public static void showErrorDialog(String msg) {
        if (SwingUtilities.isEventDispatchThread()) {
            JOptionPane.showMessageDialog(
                    instance.getFrame(),
                    msg,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } else {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        instance.getFrame(),
                        msg,
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            });
        }
    }

    public void show() {
        this.frame.setVisible(true);
        this.mainView.onStartup();
    }

    public JFrame getFrame() {
        return this.frame;
    }

    public MainView getMainView() {
        return this.mainView;
    }
}
