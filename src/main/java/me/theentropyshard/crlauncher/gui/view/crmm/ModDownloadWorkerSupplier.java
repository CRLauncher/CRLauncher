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

package me.theentropyshard.crlauncher.gui.view.crmm;

import me.theentropyshard.crlauncher.crmm.model.project.ProjectFile;
import me.theentropyshard.crlauncher.crmm.model.project.ProjectVersion;
import me.theentropyshard.crlauncher.gui.utils.Worker;

@SuppressWarnings("rawtypes")
public class ModDownloadWorkerSupplier implements WorkerSupplier {
    public ModDownloadWorkerSupplier() {

    }

    @Override
    public Worker getWorker(ModVersionsView versionsView, ProjectVersion version, ProjectFile file) {
        return new ModDownloadWorker(
            versionsView.getInstance(),
            versionsView.getModsTab().getModsView().getModsTableModel(),
            version,
            file
        );
    }
}
