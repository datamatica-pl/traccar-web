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
package org.traccar.web.client.view;

import pl.datamatica.traccar.model.GeoFenceType;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Device;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.*;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.event.StoreFilterEvent;
import com.sencha.gxt.data.shared.event.StoreFilterEvent.StoreFilterHandler;
import com.sencha.gxt.widget.core.client.ColorPalette;
import com.sencha.gxt.widget.core.client.PlainTabPanel;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.*;
import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
import org.gwtopenmaps.openlayers.client.*;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.control.DrawFeature;
import org.gwtopenmaps.openlayers.client.control.DrawFeatureOptions;
import org.gwtopenmaps.openlayers.client.control.ModifyFeature;
import org.gwtopenmaps.openlayers.client.control.ModifyFeatureOptions;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.Geometry;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.handler.*;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.traccar.web.client.GeoFenceDrawing;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.EnumKeyProvider;
import org.traccar.web.client.model.GeoFenceProperties;

import java.util.*;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.model.DeviceProperties;

public class GeoFenceWindow implements Editor<GeoFence> {

    private static GeoFenceDialogUiBinder uiBinder = GWT.create(GeoFenceDialogUiBinder.class);

    interface GeoFenceDialogUiBinder extends UiBinder<Widget, GeoFenceWindow> {
    }

    private GeoFenceDriver driver = GWT.create(GeoFenceDriver.class);

    interface GeoFenceDriver extends SimpleBeanEditorDriver<GeoFence, GeoFenceWindow> {
    }

    public interface GeoFenceHandler {
        void onSave(GeoFence geoFence);
        void onClear();
        void onCancel();
        GeoFenceDrawing repaint(GeoFence geoFence);
    }

    private final GeoFenceHandler geoFenceHandler;
    private final Map map;
    private final Vector geoFenceLayer;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    @UiField
    Window window;

    @UiField
    PlainTabPanel tabs;

    @UiField
    TextField name;

    @UiField
    TextArea description;

    @UiField(provided = true)
    ComboBox<GeoFenceType> type;

    @UiField(provided = true)
    NumberPropertyEditor<Float> floatPropertyEditor = new NumberPropertyEditor.FloatPropertyEditor();

    @UiField
    NumberField<Float> radius;

    @UiField
    ColorPalette color;

    DrawFeature drawFeature;

    ModifyFeature modifyFeature;
    final GeoFence geoFence;
    GeoFenceDrawing geoFenceDrawing;

    @UiField(provided = true)
    ColumnModel<Device> columnModel;

    @UiField(provided = true)
    ListStore<Device> deviceSelectionStore;

    @UiField(provided = true)
    @Editor.Ignore
    StoreFilterField<Device> deviceFilter;
    
    @UiField
    Grid<Device> grid;
    
    @UiField
    @Editor.Ignore
    TextButton clearButton;
    
    @UiField
    VerticalLayoutContainer devicesTab;
    
    private Set<Device> selDevices;
    private boolean selDevicesLock = false;
    private boolean selectAll = true;

    public GeoFenceWindow(GeoFence geoFence,
                          GeoFenceDrawing geoFenceDrawing,
                          ListStore<Device> devices,
                          Map map,
                          Vector geoFenceLayer,
                          GeoFenceHandler geoFenceHandler) {
        this.geoFenceHandler = geoFenceHandler;
        this.map = map;
        this.geoFenceLayer = geoFenceLayer;
        this.geoFence = new GeoFence();
        this.geoFence.copyFrom(geoFence);
        selDevices = new HashSet<>();

        ListStore<GeoFenceType> geoFenceTypeStore = new ListStore<>(
                new EnumKeyProvider<GeoFenceType>());
        geoFenceTypeStore.addAll(Arrays.asList(GeoFenceType.values()));
        type = new ComboBox<>(
                geoFenceTypeStore, new GeoFenceProperties.GeoFenceTypeLabelProvider());

        type.setForceSelection(true);
        type.setTriggerAction(ComboBoxCell.TriggerAction.ALL);

        DeviceProperties deviceProperties = GWT.create(DeviceProperties.class);
        deviceSelectionStore = new ListStore<Device>(deviceProperties.id());
        deviceSelectionStore.addAll(devices.getAll());
        
        List<ColumnConfig<Device, ?>> columnConfigList = new LinkedList<>();
        CheckBoxSelectionModel<Device> sel = createDevSelectionModel();
        
        columnConfigList.add(sel.getColumn());
        columnConfigList.add(new ColumnConfig<>(new ToStringValueProvider<Device>() {
            @Override
            public String getValue(Device device) {
                return device.getName();
            }
        }, 25, i18n.name()));        
        
        columnModel = new ColumnModel<>(columnConfigList);
        prepareDeviceFilter();
        
        uiBinder.createAndBindUi(this);
        
        grid.setSelectionModel(sel);
        for(Device d : geoFence.getDevices()) {
            deviceSelectionStore.remove(d);
            deviceSelectionStore.add(0, d);
            grid.getSelectionModel().select(d, true);
        }

        driver.initialize(this);
        driver.edit(this.geoFence);
        toggleRadiusField(this.geoFence.getType());
        
        if(GeoFenceType.LINE.equals(this.geoFence.getType())) {
            clearButton.setEnabled(false);
            type.setEnabled(false);
            tabs.remove(devicesTab);
        }

        this.geoFenceDrawing = geoFenceDrawing;
        if (geoFenceDrawing == null) {
            draw();
        } else {
            edit();
        }
    }
    
