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
    public String points;
    public Float radius;
    public String type;
    public Set<Long> deviceIds;
    
    public GeoFence toGeofence(List<Device> devices) {
        GeoFence gf = new GeoFence(id, geofenceName);
        gf.setDescription(description);
        gf.setColor(color);
        gf.setPoints(points);
        gf.setRadius(radius == null ? 0 : radius);
        gf.setType(GeoFenceType.valueOf(type));
        
        gf.setDeleted(false);
        gf.setDevices(new HashSet<>());
        for(Device d : devices)
            if(deviceIds.contains(d.getId()))
                gf.getDevices().add(d);
        return gf;
    }
}
