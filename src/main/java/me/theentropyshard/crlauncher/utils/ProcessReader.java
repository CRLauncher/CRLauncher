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
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
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

    /**
     * Reads the output and error stream of the process.
     * <p>
     * This is a (caller) blocking method, reading the output and error stream
     * of the process, mapped {@code in} and {@code err} respectively. This
     * method does not block the process.
     *
     * @param inLog  consumes a line from the output ({@code in}) stream
     * @param errLog  consumes a line from the error ({@code err}) stream
     * @throws IOException  If an I/ O error occurs
     * @see Process#getInputStream()
     * @see Process#getErrorStream()
     */
    public void read(
        Consumer<? super String> inLog,
        Consumer<? super String> errLog
    ) throws IOException {
        // These may be closed at [readLine]
        BufferedReader in = this.process.inputReader(this.charset);
        BufferedReader err = this.process.errorReader(this.charset);

        while (
            // Reads a line from `in`, proceeding to exhaust `err` if there
            // are no more lines.
            readPrimaryOrRemaining(in, inLog, err, errLog)
                // Reads a line from `err`, proceeding to exhaust `in` if there
                // are no more lines.
                && readPrimaryOrRemaining(err, errLog, in, inLog)
        ) {
            if (!this.process.isAlive()) {
                // We have to manually close on this edge-case
                in.close();
                err.close();
                break;
            }
        }
    }

    /**
     * Reads a line or all remaining lines.
     * <p>
     * This method handles two pairs of a reader-consumer. The primary pair
     * anticipates a line through {@code readLine}, returning {@code true}
     * regardless if the pair processed a line or not, so long as there may be
     * more lines; otherwise, the secondary pair is exhausted with their
     * remaining lines through the same method, returning {@code false} after
     * its certain that there are no more lines.
     *
     * @param reader1  the primary reader
     * @param consumer1  the primary consumer
     * @param reader2  the secondary reader
     * @param consumer2  the secondary consumer
     * @return {@code true} if there may be more lines; {@code false} otherwise
     * @throws IOException  If an I/ O error occurs
     * @see #readLine
     */
    private static boolean readPrimaryOrRemaining(
        BufferedReader reader1,
        Consumer<? super String> consumer1,
        BufferedReader reader2,
        Consumer<? super String> consumer2
    ) throws IOException {
        if (readLine(reader1, consumer1)) {
            // There may be more lines
            return true;
        }
        // Primary is out, exhaust the secondary
        while (readLine(reader2, consumer2)) {

        }
        // We've consumed all the lines
        return false;
    }

    /**
     * Reads a line from the reader if ready.
     * <p>
     * This method reads a line from the reader without blocking, providing the
     * result to the {@code consumer}. If the end of stream is reached, the
     * {@code reader} is {@linkplain BufferedReader#close() closed} and the
     * return value is {@code false}; {@code true} otherwise.
     *
     * @param reader  the reader providing the line
     * @param consumer  the consumer accepting the line
     * @return {@code true} if there may be more lines; {@code false} otherwise
     * @throws IOException  If an I/ O error occurs
     * @see Reader#ready()
     * @see BufferedReader#readLine()
     */
    private static boolean readLine(
        BufferedReader reader,
        Consumer<? super String> consumer
    ) throws IOException {
        final String line;
        if (reader.ready()) {
            if (Objects.nonNull(line = reader.readLine())) {
                consumer.accept(line);
            } else {
                reader.close();
                return false;
            }
        }
        return true;
    }
}