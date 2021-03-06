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

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
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
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridSelectionModel;
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
    }
    
    private ColumnModel<ApiUserGroup> prepareColumnModel() {
        UserGroupProperties ugp = new UserGroupProperties();
        List<ColumnConfig<ApiUserGroup, ?>> ccList = new ArrayList<>();
        ColumnConfig<ApiUserGroup, String> cName = new ColumnConfig<>(ugp.name(),
                125, i18n.name());
        cName.setFixed(true);
        cName.setHideable(false);
        ccList.add(cName);
        for(final UserPermission p : UserPermission.values()) {
            ColumnConfig<ApiUserGroup, Boolean> cc = new ColumnConfig<>(ugp.permission(p), 
                    9*p.name().length(), p.name());
            cc.setCell(new CheckBoxCell() {
                @Override
                public void onBrowserEvent(Cell.Context context, Element parent, Boolean value, NativeEvent event, ValueUpdater<Boolean> valueUpdater) {
                    super.onBrowserEvent(context, parent, value, event, valueUpdater);
                    if(!event.getType().equals(BrowserEvents.MOUSEUP))
                        return;
                    
                    ApiUserGroup grp = grid.getStore().get(context.getIndex());
                    Store<ApiUserGroup>.Record record = grid.getStore().getRecord(grp);
                    PermissionManager pm = new PermissionManager();
                    ApiUserGroup before = record.getModel();
                    ApiUserGroup after = new ApiUserGroup(before);
                    for(Store.Change<ApiUserGroup, ?> c : record.getChanges())
                        c.modify(after);
                    if(after.hasPermission(p))
                        after.revokePermission(p);
                    else
                        after.grantPermission(p);

                    if(after.hasPermission(p) && !before.hasPermission(p))
                        for(UserPermission up : pm.getRequiredPermissions(p))
                            if(!after.hasPermission(up))
                                after.grantPermission(up);
                    if(!after.hasPermission(p) && before.hasPermission(p))
                        for(UserPermission up: pm.getRequiringPermissions(p))
                            if(after.hasPermission(up))
                                after.revokePermission(up);
                    after.setChanged(true);
                    grid.getStore().update(after);
                } 
            });
            cc.setFixed(true);
            cc.setResizable(false);
            cc.setHideable(false);
            ccList.add(cc);
        }
        return new ColumnModel<>(ccList);
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
            required = UserPermission.getRequiredPermissions();
            requiring = UserPermission.getRequiringPermissions();
        }
        
        public Set<UserPermission> getRequiredPermissions(UserPermission up) {
            return required.get(up);
        }
        
        public Set<UserPermission> getRequiringPermissions(UserPermission up) {
            return requiring.get(up);
        }
    }
}
