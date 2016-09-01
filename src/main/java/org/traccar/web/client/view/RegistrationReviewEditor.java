package org.traccar.web.client.view;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.sencha.gxt.cell.core.client.form.DateCell;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.button.ToggleButton;
import com.sencha.gxt.widget.core.client.container.Container;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.IMaintenanceProperties;
import org.traccar.web.client.view.ReviewEditor.EditableColumnConfig;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.MaintenanceBase;
import pl.datamatica.traccar.model.RegistrationMaintenance;

public class RegistrationReviewEditor {
    
    public static interface Properties extends IMaintenanceProperties<MaintenanceBase>{
       ValueProvider<RegistrationMaintenance, Date> serviceDate();
    }
    
    final protected Properties properties = GWT.create(Properties.class);
    
    interface RegistrationReviewEditorUiBinder extends UiBinder<Widget, RegistrationReviewEditor> {
    }
    
    protected static RegistrationReviewEditorUiBinder uiBinder = GWT.create(RegistrationReviewEditorUiBinder.class);
    
    private ReviewEditor reviewEditor;
    
    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);
    
    @UiField
    SimpleContainer panel;
    
    @UiField
    VerticalLayoutContainer mainContainer;
    
    @UiField
    ToggleButton editButton;
    
    @UiField
    VerticalLayoutContainer contentContainer;
    
    private final ListStore<RegistrationMaintenance> maintenanceStore;
    private final Device device;

    public RegistrationReviewEditor(Device device, ListStore<Device> deviceStore) {
        reviewEditor = new ReviewEditor(device, deviceStore, new ReviewEditor.ReviewEditorHelper<RegistrationMaintenance, Properties>() {
            private final List<EditableColumnConfig<RegistrationMaintenance, ?>> ecc = new ArrayList<>();
            
            @Override
            public RegistrationMaintenance createMaintenance() {
                return new RegistrationMaintenance();
            }

            @Override
            public List<RegistrationMaintenance> getMaintenances(Device device) {
                return device.getRegistrations();
            }
            
            @Override
            public void setMaintenances(Device device, List<RegistrationMaintenance> maintenances) {
                device.setRegistrations(maintenances);
            }

            @Override
            public RegistrationMaintenance copy(RegistrationMaintenance other) {
                return new RegistrationMaintenance();
            }

            @Override
            public void addReviewSpecificColumns(List<ColumnConfig<RegistrationMaintenance, ?>> columnConfigList) {
                RegistrationReviewEditor.this.addReviewSpecificColumns(columnConfigList, ecc);
            }

            @Override
            public Properties getProperties() {
                return properties;
            }

            @Override
            public List<ReviewEditor.EditableColumnConfig<RegistrationMaintenance, ?>> getEditColumns() {
                return ecc;
            }
        });
        this.device = device;
        this.maintenanceStore = reviewEditor.getMaintenanceStore();
        
        uiBinder.createAndBindUi(this);
        contentContainer.add(reviewEditor.getPanel(), new VerticalLayoutData(1,1));
    }

    protected RegistrationMaintenance createMaintenance() {
        return new RegistrationMaintenance();
    }

    public void onCopyFromClicked(SelectEvent event) {
    }

    private void addReviewSpecificColumns(List<ColumnConfig<RegistrationMaintenance, ?>> columnConfigList,
            List<EditableColumnConfig<RegistrationMaintenance, ?>> ecc) {
        ColumnConfig<RegistrationMaintenance, Date> cc = new ColumnConfig<>(properties.serviceDate(), 160, i18n.maintenanceDate());
        cc.setCell(new DateCell());
        cc.setFixed(true);
        cc.setResizable(false);
        cc.setHidden(true);
        columnConfigList.add(cc);
        ecc.add(new EditableColumnConfig<>(cc));
        
        ColumnConfig<RegistrationMaintenance, Date> stateColumn = new ColumnConfig<>(properties.serviceDate(), 128, i18n.state());
        stateColumn.setFixed(true);
        stateColumn.setResizable(false);
        stateColumn.setCell(new AbstractCell<Date>() {
            @Override
            public void render(Cell.Context context, Date serviceDate, SafeHtmlBuilder sb) {                
                // do not draw anything if service interval is not set
                if (serviceDate == null) {
                    sb.appendEscaped("");
                    return;
                }
                
                long remaining = CalendarUtil.getDaysBetween(new Date(), serviceDate);

                if(remaining > 0) {
                    sb.appendHtmlConstant("<font color=\"green\">" + i18n.remaining() + " " + remaining + " " + i18n.days() + "</font>");
                } else {
                    sb.appendHtmlConstant("<font color=\"red\">" + i18n.overdue() + " " + -remaining + " " + i18n.days() + "</font>");
                }
            }
        });
        columnConfigList.add(stateColumn);
    }
    
    @UiHandler(value = "editButton")
    public void onEditClicked(SelectEvent event) {
        if (editButton.getValue()) {
            reviewEditor.startEditing();
        } else {
            reviewEditor.stopEditing();
        }
        mainContainer.forceLayout();
    }
    
    public Container getPanel() {
        return panel;
    }
    
    public void flush() {
        reviewEditor.flush();
    }
    
}
