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

package me.theentropyshard.crlauncher.crmm.model.project;

import java.util.List;

public class Member {
    private String id;
    private String userId;
    private String teamId;
    private String userName;
    private String avatar;
    private String role;
    private boolean isOwner;
    private boolean accepted;
    private List<Object> permissions;
    private List<Object> organisationPermissions;

    public Member() {

    }

    public String getId() {
        return this.id;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getTeamId() {
        return this.teamId;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public String getRole() {
        return this.role;
    }

    public boolean isOwner() {
        return this.isOwner;
    }

    public boolean isAccepted() {
        return this.accepted;
    }

    public List<Object> getPermissions() {
        return this.permissions;
    }

    public List<Object> getOrganisationPermissions() {
        return this.organisationPermissions;
    }
}