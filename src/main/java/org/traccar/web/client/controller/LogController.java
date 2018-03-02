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

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.form.TextArea;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.model.LogService;
import org.traccar.web.client.model.LogServiceAsync;
import org.traccar.web.client.model.api.ApiAuditLogEntry;
import org.traccar.web.client.model.api.ApiAuditLogEntryProperties;
import org.traccar.web.client.model.api.AuditLogService;
import org.traccar.web.client.utils.ClientLogUtils;
import org.traccar.web.client.view.AuditViewDialog;
import org.traccar.web.client.view.NavView;
import org.traccar.web.client.view.TrackerServerLogViewDialog;

public class LogController implements NavView.LogHandler {
    final LogServiceAsync service = GWT.create(LogService.class);
    final Messages i18n = GWT.create(Messages.class);
    AuditLogService audit = new AuditLogService();

    @Override
    public void onShowTrackerServerLog() {
        new TrackerServerLogViewDialog(i18n.trackerServerLog(), new TrackerServerLogViewDialog.LogHandler() {
            @Override
            public void onLoad(short size, final TextArea logArea) {
                service.getTrackerServerLog(size, new BaseAsyncCallback<String>(i18n) {
                    @Override
                    public void onSuccess(String result) {
                        logArea.setText(result);
                    }
                });
            }
        }).show();
    }

    @Override
    public void onShowWrapperLog() {
        new TrackerServerLogViewDialog(i18n.wrapperLog(), new TrackerServerLogViewDialog.LogHandler() {
            @Override
            public void onLoad(short size, final TextArea logArea) {
                service.getWrapperLog(size, new BaseAsyncCallback<String>(i18n) {
                    @Override
                    public void onSuccess(String result) {
                        logArea.setText(result);
                    }
                });
            }
        }).show();
    }

    @Override
    public void onShowAuditLog() {
        ApiAuditLogEntryProperties props = GWT.create(ApiAuditLogEntryProperties.class);
        final ListStore<ApiAuditLogEntry> store = new ListStore<>(props.id());
        store.addSortInfo(new StoreSortInfo<>(props.time(), SortDir.DESC));
        new AuditViewDialog(store, new AuditViewDialog.LogHandler() {
            @Override
            public void onLoad(Date fromDate, Date toDate) {
                audit.get(fromDate, toDate, new MethodCallback<List<ApiAuditLogEntry>>() {
                    @Override
                    public void onFailure(Method method, Throwable exception) {
                        new AlertMessageBox(i18n.error(), i18n.errRemoteCall()).show();
                        ClientLogUtils.logExceptionGwtCompatible(Level.SEVERE, exception,
                                "LogController:onShowAuditLog:onLoad:onFailure");
                    }

                    @Override
                    public void onSuccess(Method method, List<ApiAuditLogEntry> response) {
                        store.clear();
                        store.addAll(response);
                        store.applySort(true);
                    }
                });
            }
        }).show();
    }
}
