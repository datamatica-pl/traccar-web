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
package org.traccar.web.client;

import pl.datamatica.traccar.model.UserSettings;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Device;
import com.google.gwt.i18n.client.TimeZoneInfo;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.grid.GridSelectionModel;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.layer.Layer;
import org.traccar.web.client.controller.*;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.*;
import org.traccar.web.client.view.ApplicationView;
import org.traccar.web.client.view.UserSettingsDialog;
import org.traccar.web.client.widget.TimeZoneComboBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.data.shared.event.StoreAddEvent;
import com.sencha.gxt.data.shared.event.StoreHandlers;
import com.sencha.gxt.data.shared.event.StoreRemoveEvent;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import org.traccar.web.client.InitialLoader.LoadFinishedListener;
import org.traccar.web.client.model.api.Decoder;
import org.traccar.web.client.model.api.DevicesService;
import org.traccar.web.client.model.api.IUsersService.EditUserSettingsDto;
import org.traccar.web.client.model.api.UsersService;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

public class Application {

    private static final DataServiceAsync dataService = GWT.create(DataService.class);
    private static final DevicesService devices = new DevicesService();
    private static final Decoder decoder = new Decoder();
    private final static Messages i18n = GWT.create(Messages.class);

    public static DataServiceAsync getDataService() {
        return dataService;
    }
    
    public static DevicesService getDevicesService() {
        return devices;
    }
    
    public static Decoder getDecoder() {
        return decoder;
    }

    private final SettingsController settingsController;
    private final NavController navController;
    private final DeviceController deviceController;
    private final RouteController routeController;
    private final CommandController commandController;
    private final GeoFenceController geoFenceController;
    private final MapController mapController;
    private final UpdatesController updatesController;
    private final ArchiveController archiveController;
    private final ReportsController reportsController;
    private final LogController logController;
    private final GroupsController groupsController;
    private final VisibilityController visibilityController;
    private final UserGroupsController userGroupsController;
    
    private final InitialLoader initialLoader;

    private ApplicationView view;

    public Application() {
        DeviceProperties deviceProperties = GWT.create(DeviceProperties.class);
        final ListStore<Device> deviceStore = new ListStore<>(deviceProperties.id());
        deviceStore.clearSortInfo();
        final GroupStore groupStore = new GroupStore();
        ReportProperties reportProperties = GWT.create(ReportProperties.class);
        final ListStore<Report> reportStore = new ListStore<>(reportProperties.id());

        settingsController = new SettingsController(userSettingsHandler);
        visibilityController = new VisibilityController();
        mapController = new MapController(mapHandler, deviceStore, visibilityController);
        geoFenceController = new GeoFenceController(deviceStore, mapController);
        geoFenceController.getGeoFenceStore().addStoreHandlers(geoFenceStoreHandler);
        commandController = new CommandController();
        routeController = new RouteController(deviceStore, geoFenceController.getGeoFenceStore(),
                mapController);
        reportsController = new ReportsController(reportStore, deviceStore, 
                geoFenceController.getGeoFenceStore(), routeController.getStore());
        routeController.setReportHandler(reportsController);
        deviceController = new DeviceController(mapController,
                geoFenceController,
                commandController,
                routeController,
                visibilityController,
                deviceStore,
                deviceStoreHandler,
                geoFenceController.getGeoFenceStore(),
                geoFenceController.getDeviceGeoFences(),
                groupStore,
                reportStore,
                routeController.getStore(),
                reportsController,
                this);
        groupsController = new GroupsController(groupStore, deviceController);
        userGroupsController = new UserGroupsController();
        logController = new LogController();
        navController = new NavController(settingsController, reportStore, reportsController, logController, groupsController, userGroupsController);
        archiveController = new ArchiveController(archiveHandler, userSettingsHandler, deviceController.getDeviceStore(), reportStore, reportsController);
        
        updatesController = new UpdatesController();
        updatesController.addLatestPositionsListener(mapController);
        updatesController.addDevicesListener(deviceController);
        updatesController.addRoutesListener(routeController);

        initialLoader = new InitialLoader(deviceStore, groupStore);
        view = new ApplicationView(
                navController.getView(), deviceController.getView(), mapController.getView(), archiveController.getView());
    }

    public void run() {
        RootPanel.get().add(view);

        initialLoader.load(new LoadFinishedListener() {
            @Override
            public void onLoadFinished() {
                final User user = ApplicationContext.getInstance().getUser();
                navController.run();
                deviceController.run();
                mapController.run();
                if(user.hasPermission(UserPermission.HISTORY_READ))
                    archiveController.run();
                if(user.hasPermission(UserPermission.GEOFENCE_READ))
                    geoFenceController.run(new Runnable() {
                        @Override
                        public void run() {
                            if(user.hasPermission(UserPermission.REPORTS))
                                reportsController.run();
                            if(user.hasPermission(UserPermission.TRACK_READ))
                                routeController.run();
                            updatesController.run();
                        }
                    });
                else
                    updatesController.run();
                if(user.hasPermission(UserPermission.COMMAND_TCP))
                    commandController.run();
                if(user.hasPermission(UserPermission.DEVICE_GROUP_MANAGEMENT))
                    groupsController.run();
                visibilityController.run();
                setupTimeZone();
                updatesController.devicesLoaded(deviceController.getDeviceStore().getAll());
            }
        });
    }

