/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
