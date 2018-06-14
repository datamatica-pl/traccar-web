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
import org.traccar.web.shared.model.*;

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
}
