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

package me.theentropyshard.crlauncher.cosmic.itch;

import me.theentropyshard.crlauncher.cosmic.version.Version;
import me.theentropyshard.crlauncher.cosmic.version.VersionPhase;
import me.theentropyshard.crlauncher.cosmic.version.VersionType;
import me.theentropyshard.crlauncher.itch.DetailedBuild;
import me.theentropyshard.crlauncher.logging.Log;
import me.theentropyshard.crlauncher.utils.RegexUtils;
import me.theentropyshard.crlauncher.utils.SemanticVersion;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;

public class ItchVersion extends DetailedBuild implements Version {
    @Override
    public String getId() {
        return this.getUserVersion();
    }

    @Override
    public long getReleaseTime() {
        return OffsetDateTime.parse(this.getCreatedAt(), DateTimeFormatter.ISO_DATE_TIME).toEpochSecond();
    }

    @Override
    public VersionType getType() {
        char lastChar = Character.toLowerCase(this.getId().charAt(this.getId().length() - 1));

        return lastChar >= 'a' && lastChar <= 'z' ? VersionType.SNAPSHOT : VersionType.RELEASE;
    }

    @Override
    public VersionPhase getPhase() {
        SemanticVersion version = SemanticVersion.parse(this.getId());

        if (version == null) {
            Matcher matcher = RegexUtils.THREE_DIGITS.matcher(this.getId());

            if (matcher.find()) {
                String parsedVersion = matcher.group(0);
                version = SemanticVersion.parse(parsedVersion);
            } else {
                Log.warn("ItchVersion: could not determine game version");
            }
        }

        if (version != null && version.getMinor() >= 4) {
            return VersionPhase.ALPHA;
        }

        return VersionPhase.PRE_ALPHA;
    }
}
