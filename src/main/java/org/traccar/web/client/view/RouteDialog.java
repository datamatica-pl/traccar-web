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

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
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
import com.sencha.gxt.dnd.core.client.DndDragStartEvent;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent.DndDragStartHandler;
import com.sencha.gxt.dnd.core.client.GridDragSource;
import com.sencha.gxt.dnd.core.client.GridDropTarget;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.event.BeforeStartEditEvent;
import com.sencha.gxt.widget.core.client.event.BeforeStartEditEvent.BeforeStartEditHandler;
import com.sencha.gxt.widget.core.client.event.CompleteEditEvent;
import com.sencha.gxt.widget.core.client.event.CompleteEditEvent.CompleteEditHandler;
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
import java.util.Date;
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
import org.gwtopenmaps.openlayers.client.control.ModifyFeature;
import org.gwtopenmaps.openlayers.client.control.ModifyFeatureOptions;
import org.gwtopenmaps.openlayers.client.event.EventHandler;
import org.gwtopenmaps.openlayers.client.event.EventObject;
import org.traccar.web.client.GeoFenceDrawing;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.utils.Geocoder;
import org.traccar.web.client.utils.Geocoder.SearchCallback;
import org.traccar.web.client.utils.PolylineDecoder;
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
    public static final String GF_PROJECTION = "EPSG:4326";
    
    @UiField
    Window window;
    @UiField
    SimpleContainer theMap;
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
    Label routeLength;
    
    @UiField
    TextButton addButton;
    
    @UiField
    NumberField<Integer> tolerance;
    @UiField
    NumberField<Integer> archiveAfter;
    
    @UiField
    CheckBox createCorridor;
    @UiField
    NumberField<Integer> corridorWidth;    
    
    @UiField(provided = true)
    NumberPropertyEditor<Integer> integerPropertyEditor = new NumberPropertyEditor.IntegerPropertyEditor();
    Messages i18n = GWT.create(Messages.class);
    
    private final Route route;
    ListStore<RoutePointWrapper> store;
    GridEditing<RoutePointWrapper> edit;
    final RouteHandler routeHandler;
    Vector gfLayer;
    VectorFeature polyline;
    GeoFenceRenderer gfRenderer;
    boolean recomputingPath = false;
    boolean ignoreUpdate = false;
    boolean pathInvalid = false;
    private Date previousDeadline;
    private String previousName;
    StringComboBox cbName;
    private VectorFeature modifiedFeature;
    private RoutePointWrapper modifiedPt;
    String lineString;
    RoutePolylineFinder.Callback routeDrawer = new RoutePolylineFinder.Callback() {
        @Override
        public void onResult(String points, double[] distances) {
            lineString = points;
            Style st = new Style();
            st.setStrokeWidth(4);
            LineString ls = PolylineDecoder.decode(RouteDialog.this, points);

            polyline = new VectorFeature(ls, st);
            gfLayer.addFeature(polyline);
            
            List<RoutePointWrapper> rpws = new ArrayList<>(store.getAll());
            if(!rpws.get(0).getForced())
                rpws.remove(0);
            if(!rpws.get(rpws.size()-1).getForced())
                rpws.remove(rpws.size()-1);
            Date deadline = null;
            if(!rpws.isEmpty()) {
                deadline = rpws.get(0).getDeadline();
                if(deadline == null) {
                    deadline = new Date();
                    rpws.get(0).setDeadline(deadline);
                }
            }
            if(deadline != null && distances != null) {
                double totalDistance = 0;
                for(int i=1;i < rpws.size();++i) {
                    if(distances.length > i-1) {
                        deadline = new Date(deadline.getTime() + (long)(distances[i-1]/1000)*60*1000);
                        totalDistance += distances[i-1]/1000;
                    }
                    if(rpws.get(i).getDeadline() == null)
                        rpws.get(i).setDeadline(deadline);
                    ignoreUpdate = true;
                    store.update(rpws.get(i));
                    ignoreUpdate = false;
                }
                routeLength.setText(i18n.routeLength(totalDistance));
            }
            endComputingPath();
        }
    };
    
    private org.gwtopenmaps.openlayers.client.Map map;
    private ModifyFeature modifyFeature;
    private final Map<String, GeoFence> gfMap= new HashMap<>();
    
    public RouteDialog(Route route, final RouteHandler routeHandler,
            ListStore<Device> devs, ListStore<GeoFence> gfs) {
        this(route, routeHandler, devs, gfs, null);
    }
    
    public RouteDialog(Route route, final RouteHandler routeHandler, 
            ListStore<Device> devs, ListStore<GeoFence> gfs, LonLat center) {
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
        uiBinder.createAndBindUi(this);
        
        //editing!
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
        
        if(route.isForceFirst()) {
            pts.get(0).setForced(true);
        } else {
            RoutePointWrapper rpw = new RoutePointWrapper();
            if(route.getId() == 0)
                rpw.setForced(true);
            pts.add(0, rpw);
        }
        if(route.isForceLast()) {
            pts.get(pts.size()-1).setForced(true);
        } else {
            RoutePointWrapper rpw = new RoutePointWrapper();
            if(route.getId() == 0)
                rpw.setForced(true);
            pts.add(rpw);
        }
        store.addAll(pts);
        if(route.getDevice() != null)
            selectDevice.setValue(route.getDevice());
        
        corridorWidth.addValidator(new MinNumberValidator<>(1));
        corridorWidth.addValidator(new MaxNumberValidator<>(20));
        if(route.getCorridor() != null) {
            createCorridor.setValue(true);
            corridorWidth.setValue((int)(route.getCorridor().getRadius()/1000));
            corridorWidth.setEnabled(true);
        }
        if(route.getId() == 0) {
            createCorridor.setValue(true);
            corridorWidth.setValue(1);
            corridorWidth.setEnabled(true);
        }
        
        tolerance.setValue(route.getTolerance());
        tolerance.addValidator(new MinNumberValidator<>(0));
        tolerance.addValidator(new MaxNumberValidator<>(7200));
        
        archiveAfter.setValue(route.getArchiveAfter());
        archiveAfter.addValidator(new MinNumberValidator<>(0));
        archiveAfter.addValidator(new MaxNumberValidator<>(30));
        
        prepareMap(center);
        bindStoreWithMap(route);
        
        prepareDND();
    }
    
    private void prepareGrid(ListStore<GeoFence> gfs) {
        List<ColumnConfig<RoutePointWrapper, ?>> ccList = new ArrayList<>();
        ColumnConfig<RoutePointWrapper, String> cName = new ColumnConfig<>(
                pointsAccessor.name(), 109, i18n.name());
        cName.setCell(new GridCell<String>(store) {
            @Override
            public void render(Cell.Context context, String value, SafeHtmlBuilder sb) {
                if(context.getIndex() == 0 && "".equals(value))
                    super.render(context, "start point", sb);
                else if(context.getIndex() == store.size()-1 && "".equals(value))
                    super.render(context, "end point", sb);
                else
                    super.render(context, value, sb);
            }
            
        });
        ccList.add(cName);
        
        ColumnConfig<RoutePointWrapper, String> cAddress = new ColumnConfig<>(
                pointsAccessor.address(), 172, i18n.address());
        cAddress.setCell(new GridCell<String>(store));
        ccList.add(cAddress);
        
        ColumnConfig<RoutePointWrapper, Integer> cRadius = new ColumnConfig<>(
                pointsAccessor.radius(), 35, i18n.radius());
        cRadius.setCell(new GridCell<Integer>(store));
        ccList.add(cRadius);
        
        ColumnConfig<RoutePointWrapper, Boolean> cDelete = new ColumnConfig<>(
                new ValueProvider<RoutePointWrapper, Boolean>() {
                    @Override
                    public Boolean getValue(RoutePointWrapper object) {
                        if(object.isDone())
                            return null;
                        return object.getForced();
                    }
                    
                    @Override
                    public void setValue(RoutePointWrapper object, Boolean value) {
                        if(value != null) {
                            boolean val = (Boolean)value;
                            object.setForced(val);
                            store.update(object);
                        }
                    }

                    @Override
                    public String getPath() {
                        return "forced";
                    }
                    
                }, 24, "");
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
        
        ColumnConfig<RoutePointWrapper, Date> cDeadline = new ColumnConfig<>(
                new ValueProvider<RoutePointWrapper, Date>() {
            @Override
            public Date getValue(RoutePointWrapper object) {
                if(object.getDeadline() == null)
                    return new Date();
                return object.getDeadline();
            }

            @Override
            public void setValue(RoutePointWrapper object, Date value) {
                object.setDeadline(value);
            }

            @Override
            public String getPath() {
                return "time";
            }
                    
                }, 175, i18n.deadline());
        cDeadline.setCell(new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT)));
        ccList.add(cDeadline);
        
        ColumnModel<RoutePointWrapper> cm = new ColumnModel<>(ccList);
        grid = new Grid<>(store, cm);
        edit = new GridInlineEditing<>(grid);
        
        cDelete.setCell(new DeleteCell(store, edit));
        
        final TextField addr = new TextField();
        edit.addEditor(cAddress, addr);
        
        final NumberField rad = new NumberField(new NumberPropertyEditor.IntegerPropertyEditor());
        rad.addValidator(new MaxNumberValidator<>(1500));
        rad.addValidator(new MinNumberValidator<>(300));
        edit.addEditor(cRadius, rad);
        rad.addValueChangeHandler(new ValueChangeHandler<Integer>(){
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                RoutePointWrapper rpw = grid.getSelectionModel().getSelectedItem();
                rpw.setRadius(event.getValue());
                store.update(rpw);
            }
        });
        
        List<String> gfNames = new ArrayList<>();
        gfs.setEnableFilters(false);
        for(GeoFence gf : gfs.getAll()) {
            if(!gf.isDeleted() && gf.getType() != GeoFenceType.LINE && !gfMap.containsKey(gf.getName())
                    && gf.getName() != null && !"".equals(gf.getName())) {
               gfNames.add(gf.getName());
               gfMap.put(gf.getName(), gf);
            }
        }
        gfs.setEnableFilters(true);
        cbName = new StringComboBox(gfNames);
        cbName.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        cbName.setForceSelection(false);
        edit.addEditor(cName, cbName);        
        
        final DateTimeField deadline = new DateTimeField();
        edit.addEditor(cDeadline, deadline);
        
        edit.addBeforeStartEditHandler(new BeforeStartEditHandler<RoutePointWrapper>() {
            @Override
            public void onBeforeStartEdit(BeforeStartEditEvent<RoutePointWrapper> event) {
                RoutePointWrapper pt = store.get(event.getEditCell().getRow());
                if(pt.isLoading() || pt.isDone())
                    event.setCancelled(true);
                if(event.getEditCell().getCol() != 0 
                        && event.getEditCell().getCol() != 5 && !pt.isEditable())
                    event.setCancelled(true);
                if(recomputingPath)
                    event.setCancelled(true);
                if(event.getEditCell().getCol() == 0)
                    previousName = pt.getName();
                else if(event.getEditCell().getCol() == 5)
                    previousDeadline = pt.getDeadline();
            }
        });
        
        
        final RegExp latLonPatt = RegExp.compile(
                "(\\d+(\\.\\d+)?)([NS])\\s*(\\d+(\\.\\d+)?)([WE])");
        edit.addCompleteEditHandler(new CompleteEditHandler<RoutePointWrapper>() {
            @Override
            public void onCompleteEdit(CompleteEditEvent<RoutePointWrapper> event) {
                store.commitChanges();
                final RoutePointWrapper p = store.get(event.getEditCell().getRow());
                if(event.getEditCell().getCol() == 0) {
                    onNameEdited(p);
                } else if(event.getEditCell().getCol() == 1) {
                    onAddressEdited(p);
                }
                if(event.getEditCell().getCol() != 5 || previousDeadline == null)
                    return;
                long diff = p.getDeadline().getTime() - previousDeadline.getTime();
                for(int i=event.getEditCell().getRow()+1; i < store.size();++i) {
                    RoutePointWrapper rpw = store.get(i);
                    if(rpw.getDeadline() != null)
                        rpw.getDeadline().setTime(rpw.getDeadline().getTime() + diff);
                    ignoreUpdate = true;
                    store.update(rpw);
                    ignoreUpdate = false;
                }
            }
            
            public void onNameEdited(RoutePointWrapper p) {
                store.commitChanges();
                if(gfMap.containsKey(previousName)) {
                    gfMap.remove(previousName);
                    cbName.remove(previousName);
                }
                if(gfMap.containsKey(p.getName())) {
                    p.setGeofence(gfMap.get(p.getName()));
                } else {
                    if(p.getRoutePoint().getGeofence().getId() != 0) {
                        p.setGeofence(RoutePointWrapper.createGF(p.getName(), 300));
                    }
                }
                store.update(p);
                updateGfMap(p);
            }
            
            public void onAddressEdited(final RoutePointWrapper p) {
                MatchResult m = latLonPatt.exec(p.getAddress());
                if(m == null) {
                    Geocoder.search(p.getAddress(), new SearchCallback() {
                        @Override
                        public void onResult(float lon, float lat, String name) {
                            GeoFence gf = p.getRoutePoint().getGeofence();
                            p.setLoading(false);
                            gf.setPoints(lon+" "+lat);
                            gf.setAddress(p.getAddress());
                            if(gf.getName() == null || gf.getName().isEmpty())
                                gf.setName(name);
                            store.update(p);
                            updateGfMap(p);
                        }
                    });
                } else {
                    double lat = Double.parseDouble(m.getGroup(1)) * 
                            (m.getGroup(3).equals("S") ? -1 : 1);
                    double lon = Double.parseDouble(m.getGroup(4)) *
                            (m.getGroup(6).equals("W") ? -1 : 1);
                    p.setLonLat(lon, lat);
                    store.update(p);
                    updateGfMap(p);
                }
            }
        });
    }
    
    private void updateGfMap(RoutePointWrapper pt) {
        if(!gfMap.containsKey(pt.getName()) && pt.getName() != null 
                && !"".equals(pt.getName())) {
            gfMap.put(pt.getName(), pt.getRoutePoint().getGeofence());
            cbName.add(pt.getName());
        }
    }
    
    private void prepareMap(LonLat center) {
        MapOptions mapOptions = new MapOptions();
        mapOptions.setMaxExtent(new Bounds(-20037508.34, -20037508.34, 20037508.34, 20037508.34));
        MapWidget mapWidget = new MapWidget("100%", "100%", mapOptions);
        map = mapWidget.getMap();
        
        map.addLayer(OSM.Mapnik("OpenStreetMap"));
        gfLayer = new Vector(i18n.overlayType(UserSettings.OverlayType.GEO_FENCES));
        map.addLayer(gfLayer);

        // add editing feature
        ModifyFeatureOptions options = new ModifyFeatureOptions();
        options.setClickout(false);
        options.setStandalone(true);
        options.setToggle(false);
        modifyFeature = new ModifyFeature(gfLayer, options);
        map.addControl(modifyFeature);
        modifyFeature.setMode(ModifyFeature.DRAG);
        modifyFeature.activate();
        gfLayer.getEvents().register("featuremodified", gfLayer, new EventHandler() {
            @Override
            public void onHandle(EventObject eventObject) {
                if(modifyFeature.isSelectedFeatureModified() && modifiedFeature != null) {
                    LonLat center = modifiedFeature.getCenterLonLat();
                    center.transform(map.getProjection(), GF_PROJECTION);
                    modifiedPt.getRoutePoint().getGeofence().setPoints(center.lon()+" "+center.lat());
                    ignoreUpdate = true;
                    store.update(modifiedPt);
                    ignoreUpdate = false;
                    drawPolyline();
                }
            }
        });

        if(center != null)
            map.setCenter(center, 12);
        map.addMapClickListener(new MapClickListener() {
            @Override
            public void onClick(MapClickListener.MapClickEvent ev) {
                if(recomputingPath)
                    return;
                LonLat ll = ev.getLonLat();
                ll.transform(map.getProjection(), GF_PROJECTION);
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
                drawPolyline();
                selectFirst(event.getItems());
                if(!event.getItems().isEmpty()) {
                    updateGfMap(event.getItems().get(0));
                }
            }

            @Override
            public void onRemove(StoreRemoveEvent<RoutePointWrapper> event) {
                drawPolyline();
                GeoFence gf = event.getItem().getRoutePoint().getGeofence();
                if(gfMap.containsKey(gf.getName())) {
                    gfMap.remove(gf.getName());
                    cbName.remove(gf.getName());
                }
            }

            @Override
            public void onFilter(StoreFilterEvent<RoutePointWrapper> event) {
            }

            @Override
            public void onClear(StoreClearEvent<RoutePointWrapper> event) {
            }

            @Override
            public void onUpdate(StoreUpdateEvent<RoutePointWrapper> event) {
                if(ignoreUpdate)
                    return;
                if(!event.getItems().isEmpty()) {
                    drawPolyline();
                }
                selectFirst(event.getItems());
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
            
            private void selectFirst(List<RoutePointWrapper> rpws) {
                if(rpws.isEmpty())
                    return;
                GeoFence gf = rpws.get(0).getRoutePoint().getGeofence();
                gfRenderer.selectGeoFence(gf);
                GeoFenceDrawing gfDraw = gfRenderer.getDrawing(gf);
                if(gfDraw != null && gf.getId() == 0) {
                    gfLayer.removeFeature(gfDraw.getTitle());
                    modifiedFeature = gfDraw.getShape();
                    modifyFeature.selectFeature(modifiedFeature);
                    modifiedPt = rpws.get(0);
                }
            }
        });
        lineString = route.getLinePoints();
        if(lineString == null)
            return;
        for(RoutePointWrapper pt : store.getAll()) {
            GeoFence gf = pt.getRoutePoint().getGeofence();
            if(gf == null)
                continue;
            if(!gf.points().isEmpty())
                gfRenderer.drawGeoFence(gf, true);
        }
        if(route.getCorridor() != null)
            gfRenderer.drawGeoFence(route.getCorridor(), false);
        routeDrawer.onResult(lineString, null);
        LineString ls = PolylineDecoder.decode(this, lineString);
        map.zoomToExtent(ls.getBounds());
    }
    
    
    private void prepareDND() {
        GridDragSource<RoutePointWrapper> dragSource = new GridDragSource<>(grid);
        GridDropTarget<RoutePointWrapper> dropTarget = new GridDropTarget<>(grid);
        dropTarget.setAllowSelfAsSource(true);
        dropTarget.setFeedback(DND.Feedback.BOTH);
        dragSource.addDragStartHandler(new DndDragStartHandler() {
            @Override
            public void onDragStart(DndDragStartEvent event) {
                List<RoutePointWrapper> rpwl = (List<RoutePointWrapper>)event.getData();
                if(rpwl == null)
                    return;
                for(RoutePointWrapper rpw : rpwl)
                    if(rpw.isDone())
                        event.setCancelled(true);
            }
        });
    }
    
    private void drawPolyline() {        
        //drag'n'drop!
        if(recomputingPath) {
            pathInvalid = true;
            return;
        }
        startComputingPath();
        gfRenderer.clear();
        
        if(polyline != null) {
            gfLayer.removeFeature(polyline);
            polyline.destroy();
            polyline = null;
        }
        List<LonLat> pts = new ArrayList<>();
        List<RoutePointWrapper> list = new ArrayList<>(store.getAll());
        if(!list.get(0).getForced()) {
            list.remove(0);
        }
        if(!list.get(list.size()-1).getForced()) {
            list.remove(list.size()-1);
        }
        boolean error = false;
        for(RoutePointWrapper pt : list) {
            LonLat center = pt.getCenter();
            if(center == null) {
                error = true;
                continue;
            }
            gfRenderer.drawGeoFence(pt.getRoutePoint().getGeofence(), true);
            pts.add(center);
        }
        
        if(pts.size() < 2 || error) {
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
        if(pathInvalid) {
            pathInvalid = false;
            drawPolyline();
        } else
            addButton.setEnabled(!recomputingPath);
    }
    
    @Override
    public org.gwtopenmaps.openlayers.client.Map getMap() {
        return map;
    }

    @Override
    public Point createPoint(double longitude, double latitude) {
        Point point = new Point(longitude, latitude);
        point.transform(new Projection(GF_PROJECTION), new Projection(map.getProjection()));
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
        store.add(store.size()-1, new RoutePointWrapper());
        edit.startEditing(new Grid.GridCell(store.size()-2, 0));
        grid.getSelectionModel().select(store.size()-2, false);
    }
    
    public void onPointSelected(LonLat lonLat) {
        RoutePointWrapper pt = grid.getSelectionModel().getSelectedItem();
        edit.cancelEditing();
        boolean isNew = pt == null || !pt.isEditable();
        if(isNew)
            pt = new RoutePointWrapper();
        pt.setLonLat(lonLat.lon(), lonLat.lat());
        if(isNew)
            store.add(store.size()-1, pt);
        else
            store.update(pt);
    }
    
    @UiHandler("saveButton")
    public void save(SelectEvent selectEvent) {
        store.commitChanges();
        
        route.getRoutePoints().clear();
        for(RoutePointWrapper rp : store.getAll())
            route.getRoutePoints().add(rp.getRoutePoint());
        if(store.get(0).getForced()) {
            route.setForceFirst(true);
        } else {
            route.setForceFirst(false);
            route.getRoutePoints().remove(0);
        }
        if(store.get(store.size()-1).getForced()) {
            route.setForceLast(true);
        } else {
            route.setForceLast(false);
            route.getRoutePoints().remove(route.getRoutePoints().size()-1);
        }
        if(!validate(route))
            return;

        route.setName(name.getValue());
        route.setDevice(selectDevice.getCurrentValue());
        route.setLinePoints(lineString);
        
        route.setTolerance(tolerance.getValue());
        route.setArchiveAfter(archiveAfter.getValue());

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
            GeoFence.LonLat[] gll = PolylineDecoder.decodeToLonLat(lineString);
            corridor.points(gll);
            corridor.setRadius(corridorWidth.getValue().floatValue()*1000);
            corridor.setTransferDevices(new HashSet<Device>());
            corridor.setDevices(new HashSet<Device>());
            if(route.getDevice() != null) {
                corridor.getTransferDevices().add(route.getDevice());
                corridor.getDevices().add(route.getDevice());
            }
            route.setCorridor(corridor);
        } else
            route.setCorridor(null);
        
        routeHandler.onSave(route, true);
        window.hide();
    }
    
    private boolean validate(Route r) {
        if(name.getValue() == null || name.getValue().isEmpty()) {
            new AlertMessageBox(i18n.error(), i18n.errNoRouteName()).show();
            return false;
        }
        if(r.getRoutePoints().size() < 2) {
            new AlertMessageBox(i18n.error(), i18n.errNotEnoughRoutePoints()).show();
            return false;
        }
        for(int i=0;i < r.getRoutePoints().size();++i) {
            GeoFence gf = r.getRoutePoints().get(i).getGeofence();
            if(gf.points().isEmpty() || gf.getName() == null || gf.getName().isEmpty()) {
                new AlertMessageBox(i18n.error(), i18n.errInvalidRoutePoint(i)).show();
                return false;
            }
        }
        if(createCorridor.getValue() && 
                (corridorWidth.getValue() == null || !corridorWidth.validate())) {
            new AlertMessageBox(i18n.error(), i18n.errNoCorridorRadius()).show();
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
        private boolean forced;
        private static int ID_GEN = 0;
        private static final Messages i18n = GWT.create(Messages.class);
        
        public RoutePointWrapper() {
            id = ID_GEN++;
            pt = new RoutePoint();
            pt.setGeofence(createGF("", 300));
            forced = false;
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
        
        public Date getDeadline() {
            return pt.getDeadline();
        }
        
        public void setDeadline(Date deadline) {
            pt.setDeadline(deadline);
        }
        
        public boolean getForced() {
            return forced;
        }
        
        public void setForced(boolean forced) {
            this.forced = forced;
        }
        
        public RoutePoint getRoutePoint() {
            return pt;
        }
        
        public void setGeofence(GeoFence gf) {
            if(gf == null) {
                if(pt.getGeofence().getId() != 0) {
                    pt.setGeofence(createGF(pt.getGeofence().getName(),
                                   pt.getGeofence().getRadius()));
                }
            } else {
                pt.setGeofence(gf);
            }
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
            return pt.getGeofence().getId() == 0 && !isDone();
        }
        
        public boolean isDone() {
            return pt.getExitTime() != null;
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
        
        public static GeoFence createGF(String name, float radius) {
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
    
    public static class GridCell<T> extends AbstractCell<T> {
        private ListStore<RoutePointWrapper> store;
        
        public GridCell(ListStore<RoutePointWrapper> store) {
            this.store = store;
        }
        
        @Override
        public void render(Cell.Context context, T value, SafeHtmlBuilder sb) {
            sb.appendHtmlConstant("<label");
            if(store.get(context.getIndex()).isDone())
                sb.appendHtmlConstant(" style=\"color: #ccc\"");
            sb.appendHtmlConstant(">");
            if(value != null)
                sb.appendEscaped(value.toString());
            sb.appendHtmlConstant("</label>");
        }
    }
    
    static class DeleteCell extends AbstractCell<Boolean>{
        private final ImageResourceCell imc = new ImageResourceCell();
        private final CheckBoxCell cbc = new CheckBoxCell();
        
        private final ListStore<RoutePointWrapper> store;
        private final GridEditing<RoutePointWrapper> edit;
        
        public DeleteCell(ListStore<RoutePointWrapper> store, GridEditing<RoutePointWrapper> edit) {
            this.store = store;
            this.edit = edit;
        }
        
        @Override
        public Set<String> getConsumedEvents() {
            return Collections.singleton("click");
        }

        @Override
        public void onBrowserEvent(Cell.Context context, Element parent, Boolean value,
                NativeEvent event, ValueUpdater<Boolean> valueUpdater) {
            super.onBrowserEvent(context, parent, value, event, valueUpdater);
            if(value == null)
                return;
            if(context.getIndex() != 0 && context.getIndex() != store.size()-1) {
                edit.cancelEditing();
                store.remove(context.getIndex());
            } else {
                RoutePointWrapper rpw = store.get(context.getIndex());
                cbc.onBrowserEvent(context, parent, value, event, 
                        (ValueUpdater)valueUpdater);
                rpw.setForced(!value);
                store.update(rpw);
            }
        }

        @Override
        public void render(Cell.Context context, Boolean value, SafeHtmlBuilder sb) {
            if(context.getIndex() != 0 && context.getIndex() != store.size()-1)
                imc.render(context, R.remove(), sb);
            else
                cbc.render(context, value, sb);
        }
    }
}
