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

package me.theentropyshard.crlauncher.gui.utils;

import me.theentropyshard.crlauncher.logging.Log;

import javax.swing.*;

public abstract class Worker<T, V> extends SwingWorker<T, V> {
    private final String name;

    public Worker(String name) {
        this.name = name;
    }

    @Override
    protected final T doInBackground() throws Exception {
        try {
            return this.work();
        } catch (Exception e) {
            Log.error("Exception while " + this.name, e);
        }

        return null;
    }

    protected abstract T work() throws Exception;
}
