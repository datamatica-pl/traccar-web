/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
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
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.api.BasicAuthFilter;
import org.traccar.web.client.model.api.SessionService;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

public class NavView {
    private static NavViewUiBinder uiBinder = GWT.create(NavViewUiBinder.class);
    
    interface NavViewUiBinder extends UiBinder<Widget, NavView> {
    }

    public interface SettingsHandler {
        void onAccountSelected();
        void onPreferencesSelected();
        void onUsersSelected();
        void onApplicationSelected();
        void onNotificationsSelected();
    }

    private final SettingsHandler settingsHandler;

    public interface LogHandler {
        void onShowTrackerServerLog();
        void onShowWrapperLog();
        void onShowAuditLog();
    }

    final LogHandler logHandler;

    public interface GroupsHandler {
        void onShowGroups();
    }

    final GroupsHandler dGroupsHandler;
    final GroupsHandler uGroupsHandler;

    @UiField
    ContentPanel contentPanel;

    public ContentPanel getView() {
        return contentPanel;
    }

    @UiField
    TextButton settingsButton;

    @UiField
    TextButton settingsAccount;

    @UiField
    TextButton settingsPreferences;

    @UiField
    MenuItem settingsUsers;

    @UiField
    MenuItem settingsGlobal;

    @UiField
    MenuItem settingsNotifications;

    @UiField
    TextButton logsButton;

    @UiField
    MenuItem showTrackerServerLog;
    
    @UiField
    MenuItem showAuditLog;

    @UiField
    TextButton groupsButton;
    
    @UiField
    MenuItem dGroupsButton;
    
    @UiField
    MenuItem uGroupsButton;

    @UiField
    TextButton reportsButton;
    
    @UiField
    TextButton userGuideButton;

    @UiField(provided = true)
    final Messages i18n = GWT.create(Messages.class);
    
    @UiField(provided = true)
    Image logo;

    public NavView(SettingsHandler settingsHandler,
                   ListStore<Report> reportListStore,
                   ReportsMenu.ReportHandler reportHandler,
                   LogHandler logHandler,
                   GroupsHandler groupsHandler,
                   GroupsHandler uGroupsHandler) {
        this.settingsHandler = settingsHandler;
        this.logHandler = logHandler;
        this.dGroupsHandler = groupsHandler;
        this.uGroupsHandler = uGroupsHandler;
        
        logo = new Image();
        logo.setUrl("img/logo.png");

        uiBinder.createAndBindUi(this);

        User user = ApplicationContext.getInstance().getUser();

        settingsButton.setVisible(user.hasPermission(UserPermission.SERVER_MANAGEMENT)
            || user.hasPermission(UserPermission.USER_MANAGEMENT));
        settingsAccount.setVisible(true);

        settingsGlobal.setVisible(user.hasPermission(UserPermission.SERVER_MANAGEMENT));
        logsButton.setVisible(user.hasPermission(UserPermission.LOGS_ACCESS));
        showTrackerServerLog.setVisible(user.hasPermission(UserPermission.LOGS_ACCESS));
        showAuditLog.setVisible(user.hasPermission(UserPermission.AUDIT_ACCESS));
        settingsUsers.setVisible(user.hasPermission(UserPermission.USER_MANAGEMENT));
        settingsNotifications.setVisible(user.hasPermission(UserPermission.SERVER_MANAGEMENT));

        if(!user.hasPermission(UserPermission.USER_GROUP_MANAGEMENT) 
                && !user.hasPermission(UserPermission.DEVICE_GROUP_MANAGEMENT))
            groupsButton.setVisible(false);
        dGroupsButton.setVisible(user.hasPermission(UserPermission.DEVICE_GROUP_MANAGEMENT));
        uGroupsButton.setVisible(user.hasPermission(UserPermission.USER_GROUP_MANAGEMENT));

        reportsButton.setMenu(new ReportsMenu(reportHandler, new ReportsMenu.ReportSettingsHandler() {
            @Override
            public void setSettings(ReportsDialog dialog) {
            }
        }));
        reportsButton.setVisible(user.hasPermission(UserPermission.REPORTS));
    }

    @UiHandler("settingsAccount")
    public void onSettingsAccountClicked(SelectEvent event) {
        settingsHandler.onAccountSelected();
    }

    @UiHandler("settingsPreferences")
    public void onSettingsPreferencesClicked(SelectEvent event) {
        settingsHandler.onPreferencesSelected();
    }

    @UiHandler("settingsUsers")
    public void onSettingsUsersSelected(SelectionEvent<Item> event) {
        settingsHandler.onUsersSelected();
    }

    @UiHandler("settingsGlobal")
    public void onSettingsGlobalSelected(SelectionEvent<Item> event) {
        settingsHandler.onApplicationSelected();
    }

    @UiHandler("settingsNotifications")
    public void onSettingsNotificationsSelected(SelectionEvent<Item> event) {
        settingsHandler.onNotificationsSelected();
    }

    @UiHandler("logoutButton")
    public void onLogoutClicked(SelectEvent event) {
        Application.getDataService().logout(new BaseAsyncCallback<Boolean>(i18n) {
            @Override
            public void onSuccess(Boolean result) {
            }
        });
        SessionService session = GWT.create(SessionService.class);
        BasicAuthFilter.getInstance().pushCredentials(":", ":");
        session.logout(new JsonCallback(){
            @Override
            public void onFailure(Method method, Throwable exception) {
            }

            @Override
            public void onSuccess(Method method, JSONValue response) {
                Window.Location.reload();
            }
        });
    }

    @UiHandler("showTrackerServerLog")
    public void onShowTrackerServerLog(SelectionEvent<Item> event) {
        logHandler.onShowTrackerServerLog();
    }

    @UiHandler("showWrapperLog")
    public void onShowWrapperLog(SelectionEvent<Item> event) {
        logHandler.onShowWrapperLog();
    }
    
    @UiHandler("showAuditLog")
    public void onShowAuditLog(SelectionEvent<Item> event) {
        logHandler.onShowAuditLog();
    }

    @UiHandler("dGroupsButton")
    public void onDeviceGroupsClicked(SelectionEvent<Item> event) {
        dGroupsHandler.onShowGroups();
    }
    
    @UiHandler("uGroupsButton")
    public void onUserGroupsClicked(SelectionEvent<Item> event) {
        uGroupsHandler.onShowGroups();
    }
    
    @UiHandler("userGuideButton")
    public void onUserGuideClicked(SelectEvent event) {
        redirectToUserGuide(i18n.userGuideUrl());
    }
    
    public static native void redirectToUserGuide(String guideUrl) /*-{
        $wnd.location = guideUrl;
    }-*/;
}
