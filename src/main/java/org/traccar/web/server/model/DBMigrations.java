/*
 * Copyright 2014 Vitaly Litvak (vitavaque@gmail.com)
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

import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UIStateEntry;
import pl.datamatica.traccar.model.UserSettings;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.DeviceEventType;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.DeviceEvent;
import pl.datamatica.traccar.model.DeviceIconType;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.DeviceIconMode;
import pl.datamatica.traccar.model.PasswordHashMethod;
import static pl.datamatica.traccar.model.Device.DEFAULT_MOVING_ARROW_COLOR;
import static pl.datamatica.traccar.model.Device.DEFAULT_PAUSED_ARROW_COLOR;
import static pl.datamatica.traccar.model.Device.DEFAULT_STOPPED_ARROW_COLOR;
import static pl.datamatica.traccar.model.Device.DEFAULT_OFFLINE_ARROW_COLOR;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.traccar.web.server.utils.JsonXmlParser;
import pl.datamatica.traccar.model.AppVersions;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.UserGroup;
import pl.datamatica.traccar.model.UserPermission;

public class DBMigrations {
    public void migrate(EntityManager em) throws Exception {
        for (Migration migration : new Migration[] {
                new CreateApplicationSettings(),
                new CreateAppVersions(),
                new SetUpdateInterval(),
                new SetTimePrintInterval(),
                new SetDefaultFilteringSettings(),
                new SetDefaultMapViewSettings(),
                new SetDefaultMapOverlays(),
                new SetManagerFlag(),
                new SetNotificationsFlag(),
                new SetReadOnlyFlag(),
                new SetBlockedFlag(),
                new SetArchiveFlag(),
                new AddDefaultNotifications(),
                new SetDefaultDeviceTimeout(),
                new SetDefaultDeviceOdometer(),
                new SetDefaultIdleSpeedThreshold(),
                new SetDefaultMinIdleTime(),
                new SetDefaultDisallowDeviceManagementByUsers(),
                new SetDefaultEventRecordingEnabled(),
                new SetDefaultLanguage(),
                new SetDefaultMapType(),
                new CreateUserGroups(),
                new CreateAdmin(),
                new UpdateUsersUserGroups(),
                new SetDefaultDeviceIconType(),
                new SetDefaultDeviceIconModeAndRotation(),
                new SetDefaultArrowIconSettings(),
                new SetDefaultDeviceShowNameProtocolAndOdometer(),
                new SetDefaultDeviceIconArrowRadius(),
                new SetDefaultHashImplementation(),
                new SetGlobalHashSalt(),
                new SetDefaultUserSettings(),
                new SetArchiveDefaultColumns(),
                new SetReportsFilterAndPreview(),
                new SetDefaultNotificationExpirationPeriod(),
                new SetDefaultExpiredFlagForEvents(),
                new SetDefaultMatchServiceURL(),
                new SetDefaultAllowCommandsOnlyForAdmins(),
                new SetDefaultUserGroups(),
                new SetFuelLevel()
        }) {
            em.getTransaction().begin();
            try {
                migration.migrate(em);
                em.getTransaction().commit();
            } catch (Exception ex) {
                em.getTransaction().rollback();
                throw ex;
            }
        }
    }

    interface Migration {
        void migrate(EntityManager em) throws Exception;
    }

    /**
     * Create user Groups
     * Set Default User Group
     * Update User Groups of all Users
     */
    static class CreateUserGroups implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            TypedQuery<UserGroup> query = em.createQuery("SELECT x FROM UserGroup x", UserGroup.class);
            List<UserGroup> results = query.getResultList();
            if (results.isEmpty()) {
                UserGroup readonly = new UserGroup();
                readonly.setName(UserGroup.READONLY_GROUP_NAME);
                readonly.setPermissions(UserPermission.getReadOnlyPermissions());
                em.persist(readonly);
                
                UserGroup users = new UserGroup();
                users.setName(UserGroup.USERS_GROUP_NAME);
                users.setPermissions(UserPermission.getUsersPermissions());
                em.persist(users);
                
                UserGroup admins = new UserGroup();
                admins.setName(UserGroup.ADMINS_GROUP_NAME);
                admins.setPermissions(UserPermission.getAdminsPermissions());
                em.persist(admins);
            }
        }
    }
    
    /**
     * Create Administrator account
     */
    static class CreateAdmin implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            TypedQuery<User> query = em.createQuery("SELECT x FROM User x", User.class);
            List<User> results = query.getResultList();
            if (results.isEmpty()) {
                User user = new User();
                user.setLogin("admin");
                user.setPassword("admin");
                user.setPasswordHashMethod(PasswordHashMethod.PLAIN);
                user.setAdmin(true);
                user.setManager(false);
                
                List<UserGroup> userGroups = em.createQuery("SELECT u FROM UserGroup u", UserGroup.class).getResultList();
                if (!userGroups.isEmpty()) {
                    UserGroup ug = userGroups.stream().filter(g -> UserGroup.ADMINS_GROUP_NAME.equals(g.getName())).collect(Collectors.toList()).get(0);
                    user.setUserGroup(ug);
                }
                
                em.persist(user);
            }
        }
    }

    /**
     * Create initial Application Settings
     */
    static class CreateApplicationSettings implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            TypedQuery<ApplicationSettings> query = em.createQuery("SELECT x FROM ApplicationSettings x", ApplicationSettings.class);
            List<ApplicationSettings> results = query.getResultList();
            if (results.isEmpty()) {
                ApplicationSettings as = new ApplicationSettings();
                // another migrations will prepare this object
                em.persist(as);
            }
        }
    }
    
    /**
     * Create appVersions if non exists
     */
    static class CreateAppVersions implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            TypedQuery<AppVersions> query = em.createQuery("SELECT x FROM AppVersions x", AppVersions.class);
            List<AppVersions> results = query.getResultList();
            if (results.isEmpty()) {
                AppVersions as = new AppVersions();
                // constructor creates default values
                em.persist(as);
            }
        }
    }
    
    /**
     * Set default user group in application settings
     */
    static class SetDefaultUserGroups implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            List<UserGroup> userGroups = em.createQuery("SELECT u FROM UserGroup u", UserGroup.class).getResultList();
            List<UserGroup> userGroup = userGroups.stream().filter(g -> UserGroup.USERS_GROUP_NAME.equals(g.getName())).collect(Collectors.toList());
                
            if (userGroup.isEmpty())
                return;     
            UserGroup defaultGroup = userGroup.get(0);
            
            em.createQuery("UPDATE " + ApplicationSettings.class.getSimpleName() + " S SET S.defaultGroup = :group WHERE S.defaultGroup IS NULL")
                    .setParameter("group", defaultGroup)
                    .executeUpdate();
        }
    }
    
    /**
     * Sets UserGroups for all users without it 
     */
    static class UpdateUsersUserGroups implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            List<UserGroup> userGroups = em.createQuery("SELECT u FROM UserGroup u", UserGroup.class).getResultList();
            List<UserGroup> adminsGroups = userGroups.stream().filter(g -> UserGroup.ADMINS_GROUP_NAME.equals(g.getName())).collect(Collectors.toList());
            List<UserGroup> usersGroups = userGroups.stream().filter(g -> UserGroup.USERS_GROUP_NAME.equals(g.getName())).collect(Collectors.toList());
            List<UserGroup> readonlyGroups = userGroups.stream().filter(g->UserGroup.READONLY_GROUP_NAME.equals(g.getName())).collect(Collectors.toList());
            
            if (adminsGroups.isEmpty() || usersGroups.isEmpty() || readonlyGroups.isEmpty())
                return;
            UserGroup admins = adminsGroups.get(0);
            UserGroup users = usersGroups.get(0);
            UserGroup readonly = readonlyGroups.get(0);
            
            em.createQuery("UPDATE " + User.class.getSimpleName() + " U SET U.userGroup = :group "
                    + "WHERE U.userGroup IS NULL AND (admin = :isAdmin OR admin IS NULL) "
                        + "AND (U.readOnly = :readonly OR U.readOnly IS NULL)")
                    .setParameter("group", users).setParameter("isAdmin", false).setParameter("readonly", false)
                    .executeUpdate();
            em.createQuery("UPDATE "+User.class.getSimpleName()+" U SET U.userGroup = :group "
                    + "WHERE U.userGroup IS NULL AND (admin = :isAdmin OR admin IS NULL) AND U.readOnly = :readonly")
                    .setParameter("group", readonly).setParameter("isAdmin", false).setParameter("readonly", true)
                    .executeUpdate();
            em.createQuery("UPDATE " + User.class.getSimpleName() + " U SET U.userGroup = :group WHERE U.userGroup IS NULL AND admin = :isAdmin")
                    .setParameter("group", admins).setParameter("isAdmin", true)
                    .executeUpdate();
        }
    }
            
    /**
     * Set up update interval in application settings
     */
    static class SetUpdateInterval implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getSimpleName() + " S SET S.updateInterval = :ui WHERE S.updateInterval IS NULL")
                    .setParameter("ui", ApplicationSettings.DEFAULT_UPDATE_INTERVAL)
                    .executeUpdate();
        }
    }

    /**
     * set up time print interval in user settings
     */
    static class SetTimePrintInterval implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + UserSettings.class.getSimpleName() + " S SET S.timePrintInterval = :tpi WHERE S.timePrintInterval IS NULL")
                    .setParameter("tpi", UserSettings.DEFAULT_TIME_PRINT_INTERVAL)
                    .executeUpdate();
        }
    }

    /**
     * set up default map view settings
     */
    static class SetDefaultMapViewSettings implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + UserSettings.class.getSimpleName() + " S SET S.zoomLevel = :zl, S.centerLongitude = :lon, S.centerLatitude = :lat WHERE S.zoomLevel IS NULL")
                    .setParameter("zl", UserSettings.DEFAULT_ZOOM_LEVEL)
                    .setParameter("lon", UserSettings.DEFAULT_CENTER_LONGITUDE)
                    .setParameter("lat", UserSettings.DEFAULT_CENTER_LATITUDE)
                    .executeUpdate();
            em.createQuery("UPDATE " + UserSettings.class.getSimpleName() + " S SET S.maximizeOverviewMap = :b WHERE S.maximizeOverviewMap IS NULL")
                    .setParameter("b", false)
                    .executeUpdate();
        }
    }

    static class SetDefaultMapOverlays implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + UserSettings.class.getSimpleName() + " S SET S.overlays = :overlays WHERE S.overlays IS NULL")
                    .setParameter("overlays", "GEO_FENCES,VECTOR,MARKERS")
                    .executeUpdate();
        }
    }

    /**
     * set up manager flag
     */
    static class SetManagerFlag implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + User.class.getSimpleName() + " U SET U.manager = :mgr WHERE U.manager IS NULL")
                    .setParameter("mgr", Boolean.FALSE)
                    .executeUpdate();
        }
    }

    /**
     * set up default timeout to 5 minutes
     */
    static class SetDefaultDeviceTimeout implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + Device.class.getSimpleName() + " D SET D.timeout = :tmout WHERE D.timeout IS NULL OR D.timeout <= 0")
                    .setParameter("tmout", Integer.valueOf(Device.DEFAULT_TIMEOUT))
                    .executeUpdate();
        }
    }

    /**
     * set up default idle speed threshold to 0
     */
    static class SetDefaultIdleSpeedThreshold implements Migration {

        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + Device.class.getSimpleName() + " D SET D.idleSpeedThreshold = :idleSpeedThreshold WHERE D.idleSpeedThreshold IS NULL")
                    .setParameter("idleSpeedThreshold", 0d)
                    .executeUpdate();
        }
    }

    static class SetDefaultMinIdleTime implements Migration {

        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + Device.class.getSimpleName() + " D SET D.minIdleTime = :minIdleTime WHERE D.minIdleTime IS NULL")
                    .setParameter("minIdleTime", Integer.valueOf(Device.DEFAULT_MIN_IDLE_TIME))
                    .executeUpdate();
        }
    }

    static class SetDefaultDisallowDeviceManagementByUsers implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getName() + " S SET S.disallowDeviceManagementByUsers = :ddmbu WHERE S.disallowDeviceManagementByUsers IS NULL")
                    .setParameter("ddmbu", Boolean.FALSE)
                    .executeUpdate();
        }
    }

    static class SetDefaultMapType implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + UserSettings.class.getName() + " S SET S.mapType = :mt WHERE S.mapType IS NULL")
                    .setParameter("mt", UserSettings.MapType.OSM)
                    .executeUpdate();
        }
    }

    static class SetDefaultDeviceIconType implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + Device.class.getName() + " D SET D.iconType = :iconType WHERE D.icon IS NULL AND D.iconType IS NULL")
                    .setParameter("iconType", DeviceIconType.DEFAULT)
                    .executeUpdate();
        }
    }

    /**
     * Set up default hashing in application settings
     */
    static class SetDefaultHashImplementation implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getSimpleName() + " S SET S.defaultPasswordHash = :dh WHERE S.defaultPasswordHash IS NULL")
                    .setParameter("dh", PasswordHashMethod.MD5)
                    .executeUpdate();
        }
    }

    static class SetGlobalHashSalt implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getSimpleName() + " S SET S.salt = :s WHERE S.salt IS NULL")
                    .setParameter("s", PasswordUtils.generateRandomString())
                    .executeUpdate();
        }
    }

    static class SetDefaultUserSettings implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            for (User user : em.createQuery("SELECT u FROM " + User.class.getName() + " u WHERE u.userSettings IS NULL", User.class).getResultList()) {
                user.setUserSettings(new UserSettings());
                em.persist(user);
            }
        }
    }

    static class SetDefaultFilteringSettings implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + UserSettings.class.getName() + " S SET S.hideZeroCoordinates = :false, S.hideInvalidLocations = :false, S.hideDuplicates = :false WHERE S.hideZeroCoordinates IS NULL")
                    .setParameter("false", false)
                    .executeUpdate();
        }
    }

    static class SetArchiveDefaultColumns implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            for (User user : em.createQuery("SELECT u FROM User u WHERE u NOT IN (SELECT user FROM UIStateEntry WHERE name=:archiveGridStateId)", User.class)
                             .setParameter("archiveGridStateId", UIStateEntry.ARCHIVE_GRID_STATE_ID)
                             .getResultList()) {
                em.persist(UIStateEntry.createDefaultArchiveGridStateEntry(user));
            }
        }
    }

    static class SetNotificationsFlag implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + User.class.getSimpleName() + " U SET U.notifications = :n WHERE U.notifications IS NULL")
                    .setParameter("n", Boolean.FALSE)
                    .executeUpdate();
        }
    }

    static class AddDefaultNotifications implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            for (User user : em.createQuery("SELECT u FROM User u WHERE u.notifications=:b", User.class).setParameter("b", Boolean.TRUE).getResultList()) {
                user.getNotificationEvents().add(DeviceEventType.OFFLINE);
                user.getNotificationEvents().add(DeviceEventType.GEO_FENCE_ENTER);
                user.getNotificationEvents().add(DeviceEventType.GEO_FENCE_EXIT);
                // reset flag to prevent further migrations
                user.setNotifications(false);
            }
        }
    }

    static class SetDefaultEventRecordingEnabled implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getName() + " S SET S.eventRecordingEnabled = :b WHERE S.eventRecordingEnabled IS NULL")
                    .setParameter("b", Boolean.TRUE)
                    .executeUpdate();
        }
    }

    static class SetReadOnlyFlag implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + User.class.getSimpleName() + " U SET U.readOnly = :ro WHERE U.readOnly IS NULL")
                    .setParameter("ro", Boolean.FALSE)
                    .executeUpdate();
        }
    }

    static class SetBlockedFlag implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + User.class.getSimpleName() + " U SET U.blocked = :b WHERE U.blocked IS NULL")
                    .setParameter("b", Boolean.FALSE)
                    .executeUpdate();
        }
    }

    static class SetArchiveFlag implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + User.class.getSimpleName() + " U SET U.archive = :b WHERE U.archive IS NULL")
                    .setParameter("b", Boolean.TRUE)
                    .executeUpdate();
        }
    }

    static class SetDefaultLanguage implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getName() + " S SET S.language = :b WHERE S.language IS NULL")
                    .setParameter("b", "default")
                    .executeUpdate();
        }
    }

    static class SetDefaultDeviceOdometer implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + Device.class.getSimpleName() + " D SET D.odometer = :o WHERE D.odometer IS NULL OR D.odometer <= 0")
                    .setParameter("o", 0d)
                    .executeUpdate();

            em.createQuery("UPDATE " + Device.class.getSimpleName() + " D SET D.autoUpdateOdometer = :b WHERE D.autoUpdateOdometer IS NULL")
                    .setParameter("b", Boolean.FALSE)
                    .executeUpdate();
        }
    }

    static class SetReportsFilterAndPreview implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + Report.class.getSimpleName() + " R SET R.preview = :f WHERE R.preview IS NULL")
                    .setParameter("f", false)
                    .executeUpdate();
            em.createQuery("UPDATE " + Report.class.getSimpleName() + " R SET R.disableFilter = :f WHERE R.disableFilter IS NULL")
                    .setParameter("f", false)
                    .executeUpdate();
        }
    }

    static class SetDefaultNotificationExpirationPeriod implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getName() + " S SET S.notificationExpirationPeriod = :period WHERE S.notificationExpirationPeriod IS NULL")
                    .setParameter("period", Integer.valueOf(ApplicationSettings.DEFAULT_NOTIFICATION_EXPIRATION_PERIOD))
                    .executeUpdate();
        }
    }

    static class SetDefaultExpiredFlagForEvents implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + DeviceEvent.class.getName() + " E SET E.expired=:false WHERE E.expired IS NULL")
                    .setParameter("false", false)
                    .executeUpdate();
        }
    }

    static class SetDefaultDeviceIconModeAndRotation implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + Device.class.getName() + " D SET D.iconRotation=:false WHERE D.iconRotation IS NULL")
                    .setParameter("false", false)
                    .executeUpdate();
            em.createQuery("UPDATE " + Device.class.getName() + " D SET D.iconMode=:icon WHERE D.iconMode IS NULL")
                    .setParameter("icon", DeviceIconMode.ICON)
                    .executeUpdate();
        }
    }

    static class SetDefaultArrowIconSettings implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            Map<String, String> defaultColors = new HashMap<>();
            defaultColors.put("iconArrowMovingColor", DEFAULT_MOVING_ARROW_COLOR);
            defaultColors.put("iconArrowPausedColor", DEFAULT_PAUSED_ARROW_COLOR);
            defaultColors.put("iconArrowStoppedColor", DEFAULT_STOPPED_ARROW_COLOR);
            defaultColors.put("iconArrowOfflineColor", DEFAULT_OFFLINE_ARROW_COLOR);

            for (Map.Entry<String, String> e : defaultColors.entrySet()) {
                em.createQuery("UPDATE " + Device.class.getName() + " D SET D." + e.getKey() + "=:color WHERE D." + e.getKey() + " IS NULL")
                        .setParameter("color", e.getValue())
                        .executeUpdate();
            }
        }
    }

    static class SetDefaultDeviceShowNameProtocolAndOdometer implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            for (String prop : new String[] { "showName", "showProtocol", "showOdometer" }) {
                em.createQuery("UPDATE " + Device.class.getName() + " D SET D." + prop + "=:true WHERE D." + prop + " IS NULL")
                        .setParameter("true", true)
                        .executeUpdate();
            }
        }
    }

    static class SetDefaultMatchServiceURL implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getName() + " S SET S.matchServiceURL = :url WHERE S.matchServiceURL IS NULL")
                    .setParameter("url", ApplicationSettings.DEFAULT_MATCH_SERVICE_URL)
                    .executeUpdate();
        }
    }

    static class SetDefaultDeviceIconArrowRadius implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + Device.class.getName() + " D SET D.iconArrowRadius = :radius WHERE D.iconArrowRadius IS NULL")
                    .setParameter("radius", Device.DEFAULT_ARROW_RADIUS)
                    .executeUpdate();
        }
    }

    static class SetDefaultAllowCommandsOnlyForAdmins implements Migration {
        @Override
        public void migrate(EntityManager em) throws Exception {
            em.createQuery("UPDATE " + ApplicationSettings.class.getName() + " S SET S.allowCommandsOnlyForAdmins = :false WHERE S.allowCommandsOnlyForAdmins IS NULL")
                    .setParameter("false", false)
                    .executeUpdate();
        }
    }
    
    static class SetFuelLevel implements Migration {
        private static final String FUEL_LEVEL_KEY="io84";
        private static final String FUEL_USED_KEY = "io83";
        
        @Override
        public void migrate(EntityManager em) throws Exception {
            List<Position> positions = em.createQuery("FROM Position p JOIN FETCH p.device ORDER BY p.time", 
                    Position.class).getResultList();
            for(Position p : positions) {
                Map<String, Object> other = JsonXmlParser.parse(p.getOther());
                Long fuel = (Long)other.get(FUEL_LEVEL_KEY);
                if(fuel != null) {
                    p.setFuelLevel(fuel.doubleValue());
                    p.getDevice().setFuelLevel(fuel);
                }
                Long fuelUsed = (Long)other.get(FUEL_USED_KEY);
                if(fuelUsed != null) {
                    double val = fuelUsed.doubleValue()/10;
                    p.setFuelUsed(val);
                    p.getDevice().setFuelUsed(val);
                }
                em.persist(p);
                em.persist(p.getDevice());
            }
        }
    }
}
