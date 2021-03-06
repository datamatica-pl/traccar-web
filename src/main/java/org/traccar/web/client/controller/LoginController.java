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
import java.util.List;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.traccar.web.client.model.api.ApiJsonCallback;
import org.traccar.web.client.model.api.ApiRulesVersion;
import org.traccar.web.client.model.api.BasicAuthFilter;
import org.traccar.web.client.model.api.SessionService;
import org.traccar.web.client.widget.InfoMessageBox;
import org.traccar.web.shared.model.UserBlockedException;
import org.traccar.web.shared.model.UserExpiredException;
import org.traccar.web.client.model.api.IUsersService;
import org.traccar.web.client.view.RulesDialog;

public class LoginController implements LoginDialog.LoginHandler, RulesDialog.RulesHandler {

    private LoginDialog dialog;

    private Messages i18n = GWT.create(Messages.class);

    public interface LoginHandler {
        void onLogin();
    }

    private LoginHandler loginHandler;

    public void login(final LoginHandler loginHandler) {
        this.loginHandler = loginHandler;
        SessionService session = GWT.create(SessionService.class);

        session.getUser(new JsonCallback() {
            @Override
            public void onFailure(Method method, Throwable exception) {
                dialog = new LoginDialog(LoginController.this);
                hideLoadingDiv();
                dialog.show();
            }

            @Override
            public void onSuccess(Method method, JSONValue response) {
                User u = Application.getDecoder().decodeUser(response.isObject());
                List<ApiRulesVersion> active = Application.getDecoder()
                                .decodeRules(response.isObject().get("unacceptedActiveRules").isArray());
                List<ApiRulesVersion> future = Application.getDecoder()
                        .decodeRules(response.isObject().get("unacceptedFutureRules").isArray());
                if(!active.isEmpty() || !future.isEmpty()) {
                    onFailure(null, null);
                    return;
                }
                
                ApplicationContext.getInstance().setUser(u);
                Application.getDataService().authenticated(new BaseAsyncCallback<User>(i18n) {
                    @Override
                    public void onSuccess(User result) {
                        if(result == null) {
                            dialog = new LoginDialog(LoginController.this);
                            hideLoadingDiv();
                            dialog.show();
                        } else {
                            hideLoadingDiv();
                            loginHandler.onLogin();
                        }
                    }
                });
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
                    BasicAuthFilter.getInstance().pushCredentials(login, password);
                    SessionService session = GWT.create(SessionService.class);
                    session.getUser(new ApiJsonCallback(i18n) {
                        @Override
                        public void onFailure(Method method, Throwable exception) {
                            if(method.getResponse().getStatusCode() == 401) {
                                new AlertMessageBox(i18n.error(), i18n.errInvalidUsernameOrPassword()).show();
                            } else
                                super.onFailure(method, exception);
                        }
                        
                        @Override
                        public void onSuccess(Method method, JSONValue response) {
                            User u = Application.getDecoder().decodeUser(response.isObject());
                            List<ApiRulesVersion> active = Application.getDecoder()
                                .decodeRules(response.isObject().get("unacceptedActiveRules").isArray());
                            List<ApiRulesVersion> future = Application.getDecoder()
                                    .decodeRules(response.isObject().get("unacceptedFutureRules").isArray());
                            ApplicationContext.getInstance().setUser(u);

                            if(!active.isEmpty()) {
                                RulesDialog rd = new RulesDialog(active, true, LoginController.this);
                                rd.show();
                            } else if(!future.isEmpty()) {
                                RulesDialog rd = new RulesDialog(future, false, LoginController.this);
                                rd.show();
                            } else {
                                onRulesAccepted();
                            }
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
    
    public void onRulesAccepted() {
        if (loginHandler != null) {
            dialog.hide();
            loginHandler.onLogin();
        }
        dialog.clearTrackmanBodyStyle();
    }

    @Override
    public void onRegister(String email, String imei, String password, Boolean marketingCheckState) {
        if (validate(email, imei, password)) {
            IUsersService users = GWT.create(IUsersService.class);
            IUsersService.RegisterUserDto dto = new IUsersService.RegisterUserDto(email, imei, password, marketingCheckState);
            users.register(dto, new JsonCallback() {
                @Override
                public void onSuccess(Method method, JSONValue response) {
                    if(method.getResponse().getStatusCode() == Response.SC_CREATED)
                        new InfoMessageBox(i18n.success(), i18n.validationMailSent()).show();
                    else
                        new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                }
                
                @Override
                public void onFailure(Method method, Throwable exception) {
                    switch(method.getResponse().getStatusCode()) {
                        case Response.SC_CONFLICT:
                            new AlertMessageBox(i18n.error(), i18n.errUsernameTaken()).show();
                            break;
                        case Response.SC_BAD_REQUEST:
                            new AlertMessageBox(i18n.error(), i18n.errInvalidImei()).show();
                            break;
                        default:
                            new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                            break;
                    }
                }
            });
        }
    }

}
