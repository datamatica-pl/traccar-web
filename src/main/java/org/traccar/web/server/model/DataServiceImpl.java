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
package org.traccar.web.server.model;

import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.RegistrationMaintenance;
import pl.datamatica.traccar.model.Sensor;
import pl.datamatica.traccar.model.Maintenance;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.Device;
import java.util.*;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.persist.Transactional;
import org.hibernate.Session;

import org.hibernate.proxy.HibernateProxy;
import org.slf4j.LoggerFactory;
import org.traccar.web.client.model.DataService;
import org.traccar.web.server.utils.JsonXmlParser;
import org.traccar.web.server.utils.StopsDetector;
import org.traccar.web.shared.model.*;
import pl.datamatica.traccar.model.DbRoute;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.Route;
import pl.datamatica.traccar.model.RoutePoint;
import pl.datamatica.traccar.model.UserDeviceStatus;
import pl.datamatica.traccar.model.UserPermission;
import pl.datamatica.traccar.model.UserSettings;

@Singleton
public class DataServiceImpl extends RemoteServiceServlet implements DataService {
    private static final long serialVersionUID = 1;

    @Inject
    private Provider<User> sessionUser;

    @Inject
    private Provider<ApplicationSettings> applicationSettings;

    @Inject
    private Provider<EntityManager> entityManager;

    @Inject
    private Provider<HttpServletRequest> request;

    @Inject
    private MovementDetector movementDetector;
    
    private org.slf4j.Logger logger = LoggerFactory.getLogger("database");

    @Override
    public void init() throws ServletException {
        super.init();

        /**
         * Perform database migrations
         */
        try {
            new DBMigrations().migrate(entityManager.get());
        } catch (Exception e) {
            throw new RuntimeException("Unable to perform DB migrations", e);
        }

        /**
         * Start movement detector
         */
        movementDetector.start();
    }

    EntityManager getSessionEntityManager() {
        EntityManager em = entityManager.get();
        em.unwrap(Session.class).enableFilter("softDelete");
        return em;
    }

    private void setSessionUser(User user) {
        HttpSession session = request.get().getSession();
        if (user != null) {
            session.setAttribute(CurrentUserProvider.ATTRIBUTE_USER_ID, user.getId());
        } else {
            session.removeAttribute(CurrentUserProvider.ATTRIBUTE_USER_ID);
        }
    }

    User getSessionUser() {
        return sessionUser.get();
    }

    @Transactional
    @Override
    public User authenticated() {
        return getSessionUser() == null ? null : fillUserSettings(new User(getSessionUser()));
    }

    @Transactional
    @LogCall("Login '{0}'")
    @Override
    public User login(String login, String password, boolean passwordHashed) throws TraccarException {
        EntityManager entityManager = getSessionEntityManager();
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT x FROM User x WHERE x.login = :login", User.class);
        query.setParameter("login", login);
        List<User> results = query.getResultList();

        if (results.isEmpty() || password.equals("")) throw new IllegalStateException();

        User user = results.get(0);

        String storedPassword = user.getPassword();
        // login by password 'hash'
        if (passwordHashed) {
            if (!storedPassword.equals(password)) {
                throw new IllegalStateException();
            }
        } else {
            if (!storedPassword.equals(user.getPasswordHashMethod().doHash(password, getApplicationSettings().getSalt()))) {
                // check for the old implementation without salt
                // if it matches then update password with new salt
                if (storedPassword.equals(user.getPasswordHashMethod().doHash(password, ""))) {
                    user.setPassword(user.getPasswordHashMethod().doHash(password, getApplicationSettings().getSalt()));
                } else {
                    System.out.print("WARNING: Login failed - wrong password");
                    throw new AccessDeniedException();
                }
            }
        }

        if (user.isBlocked()) {
            throw new UserBlockedException();
        }

        if (user.isExpired()) {
            throw new UserExpiredException(user.getExpirationDate());
        }

        /*
         * If hash method has changed in application settings and password parameter is not hashed, rehash user password
         */
        if (!user.getPasswordHashMethod().equals(getApplicationSettings().getDefaultHashImplementation()) && !passwordHashed) {
            user.setPasswordHashMethod(getApplicationSettings().getDefaultHashImplementation());
            user.setPassword(user.getPasswordHashMethod().doHash(password, getApplicationSettings().getSalt()));
        }

        setSessionUser(user);
        return fillUserSettings(new User(user));
    }

