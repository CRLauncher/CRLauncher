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

import me.theentropyshard.crlauncher.gui.utils.Worker;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GameLogLoader extends Worker<Void, String> {
    private final GameLogTab gameLogTab;

    public GameLogLoader(GameLogTab gameLogTab) {
        super("loading game log");

        this.gameLogTab = gameLogTab;
    }

    @Override
    protected Void work() throws Exception {
        Path logFile = this.gameLogTab.getInstance().getCosmicDir().resolve("errorLogLatest.txt");

        if (!Files.exists(logFile) || Files.size(logFile) == 0L) {
            return null;
        }

        List<String> lines = Files.readAllLines(logFile);
        int lastIndex = lines.size() - 1;

        String lastLine = lines.get(lastIndex);
        lines.remove(lastLine);

        SwingUtilities.invokeLater(() -> {
            this.process(lines);
            this.gameLogTab.appendLine(lastLine);
            this.gameLogTab.toggleButtons(true);
        });

        return null;
    }

    @Override
    protected void process(List<String> chunks) {
        for (String line : chunks) {
            this.gameLogTab.appendLine(line + "\n");
        }
    }
}
