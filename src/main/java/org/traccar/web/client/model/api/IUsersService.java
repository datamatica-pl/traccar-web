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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.gwt.core.shared.GWT;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.GET;
import org.fusesource.restygwt.client.RestService;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import org.fusesource.restygwt.client.JsonCallback;
import pl.datamatica.traccar.model.DeviceEventType;
import pl.datamatica.traccar.model.User;


//@Path("https://localhost/api/v1/users")
@Path("../api/v1/users")
public interface IUsersService extends RestService{
    public static class AddUserDto {
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
    void register(AddUserDto dto, JsonCallback callback);
    
    
    public static class ResetPasswordDto {
        public String login;
        
        public ResetPasswordDto() {}
        public ResetPasswordDto(String login) {
            this.login = login;
        }
    }
    
    @POST
    @Path("resetreq")
    void resetPassword(ResetPasswordDto dto, JsonCallback callback);
    
    @GET
    void getUsers(JsonCallback callback);
    
    public static class EditUserDto {
        public final String email;
        public final String companyName;
        public final String firstName;
        public final String lastName;
        public final String phoneNumber;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ",
                timezone="GMT")
        public final Date expirationDate;
        public final Integer maxNumOfDevices;
        public final boolean manager;
        public final boolean admin;
        public final boolean archive;
        public final boolean blocked;
        public final boolean readOnly;
        public final String password;
        public final List<String> notificationEvents;
        
        public EditUserDto(User u) {
            this.email = u.getEmail();
            this.companyName = u.getCompanyName();
            this.firstName = u.getFirstName();
            this.lastName = u.getLastName();
            this.phoneNumber = u.getPhoneNumber();
            this.expirationDate = u.getExpirationDate();
            this.maxNumOfDevices = u.getMaxNumOfDevices();
            this.manager = u.getManager();
            this.admin = u.getAdmin();
            this.archive = u.isArchive();
            this.blocked = u.isBlocked();
            this.readOnly = u.getReadOnly();
            this.password = u.getPassword();
            this.notificationEvents = new ArrayList<>();
            if(u.getTransferNotificationEvents() != null)
                for(DeviceEventType det : u.getTransferNotificationEvents())
                    notificationEvents.add(det.name());
        }
    }
    
    @POST
    @Path("resend")
    void resendLink(ResetPasswordDto dto, JsonCallback callback);
}
