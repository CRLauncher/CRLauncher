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

package me.theentropyshard.crlauncher.cosmic.launcher;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.cosmic.mods.puzzle.PuzzleManager;
import me.theentropyshard.crlauncher.cosmic.mods.puzzle.PuzzleProperties;
import me.theentropyshard.crlauncher.gui.dialogs.ProgressDialog;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class PuzzleCosmicLauncher extends ModdedPatchCosmicLauncher {
    private final String version;

    public PuzzleCosmicLauncher(String javaPath, Path runDir, Path gameFilesLocation, Path clientPath, Path modsDir, String version) {
        super(javaPath, runDir, gameFilesLocation, clientPath, modsDir);

        this.version = version;
    }

    @Override
    public void buildCommand(List<String> command) {
        super.buildCommand(command);

        PuzzleManager puzzleManager = CRLauncher.getInstance().getPuzzleManager();

        try {
            ProgressDialog dialog = new ProgressDialog("Downloading Puzzle");
            dialog.setStage("Downloading Puzzle " + this.version);

            SwingUtilities.invokeLater(() -> dialog.setVisible(true));
            puzzleManager.downloadPuzzle(this.version, dialog);
            SwingUtilities.invokeLater(() -> dialog.getDialog().dispose());
        } catch (IOException e) {
            Log.error("Could not download Puzzle " + this.version, e);

            MessageBox.showErrorMessage(
                CRLauncher.frame, "Could not download Puzzle " + this.version + ": " + e.getMessage()
            );

            return;
        }

        String classpath;
        try {
            classpath = puzzleManager.getClasspath(this.version);
        } catch (IOException e) {
            Log.error("Could not make classpath for Puzzle " + this.version, e);

            MessageBox.showErrorMessage(
                CRLauncher.frame, "Could not get classpath for Puzzle " + this.version + ": " + e.getMessage()
            );

            return;
        }

        command.add("-classpath");
        command.add(classpath + File.pathSeparator + this.getClientPath());

        command.add(PuzzleManager.getMainClass(this.version));
    }
}
