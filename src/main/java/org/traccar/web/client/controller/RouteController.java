/*
 * Copyright 2017 Datamatica (dev@datamatica.pl)
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

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.event.StoreRemoveEvent;
import com.sencha.gxt.data.shared.event.StoreRemoveEvent.StoreRemoveHandler;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.fusesource.restygwt.client.Method;
import org.traccar.web.client.Application;
import org.traccar.web.client.controller.UpdatesController.RoutesListener;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.api.ApiJsonCallback;
import org.traccar.web.client.model.api.ApiRoute;
import org.traccar.web.client.model.api.RoutesService;
import org.traccar.web.client.utils.PolylineDecoder;
import org.traccar.web.client.view.ArchivedRoutesDialog;
import org.traccar.web.client.view.DeviceView;
import org.traccar.web.client.view.RouteDialog;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.GeoFence.LonLat;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.ReportFormat;
import pl.datamatica.traccar.model.ReportType;
import pl.datamatica.traccar.model.Route;
import pl.datamatica.traccar.model.RoutePoint;

public class RouteController implements DeviceView.RouteHandler, ContentController,
        ArchivedRoutesDialog.RouteHandler, RoutesListener {
    private ListStore<Device> deviceStore;
    private ListStore<GeoFence> geoFenceStore;
    private MapController mapController;
    private ReportsController reportHandler;
    private final ListStore<Route> routeStore;
    private Messages i18n = GWT.create(Messages.class);
    private RoutesService service = GWT.create(RoutesService.class);
    
    public RouteController(ListStore<Device> devStore, ListStore<GeoFence> gfStore,
            MapController mapController) {
        this.deviceStore = devStore;
        this.geoFenceStore = gfStore;
        this.mapController = mapController;
        routeStore = new ListStore<>(new ModelKeyProvider<Route>() {
            @Override
            public String getKey(Route item) {
                return Long.toString(item.getId());
            } 
        });
        routeStore.addStoreRemoveHandler(new StoreRemoveHandler<Route>() {
            @Override
            public void onRemove(StoreRemoveEvent<Route> event) {
                onSelected(null);
            }
        });
    }
    
    public void setReportHandler(ReportsController reportHandler) {
        this.reportHandler = reportHandler;
    }
    
    @Override
    public void onAdd() {
        new RouteDialog(new Route(), new RouteDialog.RouteHandler() {
            @Override
            public void onSave(final Route route, final boolean connect) {
                Application.getDataService().addRoute(route, connect,
                        new BaseAsyncCallback<Route>(i18n) {
                            @Override
                            public void onSuccess(final Route addedRoute) {
                                updateGeofences(addedRoute);
                                if(connect)
                                    routeStore.add(addedRoute);
                            }
                        });
            }
            
        }, deviceStore, geoFenceStore, mapController.getCenter()).show();
    }
    
    @Override
    public void onEdit(final Route selectedItem) {
        new RouteDialog(selectedItem, new RouteDialog.RouteHandler() {
            @Override
            public void onSave(final Route route, boolean connect) {
                Application.getDataService().updateRoute(route,
                        new BaseAsyncCallback<Route>(i18n) {
                            @Override
                            public void onSuccess(final Route updated) {
                                if(selectedItem.getCorridor() != null)
                                    geoFenceStore.remove(selectedItem.getCorridor());
                                updateGeofences(updated);
                                routeStore.update(updated);
                            }
                        });
            }
            
        }, deviceStore, geoFenceStore).show();
    }
    
    @Override
    public void onDuplicate(final Route selectedItem) {
        DateTimeFormat sdf = DateTimeFormat.getFormat("yyyyMMdd");
        Route r = new Route(selectedItem);
        r.clearId();
        r.setName(r.getName()+"_"+sdf.format(new Date()));
        if(!selectedItem.getRoutePoints().isEmpty()) {
            int dayDiff = CalendarUtil.getDaysBetween(selectedItem.getRoutePoints()
                    .get(0).getDeadline(), new Date());
            r.getRoutePoints().clear();
            for(RoutePoint rp : selectedItem.getRoutePoints()) {
                RoutePoint pt = new RoutePoint();
                Date deadline = new Date(rp.getDeadline().getTime());
                CalendarUtil.addDaysToDate(deadline, dayDiff);
                pt.setDeadline(deadline);
                pt.setGeofence(rp.getGeofence());
                r.getRoutePoints().add(pt);
            }
            LonLat[] lls = PolylineDecoder.decodeToLonLat(selectedItem.getLinePoints());
            r.setLinePoints(selectedItem.getLinePoints());
        }
        r.setStatus(Route.Status.NEW);
        r.setCorridor(null);
        
        new RouteDialog(r, new RouteDialog.RouteHandler() {
            @Override
            public void onSave(final Route route, final boolean connect) {
                Application.getDataService().addRoute(route, connect,
                        new BaseAsyncCallback<Route>(i18n) {
                            @Override
                            public void onSuccess(final Route addedRoute) {
                                updateGeofences(addedRoute);
                                if(connect)
                                    routeStore.add(addedRoute);
                            }
                        });
            }
            
        }, deviceStore, geoFenceStore).show();
    }
    
    private void updateGeofences(final Route addedRoute) {
        for(RoutePoint pt : addedRoute.getRoutePoints()) {
            if(pt.getGeofence().getTransferDevices() != null)
                pt.getGeofence().setDevices(new HashSet<>(
                        pt.getGeofence().getTransferDevices()));
            String key = Long.toString(pt.getGeofence().getId());
            if(geoFenceStore.findModelWithKey(key) == null) {
                geoFenceStore.add(pt.getGeofence());
            }
        }
        if(addedRoute.getCorridor() != null) {
            addedRoute.getCorridor().setDevices(new HashSet<Device>(
                    addedRoute.getCorridor().getTransferDevices()));
            geoFenceStore.add(addedRoute.getCorridor());
        }
        geoFenceStore.applySort(false);
    }

    
    @Override
    public void onRemove(final Route route) {
        final ConfirmMessageBox dialog = new ConfirmMessageBox(i18n.confirm(), i18n.confirmRouteRemoval());
        dialog.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
            @Override
            public void onDialogHide(DialogHideEvent event) {
                if (event.getHideButton() == PredefinedButton.YES) {
                    Application.getDataService().removeRoute(route,
                        new BaseAsyncCallback<Route>(i18n) {
                            @Override
                            public void onSuccess(final Route removed) {
                                routeStore.remove(route);
                            }
                        });
                }
            }
        });
        dialog.show();
    }
    
    @Override
    public void onSelected(final Route route) {
        mapController.selectRoute(route);
    }
    
    
    
    public ListStore<Route> getStore() {
        return routeStore;
    }

    @Override
    public ContentPanel getView() {
        throw new UnsupportedOperationException();
    }
    
    public static interface RouteMapper extends ObjectMapper<List<ApiRoute>> {}
    @Override
    public void run() {
        final RouteMapper mapper = GWT.create(RouteMapper.class);
        service.getRoutes(new ApiJsonCallback(i18n) {
            @Override
            public void onSuccess(Method method, JSONValue response) {
                List<ApiRoute> routes = mapper.read(response.toString());
                geoFenceStore.setEnableFilters(false);
                for(ApiRoute ar : routes)
                    routeStore.add(ar.toRoute(geoFenceStore.getAll(), 
                            deviceStore.getAll()));
                geoFenceStore.setEnableFilters(true);
            }
            
            
        });
    }

    @Override
    public void onAbort(Route selectedItem) {
        selectedItem.setStatus(Route.Status.CANCELLED);
        selectedItem.setCancelTimestamp(new Date());
        Application.getDataService().updateRoute(selectedItem, new BaseAsyncCallback<Route>(i18n) {
            @Override
            public void onSuccess(Route result) {
                routeStore.update(result);
            }
        });
    }
    
    @Override
    public void onArchivedChanged(Route selectedItem, boolean archive) {
        selectedItem.setArchived(archive);
        if(!archive)
            selectedItem.setArchiveAfter(0);
        Application.getDataService().updateRoute(selectedItem, 
                new BaseAsyncCallback<Route> (i18n) {
            @Override
            public void onSuccess(Route result) {
                if(result.isArchived()) {
                    Route r = routeStore.findModel(result);
                    routeStore.remove(r);
                } else
                    routeStore.add(result);
            }         
        });
    }
    
    @Override
    public void onShowArchived() {
        Application.getDataService().getArchivedRoutes(new BaseAsyncCallback<List<Route>>(i18n) {
            @Override
            public void onSuccess(List<Route> result) {
                ListStore<Route> routes = new ListStore<>(new ModelKeyProvider<Route>() {
                    @Override
                    public String getKey(Route item) {
                        return Long.toString(item.getId());
                    }
                });
                routes.addAll(result);
                
                ArchivedRoutesDialog dialog = new ArchivedRoutesDialog(routes, 
                        RouteController.this);
                dialog.show();
            }
        });
    }

    @Override
    public void onRoutesUpdated(List<ApiRoute> apiRoutes) {
        List<Route> routes = new ArrayList<>();
        geoFenceStore.setEnableFilters(false);
        for(ApiRoute ar : apiRoutes)
            routes.add(ar.toRoute(geoFenceStore.getAll(), deviceStore.getAll()));
        geoFenceStore.setEnableFilters(true);
        routeStore.replaceAll(routes);
    }

    @Override
    public void onShowReport(Route route) {
        String name = route.getName();
        
        Report report = new Report();
        report.setType(ReportType.TRACK);
        report.setName(name);
        report.setDevices(Collections.singleton(route.getDevice()));
        report.setRoute(route);
        report.setFromDate(new Date(0));
        report.setToDate(new Date());
        report.setPreview(true);
        report.setFormat(ReportFormat.HTML);
        reportHandler.generate(report);
    }
}
