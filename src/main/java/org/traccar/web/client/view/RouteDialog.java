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
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.event.BeforeStartEditEvent;
import com.sencha.gxt.widget.core.client.event.BeforeStartEditEvent.BeforeStartEditHandler;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
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
import org.traccar.web.shared.model.ValidationException;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.GeoFenceType;
import pl.datamatica.traccar.model.Route;
import pl.datamatica.traccar.model.RoutePoint;

public class RouteDialog implements MapPointSelectionDialog.PointSelectedListener {
    interface _UiBinder extends UiBinder<Widget, RouteDialog> {}
    private static _UiBinder uiBinder = GWT.create(_UiBinder.class);
    private static Resources R = GWT.create(Resources.class);
    private static RoutePointAccessor pointsAccessor = GWT.create(RoutePointAccessor.class);
    
    @UiField
    Window window;
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
    
    Messages i18n = GWT.create(Messages.class);
    
    private final Route route;
    ListStore<RoutePointWrapper> store;
    final RouteHandler routeHandler;
    
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
        for(RoutePoint rp : route.getRoutePoints())
            store.add(new RoutePointWrapper(rp));
        if(route.getDevice() != null)
            selectDevice.setValue(route.getDevice());
    }

    private void prepareGrid(ListStore<GeoFence> gfs) {
        List<ColumnConfig<RoutePointWrapper, ?>> ccList = new ArrayList<>();
        ColumnConfig<RoutePointWrapper, String> cName = new ColumnConfig<>(
                pointsAccessor.name(), 140, i18n.name());
        ccList.add(cName);
        ColumnConfig<RoutePointWrapper, String> cAddress = new ColumnConfig<>(
                pointsAccessor.address(), 330, i18n.address());
        ccList.add(cAddress);
        ColumnConfig<RoutePointWrapper, Integer> cRadius = new ColumnConfig<>(
                pointsAccessor.radius(), 50, i18n.radius());
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
        ColumnModel<RoutePointWrapper> cm = new ColumnModel<>(ccList);
        grid = new Grid<>(store, cm);
        
        final TextField addr = new TextField();
        final NumberField rad = new NumberField(new NumberPropertyEditor.IntegerPropertyEditor());
        rad.addValidator(new MaxNumberValidator<>(1500));
        rad.addValidator(new MinNumberValidator<>(300));
        
        final GridEditing<RoutePointWrapper> edit = new GridInlineEditing<>(grid);
        
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
                if(event.getEditCell().getCol() != 0 && !pt.isEditable())
                    event.setCancelled(true);
            }
        });
    }
    
    public void show() {
        window.show();
    }
    
    @UiHandler("addButton")
    public void add(SelectEvent selectEvent) {
        store.add(new RoutePointWrapper());
    }
    
    @UiHandler("addFromMap")
    public void addFromMap(SelectEvent se) {
        new MapPointSelectionDialog(this).show();
    }
    
    @Override
    public void onPointSelected(LonLat lonLat) {
        RoutePointWrapper pt = new RoutePointWrapper();
        pt.setLonLat(lonLat.lon(), lonLat.lat());
        store.add(pt);
    }
    
    @UiHandler("saveButton")
    public void save(SelectEvent selectEvent) {
        store.commitChanges();
        route.getRoutePoints().clear();
        for(RoutePointWrapper rp : store.getAll())
            route.getRoutePoints().add(rp.getRoutePoint());
        if(connect.getValue()) {
            route.setName(name.getValue());
            route.setDevice(selectDevice.getCurrentValue());
        }
        
        if(validate(route, connect.getValue())) {
            RouteGenerator g = new RouteGenerator(route, routeHandler, connect.getValue());
            g.start();
            window.hide();
        }
    }
    
    private boolean validate(Route r, boolean connect) {
        if(connect && r.getRoutePoints().size() < 2) {
            new AlertMessageBox(i18n.error(), "You have to include at least 2 points").show();
            return false;
        }
        if(r.getRoutePoints().size() < 1) {
            new AlertMessageBox(i18n.error(), "You have to include at least 1 point").show();
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
        private static final RegExp latLonPatt = RegExp.compile(
                "(\\d+(\\.\\d+)?)([NS])\\s*(\\d+(\\.\\d+)?)([WE])");
        
        private int id;
        private RoutePoint pt;
        private static int ID_GEN = 0;
        
        public RoutePointWrapper() {
            id = ID_GEN++;
            pt = new RoutePoint();
            pt.setGeofence(createGF("", 300));
        }
        
        public RoutePointWrapper(RoutePoint pt) {
            id = ID_GEN++;
            this.pt = pt;
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
            MatchResult m = latLonPatt.exec(address);
            if(m == null) {
                return;
            }
            double lat = Double.parseDouble(m.getGroup(1)) * 
                    (m.getGroup(3).equals("S") ? -1 : 1);
            double lon = Double.parseDouble(m.getGroup(4)) *
                    (m.getGroup(6).equals("W") ? -1 : 1);
            pt.setLonLat(lon, lat);
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
            if(gf == null)
                pt.setGeofence(createGF(pt.getGeofence().getName(),
                        pt.getGeofence().getRadius()));
            else
                pt.setGeofence(gf);
        }
        
        public void setLonLat(double lon, double lat) {
            GeoFence geofence = pt.getGeofence();
            geofence.setPoints(lon+" "+lat);
            if(geofence.getAddress() == null || geofence.getAddress().isEmpty()) {
                String latDir = lat < 0 ? "S" : "N";
                String lonDir = lon < 0 ? "W" : "E";
                geofence.setAddress(Math.abs(lat)+latDir+" "+Math.abs(lon)+lonDir);
            }
            if(geofence.getName() == null || geofence.getName().isEmpty())
                geofence.setName("pkt_"+(int)(lon*10)+"_"+(int)(lat*10));
        }
        
        public boolean isEditable() {
            return pt.getGeofence().getId() == 0;
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
    
    static interface Resources extends ClientBundle {
        @ClientBundle.Source("org/traccar/web/client/theme/icon/remove.png")
        ImageResource remove();
    }
}
