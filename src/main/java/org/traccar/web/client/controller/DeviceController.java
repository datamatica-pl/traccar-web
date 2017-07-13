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
package org.traccar.web.client.controller;

import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.Sensor;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.Group;
import pl.datamatica.traccar.model.Maintenance;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Device;
import java.util.*;

import com.sencha.gxt.data.shared.event.StoreHandlers;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.GroupStore;
import org.traccar.web.client.state.DeviceVisibilityHandler;
import org.traccar.web.client.view.*;
import org.traccar.web.shared.model.*;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.traccar.web.client.model.api.DevicesService;
import org.traccar.web.client.model.api.DevicesService.AddDeviceDto;
import pl.datamatica.traccar.model.ReportFormat;
import pl.datamatica.traccar.model.ReportType;
import pl.datamatica.traccar.model.Route;

public class DeviceController implements ContentController, DeviceView.DeviceHandler, 
        GroupsController.GroupRemoveHandler, UpdatesController.DevicesListener {
    private final MapController mapController;

    private final Application application;

    private final ListStore<Device> deviceStore;

    private final DeviceView deviceView;

    private Messages i18n = GWT.create(Messages.class);

    private final PositionInfoPopup positionInfo;

    private final StoreHandlers<Device> deviceStoreHandler;

    // geo-fences per device
    private final Map<Long, Set<GeoFence>> deviceGeoFences;

    private final GroupStore groupStore;

    private final DeviceVisibilityHandler deviceVisibilityHandler;

    private Device selectedDevice;
    
    private Storage localStore = null;
    
    private final ReportsController reportHandler;

    public DeviceController(MapController mapController,
                            DeviceView.GeoFenceHandler geoFenceHandler,
                            DeviceView.CommandHandler commandHandler,
                            DeviceView.RouteHandler routeHandler,
                            DeviceVisibilityHandler deviceVisibilityHandler,
                            final ListStore<Device> deviceStore,
                            StoreHandlers<Device> deviceStoreHandler,
                            ListStore<GeoFence> geoFenceStore,
                            Map<Long, Set<GeoFence>> deviceGeoFences,
                            GroupStore groupStore,
                            final ListStore<Report> reportStore,
                            ListStore<Route> routeStore,
                            ReportsController reportHandler,
                            Application application) {
        this.application = application;
        this.mapController = mapController;
        this.deviceStoreHandler = deviceStoreHandler;
        this.deviceStore = deviceStore;
        this.positionInfo = new PositionInfoPopup(deviceStore);
        this.deviceGeoFences = deviceGeoFences;
        this.groupStore = groupStore;
        this.deviceVisibilityHandler = deviceVisibilityHandler;
        this.reportHandler = reportHandler;
        
        deviceView = new DeviceView(this, geoFenceHandler, commandHandler, routeHandler,
                deviceVisibilityHandler, deviceStore, geoFenceStore, groupStore,
                reportStore, routeStore, reportHandler);
    }

    public ListStore<Device> getDeviceStore() {
        return deviceStore;
    }

    @Override
    public ContentPanel getView() {
        return deviceView.getView();
    }

    @Override
    public void run() {
        deviceStore.addStoreHandlers(deviceStoreHandler);
        showDevicesSubscriptionLeftPopup();
    }

    @Override
    public void onSelected(Device device) {
        onSelected(device, false);
    }

    @Override
    public void onSelected(Device device, boolean zoomIn) {
        mapController.selectDevice(device);
        updateGeoFences(device);
        selectedDevice = device;
        if(zoomIn) {
            mapController.zoomIn(device);
        }
    }

    @Override
    public void onAdd() {
        User user = ApplicationContext.getInstance().getUser();
        if (!user.getAdmin() &&
                user.getMaxNumOfDevices() != null &&
                deviceStore.size() >= user.getMaxNumOfDevices()) {
            new AlertMessageBox(i18n.error(), i18n.errMaxNumberDevicesReached(user.getMaxNumOfDevices().toString())).show();
            return;
        }

        new ImeiDialog(new ImeiDialog.ImeiHandler() {
            @Override
            public void onImei(String imei) {
                DevicesService devices = GWT.create(DevicesService.class);
                AddDeviceDto dto = new AddDeviceDto(imei);
                devices.addDevice(dto, new JsonCallback(){
                    @Override
                    public void onFailure(Method method, Throwable exception) {
                        MessageBox msg = new AlertMessageBox(i18n.error(), i18n.errInvalidImeiNoContact());
                        msg.show();
                    }
                    
                    
                    @Override
                    public void onSuccess(Method method, JSONValue response) {
                        if(response == null) {
                            onFailure(method, null);
                            return;
                        }
                        Device d = Application.getDecoder().decodeDevice(response.isObject());
                        if(d == null) {
                            onFailure(method, null);
                            return;
                        }
                        deviceStore.add(d);
                        onEdit(d);
                    }
                });
            }
        }).show();
    }

    @Override
    public void onEdit(Device device) {
        class UpdateHandler implements DeviceDialog.DeviceHandler {
            @Override
            public void onSave(final Device device) {
                Application.getDataService().updateDevice(device, new BaseAsyncCallback<Device>(i18n) {
                    @Override
                    public void onSuccess(Device result) {
                        onDevicesUpdated(Collections.singletonList(result));
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        MessageBox msg = null;
                        if (caught instanceof ValidationException) {
                            msg = new AlertMessageBox(i18n.error(), i18n.errNoDeviceNameOrId());
                        } else {
                            msg = new AlertMessageBox(i18n.error(), i18n.errUpdateFailed());
                        }
                        if (msg != null) {
                            msg.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
                                @Override
                                public void onDialogHide(DialogHideEvent event) {
                                    new DeviceDialog(device, deviceStore, groupStore, UpdateHandler.this).show();
                                }
                            });
                            msg.show();
                        }
                    }
                });
            }

            @Override
            public void setWindow(Window window) {
            }
        }
        
        new DeviceDialog(new Device(device), deviceStore, groupStore, new UpdateHandler()).show();
    }

    @Override
    public void onShare(final Device device) {
        Application.getDataService().getDeviceShare(device, new BaseAsyncCallback<Map<User, Boolean>>(i18n) {
            @Override
            public void onSuccess(final Map<User, Boolean> share) {
                new UserShareDialog(share, new UserShareDialog.UserShareHandler() {
                    @Override
                    public void onSaveShares(Map<User, Boolean> shares, final Window window) {
                        Application.getDataService().saveDeviceShare(device, shares, new BaseAsyncCallback<Void>(i18n) {
                            @Override
                            public void onSuccess(Void result) {
                                window.hide();
                            }
                        });
                    }
                }).show();
            }
        });
    }

    @Override
    public void onRemove(final Device device) {
        final ConfirmMessageBox dialog = new ConfirmMessageBox(i18n.confirm(), i18n.confirmDeviceRemoval());
        dialog.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
            @Override
            public void onDialogHide(DialogHideEvent event) {
                if (event.getHideButton() == PredefinedButton.YES) {
                    Application.getDataService().removeDevice(device, new BaseAsyncCallback<Device>(i18n) {
                        @Override
                        public void onSuccess(Device result) {
                            deviceStore.remove(device);
                        }
                    });
                }
            }
        });
        dialog.show();
    }

    @Override
    public void onMouseOver(int mouseX, int mouseY, Device device) {
        Position latestPosition = mapController.getLatestPosition(device);
        if (latestPosition != null) {
            positionInfo.show(mouseX, mouseY, latestPosition);
        }
    }

    @Override
    public void onMouseOut(int mouseX, int mouseY, Device device) {
        positionInfo.hide();
    }

    public void selectDevice(Device device) {
        deviceView.selectDevice(device);
        updateGeoFences(device);
        selectedDevice = device;
    }

    public void doubleClicked(Device device) {
        application.getArchiveController().selectDevice(device);
    }

    private void updateGeoFences(Device device) {
        onClearSelection();
        Set<GeoFence> geoFences = device == null ? null : deviceGeoFences.get(device.getId());
        if (geoFences != null) {
            for (GeoFence geoFence : geoFences) {
                mapController.drawGeoFence(geoFence, true);
            }
        }
    }

    @Override
    public void onClearSelection() {
        // remove old geo-fences
        if (selectedDevice != null) {
            Set<GeoFence> geoFences = deviceGeoFences.get(selectedDevice.getId());
            if (geoFences != null) {
                for (GeoFence geoFence : geoFences) {
                    mapController.removeGeoFence(geoFence);
                }
            }
        }
        selectedDevice = null;
    }

    @Override
    public void groupRemoved(Group group) {
        if(!group.isOwned())
            return;
        for (int i = 0; i < deviceStore.size(); i++) {
            Device device = deviceStore.get(i);
            if (Objects.equals(device.getGroup(), group)) {
                device.setGroup(null);
                deviceStore.update(device);
                deviceVisibilityHandler.updated(device);
            }
        }
    }
    
    @Override
    public void onDevicesUpdated(List<Device> devices) {
        for(Device result : devices) {
            deviceStore.update(result);
            mapController.updateIcon(result);
            boolean showAlert = false;
            for (Maintenance maintenance : result.getMaintenances()) {
                if (result.getOdometer() >= maintenance.getLastService() + maintenance.getServiceInterval()) {
                    showAlert = true;
                    break;
                }
            }
            mapController.updateAlert(result, showAlert);
            deviceVisibilityHandler.updated(result);
        }
    }

    private void showDevicesSubscriptionLeftPopup() {
        final Date today = new Date();
        int devicesCloseToExpireNum = 0;

        String messageBoxBody = "<p>" + i18n.devicesExpiresInfo() + "</p>";
        messageBoxBody += "<ul style='margin: 10px 0'>";
        for (Device dev : deviceStore.getAll()) {
            if (dev.isCloseToExpire(today)) {
                devicesCloseToExpireNum++;
                messageBoxBody += "<li>";
                String safeDeviceName = SafeHtmlUtils.fromString(dev.getName()).asString();
                messageBoxBody += safeDeviceName + ": ";
                int daysLeft = dev.getSubscriptionDaysLeft(today);
                if (daysLeft == 1) {
                    messageBoxBody += i18n.deviceExpireDaysNumSingular(daysLeft);
                } else {
                    messageBoxBody += i18n.deviceExpireDaysNum(daysLeft);
                }
                messageBoxBody += "</li>";
            }
        }
        messageBoxBody += "</ul>";
        messageBoxBody +=   "<p>" +
                                "<a href='http://sklep.datamatica.pl'>" +
                                    i18n.buySubscriptionLinkName() +
                                "</a>" +
                            "</p>";

        if (devicesCloseToExpireNum > 0) {
            localStore = Storage.getLocalStorageIfSupported();
            boolean isPopupShownToday = false; // If local storage is not supported each login
                                               // or refresh has an effect of show expiration pop-up
            if (localStore != null) {
                String currentDate = DateTimeFormat.getFormat("yyyy-MM-dd").format(today);
                String lastCheckDate = localStore.getItem("lastSubscriptionExpireCheck");
                
                if (lastCheckDate.equals(currentDate)) {
                    isPopupShownToday = true;
                } else {
                    localStore.setItem("lastSubscriptionExpireCheck", currentDate);
                }
            }
            
            if (!isPopupShownToday) {
                String devicesExpHeader = "<h1>" + i18n.devicesExpiresHeader() + "</h1>";
                MessageBox devicesExpPopup = new MessageBox(devicesExpHeader, messageBoxBody);
                devicesExpPopup.show();
            }
        }
    }

    @Override
    public void onShowAlarms(Device device) {
        String name = device.getName()+"_alarms";
        Date fromDate = new Date(0);
        if(device.getLastAlarmsCheck() != null)
            fromDate = device.getLastAlarmsCheck();
        Set<GeoFence> geofences = Collections.EMPTY_SET;
        if(deviceGeoFences.get(device.getId()) != null)
            geofences = deviceGeoFences.get(device.getId());
        
        Report report = new Report();
        report.setType(ReportType.EVENTS);
        report.setName(name);
        report.setDevices(Collections.singleton(device));
        report.setGeoFences(geofences);
        report.setFromDate(fromDate);
        report.setToDate(new Date());
        report.setPreview(true);
        report.setFormat(ReportFormat.HTML);
        reportHandler.generate(report);
        Application.getDataService().updateAlarmsViewTime(device, null);
    }
}
