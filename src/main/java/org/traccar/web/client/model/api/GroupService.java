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
import pl.datamatica.traccar.model.Group;

@Path("../api/v1/groups")
public interface GroupService extends RestService{
    @GET
    void getGroups(MethodCallback<List<DeviceGroupDto>> callback);
    
    static class DeviceGroupDto {
        public long id;
        public String name;
        public String description;
        public boolean owned;
        
        public Group toGroup() {
            Group g = new Group(id, name);
            g.setDescription(description);
            g.setOwned(owned);
            g.setParent(null);
            return g;
        }
    }
}
