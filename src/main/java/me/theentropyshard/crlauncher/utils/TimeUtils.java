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

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Language;

public final class TimeUtils {
    public static String getHoursMinutesSeconds(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String time = "";

        if (hours != 0) {
            time = hours + " hours";
        }

        if (minutes != 0) {
            if (hours == 0) {
                time = time + minutes + " minutes";
            } else {
                time = time + ", " + minutes + " minutes";
            }
        }

        if (seconds != 0) {
            if (minutes == 0 && hours == 0) {
                time = time + seconds + " seconds";
            } else {
                time = time + ", " + seconds + " seconds";
            }
        }

        return time;
    }
    public static String getHoursMinutesSecondsLocalized(long totalSeconds) {
        Language language = CRLauncher.getInstance().getLanguage();

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String time = "";

        if (hours != 0) {
            time = hours + " ";
            String timeUnit;
            if (hours == 1) {
                timeUnit = language.getString("general.time.units.hour1");
            } else if (hours == 2 || hours == 3 || hours == 4) {
                timeUnit = language.getString("general.time.units.hours234");
            } else {
                timeUnit = language.getString("general.time.units.hours");
            }
            time = time + timeUnit;
        }

        if (minutes != 0) {
            String timeUnit;
            if (minutes == 1) {
                timeUnit = language.getString("general.time.units.minute1");
            } else if (minutes == 2 || minutes == 3 || minutes == 4) {
                timeUnit = language.getString("general.time.units.minutes234");
            } else {
                timeUnit = language.getString("general.time.units.minutes");
            }

            if (hours == 0) {
                time = time + minutes + " " + timeUnit;
            } else {
                time = time + ", " + minutes + " " + timeUnit;
            }
        }

        if (seconds != 0) {
            String timeUnit;
            if (seconds == 1) {
                timeUnit = language.getString("general.time.units.second1");
            } else if (seconds == 2 || seconds == 3 || seconds == 4) {
                timeUnit = language.getString("general.time.units.seconds234");
            } else {
                timeUnit = language.getString("general.time.units.seconds");
            }

            if (minutes == 0 && hours == 0) {
                time = time + seconds + " " + timeUnit;
            } else {
                time = time + ", " + seconds + " " + timeUnit;
            }
        }

        return time;
    }

    private TimeUtils() {
        throw new UnsupportedOperationException();
    }
}
