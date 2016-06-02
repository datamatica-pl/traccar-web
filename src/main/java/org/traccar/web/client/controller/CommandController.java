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
import com.google.gwt.http.client.*;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.view.CommandDialog;
import org.traccar.web.client.view.DeviceView;
import org.traccar.web.client.view.LogViewDialog;
import org.traccar.web.shared.model.Command;
import org.traccar.web.shared.model.CommandType;
import org.traccar.web.shared.model.Device;

import java.util.HashMap;

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
        Command command = new Command();
        command.setType(type);
        command.setDeviceId((int) device.getId());
        command.setAttributes(new HashMap<String, Object>());
        switch (type) {
            case positionPeriodic:
                command.getAttributes().put(CommandType.KEY_FREQUENCY, frequency);
                break;
            case setTimezone:
                command.getAttributes().put(CommandType.KEY_TIMEZONE, timezone);
                break;
            case movementAlarm:
                command.getAttributes().put(CommandType.KEY_RADIUS, radius);
                break;
            case sendSms:
                command.getAttributes().put(CommandType.KEY_PHONE_NUMBER, phoneNumber);
                command.getAttributes().put(CommandType.KEY_MESSAGE, message);
                break;
            case setDefenseTime:
                command.getAttributes().put(CommandType.KEY_DEFENSE_TIME,
                        extendedAttributes.get(CommandType.KEY_DEFENSE_TIME));
                break;
            case setSOSNumbers:
                command.getAttributes().put(CommandType.KEY_SOS_NUMBER_1,
                        extendedAttributes.get(CommandType.KEY_SOS_NUMBER_1));
                command.getAttributes().put(CommandType.KEY_SOS_NUMBER_2,
                        extendedAttributes.get(CommandType.KEY_SOS_NUMBER_2));
                command.getAttributes().put(CommandType.KEY_SOS_NUMBER_3,
                        extendedAttributes.get(CommandType.KEY_SOS_NUMBER_3));
                break;
            case deleteSOSNumber:
                command.getAttributes().put(CommandType.KEY_SOS_NUMBER,
                        extendedAttributes.get(CommandType.KEY_SOS_NUMBER));
                break;
            case setCenterNumber:
                command.getAttributes().put(CommandType.KEY_CENTER_NUMBER,
                        extendedAttributes.get(CommandType.KEY_CENTER_NUMBER));
            case CUSTOM:
                command.setCommand(rawCommand);
                break;
        }

        final Messages i18n = GWT.create(Messages.class);

        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, "traccar/rest/sendCommand");

        try {
            builder.sendRequest("[" + commandMapper.write(command) + "]", new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    currentDialog.onAnswerReceived();
                    new LogViewDialog("<pre>" + response.getText() + "</pre>").show();
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    new AlertMessageBox(i18n.error(), i18n.errRemoteCall());
                }
            });
        } catch (RequestException e) {
            new AlertMessageBox(i18n.error(), e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
