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
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import java.util.List;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.MethodCallback;

/**
 *
 * @author ŁŁ
 */
public class ReportsService implements IReportsService{
    public static interface ReportMapper extends ObjectMapper<ApiReport>{}
    
    private IReportsService service = GWT.create(IReportsService.class);
    private ReportMapper mapper = GWT.create(ReportMapper.class);

    @Override
    public void getReports(JsonCallback callback) {
        service.getReports(callback);
    }

    @Override
    public void createReport(ApiReport report, JsonCallback callback) {
        service.createReport(report, callback);
    }
    
    public void updateReport(long id, ApiReport report, RequestCallback callback) {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.PUT,
            "../api/v1/reports/"+id);
        try{
            builder.sendRequest(mapper.write(report), callback);
        } catch(RequestException e) {
            callback.onError(null, e);
        }
    }

    @Override
    public void removeReport(long id, MethodCallback<Void> callback) {
        service.removeReport(id, callback);
    }
}
