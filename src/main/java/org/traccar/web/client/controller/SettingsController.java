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
import org.traccar.web.shared.model.*;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONValue;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.traccar.web.client.model.api.ApplicationSettingsService;
import org.traccar.web.client.model.api.ApplicationSettingsService.ApplicationSettingsDto;
import org.traccar.web.client.model.api.IUsersService.AddUserDto;
import org.traccar.web.client.model.api.IUsersService.EditUserDto;
import org.traccar.web.client.model.api.UsersService;

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
                                new RequestCallback() {
                            @Override
                            public void onResponseReceived(Request request, Response response) {
                                ApplicationContext.getInstance().setUser(user);
                            }

                            @Override
                            public void onError(Request request, Throwable exception) {
                                new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
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
        users.getUsers(new JsonCallback() {
            @Override
            public void onFailure(Method method, Throwable exception) {
                new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
            }

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
                                users.addUser(new AddUserDto(user), new JsonCallback() {
                                    @Override
                                    public void onFailure(Method method, Throwable exception) {
                                        new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                                    }

                                    @Override
                                    public void onSuccess(Method method, JSONValue response) {
                                        User u = Application.getDecoder().decodeUser(response.isObject());
                                        ApplicationContext.getInstance().addUser(u);
                                        userStore.add(u);
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
                                        new RequestCallback() {
                                    @Override
                                    public void onResponseReceived(Request request, Response response) {
                                    }

                                    @Override
                                    public void onError(Request request, Throwable exception) {
                                        new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
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
                                    Application.getDataService().removeUser(user, new BaseAsyncCallback<User>(i18n) {
                                        @Override
                                        public void onSuccess(User result) {
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
                                    new RequestCallback() {
                                        @Override
                                        public void onResponseReceived(Request request, Response response) {
                                            record.commit(false);
                                        }

                                        @Override
                                        public void onError(Request request, Throwable exception) {
                                            new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
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
                                    final String oldPassword = user.getPassword();
                                    user.setPassword(passwordInput.getValue());
                                    users.updateUser(user.getId(), new EditUserDto(user),
                                            new RequestCallback() {
                                        @Override
                                        public void onResponseReceived(Request request, Response response) {
                                        }

                                        @Override
                                        public void onError(Request request, Throwable exception) {
                                            new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
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
        Application.getDataService().getUsers(new BaseAsyncCallback<List<User>>(i18n) {
            @Override
            public void onSuccess(List<User> result) {
                
            }
        });
    }

    @Override
    public void onApplicationSelected() {
        final ApplicationSettingsService appSettings = GWT.create(ApplicationSettingsService.class);
        new ApplicationSettingsDialog(
                ApplicationContext.getInstance().getApplicationSettings(),
                new ApplicationSettingsDialog.ApplicationSettingsHandler() {
                    @Override
                    public void onSave(final ApplicationSettings applicationSettings) {
                        appSettings.update(ApplicationSettingsDto.create(applicationSettings), 
                                new JsonCallback() {
                            @Override
                            public void onFailure(Method method, Throwable exception) {
                                new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                            }

                            @Override
                            public void onSuccess(Method method, JSONValue response) {
                                ApplicationContext.getInstance().setApplicationSettings(applicationSettings);
                            }
                                });
                    }
                }).show();
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
