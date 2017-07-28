/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.client.model.api;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
        if(v.get("lastPosition") != null && v.get("lastPosition").isObject() != null) {
            d.setLatestPosition(decodePosition(v.get("lastPosition").isObject()));
            d.getLatestPosition().setDevice(d);
        }
        d.setGroup(ApplicationContext.getInstance().getGroup(aLong(v, "groupId")));

        d.setDeviceModelId(aLong(v, "deviceModelId"));
        d.setIconId(aLong(v, "iconId"));
        d.setCustomIconId(aLong(v, "customIconId"));

        d.setBatteryLevel(anInt(v, "batteryLevel"));
        d.setBatteryTime(date(v, "batteryTime"));
        //API adds 1 day to validTo field!
        Date validTo = date(v, "validTo");
        if(validTo != null)
            d.setValidTo(new Date(validTo.getTime()-24*60*60*1000));
        d.setHistoryLength(anInt(v, "historyLength"));
        d.setBlocked(bool(v, "blocked"));
        
        d.setSpeedLimit(aDouble(v, "speedLimit"));
        d.setStatus(string(v, "status"));
        d.setOwner(ApplicationContext.getInstance().getUser());
        
        Set<User> users = new HashSet<>();
        long[] userIds = longArr(v, "userIds");
        if(userIds == null)
            users = Collections.singleton(d.getOwner());
        else {
            for(long id : userIds)
                users.add(ApplicationContext.getInstance().getUser(id));
        }
        d.setUsers(users);

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
        
        ApiDeviceModel model = Application.getResources().model(d.getDeviceModelId());
        if(model != null)
            for(ApiCommandType ct : model.getCommandTypes()) {
                d.addSupportedCommand(CommandType.fromString(ct.getCommandName()));
            }
        if(v.get("maintenances") != null && v.get("maintenances").isArray() != null) {
            JSONArray arr = v.get("maintenances").isArray();
            List<Maintenance> ms = new ArrayList<>();
            for(int i=0;i<arr.size();++i)
                ms.add(decodeMaintenance(arr.get(i).isObject()));
            d.setMaintenances(ms);
        }
        
        d.setVehicleInfo(string(v, "vehicleInfo"));
        d.setAutoUpdateOdometer(bool(v, "autoUpdateOdometer", false));
        d.setTimeout(anInt(v, "timeout"));
        d.setTimezoneOffset(anInt(v, "timeZoneOffset"));
        d.setCommandPassword(string(v, "commandPassword"));
        d.setShowOdometer(bool(v, "showOdometer"));
        d.setShowProtocol(bool(v, "showProtocol"));
        d.setShowName(bool(v, "showName"));
        Double arrowRadius = aDouble(v, "arrowRadius");
        if(arrowRadius != null)
            d.setIconArrowRadius(arrowRadius);
        d.setIconArrowMovingColor(string(v, "arrowMovingColor"));
        d.setIconArrowOfflineColor(string(v, "arrowOfflineColor"));
        d.setIconArrowPausedColor(string(v, "arrowPausedColor"));
        d.setIconArrowStoppedColor(string(v, "arrowStoppedColor"));
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
        if(v.get("other") != null && v.get("other").isString() != null)
            p.setOther(v.get("other").isString().stringValue());
        return p;
    }
    
    public List<User> decodeUsers(JSONArray arr) {
        List<User> users = new ArrayList<>();
        for(int i=0;i<arr.size();++i)
            users.add(decodeUser(arr.get(i).isObject()));
        return users;
    }
    
    public User decodeUser(JSONObject v) {
        User u = new User();
        u.setId(aLong(v, "id"));
        u.setLogin(string(v, "login"));
        u.setEmail(string(v, "email"));
        u.setCompanyName(string(v, "companyName"));
        u.setFirstName(string(v, "firstName"));
        u.setLastName(string(v, "lastName"));
        u.setPhoneNumber(string(v, "phoneNumber"));
        u.setExpirationDate(date(v, "expirationDate"));
        u.setMaxNumOfDevices(anInt(v, "maxNumOfDevices"));
        u.setManagedBy(null);
        u.setManager(bool(v, "manager"));
        u.setAdmin(bool(v, "admin"));
        u.setArchive(bool(v, "archive"));
        u.setBlocked(bool(v, "blocked"));
        return u;
    }
    
    public Maintenance decodeMaintenance(JSONObject v) {
        if(v == null)
            return null;
        Maintenance m = new Maintenance();
        m.setId(aLong(v, "id"));
        m.setName(string(v, "name"));
        m.setServiceInterval(aDouble(v, "serviceInterval"));
        m.setLastService(aDouble(v, "lastService"));
        return m;
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
