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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.User;

import javax.inject.Inject;
import javax.inject.Provider;
import pl.datamatica.traccar.model.UserPermission;

public class UserCheck implements MethodInterceptor {
    @Inject
    private Provider<User> sessionUser;
    @Inject
    private Provider<ApplicationSettings> applicationSettings;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        RequireUser requireUser = methodInvocation.getMethod().getAnnotation(RequireUser.class);
        if (requireUser != null) checkRequireUser(requireUser);
        ManagesDevices managesDevices = methodInvocation.getMethod().getAnnotation(ManagesDevices.class);
        if (managesDevices != null) checkDeviceManagementAccess(managesDevices);
        RequireWrite requireWrite = methodInvocation.getMethod().getAnnotation(RequireWrite.class);
        if (requireWrite != null) checkRequireWrite(requireWrite);
        try {
            return methodInvocation.proceed();
        } finally {
            cleanUp();
        }
    }
    
    void checkRequireUser(RequireUser requireUser) {
        User user = sessionUser.get();
        if (user == null) {
            throw new SecurityException("Not logged in");
        }
        if (user.isBlocked()) {
            throw new SecurityException("User account is blocked");
        }
        if (user.isExpired()) {
            throw new SecurityException("User account expired");
        }
    }

    final ThreadLocal<Boolean> checkedDeviceManagement = new ThreadLocal<>();
    void checkDeviceManagementAccess(ManagesDevices managesDevices) throws Throwable {
        if (checkedDeviceManagement.get() != null) {
            return;
        }
        User user = sessionUser.get();
        if (user == null) {
            throw new SecurityException("Not logged in");
        }
        if (!user.hasPermission(UserPermission.DEVICE_EDIT)) {
            checkedDeviceManagement.set(Boolean.TRUE);
        }
    }

    final ThreadLocal<Boolean> checkedRequireWrite = new ThreadLocal<>();
    void checkRequireWrite(RequireWrite requireWrite) {
        if (checkedRequireWrite.get() != null) {
            return;
        }
        User user = sessionUser.get();
        if (user == null) {
            throw new SecurityException("Not logged in");
        }

        checkedRequireWrite.set(Boolean.TRUE);
    }

    void cleanUp() {
        checkedDeviceManagement.remove();
        checkedRequireWrite.remove();
    }
}
