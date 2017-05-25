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
import com.google.gwt.cell.client.ImageCell;
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
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
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.utils.Geocoder;
import org.traccar.web.client.utils.Geocoder.SearchCallback;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.GeoFenceType;
import pl.datamatica.traccar.model.Route;
import pl.datamatica.traccar.model.RoutePoint;

public class RouteDialog implements MapPointSelectionDialog.PointSelectedListener {
    interface _UiBinder extends UiBinder<Widget, RouteDialog> {}
    private static _UiBinder uiBinder = GWT.create(_UiBinder.class);
    private static Resources R = GWT.create(Resources.class);
    
    @UiField
    Window window;
    @UiField
    CheckBox connect;
    @UiField
    TextField name;
    @UiField(provided = true)
    Grid<TrackPoint> grid;
    @UiField
    FieldLabel selectDeviceLabel;
    @UiField(provided = true)
    ComboBox<Device> selectDevice;
    @UiField
    FieldLabel trackNameLabel;
    
    Messages i18n = GWT.create(Messages.class);
    
    private final Route route;
    ListStore<TrackPoint> store;
    final RouteHandler routeHandler;
    
    RegExp latLonPatt = RegExp.compile("(\\d+(\\.\\d+)?)([NS])\\s*(\\d+(\\.\\d+)?)([WE])"); 
    
