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
package org.traccar.web.client.controller;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.data.shared.ListStore;
import java.util.List;
import org.traccar.web.client.Application;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.view.DeviceView;
import org.traccar.web.client.view.TrackDialog;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Route;
import pl.datamatica.traccar.model.RoutePoint;

public class RouteController implements DeviceView.RouteHandler{
    private ListStore<Device> deviceStore;
    private ListStore<GeoFence> geoFenceStore;
    private Messages i18n = GWT.create(Messages.class);
    
    public RouteController(ListStore<Device> devStore, ListStore<GeoFence> gfStore) {
        this.deviceStore = devStore;
        this.geoFenceStore = gfStore;
    }
    
    @Override
    public void onAdd() {
        new TrackDialog(new TrackDialog.RouteHandler() {
            @Override
            public void onSave(Route route, boolean connect) {
                Application.getDataService().addRoute(route, connect,
                        new BaseAsyncCallback<Route>(i18n) {
                            @Override
                            public void onSuccess(Route addedRoute) {
                                for(RoutePoint pt : addedRoute.getRoutePoints()) {
                                    if(!geoFenceStore.hasRecord(pt.getGeofence()))
                                        geoFenceStore.add(pt.getGeofence());
                                }
                                geoFenceStore.applySort(false);
                            }
                        });
            }
            
        }, deviceStore, geoFenceStore).show();
    }
}
