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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.PasswordHashMethod;

/**
 *
 * @author ŁŁ
 */
@Path("../api/v1/applicationsettings")
public interface ApplicationSettingsService extends RestService{
    @GET
    void get(MethodCallback<ApplicationSettingsDto> callback);
    
    @PUT
    void update(ApplicationSettingsDto dto, JsonCallback callback);
    
    public static class ApplicationSettingsDto {
        long id;
        boolean registrationEnabled;
        short updateInterval;
        String defaultPasswordHash;
        boolean disallowDeviceManagementByUsers;
        boolean evantRecordingEnabled;
        int notificationExpirationPeriod;
        String language;
        String bingMapsKey;
        String matchServiceURL;
        boolean allowCommandsOnlyForAdmins;
        
        public ApplicationSettings toApplicationSettings() {
            ApplicationSettings as = new ApplicationSettings();
            as.setRegistrationEnabled(registrationEnabled);
            as.setUpdateInterval(updateInterval);
            as.setDefaultHashImplementation(PasswordHashMethod.fromString(defaultPasswordHash));
            as.setDisallowDeviceManagementByUsers(disallowDeviceManagementByUsers);
            as.setEventRecordingEnabled(evantRecordingEnabled);
            as.setNotificationExpirationPeriod(notificationExpirationPeriod);
            as.setLanguage(language);
            as.setBingMapsKey(bingMapsKey);
            as.setMatchServiceURL(matchServiceURL);
            as.setAllowCommandsOnlyForAdmins(allowCommandsOnlyForAdmins);
            return as;
        }
        
        public static ApplicationSettingsDto create(ApplicationSettings as) {
            ApplicationSettingsDto dto = new ApplicationSettingsDto();
            dto.registrationEnabled = as.getRegistrationEnabled();
            dto.updateInterval = as.getUpdateInterval();
            dto.defaultPasswordHash = as.getDefaultHashImplementation().getName();
            dto.disallowDeviceManagementByUsers = as.isDisallowDeviceManagementByUsers();
            dto.evantRecordingEnabled = as.isEventRecordingEnabled();
            dto.notificationExpirationPeriod = as.getNotificationExpirationPeriod();
            dto.language = as.getLanguage();
            dto.bingMapsKey = as.getBingMapsKey();
            dto.matchServiceURL = as.getMatchServiceURL();
            dto.allowCommandsOnlyForAdmins = as.isAllowCommandsOnlyForAdmins();
            return dto;
        }
    }
}