    public RouteDialog(Route route, final RouteHandler routeHandler, 
            ListStore<Device> devs, ListStore<GeoFence> gfs) {
        this.route = route;
        this.routeHandler = routeHandler;
        store = new ListStore<>(new ModelKeyProvider<TrackPoint>() {
            @Override
            public String getKey(TrackPoint item) {
                return Integer.toString(item.id);
            }
        });
        
        prepareGrid(gfs);
        selectDevice = new ComboBox(devs, new LabelProvider<Device>() {
            @Override
            public String getLabel(Device item) {
                return item.getName();
            }
        });
        uiBinder.createAndBindUi(this);
        
        connect.setValue(true);
        //editing!
        if(route.getId() != 0)
            connect.setEnabled(false);
        connect.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if(event.getValue() == null) 
                    return;
                trackNameLabel.setVisible(event.getValue());
                selectDeviceLabel.setVisible(event.getValue());
            }
            
        });
        
        name.setValue(route.getName());
        
        for(RoutePoint p : route.getRoutePoints()) {
            TrackPoint tp = new TrackPoint();
            tp.gf = p.getGeofence();
            store.add(tp);
        }
        if(route.getDevice() != null)
            selectDevice.setValue(route.getDevice());
    }

    private void prepareGrid(ListStore<GeoFence> gfs) {
        List<ColumnConfig<TrackPoint, ?>> ccList = new ArrayList<>();
        ColumnConfig<TrackPoint, String> cName = new ColumnConfig<>(
                new ValueProvider<TrackPoint, String>() {
                    @Override
                    public String getValue(TrackPoint object) {
                        return object.gf.getName();
                    }
                    
                    @Override
                    public void setValue(TrackPoint object, String value) {
                        object.gf.setName(value);
                    }
                    
                    @Override
                    public String getPath() {
                        return "name";
                    }}, 140, i18n.name());
        ccList.add(cName);
        ColumnConfig<TrackPoint, String> cAddress = new ColumnConfig<>(
                new ValueProvider<TrackPoint, String>() {
                    @Override
                    public String getValue(TrackPoint object) {
                        return object.gf.getAddress() == null ? "" : object.gf.getAddress();
                    }
                    
                    @Override
                    public void setValue(TrackPoint object, String value) {
                        object.gf.setAddress(value);
                    }
                    
                    @Override
                    public String getPath() {
                        return "address";
                    }}, 330, i18n.address());
        ccList.add(cAddress);
        ColumnConfig<TrackPoint, Integer> cRadius = new ColumnConfig<>(
                new ValueProvider<TrackPoint, Integer>() {
                    @Override
                    public Integer getValue(TrackPoint object) {
                        return (int)object.gf.getRadius();
                    }
                    
                    @Override
                    public void setValue(TrackPoint object, Integer value) {
                        object.gf.setRadius(value);
                    }
                    
                    @Override
                    public String getPath() {
                        return "radius";
                    }}, 50, i18n.radius());
        ccList.add(cRadius);
        ColumnConfig<TrackPoint, ImageResource> cDelete = new ColumnConfig<>(
                new ValueProvider<TrackPoint, ImageResource>() {
                    @Override
                    public ImageResource getValue(TrackPoint object) {
                        return R.remove();
                    }

                    @Override
                    public void setValue(TrackPoint object, ImageResource value) {
                    }

                    @Override
                    public String getPath() {
                        return "delete";
                    }
                }, 30, "");
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
        ColumnModel<TrackPoint> cm = new ColumnModel<>(ccList);
        grid = new Grid<>(store, cm);
        
        final TextField addr = new TextField();
        final NumberField rad = new NumberField(new NumberPropertyEditor.IntegerPropertyEditor());
        rad.addValidator(new MaxNumberValidator<>(1500));
        rad.addValidator(new MinNumberValidator<>(300));
        
        final GridEditing<TrackPoint> edit = new GridInlineEditing<>(grid);
        
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
                if(!gfMap.containsKey(event.getValue()))
                    return;
                edit.completeEditing();
                TrackPoint p = grid.getSelectionModel().getSelectedItem();
                p.gf = gfMap.get(event.getValue());
                store.update(p);
            }
        });
        cbName.setForceSelection(false);
        
        addr.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> e) {
                MatchResult m = latLonPatt.exec(e.getValue());
                if(m == null)
                    return;
                double lat = Double.parseDouble(m.getGroup(1));
                if(m.getGroup(3).equals("S"))
                    lat *= -1;
                double lon = Double.parseDouble(m.getGroup(4));
                if(m.getGroup(6).equals("W"))
                    lon *= -1;
                store.commitChanges();
                TrackPoint p = grid.getSelectionModel().getSelectedItem();
                p.setLonLat(lon, lat);
                store.update(p);
            }
        });

        edit.addEditor(cName, cbName);
        edit.addEditor(cAddress, addr);
        edit.addEditor(cRadius, rad);
        
        edit.addBeforeStartEditHandler(new BeforeStartEditHandler<TrackPoint>() {
            @Override
            public void onBeforeStartEdit(BeforeStartEditEvent<TrackPoint> event) {
                TrackPoint pt = store.get(event.getEditCell().getRow());
                if(event.getEditCell().getCol() != 0
                        && pt != null && pt.gf != null && pt.gf.getId() != 0)
                    event.setCancelled(true);
            }
        });
    }
    
    public void show() {
        window.show();
    }
    
    @UiHandler("addButton")
    public void add(SelectEvent selectEvent) {
        store.add(new TrackPoint());
    }
    
    @UiHandler("addFromMap")
    public void addFromMap(SelectEvent se) {
        new MapPointSelectionDialog(this).show();
    }
    
    @Override
    public void onPointSelected(LonLat lonLat) {
        TrackPoint tp = new TrackPoint();
        tp.setLonLat(lonLat.lon(), lonLat.lat());
        store.add(tp);
    }
    
    @UiHandler("saveButton")
    public void save(SelectEvent selectEvent) {
        store.commitChanges();
        for(TrackPoint tp : store.getAll()) {
            RoutePoint rp = new RoutePoint();
            rp.setGeofence(tp.gf);
            route.getRoutePoints().add(rp);
        }
        if(connect.getValue()) {
            route.setName(name.getValue());
            route.setDevice(selectDevice.getCurrentValue());
        }
        
        RouteGenerator g = new RouteGenerator(route, routeHandler, connect.getValue());
        g.start();
    }
    
    @UiHandler("cancelButton")
    public void cancel(SelectEvent selectEvent) {
       window.hide(); 
    }
    
    
    public static interface RouteHandler {
        void onSave(Route route, boolean connect);
    }
    
    static class RouteGenerator {
        List<GeoFence> waiting;
        int geocodedCount;
        List<GeoFence> ready;
        
        RouteHandler routeHandler;
        Route route;
        boolean connect;
        
        public RouteGenerator(Route route, RouteHandler rHandler, 
                boolean connect) {
            waiting = new ArrayList<>();
            ready = new ArrayList<>();
            this.route = route;
            this.routeHandler = rHandler;
            this.connect = connect;
            
            for(RoutePoint rp : route.getRoutePoints()) {
                GeoFence gf = rp.getGeofence();
                if(gf.getId() != 0)
                    ready.add(gf);
                else if(gf.getPoints() != null && !gf.getPoints().isEmpty()) {
                    ready.add(gf);
                } else {
                    waiting.add(gf);
                }
            }
        }
        
        public void start() {
            geocodedCount = 0;
            if(waiting.isEmpty())
                save();
            for(final GeoFence gf : waiting) {
                Geocoder.search(gf.getAddress(), new SearchCallback() {
                        @Override
                        public void onResult(float lon, float lat) {
                            ++geocodedCount;
                            gf.setPoints(lon+" "+lat);
                            ready.add(gf);
                            if(geocodedCount == waiting.size())
                                save();
                        }
                    });
            }
        }
        
        public void save() {
            routeHandler.onSave(route, connect);
        }
    }
    
    static class TrackPoint {
        private static int ID_GEN = 1;
        
        int id;
        GeoFence gf;
        
        public TrackPoint() {
            this(ID_GEN++, "", "", 300);
        }
        
        public TrackPoint(int id, String name, String address, int radius) {
            this.id = id;
            this.gf = new GeoFence(0, name);
            gf.setTransferDevices(Collections.EMPTY_SET);
            gf.setType(GeoFenceType.CIRCLE);
            gf.setRadius(300);
            gf.setAddress("");
        }
        
        public void setLonLat(double lon, double lat) {
            gf.setPoints(lon+" "+lat);
            if(gf.getAddress() == null || gf.getAddress().isEmpty()) {
                String latDir = lat < 0 ? "S" : "N";
                String lonDir = lon < 0 ? "W" : "E";
                gf.setAddress(Math.abs(lat)+latDir+" "+Math.abs(lon)+lonDir);
            }
            if(gf.getName() == null || gf.getName().isEmpty())
                gf.setName("pkt_"+(int)(lon*10)+"_"+(int)(lat*10));
        }
    }
    
    static interface Resources extends ClientBundle {
        @ClientBundle.Source("org/traccar/web/client/theme/icon/remove.png")
        ImageResource remove();
    }
}
