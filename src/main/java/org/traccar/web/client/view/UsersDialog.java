/*
 * Copyright 2013 Anton Tananaev (anton.tananaev@gmail.com)
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

import java.util.Date;
import java.util.List;

import com.sencha.gxt.cell.core.client.NumberCell;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
import com.sencha.gxt.cell.core.client.form.DateCell;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.UserProperties;
import pl.datamatica.traccar.model.User;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.StoreFilterField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
import java.util.ArrayList;
import pl.datamatica.traccar.model.UserGroup;
import pl.datamatica.traccar.model.UserPermission;

public class UsersDialog implements SelectionChangedEvent.SelectionChangedHandler<User> {

    private static UsersDialogUiBinder uiBinder = GWT.create(UsersDialogUiBinder.class);

    interface UsersDialogUiBinder extends UiBinder<Widget, UsersDialog> {
    }

    public interface UserHandler {
        void onAdd();
        void onEdit(User user);
        void onRemove(User user);
        void onChangePassword(User user);
        void onSaveRoles();
    }

    private UserHandler userHandler;

    @UiField
    Window window;

    @UiField
    TextButton addButton;

    @UiField
    TextButton editButton;

    @UiField
    TextButton removeButton;

    @UiField
    TextButton changePasswordButton;

    @UiField(provided = true)
    ColumnModel<User> columnModel;

    @UiField(provided = true)
    StoreFilterField<User> userFilter;
    
    @UiField(provided = true)
    ListStore<User> userStore;

    @UiField
    Grid<User> grid;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    public UsersDialog(ListStore<User> userStore, UserHandler userHandler) {
        this.userStore = userStore;
        this.userHandler = userHandler;

        UserProperties userProperties = GWT.create(UserProperties.class);

        columnModel = prepareColumnModel(userProperties);
        
        userFilter = new StoreFilterField<User>() {
            @Override
            protected boolean doSelect(Store<User> store, User parent, User item, String filter) {
                return filter.trim().isEmpty() || item.getLogin().contains(filter);
            }
        };
        userFilter.bind(this.userStore);

        uiBinder.createAndBindUi(this);
        
        if(!ApplicationContext.getInstance().getUser().hasPermission(UserPermission.USER_MANAGEMENT))
            removeButton.setVisible(false);
        grid.getSelectionModel().addSelectionChangedHandler(this);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private ColumnModel<User> prepareColumnModel(UserProperties userProperties) {
        List<ColumnConfig<User, ?>> columnConfigList = new ArrayList<>();
        columnConfigList.add(new ColumnConfig<>(userProperties.login(), 25, i18n.name()));
        
        ColumnConfig<User, String> colGroup = new ColumnConfig<>(userProperties.userGroupName(), 100, i18n.group());
        colGroup.setFixed(true);
        colGroup.setResizable(false);
        columnConfigList.add(colGroup);
        
        ColumnConfig<User, Boolean> colBlocked = new ColumnConfig<>(userProperties.blocked(), 75, i18n.blocked());
        colBlocked.setCell(new CheckBoxCell());
        colBlocked.setFixed(true);
        colBlocked.setResizable(false);
        columnConfigList.add(colBlocked);
        
        ColumnConfig<User, Date> colExpirationDate = new ColumnConfig<>(userProperties.expirationDate(), 165, i18n.expirationDate());
        colExpirationDate.setCell(new DateCell());
        colExpirationDate.setFixed(true);
        colExpirationDate.setResizable(false);
        columnConfigList.add(colExpirationDate);
        
        ColumnConfig<User, Integer> colMaxNumOfDevices = new ColumnConfig<>(userProperties.maxNumOfDevices(), 200, i18n.maxNumOfDevices());
        colMaxNumOfDevices.setCell(new NumberCell<Integer>());
        colMaxNumOfDevices.setFixed(true);
        colMaxNumOfDevices.setResizable(false);
        columnConfigList.add(colMaxNumOfDevices);
        
        GridEditing<User> editing = new GridInlineEditing<>(grid);
        NumberField<Integer> maxNumOfDevicesEditor = new NumberField<>(new NumberPropertyEditor.IntegerPropertyEditor());
        maxNumOfDevicesEditor.setAllowDecimals(false);
        maxNumOfDevicesEditor.setAllowBlank(true);
        maxNumOfDevicesEditor.setAllowNegative(false);
        editing.addEditor(colMaxNumOfDevices, maxNumOfDevicesEditor);
        
        return new ColumnModel<>(columnConfigList);
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent<User> event) {
        editButton.setEnabled(!event.getSelection().isEmpty());
        changePasswordButton.setEnabled(!event.getSelection().isEmpty());
        if(event.getSelection().isEmpty()) {
            changePasswordButton.setEnabled(false);
        } else {
            removeButton.setEnabled(!ApplicationContext.getInstance().getUser()
                    .equals(event.getSelection().get(0)));
        }
    }

    @UiHandler("addButton")
    public void onAddClicked(SelectEvent event) {
        userHandler.onAdd();
    }

    @UiHandler("editButton")
    public void onEditClicked(SelectEvent event) {
        userHandler.onEdit(grid.getSelectionModel().getSelectedItem());
    }

    @UiHandler("removeButton")
    public void onRemoveClicked(SelectEvent event) {
        userHandler.onRemove(grid.getSelectionModel().getSelectedItem());
    }

    @UiHandler("changePasswordButton")
    public void onChangePasswordClicked(SelectEvent event) {
        userHandler.onChangePassword(grid.getSelectionModel().getSelectedItem());
    }

    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        window.hide();
        userHandler.onSaveRoles();
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }
}
