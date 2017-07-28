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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.RestService;

//@Path("https://localhost/api/v1/devices")
@Path("../api/v1/devices")
public interface DevicesService extends RestService {
    @GET
    void getDevices(JsonCallback callback);
    
    @POST
    void addDevice(AddDeviceDto dto, JsonCallback callback);
    
    
    public static class AddDeviceDto {
        public String imei;
        
        public AddDeviceDto(){}
        
        public AddDeviceDto(String imei) {
            this.imei = imei;
        }
    }
}
