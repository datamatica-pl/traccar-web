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

import java.util.ArrayList;
import java.util.List;
import pl.datamatica.traccar.model.Route;
import pl.datamatica.traccar.model.RoutePoint;

public class ApiEditRoute extends ApiRoute{
    public List<ApiGeofence> newGeofences;
    public Float corridorWidth;
    public boolean archive;
    public boolean cancel;
    
    public ApiEditRoute() {   
    }
    
    public ApiEditRoute(Route r) {
        this.archive = r.isArchived();
        this.archiveAfter = r.getArchiveAfter();
        this.cancel = r.getStatus() == Route.Status.CANCELLED;
        if(r.getCorridor() != null)
            this.corridorWidth = r.getCorridor().getRadius();
        if(r.getDevice() != null)
            this.deviceId = r.getDevice().getId();
        this.forceFirst = r.isForceFirst();
        this.forceLast = r.isForceLast();
        this.name = r.getName();
        this.points = new ArrayList<ApiRoutePoint>();
        this.newGeofences = new ArrayList<ApiGeofence>();
        for(RoutePoint rp : r.getRoutePoints()) {
            this.points.add(new ApiRoutePoint(rp));
            if(rp.getGeofence().getId() == 0)
                this.newGeofences.add(new ApiGeofence(rp.getGeofence()));
        }
        this.polyline = r.getLinePoints();
        this.tolerance = r.getTolerance();
    }
}
