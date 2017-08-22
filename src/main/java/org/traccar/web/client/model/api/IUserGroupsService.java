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
import javax.ws.rs.*;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

/**
 *
 * @author ŁŁ
 */
@Path("../api/v1/usergroups")
public interface IUserGroupsService extends RestService{
    @GET
    void getGroups(MethodCallback<List<ApiUserGroup>> callback);
    
    @POST
    void addGroup(ApiUserGroup grp, MethodCallback<ApiUserGroup> callback);
    
    @DELETE
    @Path("/{id}")
    void removeGroup(@PathParam("id") long id, MethodCallback<Void> callback);
    
    @GET
    @Path("/{id}/users")
    void getGroupUsers(@PathParam("id") long id, MethodCallback<List<Long>> callback);
}
