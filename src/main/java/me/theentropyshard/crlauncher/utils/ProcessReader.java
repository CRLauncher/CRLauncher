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

package me.theentropyshard.crlauncher.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class ProcessReader {
    private final Process process;
    private final Charset charset;

    public ProcessReader(Process process) {
        this(process, StandardCharsets.UTF_8);
    }

    public ProcessReader(Process process, Charset charset) {
        this.process = process;
        this.charset = charset;
    }

    public void read(Consumer<String> inLog, Consumer<String> errLog) throws IOException {
        InputStream inputStream = this.process.getInputStream();
        InputStream errorStream = this.process.getErrorStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, this.charset));
        BufferedReader err = new BufferedReader(new InputStreamReader(errorStream, this.charset));
        String line;
        do {
            if (!in.ready()) {
                // Do nothing: only reduces nesting
            } else if ((line = in.readLine()) != null) {
                inLog.accept(line);
            } else {
                while ((line = err.readLine()) != null) {
                    errLog.accept(line);
                }
                break;
            }

            if (!err.ready()) {
                // Do nothing: only reduces nesting
            } else if ((line = err.readLine()) != null) {
                errLog.accept(line);
            } else {
                while ((line = in.readLine()) != null) {
                    inLog.accept(line);
                }
                break;
            }
        } while (true);
    }
}