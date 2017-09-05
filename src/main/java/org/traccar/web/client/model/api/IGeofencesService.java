/*
 *  Copyright (C) 2017  Datamatica (dev@datamatica.pl)
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published
 *  by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.traccar.web.client.model.api;

import java.util.List;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;
import pl.datamatica.traccar.model.GeoFence;

/**
 *
 * @author ŁŁ
 */
@Path("../api/v1/geofences")
public interface IGeofencesService extends RestService {
    @GET
    public void getGeoFences(MethodCallback<List<ApiGeofence>> callback);
    
    @POST
    public void addGeofence(ApiGeofence apiGeofence, MethodCallback<ApiGeofence> callback);
    
    @DELETE
    @Path("/{id}")
    public void removeGeofence(@PathParam("id") long id, JsonCallback callback);
    
    @GET
    @Path("/{id}/share")
    void getGeofenceShare(@PathParam("id") long id, MethodCallback<Set<Long>> callback);
}
