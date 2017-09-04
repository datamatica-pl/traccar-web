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
package org.traccar.web.client.view;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.data.shared.event.StoreAddEvent;
import com.sencha.gxt.data.shared.event.StoreClearEvent;
import com.sencha.gxt.data.shared.event.StoreDataChangeEvent;
import com.sencha.gxt.data.shared.event.StoreFilterEvent;
import com.sencha.gxt.data.shared.event.StoreHandlers;
import com.sencha.gxt.data.shared.event.StoreRecordChangeEvent;
import com.sencha.gxt.data.shared.event.StoreRemoveEvent;
import com.sencha.gxt.data.shared.event.StoreSortEvent;
import com.sencha.gxt.data.shared.event.StoreUpdateEvent;
import com.sencha.gxt.dnd.core.client.DND;
import com.sencha.gxt.dnd.core.client.GridDragSource;
import com.sencha.gxt.dnd.core.client.GridDropTarget;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.event.BeforeStartEditEvent;
import com.sencha.gxt.widget.core.client.event.BeforeStartEditEvent.BeforeStartEditHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.StringComboBox;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.event.MapClickListener;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.LineString;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.layer.OSM;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.Style;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.utils.Geocoder;
import org.traccar.web.client.utils.Geocoder.SearchCallback;
import org.traccar.web.client.utils.RoutePolylineFinder;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.GeoFenceType;
import pl.datamatica.traccar.model.Route;
import pl.datamatica.traccar.model.RoutePoint;
import pl.datamatica.traccar.model.UserSettings;

public class RouteDialog implements GeoFenceRenderer.IMapView {
    interface _UiBinder extends UiBinder<Widget, RouteDialog> {}
    private static _UiBinder uiBinder = GWT.create(_UiBinder.class);
    private static Resources R = GWT.create(Resources.class);
    private static RoutePointAccessor pointsAccessor = GWT.create(RoutePointAccessor.class);
    
    @UiField
    Window window;
    @UiField
    SimpleContainer theMap;
    @UiField
    CheckBox connect;
    @UiField
    TextField name;
    @UiField(provided = true)
    Grid<RoutePointWrapper> grid;
    @UiField
    FieldLabel selectDeviceLabel;
    @UiField(provided = true)
    ComboBox<Device> selectDevice;
    @UiField
    FieldLabel trackNameLabel;
    
    @UiField
    TextButton addButton;
    
    @UiField
    CheckBox createCorridor;
    @UiField(provided = true)
    NumberField<Integer> corridorWidth;
    
    Messages i18n = GWT.create(Messages.class);
    
    private final Route route;
    ListStore<RoutePointWrapper> store;
    GridEditing<RoutePointWrapper> edit;
    final RouteHandler routeHandler;
    Vector gfLayer;
    VectorFeature polyline;
    GeoFenceRenderer gfRenderer;
    boolean recomputingPath = false;
    LonLat[] lineString;
    RoutePolylineFinder.Callback routeDrawer = new RoutePolylineFinder.Callback() {
        @Override
        public void onResult(LonLat[] points) {
            lineString = points;
            Style st = new Style();
            st.setStrokeWidth(4);
            ArrayList<Point> linePoints = new ArrayList<>();
            for(LonLat pt : points) {
                linePoints.add(createPoint(pt.lon(), pt.lat()));
            }
            LineString ls = new LineString(linePoints.toArray(new Point[0]));

            polyline = new VectorFeature(ls, st);
            gfLayer.addFeature(polyline);
            endComputingPath();
        }
    };
    
    private org.gwtopenmaps.openlayers.client.Map map;
    
