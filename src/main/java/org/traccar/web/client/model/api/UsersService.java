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
import org.fusesource.restygwt.client.JsonCallback;
import org.traccar.web.client.model.api.IUsersService.AddUserDto;
import org.traccar.web.client.model.api.IUsersService.EditUserDto;
import org.traccar.web.client.model.api.IUsersService.ResetPasswordDto;

/**
 *
 * @author ŁŁ
 */
public class UsersService {
    public static interface EditUserDtoMapper extends ObjectMapper<EditUserDto> {}
    private IUsersService service = GWT.create(IUsersService.class);
    private EditUserDtoMapper mapper = GWT.create(EditUserDtoMapper.class);
    
    public void register(AddUserDto dto, JsonCallback callback) {
        service.register(dto, callback);
    }
    
    public void resetPassword(ResetPasswordDto dto, JsonCallback callback) {
        service.resetPassword(dto, callback);
    }
    
    public void getUsers(JsonCallback callback) {
        service.getUsers(callback);
    }

    public void updateUser(long id, EditUserDto dto, RequestCallback callback) {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.PUT,
            "../api/v1/users/"+id);
        try{
            builder.sendRequest(mapper.write(dto), callback);
        } catch(RequestException e) {
            callback.onError(null, e);
        }
    }
    
    public void resendLink(ResetPasswordDto dto, JsonCallback callback) {
        service.resendLink(dto, callback);
    }
}
