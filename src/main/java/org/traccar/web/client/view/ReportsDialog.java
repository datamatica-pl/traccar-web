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
import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import pl.datamatica.traccar.model.Period;
import pl.datamatica.traccar.model.UserSettings;
import pl.datamatica.traccar.model.ReportFormat;
import pl.datamatica.traccar.model.ReportType;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Device;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.event.StoreAddEvent;
import com.sencha.gxt.data.shared.event.StoreClearEvent;
import com.sencha.gxt.data.shared.event.StoreDataChangeEvent;
import com.sencha.gxt.data.shared.event.StoreFilterEvent;
import com.sencha.gxt.data.shared.event.StoreHandlers;
import com.sencha.gxt.data.shared.event.StoreRecordChangeEvent;
import com.sencha.gxt.data.shared.event.StoreRemoveEvent;
import com.sencha.gxt.data.shared.event.StoreSortEvent;
import com.sencha.gxt.data.shared.event.StoreUpdateEvent;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.*;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import org.traccar.web.client.editor.DateTimeEditor;
import org.traccar.web.client.editor.ListViewEditor;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.*;
import org.traccar.web.client.widget.PeriodComboBox;

import java.util.*;
import org.traccar.web.client.ApplicationContext;
import pl.datamatica.traccar.model.Route;
import pl.datamatica.traccar.model.UserPermission;

public class ReportsDialog implements Editor<Report> {
    private static ReportsDialogDialogUiBinder uiBinder = GWT.create(ReportsDialogDialogUiBinder.class);

    interface ReportsDialogDialogUiBinder extends UiBinder<Widget, ReportsDialog> {
    }

    interface ReportDriver extends SimpleBeanEditorDriver<Report, ReportsDialog> {
    }

    public interface ReportHandler {
        void onGenerate(Report report);
    }

    final ReportHandler reportHandler;
    final ReportDriver driver = GWT.create(ReportDriver.class);

    @UiField
    Window window;

    @UiField(provided = true)
    final ListStore<Report> reportStore;

    @UiField
    @Ignore
    TextButton generateButton;
    
    @UiField
    MenuItem generateHtml;
    
    @UiField
    TextField name;

    @UiField(provided = true)
    ComboBox<ReportType> type;

    @UiField
    CheckBox includeMap;

    @UiField
    CheckBox disableFilter;

    @UiField(provided = true)
    final ListStore<Device> deviceStore;

    @UiField(provided = true)
    final ListView<Device, Device> devicesList;

    final ListViewEditor<Device> devices;

    @UiField(provided = true)
    final ListStore<GeoFence> geoFenceStore;

    @UiField(provided = true)
    final ListView<GeoFence, String> geoFencesList;

    final ListViewEditor<GeoFence> geoFences;
    
    Route route;

    @UiField(provided = true)
    final PeriodComboBox period;

    @UiField
    @Ignore
    DateField fromDateField;

    @UiField
    @Ignore
    TimeField fromTimeField;

    final DateTimeEditor fromDate;

    @UiField
    @Ignore
    DateField toDateField;

    @UiField
    @Ignore
    TimeField toTimeField;

    final DateTimeEditor toDate;

    @UiField
    ContentPanel geoFencesPanel;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    ReportType prevReportType;

    public ReportsDialog(ListStore<Report> reportStore,
                         final ListStore<Device> deviceStore,
                         ListStore<GeoFence> geoFenceStore,
                         ReportHandler reportHandler) {

        this.reportStore = reportStore;
        this.reportHandler = reportHandler;
        
        this.deviceStore = deviceStore;
        this.devicesList = new ListView<>(deviceStore, new IdentityValueProvider(),
            new AbstractCell<Device>() {
                @Override
                public void render(Cell.Context context, Device value, SafeHtmlBuilder sb) {
                    String style="";
                    if(type.getCurrentValue().isPremium()
                            && value.getSubscriptionDaysLeft(new Date()) == 0)
                        style=" style=\"color:#ccc\"";
                    sb.appendHtmlConstant("<label" + style + ">")
                            .appendEscaped(value.getName())
                            .appendHtmlConstant("</label>");
                }
            });

        GeoFenceProperties geoFenceProperties = GWT.create(GeoFenceProperties.class);
        this.geoFenceStore = geoFenceStore;
        this.geoFencesList = new ListView<>(geoFenceStore, geoFenceProperties.name());

        ListStore<ReportType> reportTypeStore = new ListStore<>(
                new EnumKeyProvider<ReportType>());
        if(ApplicationContext.getInstance().getUser().isPremium())
            reportTypeStore.addAll(Arrays.asList(ReportType.values()));
        else
            reportTypeStore.addAll(Arrays.asList(new ReportType[]{
                ReportType.EVENTS, ReportType.GENERAL_INFORMATION
            }));
        type = new ComboBox<>(
                reportTypeStore, new ReportProperties.ReportTypeLabelProvider());
        type.setForceSelection(true);
        type.setTriggerAction(ComboBoxCell.TriggerAction.ALL);

        period = new PeriodComboBox();

        uiBinder.createAndBindUi(this);
        
        type.addBeforeSelectionHandler(new BeforeSelectionHandler<ReportType>() {
            @Override
            public void onBeforeSelection(BeforeSelectionEvent<ReportType> event) {
                prevReportType = type.getCurrentValue();
            }
        });
        type.addSelectionHandler(new SelectionHandler<ReportType>() {
            @Override
            public void onSelection(SelectionEvent<ReportType> event) {
                reportTypeChanged(event.getSelectedItem());
            }
        });

        period.init(fromDateField, fromTimeField, toDateField, toTimeField);

        geoFencesPanel.setHeadingText(i18n.overlayType(UserSettings.OverlayType.GEO_FENCES));
        geoFences = new ListViewEditor<>(geoFencesList);
        devices = new ListViewEditor<Device>(devicesList);
        devicesList.getSelectionModel().addBeforeSelectionHandler(new BeforeSelectionHandler<Device>() {
            @Override
            public void onBeforeSelection(BeforeSelectionEvent<Device> event) {
                Device d = event.getItem();
                if(type.getValue() != null && type.getValue().isPremium() 
                        && d.getSubscriptionDaysLeft(new Date()) <= 0) {
                    event.cancel();
                }
            }
        });
        fromDate = new DateTimeEditor(fromDateField, fromTimeField);
        toDate = new DateTimeEditor(toDateField, toTimeField);

        driver.initialize(this);
        driver.edit(new Report());
        period.selectFirst();
        devices.setValue(new HashSet<>(deviceStore.getAll()));
        
        if(!ApplicationContext.getInstance().getUser().isPremium())
            generateButton.setEnabled(false);
    }

