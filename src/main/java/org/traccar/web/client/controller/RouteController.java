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
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.controller.UpdatesController.RoutesListener;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.api.ApiEditRoute;
import org.traccar.web.client.model.api.ApiJsonCallback;
import org.traccar.web.client.model.api.ApiRoute;
import org.traccar.web.client.model.api.ApiRoute.ApiRoutePoint;
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
    public static interface LRouteMapper extends ObjectMapper<List<ApiRoute>> {}
    public static interface RouteMapper extends ObjectMapper<ApiRoute> {}
    
    private ListStore<Device> deviceStore;
    private ListStore<GeoFence> geoFenceStore;
    private MapController mapController;
    private GeoFenceController gfController;
    private ReportsController reportHandler;
    private final ListStore<Route> routeStore;
    private Messages i18n = GWT.create(Messages.class);
    private RoutesService service = GWT.create(RoutesService.class);
    private LRouteMapper lMapper = GWT.create(LRouteMapper.class);
    private RouteMapper mapper = GWT.create(RouteMapper.class);
    
    public RouteController(ListStore<Device> devStore, GeoFenceController gfController,
            MapController mapController) {
        this.deviceStore = devStore;
        this.geoFenceStore = gfController.getGeoFenceStore();
        this.mapController = mapController;
        this.gfController = gfController;
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
            public void onSave(final Route route) {
                service.createRoute(new ApiEditRoute(route), new ApiJsonCallback(i18n) {
                    @Override
                    public void onSuccess(Method method, JSONValue response) {
                        ApiRoute r = mapper.read(response.toString());
                        updateRoute(route, r);
                        updateGeofences(route);
                        routeStore.add(route);
                    }
                });
            }
            
        }, deviceStore, geoFenceStore, ApplicationContext.getInstance().getUser(),
                mapController.getCenter()).show();
    }
    
    @Override
    public void onEdit(final Route selectedItem) {
        new RouteDialog(selectedItem, new RouteDialog.RouteHandler() {
            @Override
            public void onSave(final Route route) {
                service.updateRoute(route.getId(), new ApiEditRoute(route),
                        new ApiJsonCallback(i18n) {
                    @Override
                    public void onSuccess(Method method, JSONValue response) {
                        if(selectedItem.getCorridor() != null)
                            geoFenceStore.remove(selectedItem.getCorridor());
                        ApiRoute r = mapper.read(response.toString());
                        updateRoute(route, r);
                        updateGeofences(route);
                        routeStore.update(route);
                        gfController.geoFenceRemoved(selectedItem.getCorridor());
                        gfController.geoFenceAdded(selectedItem.getCorridor());
                        Device d = selectedItem.getCorridor().getDevices().iterator().next();
                        log(d.getId()+":"+gfController.getDeviceGeoFences().get(d.getId()).size());
                    } 
                        });
            }
            
        }, deviceStore, geoFenceStore, ApplicationContext.getInstance().getUser()).show();
    }
    
    public static native void log(String msg) /*-{
        console.log(msg);
    }-*/;
    
    private void updateRoute(Route route, ApiRoute r) {
        if(route.getId() == 0)
            route.setId(r.id);
        List<RoutePoint> rps = route.getRoutePoints();
        for(int i=0;i<rps.size();++i) {
            GeoFence gf = rps.get(i).getGeofence();
            ApiRoutePoint pt = r.points.get(i);
            if(gf.getId() == 0)
                gf.setId(pt.geofenceId);
            if(rps.get(i).getId() == 0)
                rps.get(i).setId(pt.id);
        }
        if(route.getCorridor() != null && route.getCorridor().getId() == 0)
            route.getCorridor().setId(r.corridorId);
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
            public void onSave(final Route route) {
                service.createRoute(new ApiEditRoute(route), new ApiJsonCallback(i18n) {
                    @Override
                    public void onSuccess(Method method, JSONValue response) {
                        ApiRoute r = mapper.read(response.toString());
                        updateRoute(route, r);
                        updateGeofences(route);
                        routeStore.add(route);
                    }
                });
            }
            
        }, deviceStore, geoFenceStore, ApplicationContext.getInstance().getUser()).show();
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
        geoFenceStore.applySort(false);
    }

    
    @Override
    public void onRemove(final Route route) {
        final ConfirmMessageBox dialog = new ConfirmMessageBox(i18n.confirm(), i18n.confirmRouteRemoval());
        dialog.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
            @Override
            public void onDialogHide(DialogHideEvent event) {
                if (event.getHideButton() == PredefinedButton.YES) {
                    service.deleteRoute(route.getId(), new ApiJsonCallback(i18n) {
                        @Override
                        public void onSuccess(Method method, JSONValue response) {
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
    
    @Override
    public void run() {
        service.getRoutes(false, new ApiJsonCallback(i18n) {
            @Override
            public void onSuccess(Method method, JSONValue response) {
                List<ApiRoute> routes = lMapper.read(response.toString());
                geoFenceStore.setEnableFilters(false);
                for(ApiRoute ar : routes)
                    routeStore.add(ar.toRoute(geoFenceStore.getAll(), 
                            deviceStore.getAll()));
                geoFenceStore.setEnableFilters(true);
            }
        });
    }

    @Override
    public void onAbort(final Route selectedItem) {
        selectedItem.setStatus(Route.Status.CANCELLED);
        selectedItem.setCancelTimestamp(new Date());
        service.updateRoute(selectedItem.getId(), new ApiEditRoute(selectedItem),
                new ApiJsonCallback(i18n) {
            @Override
            public void onSuccess(Method method, JSONValue response) {
                routeStore.update(selectedItem);
            }
            
        });
    }
    
    @Override
    public void onArchivedChanged(final Route selectedItem, boolean archive) {
        selectedItem.setArchived(archive);
        if(selectedItem.getStatus() == Route.Status.NEW
                && selectedItem.getDevice().getSubscriptionDaysLeft(new Date()) == 0)
            selectedItem.setDevice(null);
        service.updateRoute(selectedItem.getId(), new ApiEditRoute(selectedItem),
                new ApiJsonCallback(i18n) {
            @Override
            public void onSuccess(Method method, JSONValue response) {
                if(selectedItem.isArchived()) {
                    Route r = routeStore.findModel(selectedItem);
                    routeStore.remove(r);
                } else
                    routeStore.add(selectedItem);
            }
                    
                });
    }
    
    @Override
    public void onShowArchived() {
        service.getRoutes(true, new ApiJsonCallback(i18n) {
            @Override
            public void onSuccess(Method method, JSONValue response) {
                ListStore<Route> store = new ListStore<>(new ModelKeyProvider<Route>() {
                    @Override
                    public String getKey(Route item) {
                        return Long.toString(item.getId());
                    }
                });
                List<ApiRoute> routes = lMapper.read(response.toString());
                geoFenceStore.setEnableFilters(false);
                for(ApiRoute ar : routes)
                    store.add(ar.toRoute(geoFenceStore.getAll(), 
                            deviceStore.getAll()));
                geoFenceStore.setEnableFilters(true);
                ArchivedRoutesDialog dialog = new ArchivedRoutesDialog(store, 
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
