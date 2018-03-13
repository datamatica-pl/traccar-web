/*
 * Copyright 2018 Datamatica (dev@datamatica.pl)
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Route;
import pl.datamatica.traccar.model.Route.Status;
import pl.datamatica.traccar.model.RoutePoint;

/**
 *
 * @author ŁŁ
 */
public class ApiRoute {
    public long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ",
                timezone="GMT")
    public Date createdDate;
    public String status;
    public Long corridorId;
    public String name;
    public Long deviceId;
    public int tolerance;
    public int archiveAfter;
    public boolean forceFirst;
    public boolean forceLast;
    public List<ApiRoutePoint> points;
    public String polyline;
    
    public ApiRoute() {}
    
    public Route toRoute(List<GeoFence> gfs, List<Device> devices) {
        Map<Long, GeoFence> gfMap = new HashMap<>();
        for(GeoFence gf : gfs)
            gfMap.put(gf.getId(), gf);
        GeoFence corridor = corridorId == null ? null : gfMap.get(corridorId);            
        Route r = new Route(id, createdDate, Status.valueOf(status), corridor);
        if(deviceId != null)
            for(Device d : devices)
                if(d.getId() == deviceId)
                    r.setDevice(d);
        r.setTolerance(tolerance);
        r.setArchiveAfter(archiveAfter);
        r.setForceFirst(forceFirst);
        r.setForceLast(forceLast);
        r.setName(name);
        
        for(ApiRoutePoint rp : points)
            r.getRoutePoints().add(rp.toRoutePoint(gfMap));
        
        r.setLinePoints(polyline);
        return r;
    }
}

class ApiRoutePoint {
    public long id;
    public long geofenceId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ",
            timezone="GMT")
    public Date deadline;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ",
            timezone="GMT")
    public Date enterTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ",
            timezone="GMT")
    public Date exitTime;

    public long getId() {
        return id;
    }
    
    public long getGeofenceId() {
        return geofenceId;
    }
    
    public Date getDeadline() {
        return deadline;
    }
    
    public Date getEnterTime() {
        return enterTime;
    }
    
    public Date getExitTime() {
        return exitTime;
    }
    
    public RoutePoint toRoutePoint(Map<Long, GeoFence> gfMap) {
        RoutePoint rp = new RoutePoint(id, gfMap.get(geofenceId));
        rp.setDeadline(deadline);
        rp.setEnterTime(enterTime);
        rp.setExitTime(exitTime);
        return rp;
    }
}
