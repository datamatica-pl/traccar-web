/*
 * Copyright 2017 Datamatica (dev@datamatica.pl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.web.client.model.api;

import java.util.EnumSet;
import java.util.Set;
import pl.datamatica.traccar.model.UserGroup;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author ŁŁ
 */
public class ApiUserGroup {
    public long id;
    public String name;
    public Set<UserPermission> permissions;
    private boolean changed = false;
    
    
    public ApiUserGroup() {
        permissions = EnumSet.noneOf(UserPermission.class);
    }

    public ApiUserGroup(ApiUserGroup copy) {
        this.id = copy.id;
        this.name = copy.name;
        if(copy.permissions.isEmpty())
            this.permissions = EnumSet.noneOf(UserPermission.class);
        else
            this.permissions = EnumSet.copyOf(copy.permissions);
    }
    
    public long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean hasPermission(UserPermission p) {
        return permissions.contains(p);
    }
    
    public Set<UserPermission> getPermissions() {
        return EnumSet.copyOf(permissions);
    }
    
    public void clearPermissions() {
        permissions.clear();
    }
    
    public void grantPermission(UserPermission p) {
        permissions.add(p);
    }
    
    public void revokePermission(UserPermission p) {
        permissions.remove(p);
    }
    
    public boolean isChanged() {
        return changed;
    }
    
    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public UserGroup toUserGroup() {
        UserGroup ug = new UserGroup();
        ug.setId(id);
        ug.setName(name);
        ug.setPermissions(EnumSet.copyOf(permissions));
        return ug;
    }
}
