/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
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
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.view.DeviceGroupsDialog;
import org.traccar.web.client.view.NavView;
import org.traccar.web.client.view.UserShareDialog;
import pl.datamatica.traccar.model.Group;
import pl.datamatica.traccar.model.User;

import java.util.*;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.model.GroupStore;
import org.traccar.web.client.model.api.GroupService;
import org.traccar.web.client.model.api.IGroupService.AddDeviceGroupDto;
import org.traccar.web.client.model.api.IGroupService.DeviceGroupDto;

public class GroupsController implements NavView.GroupsHandler, ContentController {
    private static final DeviceGroupsDialog.GroupsHandler EMPTY_GROUPS_HANDLER = new DeviceGroupsDialog.GroupsHandler() {
        @Override
        public void onAdd(Group parent, Group group, GroupAddHandler groupsHandler) {
        }

        @Override
        public void onSave(ChangesSaveHandler groupsHandler) {
        }

        @Override
        public void onRemove(Group group) {
        }

        @Override
        public void onCancelSaving(List<Group> newGroups) {
        }

        @Override
        public void onShare(Group group) {
        }
    };
    
    private final Messages i18n = GWT.create(Messages.class);
    private final GroupStore groupStore;
    private final GroupRemoveHandler removeHandler;

    public interface GroupAddHandler {
        void groupAdded(Group group);
    }

    public interface GroupRemoveHandler {
        void groupRemoved(Group group);
    }

    public interface ChangesSaveHandler {
        void changesSaved();
    }

    public GroupsController(GroupStore groupStore, GroupRemoveHandler removeHandler) {
        this.removeHandler = removeHandler;
        this.groupStore = groupStore;
    }

    @Override
    public ContentPanel getView() {
        return null;
    }

    @Override
    public void run() {
        //moved to InitialLoader
    }

    @Override
    public void onShowGroups() {
        final GroupService service = new GroupService();
        final Map<Group, List<Group>> originalParents = getParents();
        
        DeviceGroupsDialog.GroupsHandler handler = EMPTY_GROUPS_HANDLER;
        if(ApplicationContext.getInstance().getUser().isAdminOrManager())
            handler = new DeviceGroupsDialog.GroupsHandler() {
            @Override
            public void onAdd(Group parent, Group group, final GroupAddHandler groupsHandler) {
                service.addGroup(new AddDeviceGroupDto(group), new MethodCallback<DeviceGroupDto>() {
                    @Override
                    public void onFailure(Method method, Throwable exception) {
                        new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                    }

                    @Override
                    public void onSuccess(Method method, DeviceGroupDto response) {
                        groupsHandler.groupAdded(response.toGroup());
                    }
                    
                });
            }

            @Override
            public void onSave(final ChangesSaveHandler groupsHandler) {
                final Set<Group> setToSave = new HashSet<>();
                for (Store<Group>.Record record : groupStore.getModifiedRecords()) {
                    Group originalGroup = record.getModel();
                    Group group = new Group(originalGroup.getId()).copyFrom(originalGroup);
                    for (Store.Change<Group, ?> change : record.getChanges()) {
                        change.modify(group);
                    }
                    setToSave.add(group);
                }

                for(Group g : setToSave)
                    service.updateGroup(g.getId(), new AddDeviceGroupDto(g),
                            new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        syncOriginalParents();
                        groupsHandler.changesSaved();
                        groupStore.commitChanges();
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                    }
                                
                            });
            }

            @Override
            public void onRemove(final Group group) {
                List<Group> toRemove = new ArrayList<>();
                toRemove.add(group);
                toRemove.addAll(groupStore.getAllChildren(group));
                service.removeGroup(group.getId(), new RequestCallback(){
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        groupStore.remove(group);
                        syncOriginalParents();
                        removeHandler.groupRemoved(group);
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                    }
                });
            }

            @Override
            public void onCancelSaving(final List<Group> newGroups) {
                // Move updated nodes to the original parents
                for (Map.Entry<Group, List<Group>> entry : originalParents.entrySet()) {
                    Group originalParent = entry.getKey();
                    List<Group> subGroups = entry.getValue();
                    for (Group group : subGroups) {
                        if (!Objects.equals(groupStore.getParent(group), originalParent)) {
                            TreeStore.TreeNode<Group> subTree = groupStore.getSubTree(group);
                            groupStore.remove(group);
                            if (originalParent == null) {
                                groupStore.addSubTree(subGroups.indexOf(group), Collections.singletonList(subTree));
                            } else {
                                groupStore.addSubTree(originalParent, subGroups.indexOf(group), Collections.singletonList(subTree));
                            }
                        }
                    }
                }
                for(final Group g : newGroups)
                    service.removeGroup(g.getId(), new RequestCallback(){
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        groupStore.remove(g);
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                    }
                    });
                groupStore.rejectChanges();
            }

            @Override
            public void onShare(final Group group) {
                service.getGroupShare(group.getId(), new MethodCallback<Set<Long>>() {
                    @Override
                    public void onFailure(Method method, Throwable exception) {
                        new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                    }

                    @Override
                    public void onSuccess(Method method, Set<Long> response) {
                        new UserShareDialog(response, new UserShareDialog.UserShareHandler() {
                            @Override
                            public void onSaveShares(final List<Long> uids, final Window window) {
                                service.updateGroupShare(group.getId(), uids, 
                                        new RequestCallback() {

                                    @Override
                                    public void onResponseReceived(Request request, Response response) {
                                        User u = ApplicationContext.getInstance().getUser();
                                        if(!u.getAdmin() && !uids.contains(u.getId()))
                                            groupStore.remove(group);
                                        window.hide();
                                    }

                                    @Override
                                    public void onError(Request request, Throwable exception) {
                                        new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                                    }
                                        });
                            }
                        }).show();
                    }
                });
            }

            private void syncOriginalParents() {
                originalParents.clear();
                originalParents.putAll(getParents());
            }
        };

        new DeviceGroupsDialog(groupStore, handler).show();
    }

    private Map<Group, List<Group>> getParents() {
        Map<Group, List<Group>> result = new HashMap<>();

        for (Group group : groupStore.getAll()) {
            Group parent = groupStore.getParent(group);
            List<Group> subGroups = result.get(parent);
            if (subGroups == null) {
                subGroups = new ArrayList<>();
                result.put(parent, subGroups);
            }
            subGroups.add(group);
        }

        return result;
    }
}
