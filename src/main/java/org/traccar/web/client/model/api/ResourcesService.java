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

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

//@Path("https://localhost/api/v1/resources")
@Path("../api/v1/resources")
public interface ResourcesService extends RestService {
    @GET
    @Path("deviceicons")
    public void getDeviceIcons(MethodCallback<List<ApiDeviceIcon>> callback);
    
    @GET
    @Path("devicemodels")
    public void getDeviceModels(MethodCallback<List<ApiDeviceModel>> callback);
}
