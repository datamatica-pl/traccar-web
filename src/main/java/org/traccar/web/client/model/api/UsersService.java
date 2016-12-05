/*
 * Copyright 2016 Datamatica (dev@datamatica.pl)
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

import org.fusesource.restygwt.client.RestService;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.fusesource.restygwt.client.MethodCallback;


@Path("../api/v1/users")
public interface UsersService extends RestService{
    public class AddUserDto {
        public String email;
        public String imei;
        public String password;
        public boolean checkMarketing = false;
        
        public AddUserDto() {}
        
        public AddUserDto(String email, String imei, String password) {
            this.email = email;
            this.imei = imei;
            this.password = password;
        }
    }
    
    @POST
    void register(AddUserDto dto, MethodCallback<Void> callback);
}
