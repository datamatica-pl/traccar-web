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

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.api.ApiAuditLogEntry;
import org.traccar.web.client.model.api.ApiAuditLogEntryProperties;

public class AuditViewDialog {
    
    private static AuditViewDialogUiBinder uiBinder = GWT.create(AuditViewDialogUiBinder.class);
    
    interface AuditViewDialogUiBinder extends UiBinder<Widget, AuditViewDialog> {
    }
    
    @UiField
    Window window;
    
    @UiField
    DateField fromDate;
    
    @UiField
    DateField toDate;
    
    @UiField(provided = true)
    Grid<ApiAuditLogEntry> logGrid;
    
    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);
    
    private LogHandler handler;
    private ListStore<ApiAuditLogEntry> store;
    
    public AuditViewDialog(final ListStore<ApiAuditLogEntry> store, final LogHandler handler) {
        this.handler = handler;
        this.store = store;
        
        ColumnModel<ApiAuditLogEntry> cm = createColumnModel();
        logGrid = new Grid<>(store, cm);
        uiBinder.createAndBindUi(this);
        
        Date today = new Date(), weekAgo = new Date();
        CalendarUtil.addDaysToDate(weekAgo, -7);
        CalendarUtil.resetTime(weekAgo);
        fromDate.setValue(weekAgo);
        toDate.setValue(today);
        handler.onLoad(weekAgo, today);
    }
    
    private ColumnModel<ApiAuditLogEntry> createColumnModel() {
        ApiAuditLogEntryProperties props = GWT.create(ApiAuditLogEntryProperties.class);
        List<ColumnConfig<ApiAuditLogEntry, ?>> ccList = new ArrayList<>();
        
        ColumnConfig<ApiAuditLogEntry, String> cEvent = new ColumnConfig<>(props.note(), 400, i18n.auditLogEvent());
        ccList.add(cEvent);
        
        ColumnConfig<ApiAuditLogEntry, String> cAgent = new ColumnConfig<>(props.agentLogin(), 100, i18n.auditLogAgent());
        ccList.add(cAgent);
        
        ColumnConfig<ApiAuditLogEntry, Date> cTime = new ColumnConfig<>(props.time(), 100, i18n.time());
        cTime.setCell(new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT)));
        ccList.add(cTime);
        
        for(ColumnConfig cc : ccList) {
            cc.setHideable(false);
            cc.setFixed(true);
            cc.setResizable(false);
        }
        
        return new ColumnModel<>(ccList);
    }
    
    public void show() {
        window.show();
    }
    
    @UiHandler("show")
    public void onShowClicked(SelectEvent event) {
        handler.onLoad(fromDate.getDatePicker().getValue(), toDate.getDatePicker().getValue());
    }
    
    public interface LogHandler {
        void onLoad(Date fromDate, Date toDate);
    }
}
