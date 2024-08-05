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

package me.theentropyshard.crlauncher.gui.view.playview;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.account.Account;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PlayViewHeader extends JPanel {
    private final JComboBox<String> instanceGroups;
    private final JLabel account;

    public static PlayViewHeader instance;

    public PlayViewHeader() {
        super(new BorderLayout());

        PlayViewHeader.instance = this;

        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel instanceLabel = new JLabel("Instance group:");
        leftSide.add(instanceLabel);

        this.instanceGroups = new JComboBox<>();
        leftSide.add(this.instanceGroups);

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JLabel accountsLabel = new JLabel("Account:");
        rightSide.add(accountsLabel);

        this.account = new JLabel();
        this.setCurrentAccount(CRLauncher.getInstance().getAccountManager().getCurrentAccount());
        rightSide.add(this.account);

        this.add(leftSide, BorderLayout.WEST);
        this.add(rightSide, BorderLayout.EAST);
    }

    public void setCurrentAccount(Account account) {
        if (account != null) {
            BufferedImage image = SwingUtils.loadImageFromBase64(account.getHeadIcon());
            this.account.setIcon(new ImageIcon(image.getScaledInstance(16, 16, BufferedImage.SCALE_FAST)));
            this.account.setText(account.getUsername());
        } else {
            this.account.setText("No account selected");
        }
    }

    public JComboBox<String> getInstanceGroups() {
        return this.instanceGroups;
    }
}
