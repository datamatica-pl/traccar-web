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
package org.traccar.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.CompleteEditEvent;
import com.sencha.gxt.widget.core.client.event.CompleteEditEvent.CompleteEditHandler;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.GridSelectionModel;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.UserGroupProperties;
import org.traccar.web.client.model.api.ApiUserGroup;
import pl.datamatica.traccar.model.Group;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author ŁŁ
 */
public class UserGroupsDialog {
    private static UserGroupsDialogUiBinder uiBinder = GWT.create(UserGroupsDialogUiBinder.class);
    
    interface UserGroupsDialogUiBinder extends UiBinder<Widget, UserGroupsDialog> {
    }
    
    @UiField
    Window window;
    
    @UiField(provided = true)
    ColumnModel<ApiUserGroup> columnModel;
    
    @UiField(provided = true)
    ListStore<ApiUserGroup> userGroupsStore;
    
    @UiField
    Grid<ApiUserGroup> grid;
    
    @UiField
    TextButton editButton;
    
    @UiField
    TextButton removeButton;
    
    @UiField
    TextButton showUsersButton;
    
    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);
    
    private final UserGroupsHandler handler;
    
    public UserGroupsDialog(ListStore<ApiUserGroup> userGroupsStore, UserGroupsHandler handler) {
        this.handler = handler;
        this.userGroupsStore = userGroupsStore;
        this.columnModel = prepareColumnModel();
        uiBinder.createAndBindUi(this);
        
        GridSelectionModel<ApiUserGroup> selModel = new GridSelectionModel<>();
        selModel.setSelectionMode(Style.SelectionMode.SINGLE);
        selModel.addSelectionHandler(new SelectionHandler<ApiUserGroup>() { 
            @Override
            public void onSelection(SelectionEvent<ApiUserGroup> event) {
                onSelectionChanged(event.getSelectedItem());
            }
        });
        onSelectionChanged(null);
        grid.setSelectionModel(selModel);
        prepareEditing(grid);
    }
    
    private ColumnModel<ApiUserGroup> prepareColumnModel() {
        UserGroupProperties ugp = new UserGroupProperties();
        List<ColumnConfig<ApiUserGroup, ?>> ccList = new ArrayList<>();
        ColumnConfig<ApiUserGroup, String> cName = new ColumnConfig<>(ugp.name(),
                125, i18n.name());
        cName.setFixed(true);
        cName.setHideable(false);
        ccList.add(cName);
        for(UserPermission p : UserPermission.values()) {
            ColumnConfig<ApiUserGroup, Boolean> cc = new ColumnConfig<>(ugp.permission(p), 
                    9*p.name().length(), p.name());
            cc.setFixed(true);
            cc.setResizable(false);
            cc.setHideable(false);
            ccList.add(cc);
        }
        return new ColumnModel<>(ccList);
    }
    
    private GridEditing<ApiUserGroup> prepareEditing(final Grid<ApiUserGroup> grid) {
        final UserGroupProperties ugp = new UserGroupProperties();
        GridInlineEditing<ApiUserGroup> editing = new GridInlineEditing<>(grid);
        for(int i=1;i<grid.getColumnModel().getColumnCount();++i) {
            ColumnConfig<ApiUserGroup, Boolean> cc = grid.getColumnModel().getColumn(i);
            editing.addEditor(cc, new CheckBox());
        }
        editing.addCompleteEditHandler(new CompleteEditHandler<ApiUserGroup>() {
            @Override
            public void onCompleteEdit(CompleteEditEvent<ApiUserGroup> event) {
                GridCell cell = event.getEditCell();
                UserPermission p = UserPermission.values()[cell.getCol()-1];
                Store<ApiUserGroup>.Record record = grid.getStore().getRecord(grid.getSelectionModel().getSelectedItem());
                PermissionManager pm = new PermissionManager();
                ApiUserGroup before = record.getModel();
                ApiUserGroup after = new ApiUserGroup(before);
                for(Store.Change<ApiUserGroup, ?> c : record.getChanges())
                    c.modify(after);
                
                if(after.hasPermission(p) && !before.hasPermission(p))
                    for(UserPermission up : pm.getRequiredPermissions(p))
                        if(!after.hasPermission(up))
                            after.grantPermission(up);
                if(!after.hasPermission(p) && before.hasPermission(p))
                    for(UserPermission up: pm.getRequiringPermissions(p))
                        if(after.hasPermission(up))
                            after.revokePermission(up);
                grid.getStore().update(after);
            }
        });
        return editing;
    }

    public void show() {
        window.show();
    }
    
    public void hide() {
        window.hide();
    }
    
    public void onSelectionChanged(ApiUserGroup group) {
        boolean anythingSelected = group != null;
        editButton.setEnabled(anythingSelected);
        if(anythingSelected) {
            removeButton.setEnabled(group.getId() != ApplicationContext.getInstance().getApplicationSettings().getDefaultGroupId());
        } else {
            removeButton.setEnabled(false);
        }
        showUsersButton.setEnabled(anythingSelected);
    }
    
    @UiHandler("addButton")
    public void onAddClicked(SelectEvent event) {
        handler.onAdd();
    }
    
    @UiHandler("editButton")
    public void onEditClicked(SelectEvent event) {
        ApiUserGroup grp = grid.getSelectionModel().getSelectedItem();
        if(grp != null)
            handler.onEdit(grp);
    }
    
    @UiHandler("removeButton")
    public void onRemoveClicked(SelectEvent event) {
        final ApiUserGroup grp = grid.getSelectionModel().getSelectedItem();
        if(grp != null) {
            ConfirmMessageBox mb = new ConfirmMessageBox(i18n.confirmation(), 
                    i18n.actionNotReversible());
            mb.addDialogHideHandler(new DialogHideHandler() {
                @Override
                public void onDialogHide(DialogHideEvent event) {
                    switch(event.getHideButton()) {
                        case YES:
                            handler.onRemove(grp);
                            break;
                    }
                } 
            });
            mb.show();
        }
    }
    
    @UiHandler("showUsersButton")
    public void onShowUsersClicked(SelectEvent event) {
        ApiUserGroup grp = grid.getSelectionModel().getSelectedItem();
        if(grp != null)
            handler.onShowUsers(grp);
    }
    
    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        handler.onSave();
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }
    
    public static interface UserGroupsHandler {
        void onAdd();
        void onEdit(ApiUserGroup group);
        void onRemove(ApiUserGroup group);
        void onSave();
        void onShowUsers(ApiUserGroup group);
    }
    
        public static class PermissionManager {
        private final Map<UserPermission, Set<UserPermission>> required;
        private final Map<UserPermission, Set<UserPermission>> requiring;
        private static final PermissionManager INSTANCE = new PermissionManager();
        
        public static PermissionManager get() {
            return INSTANCE;
        }
        
        public PermissionManager() {
            required = new EnumMap<>(UserPermission.class);
            requiring = new EnumMap<>(UserPermission.class);
            
            required.put(UserPermission.DEVICE_EDIT, Collections.EMPTY_SET);
            required.put(UserPermission.DEVICE_SHARE, EnumSet.of(UserPermission.USER_MANAGEMENT));
            required.put(UserPermission.GEOFENCE_READ, Collections.EMPTY_SET);
            required.put(UserPermission.GEOFENCE_EDIT, EnumSet.of(UserPermission.GEOFENCE_READ));
            required.put(UserPermission.GEOFENCE_SHARE, EnumSet.of(UserPermission.GEOFENCE_READ, UserPermission.USER_MANAGEMENT));
            required.put(UserPermission.TRACK_READ, EnumSet.of(UserPermission.GEOFENCE_READ));
            required.put(UserPermission.TRACK_EDIT, EnumSet.of(UserPermission.GEOFENCE_READ, UserPermission.GEOFENCE_EDIT, UserPermission.TRACK_READ));
            required.put(UserPermission.TRACK_SHARE, EnumSet.of(UserPermission.GEOFENCE_READ, UserPermission.TRACK_READ, UserPermission.GEOFENCE_SHARE));
            required.put(UserPermission.HISTORY_READ, Collections.EMPTY_SET);
            required.put(UserPermission.COMMAND_TCP, Collections.EMPTY_SET);
            required.put(UserPermission.COMMAND_SMS, Collections.EMPTY_SET);
            required.put(UserPermission.COMMAND_CUSTOM, EnumSet.of(UserPermission.COMMAND_TCP));
            required.put(UserPermission.DEVICE_STATS, EnumSet.of(UserPermission.HISTORY_READ));
            required.put(UserPermission.REPORTS, EnumSet.of(UserPermission.HISTORY_READ));
            required.put(UserPermission.ALERTS_READ, EnumSet.of(UserPermission.HISTORY_READ));
            required.put(UserPermission.NOTIFICATIONS, EnumSet.of(UserPermission.HISTORY_READ, UserPermission.ALERTS_READ));
            required.put(UserPermission.DEVICE_GROUP_MANAGEMENT, EnumSet.of(UserPermission.DEVICE_EDIT, UserPermission.DEVICE_SHARE));
            required.put(UserPermission.ALL_DEVICES, EnumSet.of(UserPermission.DEVICE_EDIT, UserPermission.DEVICE_SHARE, UserPermission.HISTORY_READ, UserPermission.COMMAND_TCP, UserPermission.COMMAND_SMS, UserPermission.DEVICE_STATS, UserPermission.REPORTS, UserPermission.ALERTS_READ, UserPermission.DEVICE_GROUP_MANAGEMENT));
            required.put(UserPermission.ALL_TRACKS, EnumSet.of(UserPermission.GEOFENCE_READ, UserPermission.GEOFENCE_EDIT, UserPermission.TRACK_READ, UserPermission.TRACK_EDIT, UserPermission.TRACK_SHARE));
            required.put(UserPermission.USER_MANAGEMENT, Collections.EMPTY_SET);
            required.put(UserPermission.ALL_USERS, EnumSet.of(UserPermission.USER_MANAGEMENT));
            required.put(UserPermission.USER_GROUP_MANAGEMENT, EnumSet.of(UserPermission.USER_MANAGEMENT, UserPermission.ALL_USERS, UserPermission.SERVER_MANAGEMENT));
            required.put(UserPermission.RESOURCE_MANAGEMENT, Collections.EMPTY_SET);
            required.put(UserPermission.LOGS_ACCESS, EnumSet.of(UserPermission.GEOFENCE_READ, UserPermission.TRACK_READ, UserPermission.HISTORY_READ));
            required.put(UserPermission.AUDIT_ACCESS, EnumSet.of(UserPermission.GEOFENCE_READ, UserPermission.TRACK_READ, UserPermission.HISTORY_READ, UserPermission.LOGS_ACCESS));
            required.put(UserPermission.SERVER_MANAGEMENT, Collections.EMPTY_SET);
            required.put(UserPermission.ALLOW_MOBILE, Collections.EMPTY_SET);
            
            for(UserPermission up : UserPermission.values()) {
                EnumSet<UserPermission> requiringSet = EnumSet.noneOf(UserPermission.class);
                for(UserPermission perm : required.keySet())
                    if(required.get(perm).contains(up))
                        requiringSet.add(perm);
                requiring.put(up, requiringSet);
            }
        }
        
        public Set<UserPermission> getRequiredPermissions(UserPermission up) {
            return required.get(up);
        }
        
        public Set<UserPermission> getRequiringPermissions(UserPermission up) {
            return requiring.get(up);
        }
    }
}
