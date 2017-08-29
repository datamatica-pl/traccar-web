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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import java.util.List;
import java.util.Set;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.UserGroupProperties;
import org.traccar.web.client.model.UserProperties;
import org.traccar.web.client.model.api.UserGroupsService;
import org.traccar.web.client.view.NavView;
import org.traccar.web.client.view.UserGroupDialog;
import org.traccar.web.client.view.UserGroupsDialog;
import org.traccar.web.client.model.api.ApiUserGroup;
import org.traccar.web.client.view.UserShareDialog;
import org.traccar.web.client.view.UserShareDialog.UserShareHandler;

/**
 *
 * @author ŁŁ
 */
public class UserGroupsController implements NavView.GroupsHandler, 
        UserGroupsDialog.UserGroupsHandler{
    private final UserGroupProperties uGrpProperties = new UserGroupProperties();
    private final UserGroupsService service = new UserGroupsService();
    private final Messages i18n = GWT.create(Messages.class);
    
    private ListStore<ApiUserGroup> userGroups;
    
    public void onShowGroups() {
        service.getGroups(new MethodCallback<List<ApiUserGroup>>() {
            @Override
            public void onFailure(Method method, Throwable exception) {
                new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
            }

            @Override
            public void onSuccess(Method method, List<ApiUserGroup> response) {
                userGroups = new ListStore<>(uGrpProperties.id());
                userGroups.addAll(response);
                new UserGroupsDialog(userGroups, UserGroupsController.this).show();
            }
        });
    }

    @Override
    public void onAdd() {
        ApiUserGroup group = new ApiUserGroup();
        new UserGroupDialog(group, userGroups, new UserGroupDialog.UserGroupHandler() {
            @Override
            public void onSave(ApiUserGroup group) {
                service.addGroup(group, new MethodCallback<ApiUserGroup>(){
                    @Override
                    public void onFailure(Method method, Throwable exception) {
                        new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                    }

                    @Override
                    public void onSuccess(Method method, ApiUserGroup response) {
                        userGroups.add(response);
                    }
                });
            }
        }).show();
    }

    @Override
    public void onEdit(ApiUserGroup group) {
        new UserGroupDialog(group, null, new UserGroupDialog.UserGroupHandler() {
            @Override
            public void onSave(final ApiUserGroup group) {
                service.updateGroup(group.getId(), group, new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        userGroups.update(group);
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                    }
                });
            }
        }).show();
    }

    @Override
    public void onRemove(final ApiUserGroup group) {
        service.removeGroup(group.getId(), new MethodCallback<Void>(){
            @Override
            public void onFailure(Method method, Throwable exception) {
                new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
            }

            @Override
            public void onSuccess(Method method, Void response) {
                userGroups.remove(group);
            }
            
        });
    }
    
    @Override
    public void onShowUsers(final ApiUserGroup group) {
        service.getGroupUsers(group.getId(), new MethodCallback<Set<Long>>() {
            @Override
            public void onFailure(Method method, Throwable exception) {
                new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
            }

            @Override
            public void onSuccess(Method method, Set<Long> response) {
                boolean isDefault = group.getId() == ApplicationContext.getInstance().getApplicationSettings().getDefaultGroupId();
                UserProperties up = GWT.create(UserProperties.class);
                new UserShareDialog(response, new UserShareHandler() {
                    @Override
                    public void onSaveShares(List<Long> uids, Window window) {
                        service.updateGroupUsers(group.getId(), uids, new RequestCallback(){
                            @Override
                            public void onResponseReceived(Request request, Response response) {
                                //do nothing
                            }

                            @Override
                            public void onError(Request request, Throwable exception) {
                                new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                            }

                        });
                    }
                }, !isDefault).show();
            }
        });
    }

    @Override
    public void onSave() {
        List<ApiUserGroup> changed;
        for(final Store<ApiUserGroup>.Record r : userGroups.getModifiedRecords()) {
            ApiUserGroup grp = new ApiUserGroup(r.getModel());
            for(Store.Change<ApiUserGroup, ?> c: r.getChanges())
                c.modify(grp);
            service.updateGroup(grp.getId(), grp, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    r.commit(false);
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                }
            });
        }
    }
}