    private void setupTimeZone() {
        UserSettings userSettings = ApplicationContext.getInstance().getUserSettings();
        if (userSettings.getTimeZoneId() == null) {
            String timeZoneID = getTimeZoneFromIntlApi();
            if (timeZoneID == null) {
                TimeZoneInfo detectedZone = TimeZoneComboBox.getByOffset(-getClientOffsetTimeZone());
                timeZoneID = detectedZone == null ? null : detectedZone.getID();
            }
            if (timeZoneID != null) {
                userSettings.setTimeZoneId(timeZoneID);
                userSettingsHandler.onSave(userSettings);
            }
        }
    }

    private native String getTimeZoneFromIntlApi() /*-{
        if (typeof Intl === "undefined" || typeof Intl.DateTimeFormat === "undefined") {
            return null;
        }

        format = Intl.DateTimeFormat();

        if (typeof format === "undefined" || typeof format.resolvedOptions === "undefined") {
            return null;
        }

        timezone = format.resolvedOptions().timeZone;

        if (timezone && (timezone.indexOf("/") > -1 || timezone === 'UTC')) {
            return timezone;
        }

        return null;
    }-*/;

    private native int getClientOffsetTimeZone() /*-{
        return new Date().getTimezoneOffset();
    }-*/;

    private MapController.MapHandler mapHandler = new MapController.MapHandler() {

        @Override
        public void onDeviceSelected(Device device) {
            deviceController.selectDevice(device);
        }

        @Override
        public void onArchivePositionSelected(Position position) {
            archiveController.selectPosition(position);
        }

    };

    private ArchiveController.ArchiveHandler archiveHandler = new ArchiveController.ArchiveHandler() {

        @Override
        public void onSelected(Position position) {
            mapController.selectArchivePosition(position);
        }

        @Override
        public void onClear(Device device) {
            mapController.clearArchive(device);
        }

        @Override
        public void onDrawTrack(Track track) {
            mapController.showArchivePositions(track);
        }
    };

    public ArchiveController getArchiveController() {
        return archiveController;
    }

    private StoreHandlers<Device> deviceStoreHandler = new BaseStoreHandlers<Device>() {

        @Override
        public void onAdd(StoreAddEvent<Device> event) {
            updatesController.update();
        }

        @Override
        public void onRemove(StoreRemoveEvent<Device> event) {
            updatesController.update();
            geoFenceController.deviceRemoved(event.getItem());
        }

    };

    private StoreHandlers<GeoFence> geoFenceStoreHandler = new BaseStoreHandlers<GeoFence>() {
        @Override
        public void onAdd(StoreAddEvent<GeoFence> event) {
            for (GeoFence geoFence : event.getItems()) {
                geoFenceController.geoFenceAdded(geoFence);
            }
        }

        @Override
        public void onRemove(StoreRemoveEvent<GeoFence> event) {
            geoFenceController.geoFenceRemoved(event.getItem());
        }
    };

    private class UserSettingsHandlerImpl implements UserSettingsDialog.UserSettingsHandler {
        private UsersService users = new UsersService();
        
        @Override
        public void onSave(final UserSettings userSettings) {
            users.updateUserSettings(ApplicationContext.getInstance().getUser().getId(), 
                    new EditUserSettingsDto(userSettings), new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if(userSettings.getMinDistance() != null)
                        userSettings.setMinDistance(userSettings.getMinDistance()
                            /userSettings.getSpeedUnit().getDistanceUnit().getFactor());
                    ApplicationContext.getInstance().setUserSettings(userSettings);
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                }
                        
                    });
        }

        @Override
        public void onTakeCurrentMapState(ComboBox<UserSettings.MapType> mapType,
                                          NumberField<Double> centerLongitude,
                                          NumberField<Double> centerLatitude,
                                          NumberField<Integer> zoomLevel,
                                          CheckBox maximizeOverviewMap,
                                          GridSelectionModel<UserSettings.OverlayType> overlays) {
            String layerName = mapController.getMap().getBaseLayer().getName();
            for (UserSettings.MapType mapTypeXX : UserSettings.MapType.values()) {
                if (layerName.equals(mapTypeXX.getName())) {
                    mapType.setValue(mapTypeXX);
                    break;
                }
            }
            LonLat center = mapController.getMap().getCenter();
            center.transform(mapController.getMap().getProjection(), "EPSG:4326");
            centerLongitude.setValue(center.lon());
            centerLatitude.setValue(center.lat());
            zoomLevel.setValue(mapController.getMap().getZoom());
            maximizeOverviewMap.setValue(mapController.getOverviewMap().getJSObject()
                    .getProperty("maximizeDiv").getProperty("style").getPropertyAsString("display").equals("none"));

            overlays.deselectAll();
            for (UserSettings.OverlayType overlayType : UserSettings.OverlayType.values()) {
                Layer[] mapLayer = mapController.getMap().getLayersByName(i18n.overlayType(overlayType));
                if (mapLayer != null && mapLayer.length == 1 && mapLayer[0].isVisible()) {
                    overlays.select(overlayType, true);
                }
            }
        }

        @Override
        public void onSetZoomLevelToCurrent(NumberField<Short> field) {
            field.setValue((short) mapController.getMap().getZoom());
        }
    }

    private UserSettingsHandlerImpl userSettingsHandler = new UserSettingsHandlerImpl();
}
