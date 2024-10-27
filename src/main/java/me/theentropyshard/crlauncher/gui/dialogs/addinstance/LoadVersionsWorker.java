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

package me.theentropyshard.crlauncher.gui.dialogs.addinstance;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.cosmic.version.Version;
import me.theentropyshard.crlauncher.cosmic.version.VersionManager;
import me.theentropyshard.crlauncher.cosmic.version.VersionType;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.gui.utils.Worker;
import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LoadVersionsWorker extends Worker<Void, Version> {
    private final CosmicVersionsTableModel model;
    private final AddInstanceDialog dialog;
    private final JTable table;
    private final boolean forceNetwork;

    public LoadVersionsWorker(CosmicVersionsTableModel model, AddInstanceDialog dialog, JTable table, boolean forceNetwork) {
        super("loading Cosmic Reach versions");

        this.model = model;
        this.dialog = dialog;
        this.table = table;
        this.forceNetwork = forceNetwork;
    }

    @Override
    protected Void work() throws Exception {
        CRLauncher launcher = CRLauncher.getInstance();
        VersionManager versionManager = launcher.getVersionManager();
        Settings settings = launcher.getSettings();

        if (!versionManager.isLoaded() || this.forceNetwork) {
            versionManager.setMode(VersionManager.Mode.ONLINE);
            versionManager.load();
        }

        List<Version> versions = versionManager.getVersions();

        for (Version version : versions) {
            if (!versionManager.isInstalled(version) && settings.showOnlyInstalledVersions) {
                continue;
            }

            this.publish(version);
        }

        return null;
    }

    @Override
    protected void process(List<Version> versions) {
        for (Version version : versions) {
            this.model.addVersion(version);
        }
    }

    @Override
    protected void done() {
        TableRowSorter<CosmicVersionsTableModel> rowSorter = new TableRowSorter<>(this.model);

        this.dialog.getPreAlphasBox().addActionListener(e -> rowSorter.sort());

        rowSorter.setRowFilter(RowFilter.orFilter(Arrays.asList(
            new VersionTypeRowFilter(this.dialog.getPreAlphasBox(), VersionType.PRE_ALPHA)
        )));

        this.table.setRowSorter(rowSorter);

        this.dialog.getAddButton().setEnabled(true);

        SwingUtils.setJTableColumnsWidth(this.table, this.model.getTableColumnWidthPercentages());
    }
}