    public RouteDialog(Route route, final RouteHandler routeHandler, 
            ListStore<Device> devs, ListStore<GeoFence> gfs) {
        this.route = route;
        this.routeHandler = routeHandler;
        store = new ListStore<>(pointsAccessor.id());
        
        prepareGrid(gfs);
        selectDevice = new ComboBox(devs, new LabelProvider<Device>() {
            @Override
            public String getLabel(Device item) {
                return item.getName();
            }
        });
        selectDevice.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        corridorWidth = new NumberField<>(new NumberPropertyEditor.IntegerPropertyEditor());
        corridorWidth.addValidator(new MaxNumberValidator<>(5000));
        corridorWidth.addValidator(new MinNumberValidator<>(20));
        uiBinder.createAndBindUi(this);
        
        connect.setValue(true);
        //editing!
        if(route.getId() != 0)
            connect.setEnabled(false);
        else
            route.setStatus("NOWA");
        connect.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if(event.getValue() == null) 
                    return;
                trackNameLabel.setEnabled(event.getValue());
                selectDeviceLabel.setEnabled(event.getValue());
                drawPolyline();
                createCorridor.setEnabled(event.getValue());
                createCorridor.setValue(false);
                if(!event.getValue())
                    corridorWidth.setEnabled(false);
            }
            
        });
        createCorridor.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                corridorWidth.setEnabled(event.getValue());
            }
        });
        
        name.setValue(route.getName());
        ArrayList<RoutePointWrapper> pts = new ArrayList<>();
        for(RoutePoint rp : route.getRoutePoints())
            pts.add(new RoutePointWrapper(rp));
        store.addAll(pts);
        if(route.getDevice() != null)
            selectDevice.setValue(route.getDevice());
        if(route.getCorridor() != null) {
            createCorridor.setValue(true);
            corridorWidth.setValue((int)route.getCorridor().getRadius());
            corridorWidth.setEnabled(true);
        }
        
        prepareMap();
        bindStoreWithMap(route);
        
        prepareDND();
    }

    private void prepareGrid(ListStore<GeoFence> gfs) {
        List<ColumnConfig<RoutePointWrapper, ?>> ccList = new ArrayList<>();
        ColumnConfig<RoutePointWrapper, String> cName = new ColumnConfig<>(
                pointsAccessor.name(), 109, i18n.name());
        ccList.add(cName);
        ColumnConfig<RoutePointWrapper, String> cAddress = new ColumnConfig<>(
                pointsAccessor.address(), 172, i18n.address());
        ccList.add(cAddress);
        ColumnConfig<RoutePointWrapper, Integer> cRadius = new ColumnConfig<>(
                pointsAccessor.radius(), 35, i18n.radius());
        ccList.add(cRadius);
        ColumnConfig<RoutePointWrapper, ImageResource> cDelete = new ColumnConfig<>(
                new ValueProvider<RoutePointWrapper, ImageResource>() {
                    @Override
                    public ImageResource getValue(RoutePointWrapper object) {
                        return R.remove();
                    }

                    @Override
                    public void setValue(RoutePointWrapper object, ImageResource value) {
                    }

                    @Override
                    public String getPath() {
                        return "delete";
                    }
                }, 24, "");
        cDelete.setCell(new ImageResourceCell() {
            @Override
            public Set<String> getConsumedEvents() {
                return Collections.singleton("click");
            }
            
            
            @Override
            public void onBrowserEvent(Cell.Context context, Element parent, ImageResource value,
                    NativeEvent event, ValueUpdater<ImageResource> valueUpdater) {
                super.onBrowserEvent(context, parent, value, event, valueUpdater);
                store.remove(context.getIndex());
            }
            
        });
        ccList.add(cDelete);
        ColumnConfig<RoutePointWrapper, String> cStatus = new ColumnConfig<>(
                new ValueProvider<RoutePointWrapper, String>() {
                    @Override
                    public String getValue(RoutePointWrapper object) {
                        return object.isLoading() ? "L" : "";
                    }

                    @Override
                    public void setValue(RoutePointWrapper object, String value) {
                    }

                    @Override
                    public String getPath() {
                        return "status";
                    } 
                }, 24, "");
        ccList.add(cStatus);
        ColumnModel<RoutePointWrapper> cm = new ColumnModel<>(ccList);
        grid = new Grid<>(store, cm);
        
        final TextField addr = new TextField();
        final RegExp latLonPatt = RegExp.compile(
                "(\\d+(\\.\\d+)?)([NS])\\s*(\\d+(\\.\\d+)?)([WE])");
        addr.addValueChangeHandler(new ValueChangeHandler<String>(){
            @Override
            public void onValueChange(final ValueChangeEvent<String> event) {
                store.commitChanges();
                final RoutePointWrapper p = grid.getSelectionModel().getSelectedItem();
                
                MatchResult m = latLonPatt.exec(event.getValue());
                if(m == null) {
                    Geocoder.search(event.getValue(), new SearchCallback() {
                        @Override
                        public void onResult(float lon, float lat, String name) {
                            GeoFence gf = p.getRoutePoint().getGeofence();
                            p.setLoading(false);
                            gf.setPoints(lon+" "+lat);
                            gf.setAddress(event.getValue());
                            if(gf.getName() == null || gf.getName().isEmpty())
                                gf.setName(name);
                            store.update(p);
                        }
                    });
                } else {
                    double lat = Double.parseDouble(m.getGroup(1)) * 
                            (m.getGroup(3).equals("S") ? -1 : 1);
                    double lon = Double.parseDouble(m.getGroup(4)) *
                            (m.getGroup(6).equals("W") ? -1 : 1);
                    p.setLonLat(lon, lat);
                    store.update(p);
                }
            }
        });
        final NumberField rad = new NumberField(new NumberPropertyEditor.IntegerPropertyEditor());
        rad.addValidator(new MaxNumberValidator<>(1500));
        rad.addValidator(new MinNumberValidator<>(300));
        
        edit = new GridInlineEditing<>(grid);
        
        List<String> gfNames = new ArrayList<>();
        final Map<String, GeoFence> gfMap= new HashMap<>();
        for(GeoFence gf : gfs.getAll()) {
            if(!gf.isDeleted() && !gfMap.containsKey(gf.getName())) {
               gfNames.add(gf.getName());
               gfMap.put(gf.getName(), gf);
            }
        }
        StringComboBox cbName = new StringComboBox(gfNames);
        cbName.addValueChangeHandler(new ValueChangeHandler<String>(){
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                store.commitChanges();
                RoutePointWrapper p = grid.getSelectionModel().getSelectedItem();
                p.setGeofence(gfMap.get(event.getValue()));
                store.update(p);
            }
        });
        cbName.setForceSelection(false);

        edit.addEditor(cName, cbName);
        edit.addEditor(cAddress, addr);
        edit.addEditor(cRadius, rad);
        
        edit.addBeforeStartEditHandler(new BeforeStartEditHandler<RoutePointWrapper>() {
            @Override
            public void onBeforeStartEdit(BeforeStartEditEvent<RoutePointWrapper> event) {
                RoutePointWrapper pt = store.get(event.getEditCell().getRow());
                if(pt.isLoading())
                    event.setCancelled(true);
                if(event.getEditCell().getCol() != 0 && !pt.isEditable())
                    event.setCancelled(true);
                if(recomputingPath)
                    event.setCancelled(true);
                gfRenderer.selectGeoFence(pt.getRoutePoint().getGeofence());
            }
        });
    }
    
    private void prepareMap() {
        MapOptions mapOptions = new MapOptions();
        mapOptions.setMaxExtent(new Bounds(-20037508.34, -20037508.34, 20037508.34, 20037508.34));
        MapWidget mapWidget = new MapWidget("100%", "100%", mapOptions);
        map = mapWidget.getMap();
        
        map.addLayer(OSM.Mapnik("OpenStreetMap"));
        gfLayer = new Vector(i18n.overlayType(UserSettings.OverlayType.GEO_FENCES));
        map.addLayer(gfLayer);
        
        LonLat center = new LonLat(19, 52);
        center.transform("EPSG:4326", map.getProjection());
        map.setCenter(center, 7);
        map.addMapClickListener(new MapClickListener() {
            @Override
            public void onClick(MapClickListener.MapClickEvent ev) {
                if(recomputingPath)
                    return;
                LonLat ll = ev.getLonLat();
                ll.transform(map.getProjection(), "EPSG:4326");
                onPointSelected(ll);
            } 
        });
        theMap.add(mapWidget);
    }
    
    private void bindStoreWithMap(Route route) {
        gfRenderer = new GeoFenceRenderer(this);
        store.addStoreHandlers(new StoreHandlers<RoutePointWrapper>() {
            @Override
            public void onAdd(StoreAddEvent<RoutePointWrapper> event) {
                for(RoutePointWrapper pt : event.getItems()) {
                    GeoFence gf = pt.getRoutePoint().getGeofence();
                    if(!gf.points().isEmpty())
                        gfRenderer.drawGeoFence(gf, true);
                }
                drawPolyline();
            }

            @Override
            public void onRemove(StoreRemoveEvent<RoutePointWrapper> event) {
                GeoFence gf = event.getItem().getRoutePoint().getGeofence();
                gfRenderer.removeGeoFence(gf);
                drawPolyline();
            }

            @Override
            public void onFilter(StoreFilterEvent<RoutePointWrapper> event) {
            }

            @Override
            public void onClear(StoreClearEvent<RoutePointWrapper> event) {
            }

            @Override
            public void onUpdate(StoreUpdateEvent<RoutePointWrapper> event) {
                updateAll(event.getItems());
                drawPolyline();
            }

            @Override
            public void onDataChange(StoreDataChangeEvent<RoutePointWrapper> event) {
            }

            @Override
            public void onRecordChange(StoreRecordChangeEvent<RoutePointWrapper> event) {
            }

            @Override
            public void onSort(StoreSortEvent<RoutePointWrapper> event) {
            }
            
            private void updateAll(List<RoutePointWrapper> list) {
                for(RoutePointWrapper pt : list) {
                    GeoFence gf = pt.getRoutePoint().getGeofence();
                    gfRenderer.removeGeoFence(gf);
                    if(!gf.points().isEmpty())
                        gfRenderer.drawGeoFence(gf, true);
                }
            }
        });
        pl.datamatica.traccar.model.GeoFence.LonLat[] pts = route.getLinePoints();
        if(pts == null)
            return;
        lineString = new LonLat[pts.length];
        for(int i=0;i<pts.length;++i)
            lineString[i] = new LonLat(pts[i].lon, pts[i].lat);
        for(RoutePointWrapper pt : store.getAll()) {
            GeoFence gf = pt.getRoutePoint().getGeofence();
            if(!gf.points().isEmpty())
                gfRenderer.drawGeoFence(gf, true);
        }
        if(route.getCorridor() != null)
            gfRenderer.drawGeoFence(route.getCorridor(), false);
        routeDrawer.onResult(lineString);
    }
    
    private void prepareDND() {
        GridDragSource<RoutePointWrapper> dragSource = new GridDragSource<>(grid);
        GridDropTarget<RoutePointWrapper> dropTarget = new GridDropTarget<>(grid);
        dropTarget.setAllowSelfAsSource(true);
        dropTarget.setFeedback(DND.Feedback.BOTH);
    }
    
    private void drawPolyline() {
        //drag'n'drop!
        if(recomputingPath)
            return;
        startComputingPath();
        if(polyline != null) {
            gfLayer.removeFeature(polyline);
            polyline.destroy();
            polyline = null;
        }
        List<LonLat> pts = new ArrayList<>();
        for(RoutePointWrapper pt : store.getAll()) {
            LonLat center = pt.getCenter();
            if(center == null)
                continue;         
            pts.add(center);
        }
        if(pts.size() < 2 || !connect.getValue()) {
            endComputingPath();
            return;
        }
        
        RoutePolylineFinder.find(pts, routeDrawer);
    }
    
    private void startComputingPath() {
        recomputingPath = true;
        addButton.setEnabled(!recomputingPath);
    }
    
    private void endComputingPath() {
        recomputingPath = false;
        addButton.setEnabled(!recomputingPath);
    }
    
    @Override
    public org.gwtopenmaps.openlayers.client.Map getMap() {
        return map;
    }

    @Override
    public Point createPoint(double longitude, double latitude) {
        Point point = new Point(longitude, latitude);
        point.transform(new Projection("EPSG:4326"), new Projection(map.getProjection()));
        return point;
    }

    @Override
    public Vector getGeofenceLayer() {
        return gfLayer;
    }
    
    public void show() {
        window.show();
    }
    
    @UiHandler("addButton")
    public void add(SelectEvent selectEvent) {
        store.add(new RoutePointWrapper());
        edit.startEditing(new Grid.GridCell(store.size()-1, 0));
        grid.getSelectionModel().select(store.size()-1, false);
    }
    
    public void onPointSelected(LonLat lonLat) {
        RoutePointWrapper pt = new RoutePointWrapper();
        pt.setLonLat(lonLat.lon(), lonLat.lat());
        store.add(pt);
    }
    
    @UiHandler("saveButton")
    public void save(SelectEvent selectEvent) {
        store.commitChanges();
        if(!validate())
            return;
        
        route.getRoutePoints().clear();
        for(RoutePointWrapper rp : store.getAll())
            route.getRoutePoints().add(rp.getRoutePoint());
        if(connect.getValue()) {
            if(createCorridor.getValue() && !corridorWidth.validate())
                return;
            
            route.setName(name.getValue());
            route.setDevice(selectDevice.getCurrentValue());
            pl.datamatica.traccar.model.GeoFence.LonLat[] gll = 
                    new pl.datamatica.traccar.model.GeoFence.LonLat[lineString.length];
            for(int i=0;i<lineString.length;++i)
                gll[i] = new pl.datamatica.traccar.model.GeoFence.LonLat(lineString[i].lon(),
                    lineString[i].lat());
            route.setLinePoints(gll);
            
            if(createCorridor.getValue()) {
                GeoFence corridor;
                if(route.getCorridor() != null) {
                    corridor = route.getCorridor();
                } else {
                    corridor = new GeoFence();
                }
                corridor.setName(name.getValue()+"_c");
                corridor.setDescription(i18n.corridorOfRoute(name.getValue()));
                corridor.setType(GeoFenceType.LINE);
                corridor.points(gll);
                corridor.setRadius(corridorWidth.getValue().floatValue());
                corridor.setTransferDevices(new HashSet<Device>());
                if(route.getDevice() != null)
                    corridor.getTransferDevices().add(route.getDevice());
                route.setCorridor(corridor);
            } else {
                route.setCorridor(null);
            }
        }
        
        routeHandler.onSave(route, connect.getValue());
        window.hide();
    }
    
    private boolean validate() {
        if(connect.getValue()) {
            if(name.getValue() == null || name.getValue().isEmpty()) {
                new AlertMessageBox(i18n.error(), i18n.errNoRouteName()).show();
                return false;
            }
            if(store.size() < 2) {
                new AlertMessageBox(i18n.error(), i18n.errNotEnoughRoutePoints()).show();
                return false;
            }
            if(createCorridor.getValue() && corridorWidth.getValue() == null) {
                new AlertMessageBox(i18n.error(), i18n.errNoCorridorRadius()).show();
            }
        } else if(store.size() < 1) {
            new AlertMessageBox(i18n.error(), i18n.errNoGeoFences()).show();
            return false;
        }
        return true;
    }
    
    @UiHandler("cancelButton")
    public void cancel(SelectEvent selectEvent) {
       window.hide(); 
    }
    
    
    public static interface RouteHandler {
        void onSave(Route route, boolean connect);
    }
    
    static class RoutePointWrapper {        
        private int id;
        private RoutePoint pt;
        private boolean loading;
        private static int ID_GEN = 0;
        private static final Messages i18n = GWT.create(Messages.class);
        
        public RoutePointWrapper() {
            id = ID_GEN++;
            pt = new RoutePoint();
            pt.setGeofence(createGF("", 300));
        }
        
        public RoutePointWrapper(RoutePoint pt) {
            id = ID_GEN++;
            this.pt = pt;
            loading = false;
        }
        
        
        public int getId() {
            return id;
        }
        
        public String getName() {
            return pt.getGeofence().getName();
        }
        
        public void setName(String name) {
            pt.getGeofence().setName(name);
        }
        
        public String getAddress() {
            return pt.getGeofence().getAddress();
        }
        
        public void setAddress(String address) {
            pt.getGeofence().setAddress(address);
        }
        
        public int getRadius() {
            return (int)pt.getGeofence().getRadius();
        }
        
        public void setRadius(int radius) {
            pt.getGeofence().setRadius(radius);
        }
        
        public RoutePoint getRoutePoint() {
            return pt;
        }
        
        public void setGeofence(GeoFence gf) {
            if(gf == null) {
                if(pt.getGeofence().getId() != 0)
                    pt.setGeofence(createGF(pt.getGeofence().getName(),
                                   pt.getGeofence().getRadius()));
            } else
                pt.setGeofence(gf);
        }
        
        public void setLonLat(double lon, double lat) {
            GeoFence geofence = pt.getGeofence();
            geofence.setPoints(lon+" "+lat);
            if(geofence.getAddress() == null || geofence.getAddress().isEmpty()) {
                String latDir = lat < 0 ? "S" : "N";
                String lonDir = lon < 0 ? "W" : "E";
                geofence.setAddress(i18n.latLonFormat(
                        Math.round(Math.abs(lat)*1e3)/1e3, latDir, 
                        Math.round(Math.abs(lon)*1e3)/1e3, lonDir));
            }
            if(geofence.getName() == null || geofence.getName().isEmpty())
                geofence.setName("pkt_"+(int)Math.abs(lon*10)+"_"+(int)Math.abs(lat*10));
        }
        
        public boolean isEditable() {
            return pt.getGeofence().getId() == 0;
        }
        
        public boolean isLoading() {
            return loading;
        }
        
        public void setLoading(boolean loading) {
            this.loading = loading;
        }
        
        public LonLat getCenter() {
            if(pt.getGeofence().points().isEmpty())
                return null;
            List<GeoFence.LonLat> points = pt.getGeofence().points();
            double avgLon = 0, avgLat = 0;
            for(GeoFence.LonLat p : points) {
                avgLon += p.lon;
                avgLat += p.lat;
            }
            avgLon/=points.size();
            avgLat/=points.size();
            return new LonLat(avgLon, avgLat);
        }
        
        private GeoFence createGF(String name, float radius) {
            GeoFence gf = new GeoFence();
            gf.setName(name);
            gf.setTransferDevices(Collections.EMPTY_SET);
            gf.setType(GeoFenceType.CIRCLE);
            gf.setRadius(radius);
            return gf;
        }
    }
    
    interface RoutePointAccessor extends PropertyAccess<RoutePointWrapper> {
        ModelKeyProvider<RoutePointWrapper> id();
        ValueProvider<RoutePointWrapper, String> name();
        ValueProvider<RoutePointWrapper, String> address();
        ValueProvider<RoutePointWrapper, Integer> radius();
    }
    
    static interface Resources extends ClientBundle {
        @ClientBundle.Source("org/traccar/web/client/theme/icon/remove.png")
        ImageResource remove();
        @ClientBundle.Source("org/traccar/web/client/theme/icon/arrow_up.png")
        ImageResource arrowUp();
        @ClientBundle.Source("org/traccar/web/client/theme/icon/arrow_down.png")
        ImageResource arrowDown();
    }
}
