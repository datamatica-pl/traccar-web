/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.client.model.api;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import pl.datamatica.traccar.model.CommandType;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.DeviceIconMode;
import pl.datamatica.traccar.model.Maintenance;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.User;

public class Decoder {    
    public List<Device> decodeDevices(JSONObject v) {
        JSONArray arr = v.get("changed").isArray();
        List<Device> devices = new ArrayList<>();
        for(int i=0;i<arr.size();++i)
            devices.add(decodeDevice(arr.get(i).isObject()));
        return devices;
    }
    
    public Device decodeDevice(JSONObject v) {
        if(v == null)
            return null;
        
        Device d = new Device();
        d.setId(aLong(v, "id"));
        d.setUniqueId(string(v, "uniqueId"));
        d.setName(string(v, "deviceName"));
        d.setColor(string(v, "color"));

        d.setDeviceModelId(aLong(v, "deviceModelId"));
        d.setIconId(aLong(v, "iconId"));
        d.setCustomIconId(aLong(v, "customIconId"));

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
        
        d.setProtocol("");
        d.setAlarmEnabled(bool(v, "autoArm", false));
        d.setRegistrations(Collections.EMPTY_LIST);
        d.setSensors(Collections.EMPTY_LIST);
        
        d.setUnreadAlarms(bool(v, "unreadAlarms", false));
        d.setLastAlarmsCheck(date(v, "lastAlarmsCheck"));
        
        d.setIconMode(DeviceIconMode.ICON);
        return d;
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

    private long[] longArr(JSONObject v, String name) {
        if(v.get(name) == null || v.get(name).isArray() == null)
            return null;
        JSONArray arr = v.get(name).isArray();
        long[] lArr = new long[arr.size()];
        for(int i=0;i<arr.size();++i) {
            if(arr.get(i).isNumber() == null)
                return null;
            lArr[i] = (long)arr.get(i).isNumber().doubleValue();
        }
        return lArr;
    }
}
