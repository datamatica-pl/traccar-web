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
package org.traccar.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONValue;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.DeviceStore;
import org.traccar.web.client.model.GroupService;
import org.traccar.web.client.model.GroupServiceAsync;
import org.traccar.web.client.model.GroupStore;
import org.traccar.web.client.model.api.ApiCommandType;
import org.traccar.web.client.model.api.ApiDeviceIcon;
import org.traccar.web.client.model.api.ApiDeviceModel;
import org.traccar.web.client.model.api.ResourcesService;
import org.traccar.web.client.model.api.UsersService;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Group;
import pl.datamatica.traccar.model.User;

/**
 *
 * @author Łukasz
 */
public class InitialLoader {
    public interface LoadFinishedListener {
        void onLoadFinished();
    }
    
    private LoadFinishedListener listener;
    private final ListStore<Device> deviceStore;
    private final GroupStore groupStore;
    private Messages i18n = GWT.create(Messages.class);
    private int unansweredRequests;
    
    public InitialLoader(ListStore<Device> deviceStore, GroupStore groupStore) {
        this.groupStore = groupStore;
        this.deviceStore = deviceStore;
    }
    
    public void load(LoadFinishedListener listener) {
        this.listener = listener;
        unansweredRequests = 4;
        ResourcesService res = GWT.create(ResourcesService.class);
        UsersService users = GWT.create(UsersService.class);
        
        res.getDeviceIcons(new MethodCallback<List<ApiDeviceIcon>>() {
            @Override
            public void onFailure(Method method, Throwable exception) {
                new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
            }

            @Override
            public void onSuccess(Method method, List<ApiDeviceIcon> response) {
                onRequestAnswered();
                for(ApiDeviceIcon ico : response)
                    if(!ico.isDeleted())
                        Application.getResources().icon(ico.getId(), 
                                ico.getUrl().replace("/images/", "/markers/"));
            }
        });
        
        res.getDeviceModels(new MethodCallback<List<ApiDeviceModel>>() {
            @Override
            public void onFailure(Method method, Throwable exception) {
                new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
            }

            @Override
            public void onSuccess(Method method, List<ApiDeviceModel> response) {
                onRequestAnswered();
                for(ApiDeviceModel m : response) {
                    if(!m.isDeleted()) {
                        List<ApiCommandType> nonTcp = new ArrayList<>();
                        for(ApiCommandType ct : m.getCommandTypes())
                            if(!ct.isTCP())
                                nonTcp.add(ct);
                        m.getCommandTypes().removeAll(nonTcp);
                        Application.getResources().model(m);
                    }
                }
            }
        });
        
        final GroupServiceAsync service = GWT.create(GroupService.class);
        service.getGroups(new BaseAsyncCallback<Map<Group, Group>>(i18n) {
            @Override
            public void onSuccess(Map<Group, Group> result) {
                onRequestAnswered();
                // put root groups into the first level records to add
                List<Group> toAdd = new ArrayList<>();
                for (Map.Entry<Group, Group> entry : result.entrySet()) {
                    if (entry.getValue() == null) {
                        toAdd.add(entry.getKey());
                    }
                }
                ApplicationContext.getInstance().setGroups(result.keySet());

                // fill tree store level by level
                while (!toAdd.isEmpty()) {
                    List<Group> newToAdd = new ArrayList<>();
                    for (Group group : toAdd) {
                        Group parent = result.get(group);
                        if (parent == null) {
                            groupStore.add(group);
                        } else {
                            groupStore.add(parent, group);
                        }
                        // prepare next level groups as children of currently processed group
                        for (Map.Entry<Group, Group> entry : result.entrySet()) {
                            if (Objects.equals(entry.getValue(), group)) {
                                newToAdd.add(entry.getKey());
                            }
                        }
                    }
                    toAdd = newToAdd;
                }
            }
        });
        
        users.getUsers(new JsonCallback() {
            @Override
            public void onFailure(Method method, Throwable exception) {
                new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
            }

            @Override
            public void onSuccess(Method method, JSONValue response) {
                onRequestAnswered();
                List<User> users = Application.getDecoder().decodeUsers(response.isArray());
                ApplicationContext.getInstance().setUsers(users);
            }
        });
    }
    
    private void onRequestAnswered() {
        --unansweredRequests;
        if(unansweredRequests == 0)
            Application.getDevicesService().getDevices(new JsonCallback() {
                @Override
                public void onFailure(Method method, Throwable exception) {
                    new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                }

                @Override
                public void onSuccess(Method method, JSONValue response) {
                    List<Device> result = Application.getDecoder()
                            .decodeDevices(response.isObject());
                    deviceStore.addAll(result);
                    listener.onLoadFinished();
                }   
            });
    }
}
