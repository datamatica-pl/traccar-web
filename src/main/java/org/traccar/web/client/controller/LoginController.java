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

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.view.LoginDialog;
import pl.datamatica.traccar.model.User;

import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.traccar.web.client.model.api.BasicAuthFilter;
import org.traccar.web.client.model.api.SessionService;
import org.traccar.web.client.model.api.UsersService;
import org.traccar.web.client.widget.InfoMessageBox;
import org.traccar.web.shared.model.UserBlockedException;
import org.traccar.web.shared.model.UserExpiredException;

public class LoginController implements LoginDialog.LoginHandler {

    private LoginDialog dialog;

    private Messages i18n = GWT.create(Messages.class);

    public interface LoginHandler {
        void onLogin();
    }

    private LoginHandler loginHandler;

    public void login(final LoginHandler loginHandler) {
        this.loginHandler = loginHandler;

        Application.getDataService().authenticated(new BaseAsyncCallback<User>(i18n) {
            @Override
            public void onSuccess(User result) {
                if (result == null) {
                    dialog = new LoginDialog(LoginController.this);
                    hideLoadingDiv();
                    dialog.show();
                } else {
                    ApplicationContext.getInstance().setUser(result);
                    hideLoadingDiv();
                    loginHandler.onLogin();
                }
            }

            void hideLoadingDiv() {
                RootPanel.getBodyElement().removeChild(DOM.getElementById("loading"));
            }
        });
    }

    private boolean validate(String login, String password) {
        if (login == null || login.isEmpty() || password == null || password.isEmpty()) {
            new AlertMessageBox(i18n.error(), i18n.errUsernameOrPasswordEmpty()).show();
            return false;
        }
        return true;
    }
    
    private boolean validate(String login, String imei, String password) {
        if(imei == null || !imei.matches("^\\d+$")) {
            new AlertMessageBox(i18n.error(), i18n.errInvalidImei()).show();
            return false;
        }
        return validate(login, password);
    }

    @Override
    public void onLogin(final String login, final String password) {
        if (validate(login, password)) {
            Application.getDataService().login(login, password, new BaseAsyncCallback<User>(i18n) {
                @Override
                public void onSuccess(User result) {
                    ApplicationContext.getInstance().setUser(result);
                    BasicAuthFilter.getInstance().pushCredentials(login, password);
                    SessionService session = GWT.create(SessionService.class);
                    session.getUser(new JsonCallback() {
                        @Override
                        public void onFailure(Method method, Throwable exception) {
                            new AlertMessageBox(i18n.error(), i18n.errInvalidUsernameOrPassword()).show();
                        }

                        @Override
                        public void onSuccess(Method method, JSONValue response) {
                            if (loginHandler != null) {
                                dialog.hide();
                                loginHandler.onLogin();
                            }
                            dialog.clearTrackmanBodyStyle();
                        }
                    });
                }
                @Override
                public void onFailure(Throwable caught) {
                    if (caught instanceof UserBlockedException) {
                        new AlertMessageBox(i18n.error(), i18n.errUserAccountBlocked()).show();
                    } else if (caught instanceof UserExpiredException) {
                        new AlertMessageBox(i18n.error(), i18n.errUserAccountExpired()).show();
                    } else {
                        new AlertMessageBox(i18n.error(), i18n.errInvalidUsernameOrPassword()).show();
                    }
                }
            });
        }
    }

    @Override
    public void onRegister(String email, String imei, String password) {
        if (validate(email, imei, password)) {
            UsersService users = GWT.create(UsersService.class);
            UsersService.AddUserDto dto = new UsersService.AddUserDto(email, imei, password);
            users.register(dto, new JsonCallback() {
                @Override
                public void onSuccess(Method method, JSONValue response) {
                    switch (method.getResponse().getStatusCode()) {
                        case Response.SC_CREATED:
                            new InfoMessageBox(i18n.success(), i18n.validationMailSent()).show();
                            break;
                        case Response.SC_CONFLICT:
                            new AlertMessageBox(i18n.error(), i18n.errUsernameTaken()).show();
                            break;
                        case Response.SC_BAD_REQUEST:
                            if(method.getResponse().getText().equals("err_email_resent"))
                                new AlertMessageBox(i18n.error(), i18n.emailResent()).show();
                            else
                                new AlertMessageBox(i18n.error(), i18n.errInvalidImei()).show();
                            break;
                        default:
                            new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                            break;
                    }
                }

                @Override
                public void onFailure(Method method, Throwable exception) {
                    new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                }
            });
        }
    }

}
