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
package org.traccar.web.client.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import java.util.logging.Level;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.utils.ClientLogUtils;
import org.traccar.web.shared.model.AccessDeniedException;

public class BaseAsyncCallback<T> implements AsyncCallback<T> {

    private final Messages i18n;

    public BaseAsyncCallback(Messages i18n) {
        this.i18n = i18n;
    }

    @Override
    public void onFailure(Throwable caught) {
        if (caught instanceof AccessDeniedException) {
            new AlertMessageBox(i18n.error(), i18n.errAccessDenied()).show();
        } else if (caught instanceof StatusCodeException) {
            StatusCodeException sce = (StatusCodeException) caught;
            new AlertMessageBox(i18n.error(), i18n.errServerResponseRefresh()).show();

            String moreExInfo = "BaseAsyncCallback:onFailure:StatusCodeException \n";
            moreExInfo += "Status code: " + sce.getStatusCode() + "\n";
            moreExInfo += "Status text: " + sce.getStatusText() + "\n";
            moreExInfo += "Encoded response: " + sce.getEncodedResponse() + "\n";
            ClientLogUtils.logExceptionGwtCompatible(Level.SEVERE, caught, moreExInfo);
        } else {
            new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
            ClientLogUtils.logExceptionGwtCompatible(Level.SEVERE, caught, "BaseAsyncCallback:onFailure");
        }
    }

    @Override
    public void onSuccess(T result) {
    }

}
