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
package org.traccar.web.shared.model;

import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Group;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import pl.datamatica.traccar.model.UserGroup;
import pl.datamatica.traccar.model.UserPermission;

public class UserTest {

    /**
     * Users hierarchy:
     *
     * <pre>
     *                top manager [m1] --- (g1, g7)
     *                /          \__________
     *      (g2) --- manager [m2]           \
     *              /      \                manager [m3] --- (g5)
     * (g3) --- user [u1]  user [u2]  --- (g4)    \
     *                                            user [u3] --- (g6)
     * </pre>
     */
    //@Test
    public void testAvailableGeoFences() {
        // set up users hierarchy
        UserGroup managerGroup = new UserGroup();
        managerGroup.setPermissions(UserPermission.getUsersPermissions());
        
        User m1 = new User("m1");
        User m2 = new User("m2");
        m2.setManagedBy(m1);
        User m3 = new User("m3");
        m3.setManagedBy(m1);

        m1.setManagedUsers(set(m2, m3));
        
        User u1 = new User("u1");
        User u2 = new User("u2");
        m2.setManagedUsers(set(u1, u2));
        u1.setManagedBy(m2);
        u2.setManagedBy(m2);

        User u3 = new User("u3");
        m3.setManagedUsers(set(u3));
        u3.setManagedBy(m3);

        //set UserGroups
        m1.setUserGroup(managerGroup);
        m2.setUserGroup(managerGroup);
        m3.setUserGroup(managerGroup);
        u1.setUserGroup(managerGroup);
        u2.setUserGroup(managerGroup);
        u3.setUserGroup(managerGroup);
        
        // set up geo-fences
        GeoFence g1 = new GeoFence(1, "g1");
        GeoFence g2 = new GeoFence(2, "g2");
        GeoFence g3 = new GeoFence(3, "g3");
        GeoFence g4 = new GeoFence(4, "g4");
        GeoFence g5 = new GeoFence(5, "g5");
        GeoFence g6 = new GeoFence(6, "g6");
        GeoFence g7 = new GeoFence(7, "g7");

        m1.setGeoFences(new HashSet<>(Arrays.asList(g1, g7)));
        m2.setGeoFences(new HashSet<>(Arrays.asList(g1, g2)));
        m3.setGeoFences(new HashSet<>(Collections.singleton(g5)));
        u1.setGeoFences(new HashSet<>(Collections.singleton(g3)));
        u2.setGeoFences(new HashSet<>(Collections.singleton(g4)));
        u3.setGeoFences(new HashSet<>(Collections.singleton(g6)));

        // test
        assertEquals(set(g1, g2, g3, g4, g5, g6, g7), m1.getAllAvailableGeoFences());
        assertTrue(m1.hasAccessTo(g1));
        assertTrue(m1.hasAccessTo(g2));
        assertTrue(m1.hasAccessTo(g3));
        assertTrue(m1.hasAccessTo(g4));
        assertTrue(m1.hasAccessTo(g5));
        assertTrue(m1.hasAccessTo(g6));
        assertTrue(m1.hasAccessTo(g7));

        assertEquals(set(g1, g2, g3, g4), m2.getAllAvailableGeoFences());
        assertTrue(m2.hasAccessTo(g1));
        assertTrue(m2.hasAccessTo(g2));
        assertTrue(m2.hasAccessTo(g3));
        assertTrue(m2.hasAccessTo(g4));
        assertFalse(m2.hasAccessTo(g5));
        assertFalse(m2.hasAccessTo(g6));
        assertFalse(m2.hasAccessTo(g7));

        assertEquals(set(g5, g6), m3.getAllAvailableGeoFences());
        assertFalse(m3.hasAccessTo(g1));
        assertFalse(m3.hasAccessTo(g2));
        assertFalse(m3.hasAccessTo(g3));
        assertFalse(m3.hasAccessTo(g4));
        assertTrue(m3.hasAccessTo(g5));
        assertTrue(m3.hasAccessTo(g6));
        assertFalse(m3.hasAccessTo(g7));

        assertEquals(set(g3), u1.getAllAvailableGeoFences());
        assertFalse(u1.hasAccessTo(g1));
        assertFalse(u1.hasAccessTo(g2));
        assertTrue(u1.hasAccessTo(g3));
        assertFalse(u1.hasAccessTo(g4));
        assertFalse(u1.hasAccessTo(g5));
        assertFalse(u1.hasAccessTo(g6));
        assertFalse(u1.hasAccessTo(g7));

        assertEquals(set(g4), u2.getAllAvailableGeoFences());
        assertFalse(u2.hasAccessTo(g1));
        assertFalse(u2.hasAccessTo(g2));
        assertFalse(u2.hasAccessTo(g3));
        assertTrue(u2.hasAccessTo(g4));
        assertFalse(u2.hasAccessTo(g5));
        assertFalse(u2.hasAccessTo(g6));
        assertFalse(u2.hasAccessTo(g7));

        assertEquals(set(g6), u3.getAllAvailableGeoFences());
        assertFalse(u3.hasAccessTo(g1));
        assertFalse(u3.hasAccessTo(g2));
        assertFalse(u3.hasAccessTo(g3));
        assertFalse(u3.hasAccessTo(g4));
        assertFalse(u3.hasAccessTo(g5));
        assertTrue(u3.hasAccessTo(g6));
        assertFalse(u3.hasAccessTo(g7));
    }

