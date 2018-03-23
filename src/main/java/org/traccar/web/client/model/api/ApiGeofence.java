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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.GeoFenceType;

/**
 *
 * @author ŁŁ
 */
public class ApiGeofence {
    public long id;
    public String geofenceName;
    public String description;
    public String color;
    public boolean allDevices = false;
    public List<ApiPoint> points;
    public Float radius;
    public String type;
    public Set<Long> deviceIds;
    public String lastUpdate;
    public String address;
    public boolean isRouteOnly;
    
    public ApiGeofence(){}
    
    public ApiGeofence(GeoFence gf) {
        this.geofenceName = gf.getName();
        this.description = gf.getDescription();
        this.color = gf.getColor();
        
        this.points = new ArrayList<>();
        String[] pts = gf.getPoints().split(",");
        for(String pt : pts)
            points.add(ApiPoint.parsePoint(pt));
        
        this.radius = gf.getRadius();
        if(gf.getType() != null)
            this.type = gf.getType().name();
        this.deviceIds = new HashSet<>();
        if(gf.getDevices() != null)
            for(Device d : gf.getDevices())
                this.deviceIds.add(d.getId());
        this.address = gf.getAddress();
    }
    
    public GeoFence toGeofence(List<Device> devices) {
        GeoFence gf = new GeoFence(id, geofenceName);
        gf.setRouteOnly(isRouteOnly);
        gf.setDescription(description);
        gf.setColor(color);
        
        StringBuilder pts = new StringBuilder();
        for(ApiPoint pt : points)
            pts.append(pt.longitude).append(" ").append(pt.latitude).append(",");
        if(pts.length() > 0)
            pts.deleteCharAt(pts.length()-1);
        gf.setPoints(pts.toString());
        
        gf.setRadius(radius == null ? 0 : radius);
        gf.setType(GeoFenceType.valueOf(type));
        gf.setAddress(address);
        
        gf.setDeleted(false);
        gf.setDevices(new HashSet<Device>());
        gf.setTransferDevices(new HashSet<Device>());
        for(Device d : devices) {
            if(deviceIds.contains(d.getId())) {
                gf.getDevices().add(d);
                gf.getTransferDevices().add(d);
            }
        }
        return gf;
    }
}
