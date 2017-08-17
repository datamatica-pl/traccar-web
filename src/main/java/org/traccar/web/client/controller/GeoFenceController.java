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
package org.traccar.web.client.controller;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONValue;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.traccar.web.client.Application;
import org.traccar.web.client.GeoFenceDrawing;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.GeoFenceProperties;
import org.traccar.web.client.view.DeviceView;
import org.traccar.web.client.view.GeoFenceWindow;
import org.traccar.web.client.view.UserShareDialog;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.User;

import java.util.*;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.model.api.ApiGeofence;
import org.traccar.web.client.model.api.GeofencesService;

public class GeoFenceController implements ContentController, DeviceView.GeoFenceHandler {
    private final MapController mapController;
    private final ListStore<GeoFence> geoFenceStore;
    private final ListStore<Device> deviceStore;
    private final Map<Long, Set<GeoFence>> deviceGeoFences;
    private ListView<GeoFence, String> geoFenceListView;
    private boolean geoFenceManagementInProgress;
    private GeoFence selectedGeoFence;
    
    private GeofencesService service = new GeofencesService();

    private Messages i18n = GWT.create(Messages.class);

    public GeoFenceController(ListStore<Device> deviceStore, MapController mapController) {
        this.deviceStore = deviceStore;
        this.mapController = mapController;
        GeoFenceProperties geoFenceProperties = GWT.create(GeoFenceProperties.class);
        this.geoFenceStore = new ListStore<>(geoFenceProperties.id());
        this.geoFenceStore.addSortInfo(new Store.StoreSortInfo<>(geoFenceProperties.name(), SortDir.ASC));
        this.deviceGeoFences = new HashMap<>();
    }

    abstract class BaseGeoFenceHandler implements GeoFenceWindow.GeoFenceHandler {
        final GeoFence geoFence;

        protected BaseGeoFenceHandler(GeoFence geoFence) {
            this.geoFence = geoFence;
        }

        @Override
        public void onClear() {
            mapController.removeGeoFence(geoFence);
        }

        @Override
        public GeoFenceDrawing repaint(GeoFence geoFence) {
            mapController.removeGeoFence(geoFence);
            mapController.drawGeoFence(geoFence, false);
            return mapController.getGeoFenceDrawing(geoFence);
        }
    }

    @Override
    public void onAdd() {
        if (geoFenceManagementInProgress()) {
            return;
        }
        geoFenceManagementStarted();
        final GeoFence geoFence = new GeoFence();
        geoFence.setName(i18n.newGeoFence());
        geoFence.setTransferDevices(new HashSet<Device>());
        new GeoFenceWindow(geoFence, null, deviceStore, mapController.getMap(), mapController.getGeoFenceLayer(),
        new BaseGeoFenceHandler(geoFence) {
            @Override
            public void onSave(final GeoFence geoFence) {
                service.addGeofence(new ApiGeofence(geoFence), new MethodCallback<ApiGeofence>() {
                    @Override
                    public void onFailure(Method method, Throwable exception) {
                        onCancel();
                        new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                    }

                    @Override
                    public void onSuccess(Method method, ApiGeofence response) {
                        mapController.removeGeoFence(geoFence);
                        GeoFence gf = response.toGeofence(deviceStore.getAll());
                        geoFenceStore.add(gf);
                        geoFenceStore.applySort(false);
                        geoFenceListView.getSelectionModel().select(gf, false);
                        geoFenceManagementStopped();
                    }
                    
                });
            }

            @Override
            public void onCancel() {
                onClear();
                geoFenceManagementStopped();
            }
        }).show();
    }

