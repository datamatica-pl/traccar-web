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

import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.ApplicationSettings;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import static pl.datamatica.traccar.model.PasswordHashMethod.*;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import com.google.inject.persist.jpa.JpaPersistModule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.traccar.web.client.model.DataService;
import org.traccar.web.client.model.EventService;
import org.traccar.web.client.model.NotificationService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.Callable;

public class DataServiceTest {
    static Long currentUserId;

    public static class TestUserProvider implements Provider<User> {
        @Inject
        Provider<EntityManager> entityManager;

        @Transactional
        @Override
        public User get() {
            if (currentUserId == null) {
                return entityManager.get().createQuery("SELECT u FROM User u", User.class).getResultList().get(0);
            } else {
                return entityManager.get().find(User.class, currentUserId);
            }
        }
    }

    public static class TestPersistenceModule extends AbstractModule {
        @Override
        protected void configure() {
            install(new JpaPersistModule("test"));

            bind(DataService.class).to(DataServiceImpl.class);
            bind(NotificationService.class).to(NotificationServiceImpl.class);
            bind(EventService.class).to(EventServiceImpl.class);
            bind(HttpServletRequest.class).toProvider(new com.google.inject.Provider<HttpServletRequest>() {
                @Override
                public HttpServletRequest get() {
                    return mock(HttpServletRequest.class, RETURNS_DEEP_STUBS);
                }
            });
            bind(User.class).toProvider(TestUserProvider.class);
            bind(ApplicationSettings.class).toProvider(ApplicationSettingsProvider.class);
        }
    }

    static Injector injector = Guice.createInjector(new TestPersistenceModule());
    static DataService dataService;

    @BeforeClass
    public static void init() throws Exception {
        injector.getInstance(PersistService.class).start();
        dataService = injector.getInstance(DataService.class);
        
        runInTransaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                injector.getInstance(DBMigrations.CreateUserGroups.class).migrate(injector.getInstance(EntityManager.class));
                injector.getInstance(DBMigrations.CreateAdmin.class).migrate(injector.getInstance(EntityManager.class));
                return null;
            }
        });
    }
    
    @After
    public void cleanup() {
        currentUserId = null;
    }

    @AfterClass
    public static void destroy() {
        injector = null;
        dataService = null;
    }

    //@Test - runInTransaction not working
    public void testLoginPasswordHashAndSalt() throws Exception {
        String salt = dataService.getApplicationSettings().getSalt();
        // ordinary log in
        User admin = dataService.login("admin", "admin");
        assertEquals(MD5.doHash("admin", salt), admin.getPassword());
        // log in with hash
        admin = dataService.login("admin", MD5.doHash("admin", salt), true);
        assertEquals(MD5.doHash("admin", salt), admin.getPassword());
        // update user
        runInTransaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                injector.getInstance(EntityManager.class).createQuery("UPDATE User u SET u.password=:pwd")
                        .setParameter("pwd", MD5.doHash("admin", null))
                        .executeUpdate();
                return null;
            }
        });
        // log in with hash will not be possible anymore
        try {
            admin = dataService.login("admin", MD5.doHash("admin", salt), true);
            fail("Should be impossible to log in with different hash");
        } catch (IllegalStateException expected) {
            // do nothing since exception is expected in this case
        }
        // check logging in with old hash (for backwards compatibility)
        admin = dataService.login("admin", MD5.doHash("admin", null), true);
        assertEquals(MD5.doHash("admin", null), admin.getPassword());
        // log in and check if password is updated
        admin = dataService.login("admin", "admin");
        assertEquals(MD5.doHash("admin", salt), admin.getPassword());
    }

    private static <V> V runInTransaction(Callable<V> c) throws Exception {
        UnitOfWork unitOfWork = injector.getInstance(UnitOfWork.class);
        unitOfWork.begin();
        EntityManager entityManager = injector.getInstance(EntityManager.class);
        entityManager.getTransaction().begin();
        try {
            V result = c.call();
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception ex) {
            entityManager.getTransaction().rollback();
            throw ex;
        } finally {
            unitOfWork.end();
        }
    }
}
