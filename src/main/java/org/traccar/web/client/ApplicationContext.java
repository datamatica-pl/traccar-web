package org.traccar.web.client;

import java.util.Collection;
import java.util.HashMap;
import pl.datamatica.traccar.model.ApplicationSettings;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserSettings;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import pl.datamatica.traccar.model.Group;
import pl.datamatica.traccar.model.UserPermission;

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

    Map<Long, User> users = new HashMap<>();
    
    public User getUser(long id) {
        return users.get(id);
    }
    
    public Set<User> getUsers() {
        if(user.hasPermission(UserPermission.ALL_USERS))
            return new HashSet<>(users.values());
        Set<User> copy = new HashSet<>();
        if(user.getManagedById() == null)
            copy.addAll(users.values());
        else for(User u : users.values())
            if(u.getId() != user.getManagedById())
                copy.add(u);
        return copy;
    }
    
    public void setUsers(Collection<User> users) {
        this.users.clear();
        for(User u : users)
            this.users.put(u.getId(), u);
    }
    
    public void addUser(User u) {
        users.put(u.getId(), u);
    }
    
    Map<Long, Group> groups = new HashMap<>();
    
    public Group getGroup(Long id) {
        if(id == null)
            return null;
        return groups.get(id);
    }
    
    public void setGroups(Collection<Group> groups) {
        this.groups.clear();
        for(Group g : groups) {
            this.groups.put(g.getId(), g);
        }
    }
    
    private String lang;
    
    public String getLang() {
        String language = lang;
        if(language == null)
            language = applicationSettings.getLanguage();
        if("default".equals(language))
            return "en";
        return language;
    }
    
    public void setLang(String lang) {
        this.lang = lang;
    }
    
    private Map<String, String> messages = new HashMap<>();
    
    public String getMessage(String key) {
        if(messages.containsKey(key))
            return messages.get(key);
        return key;
    }
    
    public String getMessageTryLowerCase(String key) {
        final String lowerCaseKey = key.toLowerCase();
        if (messages.containsKey(key)) {
            return messages.get(key);
        } else if (messages.containsKey(lowerCaseKey)) {
            return messages.get(lowerCaseKey);
        }
        return key;
    }
    
    public void setMessages(Map<String, String> messages) {
        this.messages.clear();
        this.messages.putAll(messages);
    }
}