    @Override
    public void onEdit(final GeoFence geoFence) {
        if (geoFenceManagementInProgress()) {
            return;
        }
        geoFenceManagementStarted();
        GeoFenceDrawing drawing = mapController.getGeoFenceDrawing(geoFence);
        mapController.getGeoFenceLayer().removeFeature(drawing.getTitle());
        new GeoFenceWindow(geoFence, drawing, deviceStore, mapController.getMap(), mapController.getGeoFenceLayer(),
        new BaseGeoFenceHandler(geoFence) {
            @Override
            public void onSave(final GeoFence updatedGeoFence) {
                service.updateGeofence(updatedGeoFence.getId(), new ApiGeofence(updatedGeoFence),
                        new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        mapController.removeGeoFence(updatedGeoFence);
                        if (updatedGeoFence.equals(selectedGeoFence)) {
                            mapController.drawGeoFence(updatedGeoFence, true);
                            selectedGeoFence = updatedGeoFence;
                        }
                        geoFenceStore.update(updatedGeoFence);
                        geoFenceStore.applySort(false);
                        for (Collection<GeoFence> geoFences : deviceGeoFences.values()) {
                            geoFences.remove(updatedGeoFence);
                        }
                        geoFenceAdded(updatedGeoFence);
                        geoFenceManagementStopped();
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        onCancel();
                        new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                    }
                            
                        });
            }

            @Override
            public void onCancel() {
                mapController.removeGeoFence(geoFence);
                if (geoFence.equals(selectedGeoFence)) {
                    mapController.drawGeoFence(geoFence, true);
                }
                geoFenceManagementStopped();
            }
        }).show();
    }

    @Override
    public void onRemove(final GeoFence geoFence) {
        if (geoFenceManagementInProgress()) {
            return;
        }
        final ConfirmMessageBox dialog = new ConfirmMessageBox(i18n.confirm(), i18n.confirmGeoFenceRemoval());
        dialog.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
            @Override
            public void onDialogHide(DialogHideEvent event) {
                if (event.getHideButton() == Dialog.PredefinedButton.YES) {
                   service.removeGeofence(geoFence.getId(), new JsonCallback() {
                       @Override
                       public void onFailure(Method method, Throwable exception) {
                           new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                       }

                       @Override
                       public void onSuccess(Method method, JSONValue response) {
                           geoFenceStore.remove(geoFence);
                       }
                       
                    });
                }
            }
        });
        dialog.show();
    }

    @Override
    public ContentPanel getView() {
        return null;
    }

    public void run() {
        service.getGeoFences(new MethodCallback<List<ApiGeofence>>() {
            @Override
            public void onFailure(Method method, Throwable exception) {
                new AlertMessageBox(i18n.error(), exception.getMessage()).show();
            }

            @Override
            public void onSuccess(Method method, List<ApiGeofence> response) {
                List<GeoFence> gfs = new ArrayList<>();
                for(ApiGeofence agf : response)
                    gfs.add(agf.toGeofence(deviceStore.getAll()));
                geoFenceStore.addAll(gfs);
                geoFenceStore.applySort(false);
            }
        });
    }
    
    public ListStore<GeoFence> getGeoFenceStore() {
        return geoFenceStore;
    }

    @Override
    public void onSelected(GeoFence geoFence) {
        if (selectedGeoFence != null) {
            mapController.removeGeoFence(selectedGeoFence);
        }
        if (geoFence != null) {
            mapController.drawGeoFence(geoFence, true);
            mapController.selectGeoFence(geoFence);
        }
        selectedGeoFence = geoFence;
    }

    @Override
    public void onShare(final GeoFence geoFence) {
        if (geoFenceManagementInProgress()) {
            return;
        }
        service.getGeofenceShare(geoFence.getId(), new MethodCallback<Set<Long>>() {
            @Override
            public void onFailure(Method method, Throwable exception) {
                new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
            }

            @Override
            public void onSuccess(Method method, Set<Long> response) {
                Map<User, Boolean> share = new HashMap<>();
                for(User u : ApplicationContext.getInstance().getUsers())
                    share.put(u, response.contains(u.getId()));
                new UserShareDialog(share, new UserShareDialog.UserShareHandler() {
                    @Override
                    public void onSaveShares(Map<User, Boolean> shares, final Window window) {
                        List<Long> uids = new ArrayList<>();
                        for(Map.Entry<User, Boolean> e : shares.entrySet())
                            if(e.getValue())
                                uids.add(e.getKey().getId());
                        service.updateGeofenceShare(geoFence.getId(), uids,
                                new RequestCallback() {
                            @Override
                            public void onResponseReceived(Request request, Response response) {
                                window.hide();
                            }

                            @Override
                            public void onError(Request request, Throwable exception) {
                                new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                            }               
                                });
                    }
                }).show();
            }
        
        });
    }

    private boolean geoFenceManagementInProgress() {
        if (geoFenceManagementInProgress) {
            new AlertMessageBox(i18n.error(), i18n.errSaveChanges()).show();
        }
        return geoFenceManagementInProgress;
    }

    private void geoFenceManagementStarted() {
        geoFenceManagementInProgress = true;
    }

    private void geoFenceManagementStopped() {
        geoFenceManagementInProgress = false;
    }

    @Override
    public void setGeoFenceListView(ListView<GeoFence, String> geoFenceListView) {
        this.geoFenceListView = geoFenceListView;
    }

    public Map<Long, Set<GeoFence>> getDeviceGeoFences() {
        return deviceGeoFences;
    }

    public void geoFenceAdded(GeoFence geoFence) {
        for (Device device : geoFence.getTransferDevices()) {
            Set<GeoFence> geoFences = deviceGeoFences.get(device.getId());
            if (geoFences == null) {
                geoFences = new HashSet<>();
                deviceGeoFences.put(device.getId(), geoFences);
            }
            geoFences.add(geoFence);
        }
    }

    public void geoFenceRemoved(GeoFence geoFence) {
        mapController.removeGeoFence(geoFence);
        for (Map.Entry<Long, Set<GeoFence>> entry : deviceGeoFences.entrySet()) {
            entry.getValue().remove(geoFence);
        }
    }

    public void deviceRemoved(Device device) {
        deviceGeoFences.remove(device.getId());
    }
}
