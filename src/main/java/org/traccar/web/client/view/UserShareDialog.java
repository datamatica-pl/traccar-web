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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.StoreFilterField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import org.traccar.web.client.i18n.Messages;
import pl.datamatica.traccar.model.User;

import java.util.*;
import org.traccar.web.client.ApplicationContext;

public class UserShareDialog {

    private static UsersDialogUiBinder uiBinder = GWT.create(UsersDialogUiBinder.class);

    interface UsersDialogUiBinder extends UiBinder<Widget, UserShareDialog> {
    }

    public class UserShared {
        public final User user;
        public boolean shared;

        public UserShared(final User user, boolean shared) {
            this.user = user;
            this.shared = shared;
        }

        public long getId() {
            return user.getId();
        }

        public String getName() {
            return user.getLogin();
        }

        public boolean isShared() {
            return shared;
        }

        public void setShared(boolean shared) {
            this.shared = shared;
        }
    }

    public interface UserSharedProperties extends PropertyAccess<UserShared> {
        ModelKeyProvider<UserShared> id();

        ValueProvider<UserShared, String> name();

        ValueProvider<UserShared, Boolean> shared();
    }

    public interface UserShareHandler {
        void onSaveShares(List<Long> shares, Window window);
    }

    private UserShareHandler shareHandler;

    @UiField
    Window window;

    @UiField(provided = true)
    ColumnModel<UserShared> columnModel;

    @UiField(provided = true)
    ListStore<UserShared> shareStore;

    @UiField
    Grid<UserShared> grid;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);
    
    @UiField(provided = true)
    StoreFilterField<UserShared> userFilter;
    
    /**
     * Read-only user share dialog
     * @param shares
     */
    public UserShareDialog(Set<Long> shares) {
        this(shares, new UserShareHandler(){
            @Override
            public void onSaveShares(List<Long> shares, Window window) {
                //empty
            }
        }, false);
    }

    public UserShareDialog(Set<Long> shares, UserShareHandler shareHandler) {
        this(shares, shareHandler, true);
    }

    private UserShareDialog(Set<Long> shares, UserShareHandler shareHandler, 
            final boolean editable) {
        this.shareHandler = shareHandler;

        List<User> users = new ArrayList<>(ApplicationContext.getInstance().getUsers());
        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getLogin().toLowerCase().compareTo(o2.getLogin().toLowerCase());
            }
        });

        UserSharedProperties userSharedProperties = GWT.create(UserSharedProperties.class);

        shareStore = new ListStore<>(userSharedProperties.id());
        userFilter = new StoreFilterField<UserShared>() {
            @Override
            protected boolean doSelect(Store<UserShared> store, UserShared parent, UserShared item, String filter) {
                return filter.trim().isEmpty() || item.getName().contains(filter);
            }
        };
        userFilter.bind(shareStore);

        for (User user : users) {
            shareStore.add(new UserShared(user, shares.contains(user.getId())));
        }

        List<ColumnConfig<UserShared, ?>> columnConfigList = new LinkedList<>();
        columnConfigList.add(new ColumnConfig<>(userSharedProperties.name(), 25, i18n.name()));

        ColumnConfig<UserShared, Boolean> colManager = new ColumnConfig<>(userSharedProperties.shared(), 25, i18n.share());
        colManager.setCell(new CheckBoxCell() {
            @Override
            public CheckBoxCell.CheckBoxAppearance getAppearance() {
                final CheckBoxCell.CheckBoxAppearance sa = super.getAppearance();
                return new CheckBoxCell.CheckBoxAppearance() {
                    @Override
                    public void render(SafeHtmlBuilder sb, Boolean value, CheckBoxCell.CheckBoxCellOptions opts) {
                        opts.setDisabled(!editable);
                        sa.render(sb, value, opts);
                    }

                    @Override
                    public void setBoxLabel(String boxLabel, XElement parent) {
                        sa.setBoxLabel(boxLabel, parent);
                    }

                    @Override
                    public XElement getInputElement(Element parent) {
                        return sa.getInputElement(parent);
                    }

                    @Override
                    public void onEmpty(Element parent, boolean empty) {
                        sa.onEmpty(parent, empty);
                    }

                    @Override
                    public void onFocus(Element parent, boolean focus) {
                        sa.onFocus(parent, focus);
                    }

                    @Override
                    public void onValid(Element parent, boolean valid) {
                        sa.onValid(parent, valid);
                    }

                    @Override
                    public void setReadOnly(Element parent, boolean readonly) {
                        sa.setReadOnly(parent, readonly);
                    }
                };
            }
            
        });
        columnConfigList.add(colManager);

        columnModel = new ColumnModel<>(columnConfigList);

        uiBinder.createAndBindUi(this);

        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }
    
    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        window.hide();
        shareStore.commitChanges();
        List<Long> uids = new ArrayList<>();
        for(UserShared us : shareStore.getAll())
            if(us.shared)
                uids.add(us.user.getId());
        shareHandler.onSaveShares(uids, window);
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }

}