    public void show() {
        window.show();
    }

    @UiHandler("generateHtml")
    public void onGenerateHtmlSelected(SelectionEvent<Item> event) {
        generateReport(ReportFormat.HTML, false);
    }
    
    @UiHandler("generateCsv")
    public void onGenerateCsvSelected(SelectionEvent<Item> event) {
        generateReport(ReportFormat.CSV, false);
    }
    
    @UiHandler("previewButton")
    public void onPreviewClicked(SelectEvent event) {
        generateReport(ReportFormat.HTML, true);
    }
    
    private void generateReport(ReportFormat format, boolean isPreview) {

        Report report = driver.flush();
        report.setFormat(format);
        report.setPreview(isPreview);
        report.setRoute(route);
        
        int minHistory = 180;
        for(Device d: report.getDevices()) {
            if(d.getValidTo() == null || d.getValidTo().before(new Date()))
                minHistory = 2;
            else if(d.getHistoryLength() < minHistory)
                minHistory = d.getHistoryLength();
        }
        Date historyStart = new Date();
        CalendarUtil.addDaysToDate(historyStart, -minHistory);
        CalendarUtil.resetTime(historyStart);
        
        if(CalendarUtil.getDaysBetween(report.getFromDate(), report.getToDate()) > 31) {
            new AlertMessageBox(i18n.error(), i18n.errReportMax31Days()).show();
            return;
        } else if(report.getDevices().isEmpty()){
            new AlertMessageBox(i18n.error(), i18n.errNoReportDevicesSelected()).show();
            return;
        } else if(!ApplicationContext.getInstance().getUser().hasPermission(UserPermission.ALL_HISTORY) 
                && CalendarUtil.getDaysBetween(report.getFromDate(),historyStart) > 0) {
            new AlertMessageBox(i18n.error(), i18n.errNoSubscriptionMessage()).show();
            return;
        }

        if (!driver.hasErrors()) {
            reportHandler.onGenerate(report);
        }
    }

    private void reportTypeChanged(ReportType type) {
        geoFencesList.setEnabled(type != null && type.supportsGeoFences());
        includeMap.setEnabled(type != null && type.supportsMapDisplay());
        disableFilter.setEnabled(type != null && type.supportsFiltering());
        if (type != null
              && (name.getCurrentValue() == null
                  || name.getCurrentValue().trim().isEmpty()
                  || (prevReportType != null && i18n.reportType(prevReportType).equals(name.getCurrentValue())))) {
            name.setValue(i18n.reportType(type));
        }
        
        if(type.isPremium()) {
            Set<Device> sel = new HashSet<>();
            for(Device d : devices.getValue())
                if(d.getSubscriptionDaysLeft(new Date()) > 0) 
                    sel.add(d);
            devices.setValue(sel);
        }
        devicesList.refresh();
    }

    public void selectDevice(Device device) {
        devices.setValue(Collections.singleton(device));
    }
    
    public void selectRoute(Route route) {
        this.route = route;
        devices.setValue(null);
    }

    public void selectReportType(ReportType type) {
        this.type.setValue(type);
        reportTypeChanged(type);
    }

    public void selectPeriod(Date fromDate, Date toDate) {
        this.fromDate.setValue(fromDate);
        this.toDate.setValue(toDate);
    }

    public void selectPeriod(Period period) {
        this.period.setValue(period);
        this.period.update();
    }

    public void setDisableFilter(boolean b) {
        this.disableFilter.setValue(b);
    }
}
