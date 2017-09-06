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
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.GroupStore;
import org.traccar.web.client.model.api.ApiCommandType;
import org.traccar.web.client.model.api.ApiDeviceIcon;
import org.traccar.web.client.model.api.ApiDeviceModel;
import org.traccar.web.client.model.api.ApplicationSettingsService;
import org.traccar.web.client.model.api.ApplicationSettingsService.ApplicationSettingsDto;
import org.traccar.web.client.model.api.IGroupService.DeviceGroupDto;
import org.traccar.web.client.model.api.ResourcesService;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Group;
import pl.datamatica.traccar.model.User;
import org.traccar.web.client.model.api.IUsersService;
import org.traccar.web.client.model.api.GroupService;
import org.traccar.web.client.model.api.Resources;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author ŁŁ
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
        unansweredRequests = 5;
        ResourcesService res = GWT.create(ResourcesService.class);
        IUsersService users = GWT.create(IUsersService.class);
        ApplicationSettingsService settings = GWT.create(ApplicationSettingsService.class);
        User user = ApplicationContext.getInstance().getUser();
        
        settings.get(new MethodCallback<ApplicationSettingsDto>() {
            @Override
            public void onFailure(Method method, Throwable exception) {
                new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
            }

            @Override
            public void onSuccess(Method method, ApplicationSettingsDto response) {
                onRequestAnswered();
                ApplicationContext.getInstance().setApplicationSettings(response.toApplicationSettings());
            }
        });
        
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
                        Resources.getInstance().icon(ico.getId(), 
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
                        Resources.getInstance().model(m);
                    }
                }
            }
        });
        
        if(user.hasPermission(UserPermission.DEVICE_GROUP_MANAGEMENT)) {
            final GroupService service = new GroupService();
            service.getGroups(new MethodCallback<List<DeviceGroupDto>>() {
                @Override
                public void onFailure(Method method, Throwable exception) {
                    new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                }

                @Override
                public void onSuccess(Method method, List<DeviceGroupDto> response) {
                    onRequestAnswered();
                    List<Group> groups = new ArrayList<>();
                    for(DeviceGroupDto dto : response)
                        groups.add(dto.toGroup());
                    ApplicationContext.getInstance().setGroups(groups);
                    groupStore.add(groups);
                }
            });
        } else
            --unansweredRequests;
        
        if(user.hasPermission(UserPermission.USER_MANAGEMENT)) {
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
        } else {
            --unansweredRequests;
        }
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