    //@Test
    public void testAvailableGroups() {
        UserGroup managerGroup = new UserGroup();
        managerGroup.setPermissions(UserPermission.getUsersPermissions());
        
        // set up users hierarchy
        User m1 = new User("m1");
        User m2 = new User("m2");
        m2.setManagedBy(m1);
        User m3 = new User("m3");
        m3.setManagedBy(m1);

        m1.setManagedUsers(set(m2, m3));

        User u1 = new User("u1");
        User u2 = new User("u2");
        m2.setManagedUsers(set(u1, u2));
        u1.setManagedBy(m2);
        u2.setManagedBy(m2);

        User u3 = new User("u3");
        m3.setManagedUsers(set(u3));
        u3.setManagedBy(m3);

        //set UserGroups
        m1.setUserGroup(managerGroup);
        m2.setUserGroup(managerGroup);
        m3.setUserGroup(managerGroup);
        u1.setUserGroup(managerGroup);
        u2.setUserGroup(managerGroup);
        u3.setUserGroup(managerGroup);

        // set up geo-fences
        Group g1 = new Group(1, "g1");
        Group g2 = new Group(2, "g2");
        Group g3 = new Group(3, "g3");
        Group g4 = new Group(4, "g4");
        Group g5 = new Group(5, "g5");
        Group g6 = new Group(6, "g6");
        Group g7 = new Group(7, "g7");

        m1.setGroups(set(g1, g7));
        m2.setGroups(set(g1, g2));
        m3.setGroups(set(g5));
        u1.setGroups(set(g3));
        u2.setGroups(set(g4));
        u3.setGroups(set(g6));

        // test
        assertEquals(set(g1, g2, g3, g4, g5, g6, g7), m1.getAllAvailableGroups());
        assertTrue(m1.hasAccessTo(g1));
        assertTrue(m1.hasAccessTo(g2));
        assertTrue(m1.hasAccessTo(g3));
        assertTrue(m1.hasAccessTo(g4));
        assertTrue(m1.hasAccessTo(g5));
        assertTrue(m1.hasAccessTo(g6));
        assertTrue(m1.hasAccessTo(g7));

        assertEquals(set(g1, g2, g3, g4), m2.getAllAvailableGroups());
        assertTrue(m2.hasAccessTo(g1));
        assertTrue(m2.hasAccessTo(g2));
        assertTrue(m2.hasAccessTo(g3));
        assertTrue(m2.hasAccessTo(g4));
        assertFalse(m2.hasAccessTo(g5));
        assertFalse(m2.hasAccessTo(g6));
        assertFalse(m2.hasAccessTo(g7));

        assertEquals(set(g5, g6), m3.getAllAvailableGroups());
        assertFalse(m3.hasAccessTo(g1));
        assertFalse(m3.hasAccessTo(g2));
        assertFalse(m3.hasAccessTo(g3));
        assertFalse(m3.hasAccessTo(g4));
        assertTrue(m3.hasAccessTo(g5));
        assertTrue(m3.hasAccessTo(g6));
        assertFalse(m3.hasAccessTo(g7));

        assertEquals(set(g3), u1.getAllAvailableGroups());
        assertFalse(u1.hasAccessTo(g1));
        assertFalse(u1.hasAccessTo(g2));
        assertTrue(u1.hasAccessTo(g3));
        assertFalse(u1.hasAccessTo(g4));
        assertFalse(u1.hasAccessTo(g5));
        assertFalse(u1.hasAccessTo(g6));
        assertFalse(u1.hasAccessTo(g7));

        assertEquals(set(g4), u2.getAllAvailableGroups());
        assertFalse(u2.hasAccessTo(g1));
        assertFalse(u2.hasAccessTo(g2));
        assertFalse(u2.hasAccessTo(g3));
        assertTrue(u2.hasAccessTo(g4));
        assertFalse(u2.hasAccessTo(g5));
        assertFalse(u2.hasAccessTo(g6));
        assertFalse(u2.hasAccessTo(g7));

        assertEquals(set(g6), u3.getAllAvailableGroups());
        assertFalse(u3.hasAccessTo(g1));
        assertFalse(u3.hasAccessTo(g2));
        assertFalse(u3.hasAccessTo(g3));
        assertFalse(u3.hasAccessTo(g4));
        assertFalse(u3.hasAccessTo(g5));
        assertTrue(u3.hasAccessTo(g6));
        assertFalse(u3.hasAccessTo(g7));
    }

    private <T> Set<T> set(T... elements) {
        return new HashSet<T>(Arrays.asList(elements));
    }
}
