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

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.NumberCell;
import com.sencha.gxt.cell.core.client.TextButtonCell;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.resources.CommonStyles;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.widget.core.client.button.ToggleButton;
import com.sencha.gxt.widget.core.client.container.Container;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.*;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Maintenance;

import java.util.*;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.IMaintenanceProperties;
import org.traccar.web.client.view.ReviewEditor.EditableColumnConfig;

public class TechnicalReviewEditor {
    
    public static native void alert(String msg)/*-{
        console.log(msg);       
    }-*/;
    
    public static interface Properties extends IMaintenanceProperties<Maintenance>{
        ValueProvider<Maintenance, Double> serviceInterval();

        ValueProvider<Maintenance, Double> lastService();
    }
    
    final private Properties properties = GWT.create(Properties.class);
    
    interface TechnicalReviewEditorUiBinder extends UiBinder<Widget, TechnicalReviewEditor> {
    }
    
    protected static TechnicalReviewEditorUiBinder uiBinder = GWT.create(TechnicalReviewEditorUiBinder.class);
    
    @UiField(provided = true)
    NumberPropertyEditor<Double> doublePropertyEditor = new NumberPropertyEditor.DoublePropertyEditor();
    
    @UiField
    VerticalLayoutContainer contentContainer;
    
    @UiField
    VerticalLayoutContainer mainContainer;
    
    @UiField
    NumberField<Double> odometer;
    
    @UiField(provided = true)
    NumberFormat odometerFormat = NumberFormat.getFormat("0.#");
    
    @UiField
    CheckBox autoUpdateOdometer;
    