    private CheckBoxSelectionModel<Device> createDevSelectionModel() {
        final CheckBoxSelectionModel<Device> sel = new CheckBoxSelectionModel<>();
        sel.setShowSelectAll(true);
        sel.addSelectionChangedHandler(new SelectionChangedHandler<Device>() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent<Device> event) {
                if(selDevicesLock)
                    return;
                selDevices.removeAll(deviceSelectionStore.getAll());
                selDevices.addAll(grid.getSelectionModel().getSelectedItems());
            }
        });
        return sel;
    }
    
    private void prepareDeviceFilter() {
        deviceFilter = new StoreFilterField<Device>() {
            @Override
            protected boolean doSelect(Store<Device> store, Device parent, Device item, String filter) {
                return filter.trim().isEmpty() || matches(item, filter);
            }

            boolean matches(Device d, String filter) {
                return d.getName().toLowerCase().contains(filter.toLowerCase())
                        || d.getUniqueId().contains(filter);
            }
        };
        deviceFilter.bind(deviceSelectionStore);
        deviceSelectionStore.addStoreFilterHandler(new StoreFilterHandler<Device>() {
            String filter = "";
            @Override
            public void onFilter(StoreFilterEvent<Device> event) {
                if(deviceFilter.getText().equals(filter))
                    return;
                filter = deviceFilter.getText();
                selDevicesLock = true;
                for(Device d : deviceSelectionStore.getAll())
                    if(selDevices.contains(d) && !grid.getSelectionModel().isSelected(d))
                        grid.getSelectionModel().select(d, true);
                selDevicesLock = false;
            }
        });
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }
    
    @UiHandler("selectAllButton")
    public void onSelectAllClicked(SelectEvent event) {
        if(selectAll) {
            grid.getSelectionModel().selectAll();
        } else {
            grid.getSelectionModel().deselectAll();
        }
        selectAll = !selectAll;
    }


    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        if (geoFenceDrawing == null) {
            new AlertMessageBox(i18n.error(), i18n.errGeoFenceIsEmpty()).show();
            return;
        }

        window.hide();
        removeControls();
        geoFenceHandler.onSave(flush());
    }

    @UiHandler("clearButton")
    public void onClearClicked(SelectEvent event) {
        clear();
    }

    private void clear() {
        removeControls();
        geoFenceDrawing = null;
        geoFence.setPoints(null);
        geoFenceHandler.onClear();
        draw();
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        removeControls();
        geoFenceHandler.onCancel();
        window.hide();
    }

    @UiHandler("type")
    public void onTypeChanged(SelectionEvent<GeoFenceType> event) {
        clear();
        toggleRadiusField(event.getSelectedItem());
    }

    private void toggleRadiusField(GeoFenceType type) {
        radius.setEnabled(type == GeoFenceType.CIRCLE || type == GeoFenceType.LINE);
    }

    @UiHandler("color")
    public void onColorChanged(ValueChangeEvent<String> event) {
        repaint();
    }

    @UiHandler("radius")
    public void onRadiusChanged(ValueChangeEvent<Float> event) {
        repaint();
    }

    private void repaint() {
        if (geoFenceDrawing != null) {
            modifyFeature.deactivate();
            geoFenceDrawing = geoFenceHandler.repaint(flush());
            edit();
        }
    }

    private GeoFence flush() {
        GeoFence updated = driver.flush();
        // sometimes it's not flushed correctly
        updated.setType(type.getCurrentValue());
        updated.setRadius(radius.getCurrentValue());
        updated.setOwner(ApplicationContext.getInstance().getUser());
        Geometry geometry = geoFenceDrawing.getShape().getGeometry();
        Projection mapProjection = new Projection(map.getProjection());
        Projection epsg4326 = new Projection("EPSG:4326");
        Point[] vertices = null;
        switch (type.getCurrentValue()) {
            case CIRCLE:
                LonLat center = geometry.getBounds().getCenterLonLat();
                vertices = new Point[] { new Point(center.lon(), center.lat()) };
                break;
            case POLYGON:
                vertices = geometry.getVertices(false);
                break;
            case LINE:
                Point[] endpoints = geometry.getVertices(true);
                Point[] nodes = geometry.getVertices(false);
                vertices = new Point[endpoints.length + nodes.length];
                if (endpoints.length > 0) {
                    vertices[0] = endpoints[0];
                    vertices[vertices.length - 1] = endpoints[1];
                }
                for (int i = 0; i < nodes.length; i++) {
                    vertices[i + 1] = nodes[i];
                }
                break;
        }
        if (vertices != null) {
            GeoFence.LonLat[] points = new GeoFence.LonLat[vertices.length];
            for (int i = 0; i < vertices.length; i++) {
                vertices[i].transform(mapProjection, epsg4326);
                points[i] = new GeoFence.LonLat(vertices[i].getX(), vertices[i].getY());
            }
            updated.points(points);
        }
        // set up devices
        selDevicesLock = true;
        updated.setDevices(new HashSet<>(selDevices));
        selDevicesLock = false;

        return updated;
    }

    private void removeControls() {
        if (drawFeature != null) {
            drawFeature.deactivate();
            drawFeature.cancel();
            map.removeControl(drawFeature);
        }

        if (modifyFeature != null) {
            modifyFeature.deactivate();
            map.removeControl(modifyFeature);
        }
    }

    private void draw() {
        DrawFeatureOptions drawFeatureOptions = new DrawFeatureOptions();
        Handler handler = null;

        switch (type.getCurrentValue()) {
            case LINE:
                handler = new PathHandler();
                break;
            case POLYGON:
                handler = new PolygonHandler();
                break;
            case CIRCLE:
                handler = new PointHandler();
                break;
        }

        drawFeatureOptions.onFeatureAdded(new DrawFeature.FeatureAddedListener() {
            @Override
            public void onFeatureAdded(VectorFeature vectorFeature) {
                removeControls();
                geoFenceDrawing = new GeoFenceDrawing(vectorFeature, null);
                geoFenceDrawing = geoFenceHandler.repaint(flush());
                geoFenceLayer.removeFeature(vectorFeature);
                edit();
            }
        });

        PathHandlerOptions phOpt = new PathHandlerOptions();
        phOpt.setStyleMap(geoFenceLayer.getStyleMap());
        drawFeatureOptions.setHandlerOptions(phOpt);

        drawFeature = new DrawFeature(geoFenceLayer, handler, drawFeatureOptions);
        map.addControl(drawFeature);
        drawFeature.activate();
    }

    private void edit() {
        if(type.getCurrentValue() == GeoFenceType.LINE)
            return;
        
        if (modifyFeature == null) {
            // add editing feature
            ModifyFeatureOptions options = new ModifyFeatureOptions();
            options.setClickout(false);
            options.setStandalone(true);
            options.setToggle(false);
            modifyFeature = new ModifyFeature(geoFenceLayer, options);
            map.addControl(modifyFeature);
        }

        modifyFeature.activate();
        if (type.getCurrentValue() == GeoFenceType.CIRCLE) {
            modifyFeature.setMode(ModifyFeature.DRAG);
        } else if (type.getCurrentValue() == GeoFenceType.LINE) {
            modifyFeature.setMode(ModifyFeature.RESHAPE);
        } else if (type.getCurrentValue() == GeoFenceType.POLYGON) {
            modifyFeature.setMode(ModifyFeature.DRAG | ModifyFeature.RESHAPE);
        }
        modifyFeature.selectFeature(geoFenceDrawing.getShape());
    }
}
