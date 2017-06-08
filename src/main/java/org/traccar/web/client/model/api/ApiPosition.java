/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.client.model.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Position;

public class ApiPosition {
    long id;
    double latitude;
    double longitude;
    Date deviceTime;
    double speed;
    double course;
    boolean isValid;
    long deviceId;
    
    double altitude;
    
    @JsonIgnore
    Position p;

    Position getPosition(Device d) {
        if(p == null) {
            p = new Position(id);
            p.setLatitude(latitude);
            p.setLongitude(longitude);
            p.setTime(deviceTime);
            p.setSpeed(speed);
            p.setCourse(course);
            p.setValid(isValid);
            p.setDevice(d);
            p.setAltitude(altitude);
        }
        return p;
    }
}
