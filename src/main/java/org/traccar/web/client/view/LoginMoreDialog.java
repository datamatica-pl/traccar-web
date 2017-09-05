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
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.TextField;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.api.IUsersService;

public class LoginMoreDialog {
    private static _UiBinder uiBinder = GWT.create(_UiBinder.class);

    interface _UiBinder extends UiBinder<Widget, LoginMoreDialog> {
    }
    
    @UiField
    Window window;
    
    @UiField
    TextField login;
    
    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);
    
    IUsersService us = GWT.create(IUsersService.class);
    
    public LoginMoreDialog() {
        uiBinder.createAndBindUi(this);
    }
    
    public void show() {
        window.show();
    }
    
    @UiHandler("resetButton")
    public void onResetClicked(SelectEvent event) {
        IUsersService us = GWT.create(IUsersService.class);
        us.resetPassword(new IUsersService.ResetPasswordDto(login.getText()), new JsonCallback() {
            @Override
            public void onFailure(Method method, Throwable exception) {
                new AlertMessageBox(i18n.success(), i18n.resetMailSent()).show();
            }

            @Override
            public void onSuccess(Method method, JSONValue response) {
                new AlertMessageBox(i18n.success(), i18n.resetMailSent()).show();
                window.hide();
            }
        });
    }
    
    @UiHandler("resendButton")
    public void onResendClicked(SelectEvent event) {
        us.resendLink(new IUsersService.ResetPasswordDto(login.getText()), new JsonCallback() {
            @Override
            public void onFailure(Method method, Throwable exception) {
                new AlertMessageBox(i18n.success(), i18n.emailResent()).show();
            }

            @Override
            public void onSuccess(Method method, JSONValue response) {
                new AlertMessageBox(i18n.success(), i18n.emailResent()).show();
                window.hide();
            }
        });
    }
    
    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }
}
