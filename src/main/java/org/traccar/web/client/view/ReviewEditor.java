/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.Container;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.IsField;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.RowNumberer;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.DeviceProperties;
import org.traccar.web.client.model.IMaintenanceProperties;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.MaintenanceBase;

public class ReviewEditor<T extends MaintenanceBase, 
        PropertiesType extends IMaintenanceProperties> 
        implements SelectionChangedEvent.SelectionChangedHandler<T> {

    public static class EditableColumnConfig<Input, Output> {
        private ColumnConfig<Input, Output> columnConfig;
        private IsField<Output> editor;
        
        public EditableColumnConfig() {}
        public EditableColumnConfig(ColumnConfig<Input, Output> columnConfig) {
            this.columnConfig = columnConfig;
        }

        public ColumnConfig<Input, Output> getColumnConfig() {
            return columnConfig;
        }

        public void setColumnConfig(ColumnConfig<Input, Output> columnConfig) {
            this.columnConfig = columnConfig;
        }

        public IsField<Output> getEditor() {
            return editor;
        }

        public void setEditor(IsField<Output> editor) {
            this.editor = editor;
        }
        
        
        public void show(GridEditing<Input> editing) {
            if(editor != null)
                editing.addEditor(columnConfig, editor);
            columnConfig.setHidden(false);
        }
        
        public void hide(GridEditing<Input> editing) {
            if(editor != null)
                editing.removeEditor(columnConfig);
            columnConfig.setHidden(true);
        }
    }
    
    public interface ReviewEditorHelper<T extends MaintenanceBase,
            PropertiesType extends IMaintenanceProperties> {
        T createMaintenance();
        List<T> getMaintenances(Device device);
        void setMaintenances(Device device, List<T> maintenances);
        T copy(T other);
        
        void addReviewSpecificColumns(List<ColumnConfig<T, ?>> columnConfigList);
        List<EditableColumnConfig<T, ?>> getEditColumns();
        
        PropertiesType getProperties();
    }
    ReviewEditorHelper<T, PropertiesType> helper;
    
    interface ReviewEditorUiBinder extends UiBinder<Widget, ReviewEditor> {
    }
    
    protected static ReviewEditorUiBinder uiBinder = GWT.create(ReviewEditorUiBinder.class);
    
    @UiField
    SimpleContainer panel;
    
    @UiField(provided = true)
    ComboBox<Device> deviceCombo;
    
    @UiField
    TextButton copyFromButton;
    
    protected int nextIndex;
    
    @UiField
    Grid<T> grid;
    
    @UiField
    TextButton removeButton;
    
    
    ColumnConfig<T, String> nameColumn;
    
    @UiField(provided = true)
    ColumnModel<T> columnModel;

    @UiField(provided = true)
    protected ListStore<T> maintenanceStore;
    
    @UiField
    ToolBar addRemoveToolbar;
    
    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);
    
    private final Device device;
    GridEditing<T> editing;

    public ReviewEditor(final Device device, ListStore<Device> deviceStore, 
            ReviewEditorHelper<T, PropertiesType> helper) {
        this.helper = helper;
        this.device = device;
        
        maintenanceStore = new ListStore<>(helper.getProperties().indexNo());
        
        List<T> maintenances = helper.getMaintenances(device);
        if(maintenances != null) {
            // set up grid
            for (T maintenance : maintenances){
                maintenanceStore.add(maintenance);
            }
            nextIndex = maintenances.size();
        }
        
        List<ColumnConfig<T, ?>> columnConfigList = new LinkedList<>();
        RowNumberer<T> rowNumberer = new RowNumberer<>();
        rowNumberer.setHeader("#");

        columnConfigList.add(rowNumberer);
        rowNumberer.setFixed(true);
        rowNumberer.setResizable(false);
        
        nameColumn = new ColumnConfig<>(helper.getProperties().name(), 25, i18n.serviceName());
        columnConfigList.add(nameColumn);
        
        DeviceProperties deviceProperties = GWT.create(DeviceProperties.class);
        deviceCombo = new ComboBox<>(deviceStore, deviceProperties.label());
        deviceCombo.addSelectionHandler(new SelectionHandler<Device>() {
            @Override
            public void onSelection(SelectionEvent<Device> event) {
                copyFromButton.setEnabled(event.getSelectedItem() != null);
            }
        });
        
        helper.addReviewSpecificColumns(columnConfigList);
        

        for (ColumnConfig<T, ?> columnConfig : columnConfigList) {
            columnConfig.setSortable(false);
            columnConfig.setHideable(false);
            columnConfig.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        }

        columnModel = new ColumnModel<>(columnConfigList);
        
        uiBinder.createAndBindUi(this);
        
        grid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        grid.getSelectionModel().addSelectionChangedHandler(this);

        rowNumberer.initPlugin(grid);
        
        editing = new GridInlineEditing<>(grid);
    }
    
    public ListStore<T> getMaintenanceStore() {
        return maintenanceStore;
    }

    public Container getPanel() {
        return panel;
    }

    @UiHandler(value = "removeButton")
    public void onRemoveClicked(SelectEvent event) {
        maintenanceStore.remove(maintenanceStore.indexOf(grid.getSelectionModel().getSelectedItem()));
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent<T> event) {
        removeButton.setEnabled(!event.getSelection().isEmpty());
    }

    public void onOdometerChanged(ValueChangeEvent<Double> event) {
        grid.getView().refresh(false);
    }

    public void startEditing() {
        for(EditableColumnConfig<T, ?> ecc : helper.getEditColumns()) {
            ecc.show(editing);
        }
        
        editing.addEditor(nameColumn, new TextField());        
        addRemoveToolbar.setVisible(true);
        grid.getView().refresh(true);
    }

    public void stopEditing() {
        for(EditableColumnConfig<T, ?> ecc : helper.getEditColumns()) {
            ecc.hide(editing);
        }
        editing.removeEditor(nameColumn);
        addRemoveToolbar.setVisible(false);
        grid.getView().refresh(true);
    }
    
    @UiHandler(value = "addButton")
    public void onAddClicked(SelectEvent event){
        T maintenance = helper.createMaintenance();
        maintenance.setIndexNo(nextIndex++);
        maintenanceStore.add(maintenance);
    }

    @UiHandler(value = "copyFromButton")
    public void onCopyFromClicked(SelectEvent event) {
        Device device = deviceCombo.getCurrentValue();
        for (T maintenance : helper.getMaintenances(device)) {
            boolean found = false;
            for (int i = 0; i < maintenanceStore.size(); i++) {
                T next = maintenanceStore.get(i);
                if (next.getName().equals(maintenance.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                T copy = helper.copy(maintenance);
                copy.setIndexNo(nextIndex++);
                maintenanceStore.add(copy);
            }
        }
    }
    
    public void flush() {
        maintenanceStore.commitChanges();
        helper.setMaintenances(device, new ArrayList<>(maintenanceStore.getAll()));
        for (int i = 0; i < helper.getMaintenances(device).size(); i++) {
            helper.getMaintenances(device).get(i).setIndexNo(i);
        }
    }
}
