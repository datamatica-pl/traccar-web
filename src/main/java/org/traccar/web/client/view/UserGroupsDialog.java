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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import java.util.ArrayList;
import java.util.List;
import org.traccar.web.client.controller.GroupsController;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.UserGroupProperties;
import pl.datamatica.traccar.model.UserGroup;
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
    ColumnModel<UserGroup> columnModel;
    
    @UiField(provided = true)
    ListStore<UserGroup> userGroupsStore;
    
    @UiField
    Grid<UserGroup> grid;
    
    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);
    
    private final UserGroupsHandler handler;
    
    public UserGroupsDialog(ListStore<UserGroup> userGroupsStore, UserGroupsHandler handler) {
        this.handler = handler;
        this.userGroupsStore = userGroupsStore;
        this.columnModel = prepareColumnModel();
        uiBinder.createAndBindUi(this);
    }
    
    private ColumnModel<UserGroup> prepareColumnModel() {
        UserGroupProperties ugp = new UserGroupProperties();
        List<ColumnConfig<UserGroup, ?>> ccList = new ArrayList<>();
        ccList.add(new ColumnConfig<>(ugp.name(), 125, i18n.name()));
        for(UserPermission p : UserPermission.values()) {
            ColumnConfig<UserGroup, Boolean> cc = new ColumnConfig<>(ugp.permission(p), 90, p.name());
            cc.setCell(new CheckBoxCell());
            cc.setFixed(true);
            cc.setResizable(false);
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
        void onEdit(UserGroup group);
        void onRemove(UserGroup group);
        void onSave();
    }
}
