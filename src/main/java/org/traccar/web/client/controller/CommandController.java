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
import java.util.Map;
import org.traccar.web.client.model.api.ApiRequestCallback;

public class CommandController implements ContentController, DeviceView.CommandHandler, CommandDialog.CommandHandler {

    interface ParamsMapper extends ObjectMapper<Map<String, String>> {}
    
    private final ParamsMapper mapper = GWT.create(ParamsMapper.class);
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
                       String type,
                       Map<String, String> parameters) {

        final Messages i18n = GWT.create(Messages.class);

        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, 
                "../api/v1/devices/"+device.getId()+"/sendCommand/"+type);

        try {
            builder.sendRequest(mapper.write(parameters), new ApiRequestCallback(i18n) {
                @Override
                public void onSuccess(String response) {
                    currentDialog.onAnswerReceived();
                    new LogViewDialog("<pre>" + response + "</pre>").show();
                }
                
                @Override
                public void onFailure() {
                    currentDialog.hide();
                }
            });
        } catch (RequestException e) {
            new AlertMessageBox(i18n.error(), e.getLocalizedMessage()).show();
        }
    }
}
