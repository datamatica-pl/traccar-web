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
import javax.ws.rs.Path;
import org.fusesource.restygwt.client.JsonCallback;
import pl.datamatica.traccar.model.DeviceEventType;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserSettings;
import pl.datamatica.traccar.model.UserSettings.DistanceUnit;
import pl.datamatica.traccar.model.UserSettings.SpeedUnit;


//@Path("https://localhost/api/v1/users")
@Path("../api/v1/users")
public interface IUsersService extends RestService{
    public static class RegisterUserDto {
        public String email;
        public String imei;
        public String password;
        public boolean checkMarketing = false;
        
        public RegisterUserDto() {}
        
        public RegisterUserDto(String email, String imei, String password) {
            this.email = email;
            this.imei = imei;
            this.password = password;
        }
    }
    
    @POST
    @Path("register")
    void register(RegisterUserDto dto, JsonCallback callback);
    
    @POST
    void addUser(AddUserDto dto, JsonCallback callback);
    
    
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
        public String email;
        public String companyName;
        public String firstName;
        public String lastName;
        public String phoneNumber;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ",
                timezone="GMT")
        public Date expirationDate;
        public Integer maxNumOfDevices;
        public boolean manager;
        public boolean admin;
        public boolean archive;
        public boolean blocked;
        public boolean readOnly;
        public String password;
        public List<String> notificationEvents;
        
        public EditUserDto() {}
        
        public EditUserDto(User u) {
            this.email = u.getEmail();
            this.companyName = u.getCompanyName();
            this.firstName = u.getFirstName();
            this.lastName = u.getLastName();
            this.phoneNumber = u.getPhoneNumber();
            this.expirationDate = u.getExpirationDate();
            this.maxNumOfDevices = u.getMaxNumOfDevices();
            this.manager = true;
            this.admin = false;
            this.archive = true;
            this.readOnly = false;
            this.blocked = u.isBlocked();
            this.password = u.getPassword();
            this.notificationEvents = new ArrayList<>();
            if(u.getTransferNotificationEvents() != null)
                for(DeviceEventType det : u.getTransferNotificationEvents())
                    notificationEvents.add(det.name());
        }
    }
    
    public static class AddUserDto extends EditUserDto {
        public String login;
        
        public AddUserDto() {}
        
        public AddUserDto(User u) {
            super(u);
            this.login = u.getLogin();
        }
    }
    
    @POST
    @Path("resend")
    void resendLink(ResetPasswordDto dto, JsonCallback callback);
    
    public static class EditUserSettingsDto {
        public final String archiveMarkerType;
        public final double centerLatitude;
        public final double centerLongitude;
        public final boolean hideDuplicates;
        public final boolean hideInvalidLocations;
        public final boolean hideZeroCoordinates;
        public final String mapType;
        public final boolean maximizeOverviewMap;
        public final Double minDistance;
        public final String overlays;
        public final Double speedForFilter;
        public final String speedModifier;
        public final String speedUnit;
        public final short timePrintInterval;
        public final String timeZoneId;
        public final Short traceInterval;
        public final int zoomLevel;
        public final Short followedDeviceZoomLevel;
        
        public EditUserSettingsDto(UserSettings us) {
            if(us.getArchiveMarkerType() != null)
                this.archiveMarkerType = us.getArchiveMarkerType().name();
            else
                this.archiveMarkerType = null;
            this.centerLatitude = us.getCenterLatitude();
            this.centerLongitude = us.getCenterLongitude();
            this.zoomLevel = us.getZoomLevel();
            this.mapType = us.getMapType().name();
            this.maximizeOverviewMap = us.isMaximizeOverviewMap();
            this.overlays = us.getOverlays();
            this.followedDeviceZoomLevel = us.getFollowedDeviceZoomLevel();
            
            this.hideDuplicates = us.isHideDuplicates();
            this.hideInvalidLocations = us.isHideInvalidLocations();
            this.hideZeroCoordinates = us.isHideZeroCoordinates();
            
            SpeedUnit su = us.getSpeedUnit();
            this.speedForFilter = us.getSpeedForFilter();
            this.speedModifier = us.getSpeedModifier();
            if(us.getMinDistance() != null)
                this.minDistance = us.getMinDistance()/su.getDistanceUnit().getFactor();
            else
                this.minDistance = null;
            this.speedUnit = su.name();
            
            this.timePrintInterval = us.getTimePrintInterval();
            this.timeZoneId = us.getTimeZoneId();
            this.traceInterval = us.getTraceInterval();
        }
    }
}
