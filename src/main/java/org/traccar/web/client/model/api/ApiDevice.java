/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.client.model.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.glass.ui.Application;
import java.util.Collections;
import java.util.Date;
import org.traccar.web.client.ApplicationContext;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.DeviceIcon;
import pl.datamatica.traccar.model.DeviceIconMode;
import pl.datamatica.traccar.model.DeviceIconType;
import pl.datamatica.traccar.model.Position;

public class ApiDevice {
    long id;
    String uniqueId;
    String deviceName;
    String color;
    
    int deviceModelId;
    Long iconId;
    Long customIconId;
    
    Integer batteryLevel;
    Date batteryTime;
    int batteryTimeout;
    
    Date validTo;
    int historyLength;
    boolean blocked;
    
    ApiPosition lastPosition;
    Double speedLimit;
    String status;
    
    long accountId;
    
    String phoneNumber;
    String plateNumber;
    String description;
    
    Boolean autoArm;
    
    @JsonIgnore
    Device d;
    
    public Device getDevice() {
        if(d == null) {
            d = new Device();
            d.setId(id);
            d.setUniqueId(uniqueId);
            d.setName(deviceName);
            d.setColor(color);
            d.setLatestPosition(lastPosition.getPosition(d));

            d.setDeviceModelId(deviceModelId);
            d.setIconId(iconId);
            d.setCustomIconId(customIconId);

            d.setBatteryLevel(batteryLevel);
            d.setBatteryTime(batteryTime);

            d.setValidTo(validTo);
            d.setHistoryLength(historyLength);
            d.setBlocked(blocked);

            d.setSpeedLimit(speedLimit);
            d.setStatus(status);
            d.setOwner(ApplicationContext.getInstance().getUser());

            d.setPhoneNumber(phoneNumber);
            d.setPlateNumber(plateNumber);
            d.setDescription(description);
            
            d.setProtocol("null");
            d.setAlarmEnabled(autoArm);
            d.setMaintenances(Collections.EMPTY_LIST);
            d.setRegistrations(Collections.EMPTY_LIST);
            d.setSensors(Collections.EMPTY_LIST);
            
            d.setIconType(DeviceIconType.BICYCLE);
            d.setIconMode(DeviceIconMode.ICON);
        }            
        return d;
    }
    
    public Position getLastPosition() {
        return getDevice().getLatestPosition();
    }
}
