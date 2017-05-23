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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.BeforeStartEditEvent;
import com.sencha.gxt.widget.core.client.event.BeforeStartEditEvent.BeforeStartEditHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
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
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.utils.Geocoder;
import org.traccar.web.client.utils.Geocoder.SearchCallback;
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
    
    ListStore<TrackPoint> store;
    final GeoFenceHandler gfHandler;
    
    public TrackDialog(final GeoFenceHandler gfHandler, ListStore<GeoFence> gfs) {
        this.gfHandler = gfHandler;
        store = new ListStore<>(new ModelKeyProvider<TrackPoint>() {
            @Override
            public String getKey(TrackPoint item) {
                return Integer.toString(item.id);
            }
        });
        
        prepareGrid(gfs);
        uiBinder.createAndBindUi(this);
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
                    }}, 150, i18n.name());
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
                    }}, 350, i18n.address());
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
                    }}, 70, "radius");
        ccList.add(cRadius);
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
    
    @UiHandler("saveButton")
    public void save(SelectEvent selectEvent) {
        store.commitChanges();
        for(TrackPoint pt : store.getAll()) {
            final GeoFence gf = pt.gf;
            if(gf.getId() == 0) {
                Geocoder.search(pt.gf.getAddress(), new SearchCallback() {
                    @Override
                    public void onResult(float lon, float lat) {
                        gf.setPoints(lon+" "+lat);
                        gfHandler.onSave(gf);
                    }

                });
            }
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
    }
}