    @Transactional
    @Override
    public User login(String login, String password) throws TraccarException {
        return this.login(login, password, false);
    }

    @RequireUser
    @Override
    public boolean logout() {
        setSessionUser(null);
        return true;
    }
    
    private static final String ALARM_KEY = "alarm";

    private User fillUserSettings(User user) {
        if (user.getUserSettings() instanceof HibernateProxy) {
            user.setUserSettings(unproxy(user.getUserSettings()));
        }
        return user;
    }
    
    public ApplicationSettings getApplicationSettings() {
        return applicationSettings.get();
    }

    private <T> T unproxy(T entity) {
        if (entity instanceof HibernateProxy) {
            return (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
        }
        return entity;
    }
    
    @Override
    public List<Route> getRoutes() {
        EntityManager em = getSessionEntityManager();
        TypedQuery<DbRoute> tq = em.createQuery("from DbRoute r where owner = :user and archive = :false", 
                DbRoute.class).setParameter("user", getSessionUser())
                .setParameter("false", false);
        List<Route> copy = new ArrayList<>();
        for(DbRoute r : tq.getResultList())
            copy.add(r.toRoute());
        return copy;
    }
    
    @Override
    public List<Route> getArchivedRoutes() {
        EntityManager em = getSessionEntityManager();
        TypedQuery<DbRoute> tq = em.createQuery("from DbRoute r where owner = :user and archive = :true", 
                DbRoute.class).setParameter("user", getSessionUser())
                .setParameter("true", true);
        List<Route> copy = new ArrayList<>();
        for(DbRoute r : tq.getResultList())
            copy.add(r.toRoute());
        return copy;
    }
    
    @Transactional
    @RequireUser
    @Override
    public Route addRoute(Route route, boolean connect) throws TraccarException {
        EntityManager em = getSessionEntityManager();
        addRouteGeofences(route);
        route.setCreated(new Date());
        route.setOwner(getSessionUser());
        DbRoute dbr = new DbRoute(route);
        if(connect)
            em.persist(dbr);
        return dbr.toRoute();
    }
    
    @Transactional
    @RequireUser
    @Override
    public Route updateRoute(Route updated) throws TraccarException {
        EntityManager em = getSessionEntityManager();
        DbRoute existing = em.find(DbRoute.class, updated.getId());
        existing.update(updated);
        addRouteGeofences(existing);
        //attach routepoints
        em.merge(existing);
        return existing.toRoute();
    }
    
    private void addRouteGeofences(Route route) throws TraccarException {
        for(RoutePoint pt : route.getRoutePoints()) {
            GeoFence gf = pt.getGeofence();
            if(gf.getId() == 0)
                addGeoFence(gf);
        }
    }
    
    private GeoFence addGeoFence(GeoFence geoFence) throws TraccarException {
        User user = getSessionUser();
        if (geoFence.getName() == null || geoFence.getName().trim().isEmpty()) {
            throw new ValidationException();
        }

        geoFence.setUsers(new HashSet<User>());
        geoFence.getUsers().add(user);
        geoFence.setDevices(geoFence.getTransferDevices());
        getSessionEntityManager().persist(geoFence);
        logger.info("{} created geofence {} ({})", 
                user.getLogin(), geoFence.getName(), geoFence.getId());
        
        return geoFence;
    }

    @Transactional
    @RequireUser
    @Override
    public Route removeRoute(Route route) {
        EntityManager em = getSessionEntityManager();
        DbRoute dbRoute = em.find(DbRoute.class, route.getId());
        em.remove(dbRoute);
        return dbRoute.toRoute();
    }
    
    
    //REPORTS!
    @Transactional
    @RequireUser
    @Override
    public List<Device> getDevices() {
        boolean full = true;
        User user = getSessionUser();
        List<Device> devices;
        if (user.hasPermission(UserPermission.ALL_DEVICES)) {
            devices = getSessionEntityManager().createQuery("SELECT x FROM Device x LEFT JOIN FETCH x.latestPosition ORDER BY x.name", Device.class).getResultList();
        } else {
            devices = new ArrayList<>(user.getAllAvailableDevices());
            Collections.sort(devices, new Comparator<Device>() {
                @Override
                public int compare(Device o1, Device o2) {
                    String n1 = o1.getName() == null ? "" : o1.getName();
                    String n2 = o2.getName() == null ? "" : o2.getName();
                    return n1.compareTo(n2);
                }
            });
        }
        if(devices.isEmpty())
            return devices;
        TypedQuery<UserDeviceStatus> alarmQuery = getSessionEntityManager().createQuery(
                "FROM UserDeviceStatus x "
              + "WHERE x.id.user = :user AND x.id.device in (:devices)", UserDeviceStatus.class);
        alarmQuery.setParameter("user", user);
        alarmQuery.setParameter("devices", devices);
        Map<Device, UserDeviceStatus> statesMap = new HashMap<>();
        for(UserDeviceStatus x : alarmQuery.getResultList())
            statesMap.put(x.getDevice(), x);
        for(Device device : devices) {
            try {
                UserDeviceStatus deviceStatus = statesMap.get(device);                
                if(deviceStatus != null) {
                    device.setUnreadAlarms(deviceStatus.hasUnreadAlarms());
                    device.setLastAlarmsCheck(deviceStatus.getLastCheck());
                }
                
                if(device.getLatestPosition() == null)
                    continue;
                device.setProtocol(device.getLatestPosition().getProtocol());
                Map<String, Object> other = JsonXmlParser.parse(device.getLatestPosition().getOther());
                if(other.get(ALARM_KEY) != null)
                    device.setAlarmEnabled((boolean)other.get(ALARM_KEY));
            } catch (Exception | NoClassDefFoundError ex) {
//                Logger.getLogger(Device.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (full && !devices.isEmpty()) {
            List<Maintenance> maintenaces = getSessionEntityManager().createQuery("SELECT m FROM Maintenance m WHERE m.device IN :devices ORDER BY m.indexNo ASC", Maintenance.class)
                    .setParameter("devices", devices)
                    .getResultList();
            for (Maintenance maintenance : maintenaces) {
                Device device = maintenance.getDevice();
                if (device.getMaintenances() == null) {
                    device.setMaintenances(new ArrayList<Maintenance>());
                }
                device.getMaintenances().add(maintenance);
            }

            List<RegistrationMaintenance> registrations = getSessionEntityManager().createQuery("SELECT m FROM RegistrationMaintenance m WHERE m.device IN :devices ORDER BY m.indexNo ASC", RegistrationMaintenance.class)
                    .setParameter("devices", devices)
                    .getResultList();
            for (RegistrationMaintenance maintenance: registrations) {
                Device device = maintenance.getDevice();
                if(device.getRegistrations() == null) {
                    device.setRegistrations(new ArrayList<RegistrationMaintenance>());
                }
                device.getRegistrations().add(maintenance);
            }

            List<Sensor> sensors = getSessionEntityManager().createQuery("SELECT s FROM Sensor s WHERE s.device IN :devices ORDER BY s.id ASC", Sensor.class)
                    .setParameter("devices", devices)
                    .getResultList();
            for (Sensor sensor : sensors) {
                Device device = sensor.getDevice();
                if (device.getSensors() == null) {
                    device.setSensors(new ArrayList<Sensor>());
                }
                device.getSensors().add(sensor);
            }

            for (Device device : devices) {
                if (device.getMaintenances() == null) {
                    device.setMaintenances(Collections.<Maintenance>emptyList());
                }
                if (device.getSensors() == null) {
                    device.setSensors(Collections.<Sensor>emptyList());
                }
            }
        }
        return devices;
    }

    
    @Transactional
    @RequireUser
    @Override
    public List<Position> getPositions(Device device, Date from, Date to, boolean filter) throws AccessDeniedException {
        if (!getSessionUser().isArchive() || !getSessionUser().hasAccessTo(device)) {
            throw new AccessDeniedException();
        }

        // refresh device
        device = entityManager.get().find(Device.class, device.getId());

        EntityManager entityManager = getSessionEntityManager();
        UserSettings filters = getSessionUser().getUserSettings();

        List<Position> positions = new LinkedList<>();
        String queryString = "SELECT x FROM Position x WHERE x.device = :device AND x.time BETWEEN :from AND :to";

        if (filter) {
            if (filters.isHideZeroCoordinates()) {
                queryString += " AND (x.latitude != 0 OR x.longitude != 0)";
            }
            if (filters.isHideInvalidLocations()) {
                queryString += " AND x.valid = TRUE";
            }
            if (filters.getSpeedModifier() != null && filters.getSpeedForFilter() != null) {
                queryString += " AND x.speed " + filters.getSpeedModifier() + " :speed";
            }
        }

        queryString += " ORDER BY x.time";
        
        if (!getSessionUser().hasPermission(UserPermission.ALL_DEVICES)) {
            Date lastAvailableDate = device.getLastAvailablePositionDate(new Date());
            if (from.before(lastAvailableDate)) {
                from = lastAvailableDate;
            }
        }
        
        TypedQuery<Position> query = entityManager.createQuery(queryString, Position.class);
        query.setParameter("device", device);
        query.setParameter("from", from);
        query.setParameter("to", to);

        if (filter && filters.getSpeedModifier() != null && filters.getSpeedForFilter() != null) {
            query.setParameter("speed", filters.getSpeedUnit().toKnots(filters.getSpeedForFilter()));
        }

        List<Position> queryResult = query.getResultList();

        List<Position> lastNonIdlePositionsQueryResult =  entityManager
                .createQuery("SELECT p FROM Position p WHERE p.device = :device AND p.speed > :threshold ORDER BY time DESC", Position.class)
                .setParameter("device", device)
                .setParameter("threshold", device.getIdleSpeedThreshold())
                .setMaxResults(1)
                .getResultList();
        Position latestNonIdlePosition = lastNonIdlePositionsQueryResult.isEmpty()
                ? null
                : lastNonIdlePositionsQueryResult.get(0);
        final long MIN_IDLE_TIME = (long) device.getMinIdleTime() * 1000;

        StopsDetector stopsDetector = new StopsDetector();
        List<Position> deviceTrack = stopsDetector.detectStops(queryResult);
        if(deviceTrack.isEmpty())
            return new ArrayList<>(positions);
        Position positionA = deviceTrack.get(0);
        for (int i = 0; i < deviceTrack.size(); i++) {
            boolean add = true;
            Position position = deviceTrack.get(i);
            if (i > 0) {
                Position positionB = position;

                positionB.setDistance(GeoFenceCalculator.getDistance(positionA.getLongitude(), positionA.getLatitude(), positionB.getLongitude(), positionB.getLatitude()));

                if (filter && filters.isHideDuplicates()) {
                    add = !positionA.getTime().equals(positionB.getTime());
                }
                if (add && filter && filters.getMinDistance() != null) {
                    add = positionB.getDistance() >= filters.getMinDistance();
                }
            }
            
            // Always allow positon with valid status only
            if (add) {
                add = position.hasProperValidStatus();
            }
            
            // calculate Idle state
            if (position.getSpeed() != null) {
                if (position.getSpeed() > position.getDevice().getIdleSpeedThreshold()) {
                    position.setIdleStatus(Position.IdleStatus.MOVING);
                    latestNonIdlePosition = position;
                } else {
                    if (latestNonIdlePosition == null) {
                        position.setIdleStatus(Position.IdleStatus.PAUSED);
                        latestNonIdlePosition = position;
                    } else {
                        if (position.getTime().getTime() - latestNonIdlePosition.getTime().getTime() > MIN_IDLE_TIME) {
                            position.setIdleSince(latestNonIdlePosition.getTime());
                            position.setIdleStatus(Position.IdleStatus.IDLE);
                        } else {
                            position.setIdleStatus(Position.IdleStatus.PAUSED);
                        }
                    }
                }
            }
            if (add) {
                positions.add(deviceTrack.get(i));
                positionA = deviceTrack.get(i);
            }
        }

        return new ArrayList<>(positions);
    }

    @Transactional
    @RequireUser
    @Override
    public List<GeoFence> getGeoFences() {
        User user = getSessionUser();
        Set<GeoFence> geoFences;
        if (user.hasPermission(UserPermission.ALL_GEOFENCES)) {
            geoFences = new HashSet<>(getSessionEntityManager().createQuery("SELECT g FROM GeoFence g LEFT JOIN FETCH g.devices", GeoFence.class).getResultList());
        } else {
            geoFences = user.getAllAvailableGeoFences();
        }

        for (GeoFence geoFence : geoFences) {
            geoFence.setTransferDevices(new HashSet<>(geoFence.getDevices()));
        }

        return new ArrayList<>(geoFences);
    }
}
