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

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fusesource.restygwt.client.MethodCallback;

/**
 *
 * @author ŁŁ
 */
public class AuditLogService {
    public static interface LogEntryMapper extends ObjectMapper<List<ApiAuditLogEntry>>{}
    
    DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    LogEntryMapper mapper = GWT.create(LogEntryMapper.class);
    
    public void get(Date fromDate, Date toDate, final MethodCallback<List<ApiAuditLogEntry>> callback) {
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, 
                ("../api/v1/auditlog?fromDate="+dateFormat.format(fromDate)+
                        "&toDate="+dateFormat.format(toDate))
                .replace("+", "%2B"));
        try {
            rb.sendRequest("", new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    List<ApiAuditLogEntry> le = mapper.read(response.getText());
                    callback.onSuccess(null, le);
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    callback.onFailure(null, exception);
                }
                
            });
        } catch (RequestException ex) {
            callback.onFailure(null, ex);
        }
    }
}
