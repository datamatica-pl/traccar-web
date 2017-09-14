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
package org.traccar.web.client.model.api;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import org.traccar.web.client.i18n.Messages;

public abstract class ApiRequestCallback implements RequestCallback {
    private Messages i18n;
    
    public ApiRequestCallback(Messages i18n) {
        this.i18n = i18n;
    }
    
    @Override
    public void onResponseReceived(Request request, Response response) {
        if(response.getStatusCode() >= 500) {
            new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
            onFailure();
        } else if(response.getStatusCode() == 401) {
            AlertMessageBox amb = new AlertMessageBox(i18n.error(), i18n.errUserSessionExpired());
            amb.show();
            amb.addDialogHideHandler(new DialogHideHandler() {
                @Override
                public void onDialogHide(DialogHideEvent event) {
                    Window.Location.reload();
                }
            });
            onFailure();
        } else if(response.getStatusCode() != expectedStatusCode()) {
            ApiError err = ApiError.fromJson(response.getText());
            new AlertMessageBox(i18n.error(), err.getMessage()).show();
            onFailure();
        } else {
            onSuccess(response.getText());
        }
    }
    
    public abstract void onSuccess(String response);
    
    public void onFailure() {
    }
    
    protected int expectedStatusCode() {
        return 200;
    }

    @Override
    public void onError(Request request, Throwable exception) {
        new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
    }
    
}
