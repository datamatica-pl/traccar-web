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
package org.traccar.web.client.controller;

import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.NotificationSettings;
import pl.datamatica.traccar.model.NotificationTemplate;
import pl.datamatica.traccar.model.ApplicationSettings;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.box.AbstractInputMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.form.PasswordField;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.NotificationService;
import org.traccar.web.client.model.NotificationServiceAsync;
import org.traccar.web.client.model.UserProperties;
import org.traccar.web.client.view.*;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONValue;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import org.fusesource.restygwt.client.Method;
import org.traccar.web.client.model.api.ApiJsonCallback;
import org.traccar.web.client.model.api.ApiMethodCallback;
import org.traccar.web.client.model.api.ApiRequestCallback;
import org.traccar.web.client.model.api.ApiUserGroup;
import org.traccar.web.client.model.api.ApplicationSettingsService;
import org.traccar.web.client.model.api.ApplicationSettingsService.ApplicationSettingsDto;
import org.traccar.web.client.model.api.IUsersService.AddUserDto;
import org.traccar.web.client.model.api.IUsersService.EditUserDto;
import org.traccar.web.client.model.api.UserGroupsService;
import org.traccar.web.client.model.api.UsersService;
import org.traccar.web.client.widget.InfoMessageBox;
import pl.datamatica.traccar.model.UserPermission;

public class SettingsController implements NavView.SettingsHandler {

    private Messages i18n = GWT.create(Messages.class);
    private final UserSettingsDialog.UserSettingsHandler userSettingsHandler;
    private UsersService users = new UsersService();

    public SettingsController(UserSettingsDialog.UserSettingsHandler userSettingsHandler) {
        this.userSettingsHandler = userSettingsHandler;
    }

    @Override
    public void onAccountSelected() {
        new UserDialog(
                ApplicationContext.getInstance().getUser(),
                new UserDialog.UserHandler() {
                    @Override
                    public void onSave(final User user) {
                        users.updateUser(user.getId(), new EditUserDto(user),
                                new ApiRequestCallback(i18n) {           
                            @Override
                            public void onSuccess(String response) {
                                ApplicationContext.getInstance().setUser(user);
                            }
                                });
                    }
                }).show();
    }

    @Override
    public void onPreferencesSelected() {
        new UserSettingsDialog(ApplicationContext.getInstance().getUserSettings(), userSettingsHandler).show();
    }

