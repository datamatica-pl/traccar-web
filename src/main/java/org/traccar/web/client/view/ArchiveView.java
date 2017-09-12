/*
 * Copyright 2013 Anton Tananaev (anton.tananaev@gmail.com)
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

import pl.datamatica.traccar.model.PositionIconType;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.Device;
import static org.traccar.web.client.DateTimeFieldUtil.getCombineDate;

import java.util.*;

import com.google.gwt.event.logical.shared.*;
import com.sencha.gxt.data.shared.event.StoreUpdateEvent;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.CloseEvent;
import com.sencha.gxt.widget.core.client.form.*;
import com.sencha.gxt.widget.core.client.menu.*;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.ArchiveStyle;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseStoreHandlers;
import org.traccar.web.client.model.DeviceProperties;
import org.traccar.web.client.widget.PeriodComboBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.event.StoreHandlers;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

public class ArchiveView implements SelectionChangedEvent.SelectionChangedHandler<Position> {

    private static ArchiveViewUiBinder uiBinder = GWT.create(ArchiveViewUiBinder.class);

    public ArchiveStyle style = new ArchiveStyle();

    interface ArchiveViewUiBinder extends UiBinder<Widget, ArchiveView> {
    }

    public interface ArchiveHandler {
        void onSelected(Position position);
        void onLoad(Device device, Date from, Date to, boolean filter, ArchiveStyle style);
        void onSnapToRoads(boolean snapToRoads);
        void onFilterSettings();
        void onClear(Device device);
        void onChangeArchiveMarkerType(PositionIconType newMarkerType);
    }

    private ArchiveHandler archiveHandler;

    @UiField
    ContentPanel contentPanel;

    public ContentPanel getView() {
        return contentPanel;
    }

    ListStore<Device> deviceStore;

    @UiField
    DateField fromDate;

    @UiField
    TimeField fromTime;

    @UiField
    DateField toDate;

    @UiField
    TimeField toTime;

    @UiField(provided = true)
    ComboBox<Device> deviceCombo;

    @UiField(provided = true)
    PeriodComboBox periodCombo;

    @UiField(provided = true)
    TabPanel devicesTabs;

    @UiField
    TextButton reportButton;

    final Map<Long, ArchivePanel> archivePanels;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    public ArchiveView(final ArchiveHandler archiveHandler,
                       ListStore<Device> deviceStore,
                       ListStore<Report> reportStore,
                       ReportsMenu.ReportHandler reportHandler) {
        this.archiveHandler = archiveHandler;
        deviceStore.addStoreHandlers(deviceStoreHandlers);
        this.deviceStore = deviceStore;

        DeviceProperties deviceProperties = GWT.create(DeviceProperties.class);
        deviceCombo = new ComboBox<>(deviceStore, deviceProperties.label());

		periodCombo = new PeriodComboBox();

        // Markers
        for (Object[] obj : new Object[][] { { i18n.noMarkers(), null },
                                             { i18n.standardMarkers(), PositionIconType.iconArchive },
                                             { i18n.reducedMarkers(), PositionIconType.dotArchive } }) {
            CheckMenuItem item = new CheckMenuItem((String) obj[0]);
            final PositionIconType iconType = (PositionIconType) obj[1];
            item.setGroup("markers");
            item.setChecked(iconType == ApplicationContext.getInstance().getUserSettings().getArchiveMarkerType());
            if (item.isChecked()) {
                style.setIconType(iconType);
            }
            item.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    style.setIconType(iconType);
                    archiveHandler.onChangeArchiveMarkerType(iconType);
                }
            });
        }

        devicesTabs = new TabPanel();
        archivePanels = new HashMap<>();

        uiBinder.createAndBindUi(this);

        // Initialize with current time
        long min = 60 * 1000;
        Date now = new Date();
        Date to = new Date(((now.getTime() + 15 * min) / (15 * min)) * 15 * min);
        Date from = new Date(to.getTime() - 60 * min);
        fromDate.setValue(from);
        fromTime.setValue(from);
        toDate.setValue(to);
        toTime.setValue(to);

        periodCombo.init(fromDate, fromTime, toDate, toTime);

        User user = ApplicationContext.getInstance().getUser();
        reportButton.setVisible(user.hasPermission(UserPermission.REPORTS));
        reportButton.setMenu(new ReportsMenu(reportStore, reportHandler, new ReportsMenu.ReportSettingsHandler() {
            @Override
            public void setSettings(ReportsDialog dialog) {
                if (deviceCombo.getCurrentValue() != null) {
                    dialog.selectDevice(deviceCombo.getCurrentValue());
                }
                if (periodCombo.getCurrentValue() == null) {
                    dialog.selectPeriod(getCombineDate(fromDate, fromTime), getCombineDate(toDate, toTime));
                } else {
                    dialog.selectPeriod(periodCombo.getCurrentValue());
                }
            }
        }));
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent<Position> event) {
        if (event.getSelection().isEmpty()) {
            archiveHandler.onSelected(null);
        } else {
            archiveHandler.onSelected(event.getSelection().get(0));
        }
    }

    @UiHandler("loadButton")
    public void onLoadClicked(SelectEvent event) {
        archiveHandler.onLoad(
                deviceCombo.getValue(),
                getCombineDate(fromDate, fromTime),
                getCombineDate(toDate, toTime),
                true,
                new ArchiveStyle(style)
        );
    }

    @UiHandler("clearButton")
    public void onClearClicked(SelectEvent event) {
        for (Map.Entry<Long, ArchivePanel> entry : archivePanels.entrySet()) {
            Device device = deviceStore.findModelWithKey(entry.getKey().toString());
            archiveHandler.onClear(device);
            devicesTabs.remove(entry.getValue().getContentPanel());
        }
        archivePanels.clear();
    }

    @UiHandler("filterButton")
    public void onFilterClicked(SelectEvent event) {
        archiveHandler.onFilterSettings();
    }

    private StoreHandlers<Device> deviceStoreHandlers = new BaseStoreHandlers<Device>() {

        @Override
        public void onAnything() {
            Device oldDevice = deviceCombo.getValue();
            if (oldDevice != null) {
                deviceCombo.setValue(deviceStore.findModel(oldDevice));
            } else if (deviceStore.size() == 1) {
                deviceCombo.setValue(deviceStore.get(0));
            }
        }

        @Override
        public void onUpdate(StoreUpdateEvent<Device> event) {
            super.onUpdate(event);
            if (event.getItems() != null) {
                for (Device device : event.getItems()) {
                    ArchivePanel panel = archivePanels.get(device.getId());
                    if (panel != null) {
                        TabItemConfig config = devicesTabs.getConfig(panel.getContentPanel());
                        config.setText(device.getName());
                        devicesTabs.update(panel.getContentPanel(), config);
                    }
                }
            }
        }
    };

    public void selectPosition(Position position) {
        getArchivePanel(position.getDevice()).selectPosition(position);
    }

    public void selectDevice(Device device) {
        deviceCombo.setValue(device, false);
    }

    public void showPositions(Device device, List<Position> positions) {
        ArchivePanel panel = getArchivePanel(device);
        devicesTabs.setActiveWidget(panel.getContentPanel());
        panel.setPositions(positions);
    }

    private ArchivePanel getArchivePanel(Device device) {
        ArchivePanel panel = archivePanels.get(device.getId());
        if (panel == null) {
            panel = new ArchivePanel(archiveHandler);
            archivePanels.put(device.getId(), panel);
            devicesTabs.add(panel.getContentPanel(), new TabItemConfig(device.getName(), true));
        }
        return panel;
    }

    @UiHandler("devicesTabs")
    public void onDeviceTabClosed(CloseEvent<Widget> event) {
        for (Map.Entry<Long, ArchivePanel> entry : archivePanels.entrySet()) {
            if (entry.getValue().getContentPanel().equals(event.getItem())) {
                Device device = deviceStore.findModelWithKey(entry.getKey().toString());
                archiveHandler.onClear(device);
                archivePanels.remove(entry.getKey());
                break;
            }
        }
    }
}
