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
package org.traccar.web.client.controller;

import com.sencha.gxt.data.shared.ListStore;
import org.traccar.web.client.view.NavView;
import org.traccar.web.client.view.UserGroupDialog;
import org.traccar.web.client.view.UserGroupsDialog;
import pl.datamatica.traccar.model.UserGroup;

/**
 *
 * @author ŁŁ
 */
public class UserGroupsController implements NavView.GroupsHandler, 
        UserGroupsDialog.UserGroupsHandler{
    private final ListStore<UserGroup> userGroups;
    
    public UserGroupsController(ListStore<UserGroup> userGroups) {
        this.userGroups = userGroups;
    }
    
    public void onShowGroups() {
        new UserGroupsDialog(userGroups, this).show();
    }

    @Override
    public void onAdd() {
        UserGroup group = new UserGroup();
        new UserGroupDialog(group, userGroups, new UserGroupDialog.UserGroupHandler() {
            @Override
            public void onSave(UserGroup group) {
                userGroups.add(group);
            }
        }).show();
    }

    @Override
    public void onEdit(UserGroup group) {
        new UserGroupDialog(group, null, new UserGroupDialog.UserGroupHandler() {
            @Override
            public void onSave(UserGroup group) {
                
            }  
        }).show();
    }

    @Override
    public void onRemove(UserGroup group) {
        userGroups.remove(group);
    }

    @Override
    public void onSave() {
        
    }
}
