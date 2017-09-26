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
package org.traccar.web.client.controller;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.view.ReportsDialog;
import org.traccar.web.client.view.ReportsMenu;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Report;

import java.util.List;
import java.util.Map;
import org.fusesource.restygwt.client.Method;
import org.traccar.web.client.model.api.ApiJsonCallback;
import org.traccar.web.client.model.api.ApiMethodCallback;
import org.traccar.web.client.model.api.ApiReport;
import org.traccar.web.client.model.api.ApiRequestCallback;
import org.traccar.web.client.model.api.ReportsService;
import pl.datamatica.traccar.model.ReportFormat;

public class ReportsController implements ContentController, ReportsMenu.ReportHandler {
    private final Messages i18n = GWT.create(Messages.class);
    private final ReportMapper reportMapper = GWT.create(ReportMapper.class);
    private final ListStore<Report> reportStore;
    private final ListStore<Device> deviceStore;
    private final ListStore<GeoFence> geoFenceStore;
    
    private final Map<Long, Device> devMap;
    private final Map<Long, GeoFence> gfMap;

    interface ReportMapper extends ObjectMapper<ApiReport> {}

    public interface ReportHandler {
        void reportAdded(Report report);
        void reportUpdated(Report report);
        void reportRemoved(Report report);
    }

    public ReportsController(ListStore<Report> reportStore, ListStore<Device> deviceStore, ListStore<GeoFence> geoFenceStore) {
        this.reportStore = reportStore;
        this.deviceStore = deviceStore;
        this.geoFenceStore = geoFenceStore;
        
        this.devMap = new HashMap<>();
        this.gfMap = new HashMap<>();
    }
    
    private boolean isEnabled() {
        Date now = new Date();
        for(Device d : deviceStore.getAll())
            if(d.getSubscriptionDaysLeft(new Date()) > 0)
                return true;
        return false;
    }

    @Override
    public ContentPanel getView() {
        return null;
    }

    @Override
    public void run() {
        final ReportsService service = new ReportsService();
        for(Device d : deviceStore.getAll())
            devMap.put(d.getId(), d);
        for(GeoFence gf : geoFenceStore.getAll())
            gfMap.put(gf.getId(), gf);
        service.getReports(new ApiJsonCallback(i18n) {
            @Override
            public void onSuccess(Method method, JSONValue response) {
                List<Report> reports = new ArrayList<>();
                JSONArray arr = response.isArray();
                for(int i=0;i<arr.size();++i) {
                    ApiReport ar = new ApiReport(arr.get(i).isObject());
                    reports.add(ar.toReport(devMap, gfMap));
                }
                reportStore.addAll(reports);
            }
            
        });
    }

    public void generate(Report report) {
        String format = report.getFormat() == ReportFormat.HTML ? ".html" : ".csv";
        
        FormPanel form = new FormPanel("_blank");
        final String reportNameUrl = report.getType().toString().toLowerCase();
        form.setVisible(false);
        form.setAction("api/v1/reports/generate" + (report.isPreview() ? "/" + reportNameUrl + format : ""));
        form.setMethod(FormPanel.METHOD_POST);
        form.setEncoding(FormPanel.ENCODING_URLENCODED);
        HorizontalPanel container = new HorizontalPanel();
        container.add(new Hidden("report", reportMapper.write(new ApiReport(report))));
        container.add(new Hidden("lang", LocaleInfo.getCurrentLocale().getLocaleName()));
        form.add(container);
        RootPanel.get().add(form);
        try {
            form.submit();
        } finally {
            RootPanel.get().remove(form);
        }
    }

    @Override
    public ReportsDialog createDialog() {
        if(!isEnabled())
            return null;
        final ReportsService service = new ReportsService();
        return new ReportsDialog(reportStore, deviceStore, geoFenceStore, new ReportsDialog.ReportHandler() {
            @Override
            public void onAdd(Report report, final ReportHandler handler) {
                service.createReport(new ApiReport(report), new ApiJsonCallback(i18n) {
                    @Override
                    public void onSuccess(Method method, JSONValue response) {
                        handler.reportAdded(new ApiReport(response.isObject())
                                .toReport(devMap, gfMap));
                    }
                });
            }

            @Override
            public void onUpdate(final Report report, final ReportHandler handler) {
                service.updateReport(report.getId(), new ApiReport(report), 
                        new ApiRequestCallback(i18n) {
                            @Override
                            public void onSuccess(String response) {
                                handler.reportUpdated(report);
                            }
                    
                });
            }

            @Override
            public void onRemove(final Report report, final ReportHandler handler) {
                service.removeReport(report.getId(), new ApiMethodCallback<Void>(i18n) {
                        @Override
                        public void onSuccess(Method method, Void response) {
                            handler.reportRemoved(report);
                        }
                });
            }

            @Override
            public void onGenerate(Report report) {
                generate(report);
            }
        });
    }
}