    @Override
    public void onUsersSelected() {
        users.getUsers(new ApiJsonCallback(i18n) {
            @Override
            public void onSuccess(Method method, JSONValue response) {
                List<User> result = Application.getDecoder().decodeUsers(response.isArray());
                UserProperties userProperties = GWT.create(UserProperties.class);
                final ListStore<User> userStore = new ListStore<>(userProperties.id());
                userStore.addAll(result);

                new UsersDialog(userStore, new UsersDialog.UserHandler() {

                    @Override
                    public void onAdd() {
                        class AddHandler implements UserDialog.UserHandler {
                            @Override
                            public void onSave(final User user) {
                                users.addUser(new AddUserDto(user), new ApiJsonCallback(i18n) {
                                    
                                    @Override
                                    public void onSuccess(Method method, JSONValue response) {
                                        User u = Application.getDecoder().decodeUser(response.isObject());
                                        ApplicationContext.getInstance().addUser(u);
                                        userStore.add(u);
                                        new InfoMessageBox(i18n.userAddedTitle(), i18n.userAddedMessage()).show();
                                    }
                                    
                                });
                            }
                        }

                        new UserDialog(new User(), new AddHandler()).show();
                    }
                    
                    @Override
                    public void onEdit(final User user) {
                        class EditHandler implements UserDialog.UserHandler {
                            @Override
                            public void onSave(final User user) {
                                users.updateUser(user.getId(), new EditUserDto(user),
                                        new ApiRequestCallback(i18n) {

                                    @Override
                                    public void onSuccess(String response) {
                                        userStore.update(user);
                                    }
                                        });
                            }
                        }
                        new UserDialog(user, new EditHandler()).show();
                    }

                    @Override
                    public void onRemove(final User user) {
                        final ConfirmMessageBox dialog = new ConfirmMessageBox(i18n.confirm(), i18n.confirmUserRemoval());
                        dialog.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
                                @Override
                                public void onDialogHide(DialogHideEvent event) {
                                        if (event.getHideButton() == PredefinedButton.YES) {
                                            users.deleteUser(user.getId(), new ApiRequestCallback(i18n) {
                                                @Override
                                                public void onSuccess(String response) {
                                                    userStore.remove(user);
                                                }

                                            });
                                        }
                                }
                        });
                        dialog.show();
                    }
                    
                    @Override
                    public void onSaveRoles() {
                        UsersService users = new UsersService();
                        for (final Store<User>.Record record : userStore.getModifiedRecords()) {
                            User updatedUser = new User(record.getModel());
                            for (Store.Change<User, ?> change : record.getChanges()) {
                                change.modify(updatedUser);
                            }
                            users.updateUser(updatedUser.getId(), new EditUserDto(updatedUser),
                                    new ApiRequestCallback(i18n) {
                                        @Override
                                        public void onSuccess(String response) {
                                            record.commit(false);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onChangePassword(final User user) {
                        final AbstractInputMessageBox passwordInput = new AbstractInputMessageBox(new PasswordField(), i18n.changePassword(), i18n.enterNewPassword(user.getLogin())) {};
                        passwordInput.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
                            @Override
                            public void onDialogHide(DialogHideEvent event) {
                                if (event.getHideButton() == PredefinedButton.OK) {
                                    user.setPassword(passwordInput.getValue());
                                    users.updateUser(user.getId(), new EditUserDto(user),
                                            new ApiRequestCallback(i18n) {

                                        @Override
                                        public void onSuccess(String response) {
                                        }
                                            });
                                }
                            }
                        });
                        passwordInput.show();
                    }
                }).show();
            }
        
        });
    }

    @Override
    public void onApplicationSelected() {
        final ApplicationSettingsService appSettings = GWT.create(ApplicationSettingsService.class);
        UserGroupsService userGroups = new UserGroupsService();
        final ApplicationSettingsDialog.ApplicationSettingsHandler handler = 
            new ApplicationSettingsDialog.ApplicationSettingsHandler() {
                @Override
                public void onSave(final ApplicationSettings applicationSettings) {
                    appSettings.update(ApplicationSettingsDto.create(applicationSettings), 
                            new ApiJsonCallback(i18n) {
                        @Override
                        public void onSuccess(Method method, JSONValue response) {
                            ApplicationContext.getInstance().setApplicationSettings(applicationSettings);
                        }
                            });
                }
            };
        if(ApplicationContext.getInstance().getUser().hasPermission(UserPermission.USER_GROUP_MANAGEMENT)) {
            userGroups.getGroups(new ApiMethodCallback<List<ApiUserGroup>>(i18n) {
                @Override
                public void onSuccess(Method method, List<ApiUserGroup> response) {
                    new ApplicationSettingsDialog(
                    ApplicationContext.getInstance().getApplicationSettings(),
                            handler, response).show();
                }
            });
        } else {
            new ApplicationSettingsDialog(ApplicationContext.getInstance().getApplicationSettings(),
                handler, null).show();
        }
    }

    @Override
    public void onNotificationsSelected() {
        final NotificationServiceAsync service = GWT.create(NotificationService.class);
        service.getSettings(new BaseAsyncCallback<NotificationSettings>(i18n) {
            @Override
            public void onSuccess(NotificationSettings settings) {
                if (settings == null) {
                    settings = new NotificationSettings();
                }
                new NotificationSettingsDialog(settings, new NotificationSettingsDialog.NotificationSettingsHandler() {
                    @Override
                    public void onSave(NotificationSettings notificationSettings) {
                        service.saveSettings(notificationSettings, new BaseAsyncCallback<Void>(i18n));
                    }

                    @Override
                    public void onTestEmail(NotificationSettings notificationSettings) {
                        service.checkEmailSettings(notificationSettings, new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                new AlertMessageBox(i18n.notificationSettings(), i18n.testFailed() + "<br><br>" + caught.getLocalizedMessage()).show();
                            }

                            @Override
                            public void onSuccess(Void aVoid) {
                                MessageBox messageBox = new MessageBox(i18n.notificationSettings(), i18n.testSucceeded());
                                messageBox.setIcon(MessageBox.ICONS.info());
                                messageBox.show();
                            }
                        });
                    }

                    @Override
                    public void onTestPushbullet(NotificationSettings notificationSettings) {
                        service.checkPushbulletSettings(notificationSettings, new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                new AlertMessageBox(i18n.notificationSettings(), i18n.testFailed() + "<br><br>" + caught.getLocalizedMessage()).show();
                            }

                            @Override
                            public void onSuccess(Void aVoid) {
                                MessageBox messageBox = new MessageBox(i18n.notificationSettings(), i18n.testSucceeded());
                                messageBox.setIcon(MessageBox.ICONS.info());
                                messageBox.show();
                            }
                        });
                    }

                    @Override
                    public void onTestMessageTemplate(NotificationTemplate template) {
                        service.checkTemplate(template, new AsyncCallback<String>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                new AlertMessageBox(i18n.notificationSettings(), i18n.testFailed() + "<br><br>" + caught.getLocalizedMessage()).show();
                            }

                            @Override
                            public void onSuccess(String result) {
                                new MessageBox(i18n.notificationSettings(), result).show();
                            }
                        });
                    }
                }).show();
            }
        });
    }
}
