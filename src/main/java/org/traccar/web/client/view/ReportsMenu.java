/*
 * Copyright 2016 Vitaly Litvak (vitavaque@gmail.com)
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
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseStoreHandlers;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.ReportType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.traccar.web.client.ApplicationContext;

public class ReportsMenu extends Menu {
    public interface ReportHandler {
        ReportsDialog createDialog();
    }

    public interface ReportSettingsHandler {
        void setSettings(ReportsDialog dialog);
    }

    private final Messages i18n = GWT.create(Messages.class);
    private final ListStore<Report> reports;
    private final Map<String, MenuItem> userReports = new HashMap<>();
    private final ReportHandler reportHandler;
    private final ReportSettingsHandler reportSettingsHandler;

    public ReportsMenu(ListStore<Report> reports,
                       final ReportHandler reportHandler,
                       final ReportSettingsHandler reportSettingsHandler) {
        this.reports = reports;
        this.reportHandler = reportHandler;
        this.reportSettingsHandler = reportSettingsHandler;
        ReportType[] available = ApplicationContext.getInstance().getUser()
                .isPremium() ? ReportType.values() : ReportType.getFreeTypes();
        for (final ReportType type : available) {
            MenuItem reportItem = new MenuItem(i18n.reportType(type));
            reportItem.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    if(!ApplicationContext.getInstance().getUser().isPremium()
                            && type != ReportType.GENERAL_INFORMATION 
                            && type != ReportType.EVENTS) {
                        new AlertMessageBox(i18n.error(), i18n.reportsForPremium()).show();
                    } else{
                        ReportsDialog dialog = reportHandler.createDialog();
                        dialog.selectReportType(type);
                        reportSettingsHandler.setSettings(dialog);
                        dialog.show();
                    }
                }
            });
            add(reportItem);
        }
    }
}
