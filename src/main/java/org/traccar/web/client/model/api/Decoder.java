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

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.traccar.web.client.ApplicationContext;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.DeviceEventType;
import pl.datamatica.traccar.model.DeviceIconMode;
import pl.datamatica.traccar.model.Maintenance;
import pl.datamatica.traccar.model.MaintenanceBase;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.PositionIconType;
import pl.datamatica.traccar.model.RegistrationMaintenance;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserGroup;
import pl.datamatica.traccar.model.UserPermission;
import pl.datamatica.traccar.model.UserSettings;

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
        d.setIdleSpeedThreshold(aDouble(v, "idleSpeedThreshold"));
        d.setStatus(string(v, "status"));
        d.setOwner(ApplicationContext.getInstance().getUser(aLong(v, "accountId")));
        
        d.setIgnition(bool(v, "ignition"));
        d.setIgnitionTime(date(v, "ignitionTime"));
        
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
        
        List<Maintenance> ms = new ArrayList<>();
        if(v.get("maintenances") != null && v.get("maintenances").isArray() != null) {
            JSONArray arr = v.get("maintenances").isArray();
            for(int i=0;i<arr.size();++i) {
                MaintenanceBase mb = decodeMaintenance(arr.get(i).isObject(), i);
                if(mb == null)
                    continue;
                ms.add((Maintenance)mb);
            }
        }
        d.setMaintenances(ms);
        
        List<RegistrationMaintenance> rms = new ArrayList<>();
        if(v.get("registrations") != null && v.get("registrations").isArray() != null) {
            JSONArray arr = v.get("registrations").isArray();
            for(int i=0;i<arr.size();++i) {
                MaintenanceBase mb = decodeMaintenance(arr.get(i).isObject(), i);
                if(mb == null)
                    continue;
                rms.add((RegistrationMaintenance)mb);
            }
        }
        d.setRegistrations(rms);
        
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
    
    public List<Position> decodePositions(Device d, JSONObject obj) {
        if(obj == null || obj.get("changed") == null 
                || obj.get("changed").isArray() == null)
            return Collections.EMPTY_LIST;
        JSONArray arr = obj.get("changed").isArray();
        ArrayList<Position> ps = new ArrayList<>();
        for(int i=0;i<arr.size();++i) {
            Position p = decodePosition(arr.get(i).isObject());
            p.setDevice(d);
            ps.add(p);
        }
        return ps;
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
        u.setManagedById(aLong(v, "managedById"));
        u.setBlocked(bool(v, "blocked"));
        u.setPassword(string(v, "password"));
        if(v.get("notificationEvents") != null && v.get("notificationEvents").isArray() != null) {
            JSONArray ne = v.get("notificationEvents").isArray();
            Set<DeviceEventType> notificationEvents = new HashSet<>();
            for(int i=0;i<ne.size();++i)
                if(ne.get(i).isString() != null)
                    notificationEvents.add(DeviceEventType.valueOf(ne.get(i)
                            .isString().stringValue()));
            u.setNotificationEvents(notificationEvents);
            u.setTransferNotificationEvents(new HashSet<>(notificationEvents));
        }
        u.setPremium(bool(v, "premium"));
        if(v.containsKey("settings") && v.get("settings").isObject() != null) {
            u.setUserSettings(decodeUserSettings(v.get("settings").isObject()));
        }
        if(v.containsKey("userGroup") && v.get("userGroup").isObject() != null) {
            u.setUserGroup(decodeUserGroup(v.get("userGroup").isObject()));
        }
        u.setUserGroupName(string(v, "userGroupName"));
        return u;
    }
    
    public UserSettings decodeUserSettings(JSONObject v) {
        UserSettings us = new UserSettings();
        String markerType = string(v, "archiveMarkerType");
        if(markerType != null && !markerType.isEmpty()) {
            us.setArchiveMarkerType(PositionIconType.valueOf(markerType));
        }
        us.setCenterLatitude(aDouble(v, "centerLatitude"));
        us.setCenterLongitude(aDouble(v, "centerLongitude"));
        us.setFollowedDeviceZoomLevel(aShort(v, "followedDeviceZoomLevel"));
        us.setHideDuplicates(bool(v, "hideDuplicates"));
        us.setHideInvalidLocations(bool(v, "hideInvalidLocations"));
        us.setHideZeroCoordinates(bool(v, "hideZeroCoordinates"));
        String mapType = string(v, "mapType");
        if(mapType != null && !mapType.isEmpty())
            us.setMapType(UserSettings.MapType.valueOf(mapType));
        us.setMaximizeOverviewMap(bool(v, "maximizeOverviewMap"));
        us.setMinDistance(aDouble(v, "minDistance"));
        us.setOverlays(string(v, "overlays"));
        us.setSpeedForFilter(aDouble(v, "speedForFilter"));
        us.setSpeedModifier(string(v, "speedModifier"));
        String speedUnit = string(v, "speedUnit");
        if(speedUnit != null && !speedUnit.isEmpty())
            us.setSpeedUnit(UserSettings.SpeedUnit.valueOf(speedUnit));
        us.setTimePrintInterval(aShort(v, "timePrintInterval"));
        us.setTimeZoneId(string(v, "timeZoneId"));
        us.setTraceInterval(aShort(v, "traceInterval"));
        us.setZoomLevel(anInt(v, "zoomLevel"));
        return us;
    }
    
    public UserGroup decodeUserGroup(JSONObject v) {
        UserGroup g = new UserGroup();
        g.setId(aLong(v, "id"));
        g.setName(string(v, "name"));
        
        Set<UserPermission> p = EnumSet.noneOf(UserPermission.class);
        JSONArray arr = v.get("permissions").isArray();
        for(int i=0;i<arr.size();++i)
            p.add(UserPermission.valueOf(arr.get(i).isString().stringValue()));
        g.setPermissions(p);
        
        return g;
    }
    
    public MaintenanceBase decodeMaintenance(JSONObject v, int i) {
        if(v == null)
            return null;
        MaintenanceBase m;
        if(aDouble(v, "lastService") != null) {
            Maintenance mt = new Maintenance();
            mt.setServiceInterval(aDouble(v, "serviceInterval"));
            mt.setLastService(aDouble(v, "lastService"));
            m = mt;
        } else {
            RegistrationMaintenance mt = new RegistrationMaintenance();
            mt.setServiceDate(date(v, "serviceDate"));
            m = mt;
        }
        m.setId(aLong(v, "id"));
        m.setName(string(v, "name"));
        m.setIndexNo(i);
        return m;
    }
    
    public List<ApiRulesVersion> decodeRules(JSONArray arr) {
        List<ApiRulesVersion> rvs = new ArrayList<>();
        if(arr == null)
            return rvs;
        for(int i=0;i<arr.size();++i) {
            ApiRulesVersion rv = new ApiRulesVersion();
            JSONObject obj = arr.get(i).isObject();
            if(obj == null)
                continue;
            rv.id = aLong(obj, "id");
            rv.description = string(obj, "description");
            rv.startDate = date(obj, "startDate");
            rv.url = string(obj, "url");
            rv.isObligatory = bool(obj, "isObligatory");
            rvs.add(rv);
        }
        return rvs;
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
    
    private Short aShort(JSONObject v, String name) {
        Double value = aDouble(v, name);
        if(value == null)
            return null;
        return value.shortValue();
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
