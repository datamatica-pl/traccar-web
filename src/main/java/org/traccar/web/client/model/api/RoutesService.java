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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("../api/v1/routes")
public interface RoutesService extends RestService {
    public static interface ApiRouteMapper extends ObjectMapper<ApiRoute> {}
    
    @GET
    void getRoutes(JsonCallback callback);
    
    @PUT
    @Path("/{id}")
    void updateRoute(@PathParam("id") long id, ApiEditRoute route, JsonCallback callback);
    
    @POST
    void createRoute(ApiEditRoute route, JsonCallback callback);
}
