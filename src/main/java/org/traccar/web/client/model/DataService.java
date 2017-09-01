/*
 * Copyright 2013 Anton Tananaev (anton.tananaev@gmail.com)
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
package org.traccar.web.client.model;

import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.Device;
import java.util.Date;
import java.util.List;

import org.traccar.web.shared.model.*;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import pl.datamatica.traccar.model.Route;

@RemoteServiceRelativePath("dataService")
public interface DataService extends RemoteService {
  
    User authenticated();
    User login(String login, String password, boolean passwordHashed) throws TraccarException;
    User login(String login, String password) throws TraccarException;
    boolean logout();
    
    List<Device> getDevices();
    List<Position> getPositions(Device device, Date from, Date to, boolean filter) throws AccessDeniedException;
    List<GeoFence> getGeoFences();
    
    List<Route> getRoutes();
    Route addRoute(Route route, boolean connect) throws TraccarException;
    Route updateRoute(Route updated) throws TraccarException;
    Route removeRoute(Route route);
    
    ApplicationSettings getApplicationSettings();
}
