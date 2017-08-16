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
import java.util.Set;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.MethodCallback;
import org.traccar.web.client.model.api.IDevicesService.AddDeviceDto;
import org.traccar.web.client.model.api.IDevicesService.EditDeviceDto;
import pl.datamatica.traccar.model.User;

public class DevicesService {
    public static interface EditDeviceDtoMapper extends ObjectMapper<EditDeviceDto>{}
    public static interface LLongMapper extends ObjectMapper<List<Long>> {}
    
    private IDevicesService service = GWT.create(IDevicesService.class);
    private EditDeviceDtoMapper mapper = GWT.create(EditDeviceDtoMapper.class);
    private LLongMapper llMapper = GWT.create(LLongMapper.class);
    
    public void getDevices(JsonCallback callback) {
        service.getDevices(callback);
    }

    public void addDevice(AddDeviceDto dto, JsonCallback callback) {
        service.addDevice(dto, callback);
    }
    
    public void updateDevice(long id, EditDeviceDto dto, RequestCallback callback) {
        RequestBuilder builder = new MyRequestBuilder("PATCH",
            "../api/v1/devices/"+id);
        try{
            builder.sendRequest(mapper.write(dto), callback);
        } catch(RequestException e) {
            callback.onError(null, e);
        }
    }
    
    public void getDeviceShare(long id, MethodCallback<Set<Long>> callback) {
        service.getDeviceShares(id, callback);
    }
    
    public void updateDeviceShare(long id, List<Long> uids, RequestCallback callback) {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.PUT,
            "../api/v1/devices/"+id+"/share");
        try {
            builder.sendRequest(llMapper.write(uids), callback);
        } catch(RequestException e) {
            callback.onError(null, e);
        }
    }
}
