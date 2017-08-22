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
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.UserProperties;
import org.traccar.web.client.utils.ConstValueProvider;
import pl.datamatica.traccar.model.User;

/**
 *
 * @author ŁŁ
 */
public class UserGroupUsersDialog {
    
    private static UserGroupUsersDialogUiBinder uiBinder = GWT.create(UserGroupUsersDialogUiBinder.class);
    private static Resources R = GWT.create(Resources.class);
    
    interface UserGroupUsersDialogUiBinder extends UiBinder<Widget, UserGroupUsersDialog> {
    }
    
    @UiField
    Window window;
    
    @UiField(provided = true)
    ColumnModel<User> columnModel;
    
    @UiField(provided = true)
    ListStore<User> usersStore;
    
    @UiField
    Grid<User> grid;
    
    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);
    private UserGroupUsersHandler handler;
    
    public UserGroupUsersDialog(String groupName, ListStore<User> usersStore, UserGroupUsersHandler handler) {
        this.usersStore = usersStore;
        columnModel = prepareColumnModel();
        uiBinder.createAndBindUi(this);
        window.setHeadingText(i18n.userGroupUsersTitle(groupName));
        grid.setHideHeaders(true);
        this.handler = handler;
    }
    
    private ColumnModel<User> prepareColumnModel() {
        List<ColumnConfig<User, ?>> ccList = new ArrayList<>();
        UserProperties userProps = GWT.create(UserProperties.class);
        ColumnConfig<User, String> cLogin = new ColumnConfig<>(userProps.login(),
            200, "");
        ccList.add(cLogin);
        
        ColumnConfig<User, ImageResource> cDelete = new ColumnConfig<>(
                new ConstValueProvider<User, ImageResource>(R.remove()), 24, "");
        cDelete.setCell(new ImageResourceCell() {
            @Override
            public Set<String> getConsumedEvents() {
                return Collections.singleton("click");
            }
            
            
            @Override
            public void onBrowserEvent(Cell.Context context, Element parent, ImageResource value,
                    NativeEvent event, ValueUpdater<ImageResource> valueUpdater) {
                super.onBrowserEvent(context, parent, value, event, valueUpdater);
                final User u = usersStore.get(context.getIndex());
                ConfirmMessageBox mb = new ConfirmMessageBox(i18n.confirmation(), 
                    i18n.actionNotReversible());
                mb.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
                    @Override
                    public void onDialogHide(DialogHideEvent event) {
                        switch(event.getHideButton()) {
                            case YES:
                                handler.onUserRemovedFromGroup(u);
                                break;
                        }
                    } 
                });
                mb.show();
            }
            
        });
        ccList.add(cDelete);
        return new ColumnModel<>(ccList);
    }
    
    public void show() {
        window.show();
    }
    
    static interface Resources extends ClientBundle {
        @ClientBundle.Source("org/traccar/web/client/theme/icon/remove.png")
        ImageResource remove();
    }
    
    public static interface UserGroupUsersHandler {
        void onUserRemovedFromGroup(User u);
    }
}