    @UiField
    ToggleButton editButton;
    
    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);
    
    ReviewEditor reviewEditor;
    
    @UiField
    SimpleContainer panel;
    
    private final Device device;
    private final ListStore<Maintenance> maintenanceStore;


    public TechnicalReviewEditor(Device device, ListStore<Device> deviceStore) {
        reviewEditor = new ReviewEditor(device, deviceStore, new ReviewEditor.ReviewEditorHelper<Maintenance, Properties>() {
            private final List<EditableColumnConfig<Maintenance, ?>> editableColumnConfig = new ArrayList<>();
            
            @Override
            public Maintenance createMaintenance() {
                return new Maintenance();
            }

            @Override
            public List<Maintenance> getMaintenances(Device device) {
                return device.getMaintenances();
            }
            
            @Override
            public void setMaintenances(Device device, List<Maintenance> maintenances) {
                device.setMaintenances(maintenances);
            }

            @Override
            public Maintenance copy(Maintenance other) {
                Maintenance copy = new Maintenance(other);
                copy.setId(0);
                copy.setLastService(0);
                return copy;
            }

            @Override
            public void addReviewSpecificColumns(List<ColumnConfig<Maintenance, ?>> columnConfigList) {
                TechnicalReviewEditor.this.addReviewSpecificColumns(columnConfigList, editableColumnConfig);
            }

            @Override
            public Properties getProperties() {
                return properties;
            }

            @Override
            public List<EditableColumnConfig<Maintenance, ?>> getEditColumns() {
                return editableColumnConfig;
            }
        });
        
        this.maintenanceStore = reviewEditor.getMaintenanceStore();
        this.device = device;
        
        uiBinder.createAndBindUi(this);
        
        // set up device odometer settings
        odometer.setValue(device.getOdometer());
        autoUpdateOdometer.setValue(device.isAutoUpdateOdometer());
        
        contentContainer.add(reviewEditor.getPanel(), new VerticalLayoutData(1,1));
        
        odometer.addValueChangeHandler(new ValueChangeHandler<Double>(){
            @Override
            public void onValueChange(ValueChangeEvent<Double> event) {
                reviewEditor.onOdometerChanged(event);
            }
        });
    }
    
    private void addReviewSpecificColumns(List<ColumnConfig<Maintenance, ?>> columnConfigList, List<EditableColumnConfig<Maintenance, ?>> ecc) {
        EditableColumnConfig<Maintenance, Double> serviceInterval = createNumericColumn(this.properties.serviceInterval(), 150, i18n.mileageInterval() + " (" + i18n.km() + ")");
        ecc.add(serviceInterval);
        columnConfigList.add(serviceInterval.getColumnConfig());
        
        EditableColumnConfig<Maintenance, Double> lastService = createNumericColumn(this.properties.lastService(), 130, i18n.lastServiceMileage() + " (" + i18n.km() + ")");
        ecc.add(lastService);
        columnConfigList.add(lastService.getColumnConfig());
        
        ColumnConfig<Maintenance, Double> stateColumn = new ColumnConfig<>(properties.lastService(), 128, i18n.state());
        stateColumn.setFixed(true);
        stateColumn.setResizable(false);
        stateColumn.setCell(new AbstractCell<Double>() {
            @Override
            public void render(Cell.Context context, Double value, SafeHtmlBuilder sb) {
                Maintenance m = maintenanceStore.get(context.getIndex());
                Store.Record record = maintenanceStore.getRecord(m);

                double serviceInterval = (Double) record.getValue(properties.serviceInterval());
                // do not draw anything if service interval is not set
                if (serviceInterval == 0d) {
                    sb.appendEscaped("");
                    return;
                }

                double lastService = (Double) record.getValue(properties.lastService());
                double remaining = lastService + serviceInterval - odometer.getCurrentValue();

                if (remaining > 0) {
                    sb.appendHtmlConstant("<font color=\"green\">" + i18n.remaining() + " " + odometerFormat.format(remaining) + " " + i18n.km() + "</font>");
                } else {
                    sb.appendHtmlConstant("<font color=\"red\">" + i18n.overdue() + " " + odometerFormat.format(-remaining) + " " + i18n.km() + "</font>");
                }
            }
        });
        columnConfigList.add(stateColumn);

        ColumnConfig<Maintenance, String> resetColumn = new ColumnConfig<>(new ValueProvider<Maintenance, String>() {
            @Override
            public String getValue(Maintenance object) {
                return i18n.reset();
            }

            @Override
            public void setValue(Maintenance object, String value) {
            }

            @Override
            public String getPath() {
                return "reset";
            }
        }, 46);
        resetColumn.setFixed(true);
        resetColumn.setResizable(false);
        // IMPORTANT we want the text element (cell parent) to only be as wide as
        // the cell and not fill the cell
        resetColumn.setColumnTextClassName(CommonStyles.get().inlineBlock());
        resetColumn.setColumnTextStyle(SafeStylesUtils.fromTrustedString("padding: 1px 3px 0;"));
        TextButtonCell resetButton = new TextButtonCell();
        resetButton.addSelectHandler(new SelectEvent.SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                int row = event.getContext().getIndex();
                Maintenance m = maintenanceStore.get(row);
                maintenanceStore.getRecord(m).addChange(properties.lastService(), odometer.getCurrentValue());
            }
        });
        resetColumn.setCell(resetButton);
        columnConfigList.add(resetColumn);
    }

    private EditableColumnConfig<Maintenance, Double> createNumericColumn(
            ValueProvider<Maintenance,Double> provider,
            int width, 
            String title) {
        ColumnConfig<Maintenance, Double> serviceIntervalColumn = new ColumnConfig<>(provider, width, title);
        serviceIntervalColumn.setFixed(true);
        serviceIntervalColumn.setResizable(false);
        serviceIntervalColumn.setHidden(true);
        serviceIntervalColumn.setCell(new NumberCell<Double>(odometerFormat));
        
        EditableColumnConfig<Maintenance, Double> serviceIntervalECC = new EditableColumnConfig<>(serviceIntervalColumn);
        NumberField<Double> serviceIntervalEditor = new NumberField<>(doublePropertyEditor);
        serviceIntervalEditor.setAllowDecimals(false);
        serviceIntervalEditor.setAllowBlank(false);
        serviceIntervalEditor.setAllowNegative(false);
        serviceIntervalECC.setEditor(serviceIntervalEditor);
        
        return serviceIntervalECC;
    }
     
    protected Maintenance createMaintenance() {
        return new Maintenance();
    }
    
    public void flush() {
        reviewEditor.flush();
        device.setOdometer(odometer.getCurrentValue());
        device.setAutoUpdateOdometer(autoUpdateOdometer.getValue());
    }
    
    @UiHandler(value = "editButton")
    public void onEditClicked(SelectEvent event) {
        if (editButton.getValue()) {
            startEditing();
        } else {
            stopEditing();
        }
    }
    
    protected void startEditing() {
        reviewEditor.startEditing();
        mainContainer.forceLayout();
    }
    
    protected void stopEditing() {
        reviewEditor.stopEditing();
    }

    public Container getPanel() {
        return panel;
    }
}
