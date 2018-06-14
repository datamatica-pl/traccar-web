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
package org.traccar.web.client.i18n;

import com.sencha.gxt.widget.core.client.menu.MenuItem;
import pl.datamatica.traccar.model.Period;
import pl.datamatica.traccar.model.UserSettings;
import pl.datamatica.traccar.model.ReportType;
import pl.datamatica.traccar.model.GeoFenceType;
import pl.datamatica.traccar.model.DeviceEventType;
import pl.datamatica.traccar.model.DeviceIconMode;
import org.traccar.web.shared.model.*;
import pl.datamatica.traccar.model.Route;

public interface Messages extends com.google.gwt.i18n.client.Messages {
    String clearCookies();
    
    String acceptTerms();
    
    String termsLabel();
    
    String howToClearCookies();
    
    String authentication();

    String user();

    String password();

    String resetPassword();
    
    String login();

    String register();

    String save();

    String cancel();

    String globalSettings();

    String registration();

    String updateInterval();

    String device();

    String archive();

    String from();

    String to();

    String load();

    String clear();

    String valid();

    String time();

    String latitude();

    String longitude();

    String altitude();

    String speed();

    String course();

    String power();

    String deviceId();
    String name();

    String uniqueIdentifier();

    String devices();

    String add();

    String edit();

    String remove();

    String settings();

    String account();

    String preferences();

    String users();

    String global();
    
    String userGuide();

    String logout();

    String follow();

    String state();

    String attribute();

    String value();

    String address();

    String administrator();

    String map();

    String speedUnits();

    String error();

    String errNoResults();

    String errFillFields();

    String errDeviceExists();

    String errUsernameTaken();

    String confirm();

    String confirmUserRemoval();

    String errUsernameOrPasswordEmpty();

    String errInvalidUsernameOrPassword();

    String confirmDeviceRemoval();

    String errRemoteCall();

    String recordTrace();

    String timePrintInterval();

    String trackerServerLog();

    String refresh();

    String close();

    String logSize();

    String defaultMapState();

    String zoom();

    String takeFromMap();

    String day();

    String hour();

    String minute();

    String second();

    String ago(String dateTimeString);

    String meter();

    String manager();

    String share();

    String deviceTimeout();

    String offline();

    String since(String dateTimeString);

    String idle();

    String disallowDeviceManagementByUsers();

    String idleWhenSpeedIsLE();

    String distance();

    String exportToCSV();

    String exportToGPX();

    String changePassword();

    String enterNewPassword(String p0);

    String importData();

    String fileToImport();

    String log();

    String importingData();

    String defaultHashImplementation();

    String filter();

    String hideZeroCoordinates();

    String hideInvalidLocations();

    String hideDuplicates();

    String ignoreLocationsWithDistanceFromPreviousLT();

    String disableFilter();

    String server();

    String port();

    String secureConnectionType();

    String useAuthorization();

    String test();

    String notifications();

    String notificationSettings();

    String testFailed();

    String testSucceeded();

    String email();

    String invalidEmail();

    String fromAddress();

    String style();

    String fullPalette();

    String smallPalette();

    String standardMarkers();

    String reducedMarkers();

    String zoomToTrack();

    String exportData();

    String errNoDeviceNameOrId();

    String eventRecordingEnabled();

    String language();

    String readOnly();

    String protocol();

    String objects();

    String description();

    String geoFence();

    String type();

    String width();

    String radius();

    String color();

    String errGeoFenceIsEmpty();

    String confirmGeoFenceRemoval();

    String newGeoFence();

    String geoFenceType(@Select GeoFenceType type);

    String errSaveChanges();

    String applyToAllDevices();

    String deviceEventType(@Select DeviceEventType type);

    String event();

    String accessToken();

    String messageTemplates();

    String subject();

    String contentType();

    String placeholderDescription(@Select MessagePlaceholder placeholder);

    String defaultNotificationTemplate(@Select DeviceEventType type,
                                       @Optional String deviceName,
                                       @Optional String geoFenceName,
                                       @Optional String eventTime,
                                       @Optional String positionTime,
                                       @Optional String maintenanceName,
                                       @Optional String positionAddress,
                                       @Optional String positionLat,
                                       @Optional String positionLon,
                                       @Optional String positionAlt,
                                       @Optional String positionSpeed,
                                       @Optional String positionCourse);

    String noMarkers();

    String select();

    String defaultIcon();

    String selectedIcon();

    String offlineIcon();

    String upload();

    String confirmDeviceIconRemoval();

    String odometer();

    String km();

    String auto();

    String maintenance();
    
    String technicalReview();
    
    String registrationReview();
    
    String insuranceValidity();

    String serviceName();

    String mileageInterval();

    String lastServiceMileage();

    String remaining();

    String overdue();

    String reset();

    String sensors();

    String parameter();

    String visible();

    String copyFrom();

    String intervals();

    String customIntervals();

    String intervalFrom();

    String text();

    String interval();

    String trackerPhoneNumber();

    String plateNumber();

    String vehicleBrandModelColor();
    
    String deviceModel();

    String errUserAccountBlocked();

    String errUserAccountExpired();

    String errMaxNumberDevicesReached(String p0);

    String errUserSessionExpired();

    String errUserDisconnected();
    
    String userAddedTitle();
    
    String userAddedMessage();

    String firstName();

    String lastName();

    String companyName();

    String expirationDate();

