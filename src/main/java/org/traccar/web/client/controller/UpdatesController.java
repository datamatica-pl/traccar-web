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

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import java.util.ArrayList;
import java.util.List;
import org.fusesource.restygwt.client.Method;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.api.ApiJsonCallback;
import org.traccar.web.client.model.api.ApiMethodCallback;
import org.traccar.web.client.model.api.ApiRoute;
import org.traccar.web.client.model.api.Decoder;
import org.traccar.web.client.model.api.RoutesService;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.Route;
import pl.datamatica.traccar.model.UserPermission;

public class UpdatesController {
    public interface LatestPositionsListener {
        void onPositionsUpdated(List<Position> result);
    }
    public interface DevicesListener {
        void onDevicesUpdated(List<Device> devices);
    }
    public interface RoutesListener {
        void onRoutesUpdated(List<ApiRoute> routes);
    }
    
    private static final int MAX_UPDATE_FAILURE_COUNT = 3;
    private final Messages i18n = GWT.create(Messages.class);
    
    private Timer updateTimer;
    private int updateFailureCount;
    
    private List<LatestPositionsListener> latestPositionsListeners;
    private List<DevicesListener> devicesListeners;
    private List<RoutesListener> routesListeners;
    
    public UpdatesController() {
        latestPositionsListeners = new ArrayList<>();
        devicesListeners = new ArrayList<>();
        routesListeners = new ArrayList<>();
    }
    
    public static interface RouteMapper extends ObjectMapper<List<ApiRoute>> {}
    
    public void update() {
        updateTimer.cancel();
        Application.getDevicesService().getDevices(new ApiJsonCallback(i18n) {
            @Override
            public void onFailure(Method method, Throwable caught) {
                if(++updateFailureCount >= MAX_UPDATE_FAILURE_COUNT) {
                    updateTimer.cancel();
                    super.onFailure(method, caught);
                } else {
                    updateTimer.schedule(ApplicationContext.getInstance()
                        .getApplicationSettings().getUpdateInterval());
                }
            }
            
            @Override
            public void onSuccess(Method method, JSONValue response) {
                Decoder dec = Application.getDecoder();
                List<Device> dev = dec.decodeDevices(response.isObject());
                devicesLoaded(dev);
            }
        });
        
        RoutesService service = GWT.create(RoutesService.class);
        final RouteMapper mapper = GWT.create(RouteMapper.class);
        if(ApplicationContext.getInstance().getUser().hasPermission(UserPermission.TRACK_READ))
            service.getRoutes(false, new ApiJsonCallback(i18n) {
                @Override
                public void onSuccess(Method method, JSONValue response) {
                    List<ApiRoute> routes = mapper.read(response.toString());
                    for(RoutesListener l : routesListeners)
                        l.onRoutesUpdated(routes);
                }                
            });
    }
    
    public void devicesLoaded(List<Device> dev) {
        List<Position> pos = new ArrayList<>();
        for(Device d : dev) {
            if(d.getLatestPosition() != null)
                pos.add(d.getLatestPosition());
        }
        for(LatestPositionsListener listener : latestPositionsListeners)
            listener.onPositionsUpdated(pos);
        for(DevicesListener listener : devicesListeners)
            listener.onDevicesUpdated(dev);
        updateFailureCount = 0;
        updateTimer.schedule(ApplicationContext.getInstance()
                .getApplicationSettings().getUpdateInterval());
    }
    
    public void run() {       
        updateTimer = new Timer() {
            @Override
            public void run() {
                update();
            }
        };
        updateTimer.schedule(ApplicationContext.getInstance()
                        .getApplicationSettings().getUpdateInterval());
    }
    
    public void addLatestPositionsListener(LatestPositionsListener listener) {
        latestPositionsListeners.add(listener);
    }
    
    public void addDevicesListener(DevicesListener listener) {
        devicesListeners.add(listener);
    }
    
    public void addRoutesListener(RoutesListener listener) {
        routesListeners.add(listener);
    }
}
