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
package org.traccar.web.server.model;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.persist.Transactional;
import org.traccar.web.client.model.DataService;
import org.traccar.web.client.model.GroupService;
import org.traccar.web.shared.model.AccessDeniedException;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Group;
import pl.datamatica.traccar.model.User;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.*;

@Singleton
public class GroupServiceImpl extends RemoteServiceServlet implements GroupService {
    @Inject
    private Provider<EntityManager> entityManager;

    @Inject
    private Provider<User> sessionUser;

    @Inject
    private DataService dataService;

    @Transactional
    @RequireUser
    @Override
    public Map<Group, Group> getGroups() {
        List<Group> groups = new ArrayList<>();

        if (sessionUser.get().getAdmin()) {
            groups = entityManager.get().createQuery("SELECT x FROM Group x", Group.class).getResultList();
            for(Group g : groups) {
                g.setShared(true);
                g.setOwned(true);
            }
        } else {
            if(sessionUser.get().getManager()) {
                groups = new ArrayList<>(sessionUser.get().getAllAvailableGroups());
                for(Group group : groups) {
                    Set<User> users = new HashSet<>(group.getUsers());
                    users.removeAll(dataService.getUsers());   
                    users.remove(sessionUser.get());
                    group.setOwned(users.isEmpty());
                    group.setShared(true);
                }
            }
            for (Device device : dataService.getDevices()) {
                if (device.getGroup() != null && !groups.contains(device.getGroup())) {
                    device.getGroup().setShared(false);
                    groups.add(device.getGroup());
                }
            }
        }
        
        for(int i=0;i<groups.size();++i) {
            Group parent = groups.get(i).getParent();
            if(parent != null && !groups.contains(parent)) {
                parent.setShared(false);
                groups.add(parent);
            }
        }

        Map<Group, Group> result = new HashMap<>();
        for (Group group : groups) {
            result.put(group, group.getParent());
        }
        return result;
    }

    @Transactional
    @RequireUser(roles = {Role.ADMIN, Role.MANAGER})
    @RequireWrite
    @Override
    public Group addGroup(Group parent, Group group) {
        Group toSave = new Group().copyFrom(group);

        toSave.setUsers(new HashSet<User>(1));
        toSave.getUsers().add(sessionUser.get());
        toSave.setParent(parent == null ? null : entityManager.get().find(Group.class, parent.getId()));
        entityManager.get().persist(toSave);

        return toSave;
    }

    @Transactional
    @RequireUser(roles = {Role.ADMIN, Role.MANAGER})
    @RequireWrite
    @Override
    public void updateGroups(Map<Group, List<Group>> groups) throws AccessDeniedException {
        User user = sessionUser.get();
        for (Map.Entry<Group, List<Group>> entry : groups.entrySet()) {
            Group parent = entry.getKey();
            for (Group group : entry.getValue()) {
                Group toSave = entityManager.get().find(Group.class, group.getId());
                if (!user.hasAccessTo(toSave)) {
                    throw new AccessDeniedException();
                }
                toSave.copyFrom(group);
                toSave.setParent(parent == null ? null : entityManager.get().find(Group.class, parent.getId()));
            }
        }
    }

    @Transactional
    @RequireUser(roles = {Role.ADMIN, Role.MANAGER})
    @RequireWrite
    @Override
    public void removeGroups(List<Group> groups) throws AccessDeniedException {
        User user = sessionUser.get();

        for (Group group : groups) {
            Group toRemove = entityManager.get().find(Group.class, group.getId());
            if (!user.hasAccessTo(toRemove)) {
                throw new AccessDeniedException();
            }

            if (user.getAdmin() || user.getManager()) {
                toRemove.getUsers().removeAll(dataService.getUsers());
            }
            toRemove.getUsers().remove(user);
            if (toRemove.getUsers().isEmpty()) {
                entityManager.get().createQuery("UPDATE Device d SET d.group=null WHERE d.group=:group").setParameter("group", toRemove).executeUpdate();
                entityManager.get().remove(toRemove);
            }
        }
    }

    @Transactional
    @RequireUser
    @Override
    public Map<User, Boolean> getGroupShare(Group group) {
        group = entityManager.get().find(Group.class, group.getId());
        List<User> users = dataService.getUsers();
        Map<User, Boolean> result = new HashMap<>(users.size());
        for (User user : users) {
            user = new User(user);
            user.setUserSettings(null);
            user.setPassword(null);
            result.put(user, group.getUsers().contains(user));
        }
        return result;
    }

    @Transactional
    @RequireUser(roles = { Role.ADMIN, Role.MANAGER })
    @RequireWrite
    @Override
    public void saveGroupShare(Group group, Map<User, Boolean> share) {
        group = entityManager.get().find(Group.class, group.getId());

        for (User user : dataService.getUsers()) {
            Boolean shared = share.get(user);
            if (shared == null) continue;
            user = entityManager.get().find(User.class, user.getId());
            if (shared) {
                group.getUsers().add(user);
            } else {
                group.getUsers().remove(user);
            }
        }
    }
}
