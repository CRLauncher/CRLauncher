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
import me.theentropyshard.crlauncher.cosmic.mods.cosmicquilt.CosmicQuiltProperties;
import me.theentropyshard.crlauncher.gui.dialogs.CRDownloadDialog;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.quilt.QuiltManager;
import me.theentropyshard.crlauncher.quilt.maven.Dependency;
import me.theentropyshard.crlauncher.quilt.maven.MavenDownloader;
import me.theentropyshard.crlauncher.network.download.DownloadList;
import me.theentropyshard.crlauncher.network.download.HttpDownload;
import me.theentropyshard.crlauncher.utils.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuiltCosmicLauncher extends ModdedCosmicLauncher {
    private static final Logger LOG = LogManager.getLogger(QuiltCosmicLauncher.class);

    private final String version;

    public QuiltCosmicLauncher(Path runDir, Path gameFilesLocation, Path clientPath, Path modsDir, String version) {
        super(runDir, gameFilesLocation, clientPath, modsDir);

        this.version = version;
    }

    @Override
    public void buildCommand(List<String> command) {
        this.defineProperty(CosmicQuiltProperties.LAUNCH_DIR.copy(this.getGameFilesLocation()));
        this.defineProperty(CosmicQuiltProperties.GAME_JAR_PATH.copy(this.getClientPath()));
        this.defineProperty(CosmicQuiltProperties.MODS_FOLDER.copy(this.getModsDir()));

        super.buildCommand(command);

        QuiltManager quiltManager = CRLauncher.getInstance().getQuiltManager();

        try {
            CRDownloadDialog dialog = new CRDownloadDialog();
            dialog.setStage("Downloading Cosmic Quilt " + this.version);

            SwingUtilities.invokeLater(() -> dialog.setVisible(true));
            quiltManager.downloadCosmicQuilt(this.version, dialog);
            SwingUtilities.invokeLater(() -> dialog.getDialog().dispose());
        } catch (IOException e) {
            LOG.error("Could not download Cosmic Quilt {}", this.version, e);

            MessageBox.showErrorMessage(
                    CRLauncher.frame,
                    "Could not download Cosmic Quilt " + this.version + ": " + e.getMessage()
            );

            return;
        }

        command.add("-classpath");
        try {
            command.add(quiltManager.getClasspathFor(this.version));
        } catch (IOException e) {
            LOG.error("Could not get classpath for " + this.version, e);

            MessageBox.showErrorMessage(
                    CRLauncher.frame,
                    "Could not get classpath for " + this.version + ": " + e.getMessage()
            );

            return;
        }
        command.add(CosmicQuiltProperties.MAIN_CLASS);
    }
}
