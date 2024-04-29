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

import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.CosmicQuiltProperties;
import me.theentropyshard.crlauncher.gui.dialogs.CRDownloadDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class QuiltCosmicLauncher extends ModdedCosmicLauncher {
    private static final Logger LOG = LogManager.getLogger(QuiltCosmicLauncher.class);

    private final String version;

    public QuiltCosmicLauncher(Path runDir, Path gameFilesLocation, Path clientPath, Path modsDir, String version) {
        super(runDir, gameFilesLocation, clientPath, modsDir);

        this.version = version;
    }

    private void downloadCosmicQuilt(Path cosmicQuiltDir, String version) {
        CRDownloadDialog dialog = new CRDownloadDialog();
        dialog.getProgressBar().setIndeterminate(true);
        dialog.setStage("Downloading Cosmic Quilt...");

        SwingUtilities.invokeLater(() -> dialog.setVisible(true));


        SwingUtilities.invokeLater(() -> dialog.getDialog().dispose());
    }

    @Override
    public void buildCommand(List<String> command) {
        this.defineProperty(CosmicQuiltProperties.LAUNCH_DIR.copy(this.getGameFilesLocation()));
        this.defineProperty(CosmicQuiltProperties.MODS_FOLDER.copy(this.getModsDir()));

        super.buildCommand(command);

        Path cosmicQuiltDir = this.getGameFilesLocation().resolve("cosmic_quilt_%s".formatted(this.version));
        if (!Files.exists(cosmicQuiltDir)) {
            this.downloadCosmicQuilt(cosmicQuiltDir, this.version);
        }

        command.add("-classpath");

        List<String> classpath = new ArrayList<>();
        classpath.add(this.getClientPath().toString());

        command.add(String.join(File.pathSeparator, classpath));
        command.add(CosmicQuiltProperties.MAIN_CLASS);
    }
}
