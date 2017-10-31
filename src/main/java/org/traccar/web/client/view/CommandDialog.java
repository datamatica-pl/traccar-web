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
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.*;
import org.traccar.web.client.i18n.Messages;
import pl.datamatica.traccar.model.Device;

import java.util.Map;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.model.api.ApiCommandType;
import org.traccar.web.client.model.api.Resources;
import pl.datamatica.traccar.model.UserPermission;

public class CommandDialog {
    private static CommandDialogUiBinder uiBinder = GWT.create(CommandDialogUiBinder.class);

    interface CommandDialogUiBinder extends UiBinder<Widget, CommandDialog> {
    }

    public interface CommandHandler {
        void onSend(Device device,
                    String type,
                    Map<String, String> parameters);
    }

    @UiField
    Window window;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    @UiField(provided = true)
    NumberPropertyEditor<Integer> integerPropertyEditor = new NumberPropertyEditor.IntegerPropertyEditor();

    @UiField(provided = true)
    ComboBox<ApiCommandType> typeCombo;

    @UiField
    TextButton sendButton;
    
    @UiField
    VerticalLayoutContainer container;
    
    @UiField
    FieldLabel sizeSentinel; 

    final Device device;
    final CommandHandler commandHandler;
    final CommandArgumentsBinder contentBinder;

    public CommandDialog(Device device, CommandHandler commandHandler) {
        this.device = device;
        this.commandHandler = commandHandler;

        ListStore<ApiCommandType> commandTypes = new ListStore<>(new ModelKeyProvider<ApiCommandType>() {
            @Override
            public String getKey(ApiCommandType item) {
                return item.getCommandName();
            }
            
        });
        commandTypes.addAll(Resources.getInstance().model(device.getDeviceModelId()).getCommandTypes());
        if(ApplicationContext.getInstance().getUser().hasPermission(UserPermission.COMMAND_CUSTOM)) {
            commandTypes.add(ApiCommandType.CUSTOM);
            commandTypes.add(ApiCommandType.EXTENDED_CUSTOM);
        }

        this.typeCombo = new ComboBox<>(commandTypes, new LabelProvider<ApiCommandType>() {
            @Override
            public String getLabel(ApiCommandType item) {
                return ApplicationContext.getInstance().getMessageTryLowerCase("command_" + item.getCommandName());
            }
        });

        uiBinder.createAndBindUi(this);
        this.contentBinder = new CommandArgumentsBinder(container);

        if (commandTypes.size() == 0) {
            this.sendButton.setEnabled(false);
        }

        typeCombo.addSelectionHandler(new SelectionHandler<ApiCommandType>() {
            @Override
            public void onSelection(SelectionEvent<ApiCommandType> event) {
                toggleUI(event.getSelectedItem());
            }
        });
    }

    private void toggleUI(ApiCommandType type) {
        contentBinder.bind(type);
        window.forceLayout();
    }

    public void show() {
        window.show();
        sizeSentinel.setVisible(false);
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("sendButton")
    public void onSendClicked(SelectEvent event) {
        if(!contentBinder.validate())
            return;
        window.disable();
        commandHandler.onSend(device,
                typeCombo.getCurrentValue().getCommandName(),
                contentBinder.getParamMap());
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }

    public void onAnswerReceived() {
        window.enable();
    }
}
