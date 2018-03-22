/*
 * Copyright 2018 Datamatica (dev@datamatica.pl)
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
import javax.ws.rs.*;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.RestService;

@Path("../api/v1/routes")
public interface RoutesService extends RestService {
    public static interface ApiRouteMapper extends ObjectMapper<ApiRoute> {}
    
    @GET
    void getRoutes(@QueryParam("archived") boolean archive, JsonCallback callback);
    
    @PUT
    @Path("/{id}")
    void updateRoute(@PathParam("id") long id, ApiEditRoute route, JsonCallback callback);
    
    @POST
    void createRoute(ApiEditRoute route, JsonCallback callback);
    
    @DELETE
    @Path("/{id}")
    void deleteRoute(@PathParam("id") long id, JsonCallback callback);
}
