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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.utils.Geocoder;
import org.traccar.web.client.utils.Geocoder.SearchCallback;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.GeoFenceType;

public class TrackDialog {
    interface _UiBinder extends UiBinder<Widget, TrackDialog> {}
    private static _UiBinder uiBinder = GWT.create(_UiBinder.class);
    
    @UiField
    Window window;
    @UiField
    CheckBox connect;
    @UiField
    TextField name;
    @UiField(provided = true)
    Grid<TrackPoint> grid;
    
    Messages i18n = GWT.create(Messages.class);
    
    //TrackPointProperties trackPointProperties = GWT.create(TrackPointProperties.class); 
    
    ListStore<TrackPoint> store;
    final GeoFenceHandler gfHandler;
    
    public TrackDialog(final GeoFenceHandler gfHandler) {
        this.gfHandler = gfHandler;
        store = new ListStore<>(new ModelKeyProvider<TrackPoint>() {
            @Override
            public String getKey(TrackPoint item) {
                return Integer.toString(item.id);
            }
        });
        
        prepareGrid();
        uiBinder.createAndBindUi(this);        
    }

    private void prepareGrid() {
        List<ColumnConfig<TrackPoint, ?>> ccList = new ArrayList<>();
        ColumnConfig<TrackPoint, String> cName = new ColumnConfig<>(
                new ValueProvider<TrackPoint, String>() {
                    @Override
                    public String getValue(TrackPoint object) {
                        return object.name;
                    }
                    
                    @Override
                    public void setValue(TrackPoint object, String value) {
                        object.name = value;
                    }
                    
                    @Override
                    public String getPath() {
                        return "name";
                    }}, 150, i18n.name());
        ccList.add(cName);
        ColumnConfig<TrackPoint, String> cAddress = new ColumnConfig<>(
                new ValueProvider<TrackPoint, String>() {
                    @Override
                    public String getValue(TrackPoint object) {
                        return object.address;
                    }
                    
                    @Override
                    public void setValue(TrackPoint object, String value) {
                        object.address = value;
                    }
                    
                    @Override
                    public String getPath() {
                        return "address";
                    }}, 350, i18n.address());
        ccList.add(cAddress);
        ColumnConfig<TrackPoint, Integer> cRadius = new ColumnConfig<>(
                new ValueProvider<TrackPoint, Integer>() {
                    @Override
                    public Integer getValue(TrackPoint object) {
                        return object.radius;
                    }
                    
                    @Override
                    public void setValue(TrackPoint object, Integer value) {
                        object.radius = value;
                    }
                    
                    @Override
                    public String getPath() {
                        return "radius";
                    }}, 70, "radius");
        ccList.add(cRadius);
        ColumnModel<TrackPoint> cm = new ColumnModel<>(ccList);
        grid = new Grid<>(store, cm);
        
        GridEditing<TrackPoint> edit = new GridInlineEditing<>(grid);
        edit.addEditor(cName, new TextField());
        edit.addEditor(cAddress, new TextField());
        NumberField nf = new NumberField(new NumberPropertyEditor.IntegerPropertyEditor());
        nf.addValidator(new MaxNumberValidator<>(1500));
        nf.addValidator(new MinNumberValidator<>(300));
        edit.addEditor(cRadius, nf);
    }
    
    public void show() {
        window.show();
    }
    
    @UiHandler("addButton")
    public void add(SelectEvent selectEvent) {
        store.add(new TrackPoint());
    }
    
    @UiHandler("saveButton")
    public void save(SelectEvent selectEvent) {
        store.commitChanges();
        for(TrackPoint pt : store.getAll()) {
            final GeoFence gf = new GeoFence();
            gf.setName(pt.name);
            gf.setRadius(pt.radius);
            gf.setType(GeoFenceType.CIRCLE);
            gf.setTransferDevices(new HashSet<Device>());
            gf.setDeleted(false);
            Geocoder.search(pt.address, new SearchCallback() {
                @Override
                public void onResult(float lon, float lat) {
                    gf.setPoints(lon+" "+lat);
                    gfHandler.onSave(gf);
                }
                
            });
        }
    }
    
    @UiHandler("cancelButton")
    public void cancel(SelectEvent selectEvent) {
       window.hide(); 
    }
    
    
    public static interface GeoFenceHandler {
        void onSave(GeoFence gf);
    }
    
    static class TrackPoint {
        private static int ID_GEN = 1;
        
        int id;
        String name;
        String address;
        int radius;
        
        public TrackPoint() {
            this(ID_GEN++, "", "", 300);
        }
        
        public TrackPoint(int id, String name, String address, int radius) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.radius = radius;
        }
    }
    
//    interface TrackPointProperties extends PropertyAccess<TrackPoint> {
//        ModelKeyProvider<TrackPoint> id();
//        ValueProvider<TrackPoint, String> name();
//        ValueProvider<TrackPoint, String> address();
//        ValueProvider<TrackPoint, Integer> radius();
//    }
}
