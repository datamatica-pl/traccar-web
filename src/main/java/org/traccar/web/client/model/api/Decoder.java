/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.client.model.api;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.traccar.web.client.ApplicationContext;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.DeviceIconMode;
import pl.datamatica.traccar.model.DeviceIconType;
import pl.datamatica.traccar.model.Group;
import pl.datamatica.traccar.model.Position;

public class Decoder {
    private final Map<Long, Group> groupMap = new HashMap<>();
    
    public List<Device> decodeDevices(JSONObject v) {
        JSONArray arr = v.get("changed").isArray();
        List<Device> devices = new ArrayList<>();
        for(int i=0;i<arr.size();++i)
            devices.add(decodeDevice(arr.get(i).isObject()));
        return devices;
    }
    
    public Device decodeDevice(JSONObject v) {
        Device d = new Device();
        d.setId(aLong(v, "id"));
        d.setUniqueId(string(v, "uniqueId"));
        d.setName(string(v, "deviceName"));
        d.setColor(string(v, "color"));
        
        if(v.get("lastPosition") != null && v.get("lastPosition").isObject() != null) {
            d.setLatestPosition(decodePosition(v.get("lastPosition").isObject()));
            d.getLatestPosition().setDevice(d);
        }
        d.setGroup(groupMap.get(aLong(v, "groupId")));

        d.setDeviceModelId(aLong(v, "deviceModelId"));
        d.setIconId(aLong(v, "iconId"));
        d.setCustomIconId(aLong(v, "customIconId"));

        d.setBatteryLevel(anInt(v, "batteryLevel"));
        d.setBatteryTime(date(v, "batteryTime"));

        d.setValidTo(date(v, "validTo"));
        d.setHistoryLength(anInt(v, "historyLength"));
        d.setBlocked(bool(v, "blocked"));
        
        d.setSpeedLimit(aDouble(v, "speedLimit"));
        d.setStatus(string(v, "status"));
        d.setOwner(ApplicationContext.getInstance().getUser());
        d.setUsers(Collections.singleton(d.getOwner()));

        d.setPhoneNumber(string(v, "phoneNumber"));
        d.setPlateNumber(string(v, "plateNumber"));
        d.setDescription(string(v, "description"));
        
        d.setProtocol("null");
        d.setAlarmEnabled(bool(v, "autoArm", false));
        d.setMaintenances(Collections.EMPTY_LIST);
        d.setRegistrations(Collections.EMPTY_LIST);
        d.setSensors(Collections.EMPTY_LIST);

        d.setIconType(DeviceIconType.BICYCLE);
        d.setIconMode(DeviceIconMode.ICON);
        return d;
    }
    
    public Position decodePosition(JSONObject v) {
        if(v == null)
            return null;
        Position p = new Position(aLong(v, "id"));
        p.setLatitude(aDouble(v, "latitude"));
        p.setLongitude(aDouble(v, "longitude"));
        p.setTime(date(v, "deviceTime"));
        p.setSpeed(aDouble(v, "speed"));
        p.setCourse(aDouble(v, "course"));
        p.setValid(bool(v, "isValid"));        
        p.setAltitude(aDouble(v, "altitude"));
        return p;
    }
    
    public void setGroups(Collection<Group> groups) {
        groupMap.clear();
        for(Group g : groups)
            groupMap.put(g.getId(), g);
    }
    
    
    private Date date(JSONObject v, String name) {
        String date = string(v, name);
        if(date == null)
            return null;
        //yyyy-MM-dd'T'HH:mm:ssX
        DateTimeFormat fmt = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        return fmt.parse(date);
    }
    
    private String string(JSONObject v, String name) {
        if(v.get(name) == null || v.get(name).isString() == null)
            return null;
        return v.get(name).isString().stringValue();
    }
    
    private Long aLong(JSONObject v, String name) {
        Double val = aDouble(v, name);
        if(val == null)
            return null;
        return (long)val.longValue();
    }
    
    private Integer anInt(JSONObject v, String name) {
        Double val = aDouble(v, name);
        if(val == null)
            return null;
        return (int)val.intValue();
    }
    
    private Double aDouble(JSONObject v, String name) {
        if(v.get(name) == null || v.get(name).isNumber() == null)
            return null;
        return v.get(name).isNumber().doubleValue();
    }
    
    private Boolean bool(JSONObject v, String name) {
        if(v.get(name) == null || v.get(name).isBoolean() == null)
            return null;
        return v.get(name).isBoolean().booleanValue();
    }
    
    private boolean bool(JSONObject v, String name, boolean def) {
        Boolean b = bool(v, name);
        return b == null ? def : b;
    }
}
