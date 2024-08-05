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

package me.theentropyshard.crlauncher.cosmic.account;

import java.io.IOException;

public abstract class Account {
    public static final String DEFAULT_HEAD_ICON =
        "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAAAXNSR0IArs4c6QAAAARnQU1BAACx" +
        "jwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAOCSURBVFhHrZfLa1NBFMYnadq82iZpmlIUWwUR" +
        "Fy4aqS6qLmqhVHTnRlz4P6igFcGFglRB0ZU7Fy5cuRRsFrWIoChopTQ+EPG9UJpI0rSNfSR+38y9" +
        "NY/7mLT9wZc5M/fmnnNnzjyuR2gyPj4eQXEEGoKS0HYoCvEZOegr9AaahB6OjIxkUbriGgAc70Rx" +
        "DjoJhdmmQRF6AI0hkLRsscE2ADgOorgEnYb8bFsHK9Ad6CICmZMtNVgGAOc7UPAN9sqGjfMeOo4g" +
        "3qrqf+oCgPM9KFLQFtlgQVNTkwiFQqKlpUWUy2WxtLQkFhYWRKlUMu6wJAMdRRAvVFVRFQCc96J4" +
        "Btk6b29vF9FoVHg81bHTeTabFfPz80aLJUzMQ5U9sfYUOOc4P4X2yQYLIpGIdO7E7OysWxAcjv1m" +
        "Tnj5Y3ABsnXe3Nzs6px0dHQIr7fysXXshq4q0+gBvH0PindQiHUrYrGY7H4dOBRzc5ZJb8LZ0Yde" +
        "SJuhnoFsnRMmnC4a9/qgURqeVCoVRCb/hB1jgx3d3d3C79dbDgqFgshkmPSOcLHa6oXzwzAcnZPl" +
        "5WXDckfz3gB0jEMwKKsuuGT2GlwXuCZoMsgA+pTtTLFY1Aoil8uJlRXmmBZ9DICLjxYcV6cg8vm8" +
        "DKABej2Ygr9hJFRdj0AgIMLhsFwbCJdiJh7LBik5rhh2sIs5JBxrirbLPmBHmT3wAcYuVXemtbVV" +
        "tLW12c5z9gCHQTdhQYY98EXZ9vh8PrkOxONxx0WG1zo7O0VXV5fcMTX4zABeK9sa07nuIkSCwaD8" +
        "j0YQUwzgsbLr4ZbbwNtUwcATiUTdtl3DBAN4AnEm1MHdz8z09cBeY87YUIAeebEjce7clU0VcEt1" +
        "+LM23EFteuE+fOfNaXgLyitTwSOXS/dpweFjTtTAjeg6DRkAIvmF4jJtk0aSzg2LZ92Ez080zB4g" +
        "tyF+VEiYRJtFzbNeQVeUWREAIuIOcgL6yLrLsaohKoaS5w4ezzkEkiovuMDZMAzJIDaZH9AwfPAT" +
        "bo2618QNXBkPogeeq5aNg2e9RHEAz3b/MDGZmZnx44+j6L7zqNalsQ44nCyurq5ewwF1bGBg4K/R" +
        "XIXrPEun09sQxFnoFKquRzcCx3+ge9icbiSTye9GsyXaE316ejqIOT2EQHiES6LsgZO4uio/u76h" +
        "PgVNYnue6O/vX1SXnBDiHxj1Hv5Wo7QeAAAAAElFTkSuQmCC";

    private String username;
    private long uniqueId;
    private String headIcon;

    public Account() {

    }

    public Account(String username) {
        this.username = username;
    }

    public abstract void authenticate() throws IOException;

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getHeadIcon() {
        return this.headIcon;
    }

    public void setHeadIcon(String headIcon) {
        this.headIcon = headIcon;
    }
}
