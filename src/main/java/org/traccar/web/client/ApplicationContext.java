package org.traccar.web.client;

import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserSettings;

import java.util.HashSet;
import java.util.Set;

public class ApplicationContext {

    private static final ApplicationContext context = new ApplicationContext();

    public static ApplicationContext getInstance() {
        return context;
    }

    private FormatterUtil formatterUtil;

    public void setFormatterUtil(FormatterUtil formatterUtil) {
        this.formatterUtil = formatterUtil;
    }

    public FormatterUtil getFormatterUtil() {
        if (formatterUtil == null) {
            formatterUtil = new FormatterUtil();
        }
        return formatterUtil;
    }

    private ApplicationSettings applicationSettings;

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public ApplicationSettings getApplicationSettings() {
        if (applicationSettings != null) {
            return applicationSettings;
        } else {
            return new ApplicationSettings(); // default settings
        }
    }

    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUserSettings(UserSettings userSettings) {
        if (user != null) {
            user.setUserSettings(userSettings);
        }
    }

    public UserSettings getUserSettings() {
        if (user != null && user.getUserSettings() != null) {
            return user.getUserSettings();
        } else {
            return new UserSettings(); // default settings
        }
    }

    private Set<Long> followedDeviceIds;

    public void follow(Device device) {
        if (followedDeviceIds == null) {
            followedDeviceIds = new HashSet<>();
        }
        followedDeviceIds.add(device.getId());
    }

    public void stopFollowing(Device device) {
        if (followedDeviceIds != null) {
            followedDeviceIds.remove(device.getId());
        }
    }

    public boolean isFollowing(Device device) {
        return followedDeviceIds != null && followedDeviceIds.contains(device.getId());
    }

    private Set<Long> recordTraceDeviceIds;

    public void recordTrace(Device device) {
        if (recordTraceDeviceIds == null) {
            recordTraceDeviceIds = new HashSet<>();
        }
        recordTraceDeviceIds.add(device.getId());
    }

    public void stopRecordingTrace(Device device) {
        if (recordTraceDeviceIds != null) {
            recordTraceDeviceIds.remove(device.getId());
        }
    }

    public boolean isRecordingTrace(Device device) {
        return recordTraceDeviceIds != null && recordTraceDeviceIds.contains(device.getId());
    }
}