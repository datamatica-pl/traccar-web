/*
 * Copyright 2018 Datamatica (dev@datamatica.pl)
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
import com.google.gwt.event.dom.client.ChangeEvent;
import com.sencha.gxt.widget.core.client.Window;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import org.traccar.web.client.view.LoginDialog.LoginHandler;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.sencha.gxt.widget.core.client.button.TextButton;

/**
 *
 * @author ŁŁ
 */
public class DemoRulesDialog {
    private static _UiBinder uiBinder = GWT.create(_UiBinder.class);
    
    interface _UiBinder extends UiBinder<Widget, DemoRulesDialog> {
    }
    
    @UiField
    Window window;
    
    @UiField
    TextButton continueButton;
    
    @UiField
    CheckBox cbAccept;
    
    private LoginHandler handler;
    
    public DemoRulesDialog(final LoginHandler handler) {
        uiBinder.createAndBindUi(this);
        this.handler = handler;
        cbAccept.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                continueButton.setEnabled(event.getValue());
            }
        });
    }
    
    public void show() {
        window.show();
    }
    
    @UiHandler("continueButton")
    public void onContinueClicked(SelectEvent event) {
        handler.onLogin("testowy", "testowy");
        window.hide();
    }
    
    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }
}
