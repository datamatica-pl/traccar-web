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
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.ContentPanel;
import java.util.List;
import org.traccar.web.client.Application;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.view.DeviceView;
import org.traccar.web.client.view.RouteDialog;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Route;
import pl.datamatica.traccar.model.RoutePoint;

public class RouteController implements DeviceView.RouteHandler, ContentController{
    private ListStore<Device> deviceStore;
    private ListStore<GeoFence> geoFenceStore;
    private final ListStore<Route> routeStore;
    private Messages i18n = GWT.create(Messages.class);
    
    public RouteController(ListStore<Device> devStore, ListStore<GeoFence> gfStore) {
        this.deviceStore = devStore;
        this.geoFenceStore = gfStore;
        routeStore = new ListStore<>(new ModelKeyProvider<Route>() {
            @Override
            public String getKey(Route item) {
                return Long.toString(item.getId());
            } 
        });
    }
    
    @Override
    public void onAdd() {
        new RouteDialog(new Route(), new RouteDialog.RouteHandler() {
            @Override
            public void onSave(final Route route, final boolean connect) {
                Application.getDataService().addRoute(route, connect,
                        new BaseAsyncCallback<Route>(i18n) {
                            @Override
                            public void onSuccess(final Route addedRoute) {
                                updateGeofences(addedRoute);
                                if(connect)
                                    routeStore.add(addedRoute);
                            }
                        });
            }
            
        }, deviceStore, geoFenceStore).show();
    }
    
    @Override
    public void onEdit(Route selectedItem) {
        new RouteDialog(selectedItem, new RouteDialog.RouteHandler() {
            @Override
            public void onSave(final Route route, boolean connect) {
                Application.getDataService().updateRoute(route,
                        new BaseAsyncCallback<Route>(i18n) {
                            @Override
                            public void onSuccess(final Route updated) {
                                updateGeofences(updated);
                                routeStore.update(updated);
                            }
                        });
            }
            
        }, deviceStore, geoFenceStore).show();
    }
    
    private void updateGeofences(final Route addedRoute) {
                        for(RoutePoint pt : addedRoute.getRoutePoints()) {
                            String key = Long.toString(pt.getGeofence().getId());
                            if(geoFenceStore.findModelWithKey(key) == null) {
                                geoFenceStore.add(pt.getGeofence());
                            }
                        }
                        geoFenceStore.applySort(false);
                    }

    
    @Override
    public void onRemove(final Route route) {
        Application.getDataService().removeRoute(route,
                new BaseAsyncCallback<Route>(i18n) {
                    @Override
                    public void onSuccess(final Route removed) {
                        routeStore.remove(route);
                    }
                });
    }
    
    
    
    public ListStore<Route> getStore() {
        return routeStore;
    }

    @Override
    public ContentPanel getView() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void run() {
        Application.getDataService().getRoutes(new BaseAsyncCallback<List<Route>>(i18n) {
            @Override
            public void onSuccess(List<Route> result) {
                routeStore.addAll(result);
            }
        });
    }
}
