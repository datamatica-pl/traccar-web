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
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Date;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.Route;

public interface DataServiceAsync {

    void authenticated(AsyncCallback<User> callback);
    void login(String login, String password, boolean passwordHashed, AsyncCallback<User> callback);
    void login(String login, String password, AsyncCallback<User> callback);
    void logout(AsyncCallback<Boolean> callback);
    
    void getDevices(AsyncCallback<List<Device>> callback);
    void getPositions(Device device, Date from, Date to, boolean filter, AsyncCallback<List<Position>> callback);
    void getGeoFences(AsyncCallback<List<GeoFence>> async);
    
    void addRoute(Route route, boolean connect, AsyncCallback<Route> async);
    public void getRoutes(AsyncCallback<List<Route>> asyncCallback);
    public void updateRoute(Route updated, AsyncCallback<Route> asyncCallback);
    public void removeRoute(Route route, AsyncCallback<Route> asyncCallback);
    
    void getApplicationSettings(AsyncCallback<ApplicationSettings> async);
}
