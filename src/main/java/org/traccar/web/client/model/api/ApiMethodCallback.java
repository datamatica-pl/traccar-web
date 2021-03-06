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

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;

public abstract class ApiMethodCallback<T> implements MethodCallback<T> {

    private Messages i18n;

    public ApiMethodCallback(Messages i18n) {
        this.i18n = i18n;
    }
    
    @Override
    public void onFailure(Method method, Throwable exception) {
        AlertMessageBox alert;
        if(method.getResponse().getStatusCode() >= 500) {
            alert = new AlertMessageBox(i18n.error(), i18n.errRemoteCall());
        } else if(method.getResponse().getStatusCode() == 401) {
            alert = new AlertMessageBox(i18n.error(), i18n.errUserSessionExpired());
            alert.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
                @Override
                public void onDialogHide(DialogHideEvent event) {
                    Window.Location.reload();
                }
            });
        } else {
            ApiError err = ApiError.fromJson(method.getResponse().getText());
            alert = new AlertMessageBox(i18n.error(), err.getMessage());
        }
        alert.show();
    }
    
}
