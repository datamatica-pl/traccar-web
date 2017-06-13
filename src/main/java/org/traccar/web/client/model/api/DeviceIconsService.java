/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.client.model.api;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("../api/v1/resources/deviceicons")
public interface DeviceIconsService extends RestService {
    @GET
    public void getIconList(MethodCallback<List<ApiDeviceIcon>> callback);
}
