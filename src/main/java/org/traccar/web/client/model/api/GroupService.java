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
import java.util.Map;
import java.util.Set;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.MethodCallback;
import org.traccar.web.client.model.api.IGroupService.AddDeviceGroupDto;
import org.traccar.web.client.model.api.IGroupService.DeviceGroupDto;
import pl.datamatica.traccar.model.User;

/**
 *
 * @author ŁŁ
 */
public class GroupService {
    public static interface AddDeviceGroupDtoMapper extends ObjectMapper<AddDeviceGroupDto>{}
    public static interface LLongMapper extends ObjectMapper<List<Long>>{}

    private IGroupService service = GWT.create(IGroupService.class);
    private AddDeviceGroupDtoMapper mapper = GWT.create(AddDeviceGroupDtoMapper.class);
    private LLongMapper llMapper = GWT.create(LLongMapper.class);
    
    public void getGroups(MethodCallback<List<DeviceGroupDto>> callback) {
        service.getGroups(callback);
    }

    public void addGroup(AddDeviceGroupDto dto, MethodCallback<DeviceGroupDto> callback) {
        service.addGroup(dto, callback);
    }
    
    public void updateGroup(long id, AddDeviceGroupDto group, final RequestCallback callback) {
        RequestBuilder rb = new RequestBuilder(RequestBuilder.PUT, "../api/v1/devicegroups/"+id);
        try {
            rb.sendRequest(mapper.write(group), callback);
        } catch (RequestException ex) {
            callback.onError(null, ex);
        }
    }
    
    public void removeGroup(long id, RequestCallback callback) {
        RequestBuilder rb = new RequestBuilder(RequestBuilder.DELETE, "../api/v1/devicegroups/"+id);
        try {
            rb.sendRequest(null, callback);
        } catch(RequestException ex) {
            callback.onError(null, ex);
        }
    }
    
    public void getGroupShare(long id, MethodCallback<Set<Long>> callback) {
        service.getGroupShare(id, callback);
    }

    public void updateGroupShare(long id, List<Long> uids, RequestCallback callback) {
        RequestBuilder rb = new RequestBuilder(RequestBuilder.PUT, 
                "../api/v1/devicegroups/"+id+"/share");
        try {
            rb.sendRequest(llMapper.write(uids), callback);
        } catch(RequestException e) {
            callback.onError(null, e);
        }
    }
}
