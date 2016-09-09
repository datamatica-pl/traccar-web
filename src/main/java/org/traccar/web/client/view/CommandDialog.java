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
import com.google.gwt.i18n.client.TimeZoneInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.StringLabelProvider;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.*;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.EnumKeyProvider;
import org.traccar.web.client.widget.TimeZoneComboBox;
import org.traccar.web.shared.model.CommandType;
import org.traccar.web.shared.model.Device;

import java.util.HashMap;
import java.util.List;

public class CommandDialog {
    private static CommandDialogUiBinder uiBinder = GWT.create(CommandDialogUiBinder.class);

    interface CommandDialogUiBinder extends UiBinder<Widget, CommandDialog> {
    }

    public interface CommandHandler {
        void onSend(Device device,
                    CommandType type,
                    int frequency,
                    int timeZone,
                    int radius,
                    String phoneNumber, String message,
                    HashMap<String, Object> extendedAttributes,
                    String rawCommand);
    }

    @UiField
    Window window;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    @UiField(provided = true)
    NumberPropertyEditor<Integer> integerPropertyEditor = new NumberPropertyEditor.IntegerPropertyEditor();

    @UiField(provided = true)
    ComboBox<CommandType> typeCombo;

    @UiField
    FieldLabel lblFrequency;

    @UiField
    NumberField<Integer> frequency;

    @UiField(provided = true)
    ComboBox<String> frequencyUnit;

    @UiField
    FieldLabel lblTimeZone;

    @UiField(provided = true)
    ComboBox<TimeZoneInfo> timeZone;

    @UiField
    FieldLabel lblRadius;

    @UiField
    NumberField<Integer> radius;

    @UiField
    FieldLabel lblCustomMessage;

    @UiField
    FieldLabel lblPhoneNumber;

    @UiField
    TextField phoneNumber;

    @UiField
    FieldLabel lblMessage;

    @UiField
    TextField message;

    @UiField
    FieldLabel lblDefenseTime;

    @UiField
    TextField defenseTime;

    @UiField
    FieldLabel lblSOSNumber1;

    @UiField
    FieldLabel lblSOSNumber2;

    @UiField
    FieldLabel lblSOSNumber3;

    @UiField
    FieldLabel lblSOSNumber;

    @UiField
    FieldLabel lblCenterNumber;

    @UiField
    TextField SOSNumber1;

    @UiField
    TextField SOSNumber2;

    @UiField
    TextField SOSNumber3;

    @UiField
    TextField SOSNumber;

    @UiField
    TextField centerNumber;

    @UiField
    TextField customMessage;

    @UiField
    TextButton sendButton;

    final Device device;
    final CommandHandler commandHandler;
    final CommandArgumentsBinder contentBinder;

    public CommandDialog(Device device, CommandHandler commandHandler) {
        this.device = device;
        this.commandHandler = commandHandler;

        ListStore<CommandType> commandTypes = new ListStore<>(new EnumKeyProvider<CommandType>());
        List<CommandType> supportedCommands = device.getSupportedCommands();
        commandTypes.addAll(device.getSupportedCommands());

        this.typeCombo = new ComboBox<>(commandTypes, new LabelProvider<CommandType>() {
            @Override
            public String getLabel(CommandType item) {
                return i18n.commandType(item);
            }
        });

        ListStore<String> frequencyUnits = new ListStore<>(new ModelKeyProvider<String>() {
            @Override
            public String getKey(String item) {
                return item;
            }
        });
        frequencyUnits.add(i18n.second());
        frequencyUnits.add(i18n.minute());
        frequencyUnits.add(i18n.hour());
        this.frequencyUnit = new ComboBox<>(frequencyUnits, new StringLabelProvider<>());
        this.timeZone = new TimeZoneComboBox();

        uiBinder.createAndBindUi(this);
        this.contentBinder = CommandArgumentsBinder.getInstance(device.getProtocol(), this);

        if (commandTypes.size() == 0) {
            this.sendButton.setEnabled(false);
        }

        typeCombo.addSelectionHandler(new SelectionHandler<CommandType>() {
            @Override
            public void onSelection(SelectionEvent<CommandType> event) {
                toggleUI(event.getSelectedItem());
            }
        });
    }

    private void toggleUI(CommandType type) {
        contentBinder.bind(type);
        window.forceLayout();
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("sendButton")
    public void onSendClicked(SelectEvent event) {
        int frequency = -1;
        if (this.frequency.getCurrentValue() != null) {
            String unit = frequencyUnit.getCurrentValue();
            frequency = this.frequency.getCurrentValue();
            frequency *=
                    i18n.minute().equals(unit) ? 60
                    : i18n.hour().equals(unit) ? 3600 : 1;
        }
        int timezone = -1;
        if (this.timeZone.getCurrentValue() != null) {
            timezone = timeZone.getCurrentValue().getStandardOffset() * 60;
        }
        int radius = -1;
        if (this.radius.getCurrentValue() != null) {
            radius = this.radius.getCurrentValue();
        }

        HashMap<String, Object> extendedAttributes = new HashMap();

        if (this.defenseTime.getCurrentValue() != null) {
            extendedAttributes.put(CommandType.KEY_DEFENSE_TIME, this.defenseTime.getCurrentValue());
        }
        if (this.SOSNumber1.getCurrentValue() != null) {
            extendedAttributes.put(CommandType.KEY_SOS_NUMBER_1, this.SOSNumber1.getCurrentValue());
        }
        if (this.SOSNumber2.getCurrentValue() != null) {
            extendedAttributes.put(CommandType.KEY_SOS_NUMBER_2, this.SOSNumber2.getCurrentValue());
        }
        if (this.SOSNumber3.getCurrentValue() != null) {
            extendedAttributes.put(CommandType.KEY_SOS_NUMBER_3, this.SOSNumber3.getCurrentValue());
        }
        if (this.SOSNumber.getCurrentValue() != null) {
            extendedAttributes.put(CommandType.KEY_SOS_NUMBER, this.SOSNumber.getCurrentValue());
        }
        if (this.centerNumber.getCurrentValue() != null) {
            extendedAttributes.put(CommandType.KEY_CENTER_NUMBER, this.centerNumber.getCurrentValue());
        }

        window.disable();
        commandHandler.onSend(device,
                typeCombo.getCurrentValue(),
                frequency,
                timezone,
                radius,
                phoneNumber.getCurrentValue(),
                message.getCurrentValue(),
                extendedAttributes,
                customMessage.getCurrentValue());
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }

    public void onAnswerReceived() {
        window.enable();
    }
}
