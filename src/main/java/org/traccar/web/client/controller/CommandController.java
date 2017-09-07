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
package org.traccar.web.client.controller;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONString;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.view.CommandDialog;
import org.traccar.web.client.view.DeviceView;
import org.traccar.web.client.view.LogViewDialog;
import org.traccar.web.shared.model.Command;
import pl.datamatica.traccar.model.CommandType;
import pl.datamatica.traccar.model.Device;

import java.util.HashMap;
import org.traccar.web.client.model.api.ApiRequestCallback;

public class CommandController implements ContentController, DeviceView.CommandHandler, CommandDialog.CommandHandler {

    interface CommandMapper extends ObjectMapper<Command> {}

    private final CommandMapper commandMapper = GWT.create(CommandMapper.class);
    private CommandDialog currentDialog;

    @Override
    public ContentPanel getView() {
        return null;
    }

    @Override
    public void run() {
    }

    @Override
    public void onCommand(Device device) {
        currentDialog = new CommandDialog(device, this);
        currentDialog.show();
    }

    @Override
    public void onSend(Device device,
                       CommandType type,
                       int frequency,
                       int timezone,
                       int radius,
                       String phoneNumber,
                       String message,
                       HashMap<String, Object> extendedAttributes,
                       String rawCommand) {
        JSONObject attrs = new JSONObject();
        switch (type) {
            case positionPeriodic:
                attrs.put(CommandType.KEY_FREQUENCY, new JSONString(Integer.toString(frequency)));
                if (extendedAttributes.get(CommandType.KEY_FREQUENCY_STOP) != null) {
                    attrs.put(CommandType.KEY_FREQUENCY_STOP,
                            new JSONString(extendedAttributes.get(CommandType.KEY_FREQUENCY_STOP).toString()));
                }
                break;
            case positionStop:
                attrs.put(CommandType.KEY_FREQUENCY, new JSONString(Integer.toString(frequency)));
                break;
            case setTimezone:
                attrs.put(CommandType.KEY_TIMEZONE, new JSONString(Long.toString(timezone/3600)));
                break;
            case movementAlarm:
                attrs.put(CommandType.KEY_RADIUS, new JSONString(Integer.toString(radius)));
                break;
            case sendSms:
                attrs.put(CommandType.KEY_PHONE_NUMBER, new JSONString(phoneNumber));
                attrs.put(CommandType.KEY_MESSAGE, new JSONString(message));
                break;
            case setDefenseTime:
                attrs.put(CommandType.KEY_DEFENSE_TIME,
                        new JSONString(extendedAttributes.get(CommandType.KEY_DEFENSE_TIME).toString()));
                break;
            case setSOSNumbers:
                attrs.put(CommandType.KEY_SOS_NUMBER_1,
                        new JSONString(extendedAttributes.get(CommandType.KEY_SOS_NUMBER_1).toString()));
                attrs.put(CommandType.KEY_SOS_NUMBER_2,
                        new JSONString(extendedAttributes.get(CommandType.KEY_SOS_NUMBER_2).toString()));
                attrs.put(CommandType.KEY_SOS_NUMBER_3,
                        new JSONString(extendedAttributes.get(CommandType.KEY_SOS_NUMBER_3).toString()));
                break;
            case deleteSOSNumber:
                attrs.put(CommandType.KEY_SOS_NUMBER,
                        new JSONString(extendedAttributes.get(CommandType.KEY_SOS_NUMBER).toString()));
                break;
            case positionPeriodicAlt:
                attrs.put(CommandType.KEY_FREQUENCY, new JSONString(Integer.toString(frequency)));
                break;
            case setCenterNumber:
                attrs.put(CommandType.KEY_CENTER_NUMBER,
                        new JSONString(extendedAttributes.get(CommandType.KEY_CENTER_NUMBER).toString()));
                break;
            case extendedCustom:
                attrs.put(CommandType.KEY_MESSAGE, new JSONString(rawCommand));
            case custom:
                attrs.put("command", new JSONString(rawCommand));
                break;
        }

        final Messages i18n = GWT.create(Messages.class);

        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, 
                "../api/v1/devices/"+device.getId()+"/sendCommand/"+type.name());

        try {
            builder.sendRequest(attrs.toString(), new ApiRequestCallback(i18n) {
                @Override
                public void onSuccess(String response) {
                    currentDialog.onAnswerReceived();
                    new LogViewDialog("<pre>" + response + "</pre>").show();
                }
            });
        } catch (RequestException e) {
            new AlertMessageBox(i18n.error(), e.getLocalizedMessage()).show();
        }
    }
}
