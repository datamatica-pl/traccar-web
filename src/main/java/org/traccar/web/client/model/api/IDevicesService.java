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

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.RestService;
import pl.datamatica.traccar.model.Device;

//@Path("https://localhost/api/v1/devices")
@Path("../api/v1/devices")
public interface IDevicesService extends RestService {
    @GET
    void getDevices(JsonCallback callback);
    
    @POST
    void addDevice(AddDeviceDto dto, JsonCallback callback);
    
    
    public static class AddDeviceDto {
        public String imei;
        
        public AddDeviceDto(){}
        
        public AddDeviceDto(String imei) {
            this.imei = imei;
        }
    }
    
    public static class EditDeviceDto {
        public final String deviceName;
        public final Long deviceModelId;
        public final Long iconId;
        public final Long customIconId;
        public final String color;
        public final String phoneNumber;
        public final String plateNumber;
        public final String description;
        public final Double speedLimit;
        public final Double fuelCapacity;
        
        public final Long groupId;
        public final String vehicleInfo;
        public final boolean autoUpdateOdometer;
        public final int timeout;
        public final Integer minIdleTime;
        public final Double idleSpeedThreshold;
        public final int timeZoneOffset;
        public final String commandPassword;
        public final int historyLength;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ",
                timezone="GMT")
        public final Date validTo;
        public final boolean showOdometer;
        public final boolean showProtocol;
        //
        public final boolean showName;
        public final Double arrowRadius;
        public final String arrowMovingColor;
        public final String arrowStoppedColor;
        public final String arrowPausedColor;
        public final String arrowOfflineColor;
        
        public EditDeviceDto(Device device) {
            this.deviceName = device.getName();
            this.deviceModelId = device.getDeviceModelId();
            this.iconId = device.getIconId();
            this.customIconId = device.getCustomIconId();
            this.color = device.getColor();
            this.phoneNumber = device.getPhoneNumber();
            this.plateNumber = device.getPlateNumber();
            this.description = device.getDescription();
            this.speedLimit = device.getSpeedLimit();
            this.fuelCapacity = device.getFuelCapacity();
            this.groupId = device.getGroup() == null ? null : device.getGroup().getId();
            this.vehicleInfo = device.getVehicleInfo();
            this.autoUpdateOdometer = device.isAutoUpdateOdometer();
            this.timeout = device.getTimeout();
            this.minIdleTime = device.getMinIdleTime();
            this.idleSpeedThreshold = device.getIdleSpeedThreshold();
            this.timeZoneOffset = device.getTimezoneOffset();
            this.commandPassword = device.getCommandPassword();
            this.historyLength = device.getHistoryLength();
            this.validTo = device.getValidTo();
            this.showOdometer = device.isShowOdometer();
            this.showProtocol = device.isShowProtocol();
            this.showName = device.isShowName();
            this.arrowRadius = device.getIconArrowRadius();
            this.arrowMovingColor = device.getIconArrowMovingColor();
            this.arrowStoppedColor = device.getIconArrowStoppedColor();
            this.arrowPausedColor = device.getIconArrowPausedColor();
            this.arrowOfflineColor = device.getIconArrowOfflineColor();
        }
    }
}