    String maxNumOfDevices();

    String blocked();
    
    String phoneNumber();

    String overlays();

    String overlay();

    String overlayType(@Select UserSettings.OverlayType type);

    String snapToRoads();

    String period(@Select Period period);

    String periodComboBox_SelectPeriod();

    String traceInterval();

    String followedDeviceZoomLevel();

    String followedDeviceZoomLevelToolTip();

    String useCurrentZoomLevel();

    String errAccessDenied();

    String errMaxNumOfDevicesExceeded(int maxNumOfDevices);

    String timeZone();

    String bingMapsKey();

    String reports();

    String report();

    String timePeriod();

    String generate();

    String newReport();

    String reportType(@Select ReportType type);

    String confirmReportRemoval();

    String routeStart();

    String routeEnd();

    String routeLength();

    String moveDuration();

    String stopDuration();

    String topSpeed();

    String averageSpeed();

    String overspeedCount();

    String speedLimit();
    
    String fuelCapacity();

    String status();

    String start();

    String end();

    String duration();

    String moving();

    String stopped();

    String stopPosition();

    String overspeedPosition();

    String geoFenceIn();

    String geoFenceOut();

    String geoFenceName();

    String geoFencePosition();

    String eventPosition();

    String totalOffline();

    String totalGeoFenceEnters();

    String totalGeoFenceExits();

    String totalMaintenanceRequired();

    String date();

    String totalMileage();

    String logs();

    String includeMap();

    String minIdleTime();

    String wrapperLog();

    String preview();

    String maximizeOverviewMap();

    String command();

    String send();

    String customMessage();

    String frequency();
    
    String frequencyStop();

    String groups();

    String newGroup();

    String confirmGroupRemoval();

    String group();

    String noGroup();

    String online();

    String message();

    String notificationExpiryPeriod();

    String loadingData();

    String icon();

    String showOnMap();

    String deviceIconMode(@Select DeviceIconMode mode);

    String iconRotation();

    String arrowColorMoving();

    String arrowColorPaused();

    String arrowColorStopped();

    String arrowColorOffline();

    String showName();

    String showProtocol();

    String showOdometer();

    String timezoneOffset();

    String matchServiceURL();

    String errSnapToRoads(int code, String text);

    String errNoSubscriptionTitle();
    String errNoSubscriptionMessage();
    
    String arrowSize();

    String allowCommandsOnlyForAdmins();
    
    String defenseTime();
    
    String SOSNumber1();
    
    String SOSNumber2();
    
    String SOSNumber3();
    
    String SOSNumber();
    
    String centerNumber();
    
    String commandPassword();
    
    String days();
    
    String maintenanceDate();
    
    String alarmIconHint();
    
    String ignition();
    
    String deviceValidTo();
    
    String historyLength();
    
    String devicesExpiresHeader();
    
    String devicesExpiresInfo();
    
    String deviceExpireDaysNum(int days);
    
    String deviceExpireDaysNumSingular(int days);
    
    String buySubscriptionLinkName();
    
    String imei();
    
    String success();
    
    String validationMailSent();
    
    String errInvalidImei();
    
    String ok();
    
    String selectDeselectAll();

    public String reportsForPremium();
    
    String resetMailSent();
    String resetMailSentFailure();
    
    String errUpdateFailed();
    
    String errInvalidImeiNoContact();
    
    //tracks
    String tracks();
    String trackName();
    String connectPoints();
    String trackCreation();
    String selectDevice();
    String addFromMap();
    String latLonFormat(double lat, String latDir, double lon, String lonDir);
    String createCorridor();
    String corridorOfRoute(String routeName);
    String errNoRouteName();
    String errNotEnoughRoutePoints();
    String errNoCorridorRadius();
    String errNoGeoFences();

    public String emailResent();
    String moreLoginOptionsTitle();
    String moreLoginOptionsInfo();
    String resendLink();
    String loginDialogMoreInfo();
    String more();

    public String unknownModel();
    public String errReportMax31Days();
    
    String userGroups();
    String deviceGroups();
    String showUsers();
    String copyPermissionsFrom();

    public String confirmation();

    public String actionNotReversible();
    String userGroupUsersTitle(String name);
    String defaultGroup();
    String defaultIconId();
    
    String auditLog();
    String auditLogEvent();
    String auditLogAgent();
    String show();
    String auditInfo();
    String showLogFrom();
    
    String errInitialLoadFailed();
    String errValMustBeDivisibleBy(int val);

    public String errNoReportDevicesSelected();
    String errNoReportRouteSelected();

    String addToArchive();
    String abortRoute();
    String duplicateRoute();
    String showArchivedRoutes();
    String archivedRoutes();
    String restoreRoute();
    String deadline();
    String archiveAfter();
    String tolerance();
    String errInvalidRoutePoint(int i);
    String confirmRouteRemoval();
    String newPoint();
    String routeStatus(@Select Route.Status status);
    String routeLength(double length);
    String lblOnlyPremiumDevices();
    
    String accept();
    String rulesDialogHeader();
    String rulesDialogExplanation();
    String rulesUrl();
    String rulesStartDate();
    String rulesType();
    String belongsToGroup();
    
    String loginAsDemoUser();
    String lblContinue();
    String acceptDemoRules();
    String demoRulesExplanation();
    String demoRulesHeader();

    public String osrmError(@Select int code);
    String recalculate();
    
    String freeHistory();
}
