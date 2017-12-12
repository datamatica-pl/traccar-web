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

import pl.datamatica.traccar.model.UserSettings;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.Group;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.GroupedDevice;
import pl.datamatica.traccar.model.Device;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ClientBundle.Source;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.event.StoreAddEvent;
import com.sencha.gxt.data.shared.event.StoreRemoveEvent;
import com.sencha.gxt.data.shared.event.StoreUpdateEvent;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.BeforeShowContextMenuEvent;
import com.sencha.gxt.widget.core.client.event.BeforeShowContextMenuEvent.BeforeShowContextMenuHandler;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.HeaderClickEvent;
import com.sencha.gxt.widget.core.client.event.HeaderClickEvent.HeaderClickHandler;
import com.sencha.gxt.widget.core.client.event.RowMouseDownEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.StoreFilterField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.tree.Tree;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;
import com.sencha.gxt.widget.core.client.treegrid.TreeGridView;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseStoreHandlers;
import org.traccar.web.client.model.DeviceStore;
import org.traccar.web.client.model.GeoFenceProperties;
import org.traccar.web.client.model.GroupStore;
import org.traccar.web.client.state.DeviceVisibilityChangeHandler;
import org.traccar.web.client.state.DeviceVisibilityHandler;

import java.util.*;
import pl.datamatica.traccar.model.ReportType;
import pl.datamatica.traccar.model.Route;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

public class DeviceView implements RowMouseDownEvent.RowMouseDownHandler, CellDoubleClickEvent.CellDoubleClickHandler {

    private static DeviceViewUiBinder uiBinder = GWT.create(DeviceViewUiBinder.class);
    private static final int IGNITION_EXPIRATION_SECONDS = 900;

    interface DeviceViewUiBinder extends UiBinder<Widget, DeviceView> {
    }

    public interface DeviceHandler {
        void onSelected(Device device);
        void onSelected(Device device, boolean zoomIn);
        void onAdd();
        void onEdit(Device device);
        void onShare(Device device);
        void onRemove(Device device);
        void onMouseOver(int mouseX, int mouseY, Device device);
        void onMouseOut(int mouseX, int mouseY, Device device);
        void doubleClicked(Device device);
        void onClearSelection();
        void onShowAlarms(Device device);
    }

    public interface GeoFenceHandler {
        void onAdd();
        void onEdit(GeoFence geoFence);
        void onRemove(GeoFence geoFence);
        void onSelected(GeoFence geoFence);
        void onShare(GeoFence geoFence);
        void setGeoFenceListView(ListView<GeoFence, String> geoFenceListView);
    }
    
    public interface RouteHandler {
        void onAdd();
        void onEdit(Route selectedItem);
        void onRemove(Route selectedItem);
        void onSelected(Route route);
        public void onAbort(Route selectedItem);
        public void onArchivedChanged(Route selectedItem, boolean archived);
        public void onShowArchived();
    }

    public interface CommandHandler {
        void onCommand(Device device);
    }

    private static class GroupsHandler extends BaseStoreHandlers {
        private final ListStore<Device> globalDeviceStore;
        private final DeviceStore deviceStore;
        private final GroupStore groupStore;
        private final List<Device> pendingDevices = new ArrayList<>();

        private GroupsHandler(ListStore<Device> globalDeviceStore,
                              DeviceStore deviceStore,
                              GroupStore groupStore) {
            this.globalDeviceStore = globalDeviceStore;
            this.deviceStore = deviceStore;
            this.groupStore = groupStore;

            this.globalDeviceStore.addStoreHandlers(this);
            this.groupStore.addStoreHandlers(this);
        }

        @Override
        public void onAdd(StoreAddEvent event) {
            groupsAdded(groups(event.getItems()));
            devicesAdded(devices(event.getItems()));
        }

        @Override
        public void onUpdate(StoreUpdateEvent event) {
            groupsUpdated(groups(event.getItems()));
            devicesUpdated(devices(event.getItems()));
        }

        @Override
        public void onRemove(StoreRemoveEvent event) {
            GroupedDevice node = (GroupedDevice) event.getItem();
            if (deviceStore.contains(node)) {
                pendingDevices.addAll(devices(node));
                Group parent = (Group) deviceStore.getParent(node);
                if(!deviceStore.isGroup(node))
                    deviceStore.remove(node);
                else {
                    Group g = (Group)node;
                    if(g.isOwned())
                        deviceStore.remove(g);
                    else {
                        g.setShared(false);
                        deviceStore.update(g);
                    }
                }
                if (parent != null) {
                    removeGroupsIfEmpty(parent);
                }
            }
        }

        List<Group> groups(List items) {
            return items.isEmpty() || !(items.get(0) instanceof Group) ? Collections.<Group>emptyList() : (List<Group>) items;
        }

        List<Device> devices(List items) {
            return items.isEmpty() || !(items.get(0) instanceof Device) ? Collections.<Device>emptyList() : (List<Device>) items;
        }

        void groupsAdded(List<Group> groups) {
            List<Device> withoutGroups = new ArrayList<>();
            for (Iterator<Device> it = pendingDevices.iterator(); it.hasNext(); ) {
                Device device = it.next();
                if (device.getGroup() == null) {
                    withoutGroups.add(device);
                } else {
                    if (addDeviceGroups(device)) {
                        deviceStore.add(device.getGroup(), device);
                        it.remove();
                    }
                }
            }
            if (withoutGroups.size() == pendingDevices.size()) {
                for (Device device : pendingDevices) {
                    deviceStore.add(device);
                }
                pendingDevices.clear();
            }
        }

        void groupsUpdated(List<Group> groups) {
            Set<Device> devicesToUpdate = new HashSet<>();
            for (Group group : groups) {
                if (!deviceStore.contains(group)) {
                    continue;
                }
                // check parents
                Group oldParent = (Group) deviceStore.getParent(group);
                Group newParent = groupStore.getParent(group);
                if (Objects.equals(oldParent, newParent)) {
                    deviceStore.update(group);
                } else {
                    devicesToUpdate.addAll(devices(group));
                    groupStore.remove(group);
                }
            }

            devicesAdded(new ArrayList<>(devicesToUpdate));
        }

        List<Device> devices(GroupedDevice node) {
            if (deviceStore.contains(node)) {
                List<Device> result = new ArrayList<>();
                for (GroupedDevice child : deviceStore.getAllChildren(node)) {
                    if (child instanceof Device) {
                        result.add((Device) child);
                    }
                }
                return result;
            } else {
                return Collections.emptyList();
            }
        }

        boolean addDeviceGroups(Device device) {
            return addGroupsHierarchy(device.getGroup());
        }

        boolean addGroupsHierarchy(Group group) {
            List<Group> groupsHierarchy = new ArrayList<>();
            Group nextParent = group;
            while (nextParent != null) {
                if (!groupStore.contains(nextParent)) {
                    return false;
                }
                groupsHierarchy.add(0, nextParent);
                nextParent = groupStore.getParent(nextParent);
            }
            for (Group nextGroup : groupsHierarchy) {
                if (!deviceStore.contains(nextGroup)) {
                    Group parent = groupStore.getParent(nextGroup);
                    if (parent == null) {
                        deviceStore.add(nextGroup);
                    } else {
                        deviceStore.add(parent, nextGroup);
                    }
                }
            }
            return true;
        }

        void devicesAdded(List<Device> devices) {
            Comparator<GroupedDevice> byName = new Comparator<GroupedDevice>() {
                @Override
                public int compare(GroupedDevice o1, GroupedDevice o2) {
                    String n1 = o1.getName() == null ? "" : o1.getName();
                    String n2 = o2.getName() == null ? "" : o2.getName();
                    return n1.compareTo(n2);
                }
            };

            Map<Group, List<Device>> byGroup = new HashMap<>();
            List<Device> deviceWithoutGroups = new ArrayList<>();
            for (Device device : devices) {
                if (device.getGroup() == null) {
                    deviceWithoutGroups.add(device);
                } else {
                    List<Device> groupDevices = byGroup.get(device.getGroup());
                    if (groupDevices == null) {
                        groupDevices = new ArrayList<>();
                        byGroup.put(device.getGroup(), groupDevices);
                    }
                    groupDevices.add(device);
                }
            }

            List<Group> sortedGroups = new ArrayList<>(byGroup.keySet());
            Collections.sort(sortedGroups, byName);
            Collections.sort(deviceWithoutGroups, byName);

            for (Group group : sortedGroups) {
                List<Device> groupDevices = byGroup.get(group);
                Collections.sort(groupDevices, byName);
                if (addGroupsHierarchy(group)) {
                    for (Device device : groupDevices) {
                        deviceStore.add(group, device);
                    }
                } else {
                    pendingDevices.addAll(groupDevices);
                }
            }

            if (pendingDevices.isEmpty()) {
                for (Device device : deviceWithoutGroups) {
                    deviceStore.add(device);
                }
            } else {
                pendingDevices.addAll(deviceWithoutGroups);
            }
        }

        void devicesUpdated(List<Device> devices) {
            for (Device device : devices) {
                if (deviceStore.contains(device)) {
                    Group oldGroup = (Group) deviceStore.getParent(device);
                    Group newGroup = device.getGroup();
                    if (Objects.equals(oldGroup, newGroup)) {
                        deviceStore.update(device);
                    } else {
                        deviceStore.remove(device);
                        devicesAdded(Collections.singletonList(device));
                        removeGroupsIfEmpty(oldGroup);
                    }
                } else {
                    if (device.getGroup() == null) {
                        deviceStore.add(device);
                    } else {
                        devicesAdded(Collections.singletonList(device));
                    }
                }
            }
        }

        void removeGroupsIfEmpty(Group group) {
            Group parent = group;
            while (parent != null && devices(parent).isEmpty()) {
                Group nextParent = (Group) deviceStore.getParent(parent);
                deviceStore.remove(parent);
                parent = nextParent;
            }
        }
    }

    private static class DeviceGridView extends TreeGridView<GroupedDevice> {
        Messages i18n = GWT.create(Messages.class);

        final DeviceVisibilityHandler deviceVisibilityHandler;
        final GroupStore groupStore;

        private DeviceGridView(DeviceVisibilityHandler deviceVisibilityHandler, GroupStore groupStore) {
            this.deviceVisibilityHandler = deviceVisibilityHandler;
            this.groupStore = groupStore;
        }

        @Override
        protected Menu createContextMenu(int colIndex) {
            Menu menu = super.createContextMenu(colIndex);
            if (colIndex == 0) {
                CheckMenuItem idle = new CheckMenuItem(capitalize(i18n.idle()));
                idle.setChecked(!deviceVisibilityHandler.getHideIdle());
                idle.addSelectionHandler(new SelectionHandler<Item>() {
                    @Override
                    public void onSelection(SelectionEvent<Item> event) {
                        deviceVisibilityHandler.setHideIdle(!deviceVisibilityHandler.getHideIdle());
                    }
                });
                menu.add(idle);

                CheckMenuItem moving = new CheckMenuItem(capitalize(i18n.moving()));
                moving.setChecked(!deviceVisibilityHandler.getHideMoving());
                moving.addSelectionHandler(new SelectionHandler<Item>() {
                    @Override
                    public void onSelection(SelectionEvent<Item> event) {
                        deviceVisibilityHandler.setHideMoving(!deviceVisibilityHandler.getHideMoving());
                    }
                });
                menu.add(moving);

                CheckMenuItem offline = new CheckMenuItem(capitalize(i18n.offline()));
                offline.setChecked(!deviceVisibilityHandler.getHideOffline());
                offline.addSelectionHandler(new SelectionHandler<Item>() {
                    @Override
                    public void onSelection(SelectionEvent<Item> event) {
                        deviceVisibilityHandler.setHideOffline(!deviceVisibilityHandler.getHideOffline());
                    }
                });
                menu.add(offline);

                CheckMenuItem online = new CheckMenuItem(capitalize(i18n.online()));
                online.setChecked(!deviceVisibilityHandler.getHideOnline());
                online.addSelectionHandler(new SelectionHandler<Item>() {
                    @Override
                    public void onSelection(SelectionEvent<Item> event) {
                        deviceVisibilityHandler.setHideOnline(!deviceVisibilityHandler.getHideOnline());
                    }
                });
                menu.add(online);

                List<Group> groupsList = groupStore.toList();
                if (!groupsList.isEmpty()) {
                    MenuItem groups = new MenuItem(i18n.groups());
                    groups.setSubMenu(new Menu());
                    for (final Group group : groupsList) {
                        SafeHtmlBuilder name = new SafeHtmlBuilder();
                        for (int i = 0; i < groupStore.getDepth(group); i++) {
                            name.appendHtmlConstant("&nbsp;&nbsp;");
                        }
                        name.appendEscaped(group.getName());
                        CheckMenuItem groupItem = new CheckMenuItem();
                        groupItem.setHTML(name.toSafeHtml());
                        groupItem.setChecked(!deviceVisibilityHandler.isHiddenGroup(group.getId()));
                        groupItem.addSelectionHandler(new SelectionHandler<Item>() {
                            @Override
                            public void onSelection(SelectionEvent<Item> event) {
                                if (deviceVisibilityHandler.isHiddenGroup(group.getId())) {
                                    deviceVisibilityHandler.removeHiddenGroup(group.getId());
                                } else {
                                    deviceVisibilityHandler.addHiddenGroup(group.getId());
                                }
                            }
                        });
                        groups.getSubMenu().add(groupItem);
                    }
                    menu.add(groups);
                }
            }
            return menu;
        }

        private String capitalize(String s) {
            return Character.isUpperCase(s.charAt(0))
                    ? s
                    : (Character.toUpperCase(s.charAt(0)) + s.substring(1, s.length()));
        }
    }

    private static class DeviceOnlyCheckBoxCell extends CheckBoxCell {
        private final DeviceStore deviceStore;

        private DeviceOnlyCheckBoxCell(DeviceStore deviceStore) {
            this.deviceStore = deviceStore;
        }

        @Override
        public void render(Context context, Boolean value, SafeHtmlBuilder sb) {            
            if (isDevice(context)) {
                super.render(context, value, sb);
            }
        }

        @Override
        public void onBrowserEvent(Context context, Element parent, Boolean value, NativeEvent event, ValueUpdater<Boolean> valueUpdater) {
            if(isDevice(context))
                super.onBrowserEvent(context, parent, value, event, valueUpdater);
        }
        
        private boolean isDevice(Context context) {
            GroupedDevice item = deviceStore.findModelWithKey((String)context.getKey());
            return item instanceof Device;
        }
    }
    
    private static interface IGroupedDeviceBinding {
        public void bindName(SafeHtmlBuilder sb);
        public void bindIcons(SafeHtmlBuilder sb);
    }
    
    static class GroupedDeviceBinding implements IGroupedDeviceBinding {
        interface Templates extends SafeHtmlTemplates {
            @Template("<div style=\"float: left; margin: auto 0.1cm\" title=\"{1}\" name=\"{2}\">{0}</div>")
            SafeHtml cell(SafeHtml value, String title, String name);
        }
        private static final Templates templates = GWT.create(Templates.class);
        private static final Messages i18n = GWT.create(Messages.class);
        
        private ImageResource ignition;
        private ImageResource alarms;
        private String name;
        
        private Device device;
        
        public GroupedDeviceBinding(GroupedDevice groupedDevice, Resources resources) {
            name = groupedDevice.getName();
            if(groupedDevice instanceof Device) {
                init((Device)groupedDevice, resources);
            }
        }
        
        private void init(Device device, Resources resources) {
            this.device = device;
            ignition = resources.ignitionDisabled();
            if(device.getIgnition() != null && device.getIgnitionTime() != null
                && device.getIgnition()) {
                // Set ignition to enabled if device has been seen lesss than 15 seconds ago
                long ignitionTime = device.getIgnitionTime().getTime();
                long currentTime = new Date().getTime();
                long lastPositionSecondsAgo = (currentTime - ignitionTime)/1000;
                if (lastPositionSecondsAgo <= IGNITION_EXPIRATION_SECONDS) {
                    ignition = resources.ignitionEnabled();
                }
            }
            alarms = device.hasUnreadAlarms()? resources.speedAlarmActive() : resources.speedAlarmInactive();
        }
        
        @Override
        public void bindName(SafeHtmlBuilder sb) {
            sb.appendEscaped(name);
        }
        
        @Override
        public void bindIcons(SafeHtmlBuilder sb) {
            if(ApplicationContext.getInstance().getUser().hasPermission(UserPermission.ALERTS_READ))
                appendIfExists(sb, alarms, i18n.alarmIconHint(), "alarms");
            appendIfExists(sb, ignition, i18n.ignition(), "ignition");
        }
        
        private Device getDevice() {
            return device;
        }

        private void appendIfExists(SafeHtmlBuilder sb, ImageResource image, String title, String name) {
            if(image != null) {
                sb.append(templates.cell(AbstractImagePrototype.create(image).getSafeHtml(), title, name));
            }
        }
    }

    private final DeviceHandler deviceHandler;

    private final GeoFenceHandler geoFenceHandler;

    private final CommandHandler commandHandler;
    
    private final RouteHandler routeHandler;

    @UiField
    ContentPanel contentPanel;

    public ContentPanel getView() {
        return contentPanel;
    }

    @UiField
    ToolBar toolbar;

    @UiField
    TextButton addButton;

    @UiField(provided = true)
    TabPanel objectsTabs;
    
    @UiField
    VerticalLayoutContainer deviceList;

    ColumnModel<GroupedDevice> columnModel;

    final DeviceStore deviceStore;

    @UiField(provided = true)
    StoreFilterField<GroupedDevice> deviceFilter;

    @UiField(provided = true)
    TreeGrid<GroupedDevice> grid;

    TreeGridView<GroupedDevice> view;

    @UiField
    VerticalLayoutContainer geoFenceList;
    
    @UiField(provided = true)
    TabItemConfig geoFencesTabConfig;

    @UiField(provided = true)
    ListStore<GeoFence> geoFenceStore;

    @UiField(provided = true)
    ListView<GeoFence, String> geoFenceListView;
    
    @UiField(provided = true)
    StoreFilterField<GeoFence> gfFilter;
    
    @UiField(provided=true)
    TabItemConfig tracksTabConfig;
    
    @UiField
    VerticalLayoutContainer routeList;
    
    @UiField(provided = true)
    Grid<Route> routeGrid;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    public DeviceView(final DeviceHandler deviceHandler,
                      final GeoFenceHandler geoFenceHandler,
                      final CommandHandler commandHandler,
                      final RouteHandler routeHandler,
                      final DeviceVisibilityHandler deviceVisibilityHandler,
                      final ListStore<Device> globalDeviceStore,
                      final ListStore<GeoFence> geoFenceStore,
                      GroupStore groupStore,
                      final ListStore<Report> reportStore,
                      ListStore<Route> routeStore,
                      final ReportsMenu.ReportHandler reportHandler) {
        this.deviceHandler = deviceHandler;
        this.geoFenceHandler = geoFenceHandler;
        this.commandHandler = commandHandler;
        this.geoFenceStore = geoFenceStore;
        this.routeHandler = routeHandler;
        
        // create a new devices store so the filtering will not affect global store
        this.deviceStore = new DeviceStore(groupStore, globalDeviceStore);
        this.deviceStore.setAutoCommit(true);
        
        prepareDeviceGrid(globalDeviceStore, groupStore,
                deviceVisibilityHandler, reportHandler);
        
        // configure device store filtering
        deviceFilter = new StoreFilterField<GroupedDevice>() {
            @Override
            protected boolean doSelect(Store<GroupedDevice> store, GroupedDevice parent, GroupedDevice item, String filter) {
                return filter.trim().isEmpty() || matches(item, filter);
            }

            boolean matches(GroupedDevice item, String filter) {
                if (deviceStore.isGroup(item)) {
                    for (GroupedDevice child : deviceStore.getChildren(item)) {
                        if (matches(child, filter)) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    Device d = (Device) item;
                    for(User u : d.getUsers())
                        if(u.getLogin().toLowerCase().contains(filter.toLowerCase()))
                            return true;
                    return item.getName().toLowerCase().contains(filter.toLowerCase())
                            || d.getUniqueId().contains(filter);
                }
            }
        };
        deviceFilter.bind(this.deviceStore);

        // geo-fences
        geoFencesTabConfig = new TabItemConfig(i18n.overlayType(UserSettings.OverlayType.GEO_FENCES));
        
        GeoFenceProperties geoFenceProperties = GWT.create(GeoFenceProperties.class);

        geoFenceListView = new ListView<GeoFence, String>(geoFenceStore, geoFenceProperties.name()) {
            @Override
            protected void onMouseDown(Event e) {
                int index = indexOf(e.getEventTarget().<Element>cast());
                if (index != -1) {
                    geoFenceHandler.onSelected(geoFenceListView.getStore().get(index));
                }
                super.onMouseDown(e);
            }
        };
        geoFenceListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        geoFenceListView.getSelectionModel().addSelectionChangedHandler(geoFenceSelectionHandler);
        geoFenceListView.setContextMenu(createGfContextMenu());
        
        gfFilter = new StoreFilterField<GeoFence>() {
            @Override
            protected boolean doSelect(Store<GeoFence> store, GeoFence parent, GeoFence item, String filter) {
                return filter.trim().isEmpty() 
                        || item.getName().toLowerCase().contains(filter.toLowerCase());
            }
            
        };
        gfFilter.bind(this.geoFenceStore);

        geoFenceHandler.setGeoFenceListView(geoFenceListView);
        
        tracksTabConfig = new TabItemConfig(i18n.tracks());
        prepareRouteGrid(routeStore, reportHandler);

        // tab panel
        objectsTabs = new TabPanel();

        uiBinder.createAndBindUi(this);
        
        User user = ApplicationContext.getInstance().getUser();
        if(!user.hasPermission(UserPermission.GEOFENCE_READ)) {
            objectsTabs.remove(geoFenceList);
        }
        if(!user.hasPermission(UserPermission.TRACK_READ)) {
            objectsTabs.remove(routeGrid);
        }
        
        grid.getSelectionModel().addSelectionChangedHandler(deviceSelectionHandler);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        view.setAutoFill(true);
        view.setForceFit(true);
        view.setSortingEnabled(false);
        
        toggleManagementButtons(null);
    }

    private void prepareDeviceGrid(final ListStore<Device> globalDeviceStore,
            final GroupStore groupStore,
            final DeviceVisibilityHandler deviceVisibilityHandler,
            final ReportsMenu.ReportHandler reportHandler) {
        final Resources resources = GWT.create(Resources.class);
        List<ColumnConfig<GroupedDevice, ?>> columnConfigList = new LinkedList<>();

        //'Visible' column
        SafeHtmlBuilder shb = new SafeHtmlBuilder();
        shb.appendHtmlConstant("<input type=\"checkbox\"></input>");
        final ColumnConfig<GroupedDevice, Boolean> colVisible = new ColumnConfig<>(new ValueProvider<GroupedDevice, Boolean>() {
            @Override
            public Boolean getValue(GroupedDevice node) {
                if (deviceStore.isDevice(node)) {
                    Device device = deviceStore.getDevice(node);
                    return deviceVisibilityHandler.isVisible(device);
                }
                return true;
            }

            @Override
            public void setValue(GroupedDevice node, Boolean value) {
                if (deviceStore.isDevice(node)) {
                    Device device = deviceStore.getDevice(node);
                    deviceVisibilityHandler.setVisible(device, value);
                }
            }

            @Override
            public String getPath() {
                return "visible";
            }
        }, 50, shb.toSafeHtml());
        colVisible.setCell(new DeviceOnlyCheckBoxCell(deviceStore));
        colVisible.setFixed(true);
        colVisible.setResizable(false);
        colVisible.setToolTip(new SafeHtmlBuilder().appendEscaped(i18n.visible()).toSafeHtml());
        columnConfigList.add(colVisible);
        
        // handle visibility change events
        deviceVisibilityHandler.addVisibilityChangeHandler(new DeviceVisibilityChangeHandler() {
            @Override
            public void visibilityChanged(Long deviceId, boolean visible) {
                Device device = globalDeviceStore.findModelWithKey(deviceId.toString());
                globalDeviceStore.update(device);
            }
        });
        
        // Name column
        ColumnConfig<GroupedDevice, GroupedDeviceBinding> colName = new ColumnConfig<>(new ValueProvider<GroupedDevice, GroupedDeviceBinding>() {
            @Override
            public GroupedDeviceBinding getValue(GroupedDevice object) {
                return new GroupedDeviceBinding(object, resources);
            }

            @Override
            public String getPath() {
                return "name";
            }

            @Override
            public void setValue(GroupedDevice object, GroupedDeviceBinding value) {
            }
        }, 0, i18n.name());
        colName.setCell(new AbstractCell<GroupedDeviceBinding>(BrowserEvents.MOUSEOVER, BrowserEvents.MOUSEOUT) {
            @Override
            public void render(Cell.Context context, GroupedDeviceBinding value, SafeHtmlBuilder sb) {
                if (value == null) 
                    return;
                value.bindName(sb);
            }

            public void onBrowserEvent(Cell.Context context, Element parent, GroupedDeviceBinding value, NativeEvent event, ValueUpdater<GroupedDeviceBinding> valueUpdater) {
                if (event.getType().equals(BrowserEvents.MOUSEOVER) || event.getType().equals(BrowserEvents.MOUSEOUT)) {
                    Element target = Element.as(event.getEventTarget());
                    Tree.TreeNode<GroupedDevice> node = grid.findNode(target);
                    if (node != null && node.getModel() instanceof Device) {
                        Device device = (Device) node.getModel();
                        if (event.getType().equals(BrowserEvents.MOUSEOVER)) {
                            deviceHandler.onMouseOver(event.getClientX(), event.getClientY(), device);
                        } else {
                            deviceHandler.onMouseOut(event.getClientX(), event.getClientY(), device);
                        }
                    }
                } else {
                    super.onBrowserEvent(context, parent, value, event, valueUpdater);
                }
            }
        });
        columnConfigList.add(colName);

        ColumnConfig<GroupedDevice, GroupedDeviceBinding> colStatus = new ColumnConfig<>(new ValueProvider<GroupedDevice, GroupedDeviceBinding>() {
            @Override
            public GroupedDeviceBinding getValue(GroupedDevice object) {
                return new GroupedDeviceBinding(object, resources);
            }

            @Override
            public void setValue(GroupedDevice object, GroupedDeviceBinding value) {
            }

            @Override
            public String getPath() {
                return "status";
            }
            
        }, 60, i18n.status());
        //webcentersuite.blogspot.com/2011/11/custom-gwt-clickable-cell-with-multiple.html
        colStatus.setCell(new AbstractCell<GroupedDeviceBinding>("click"){    
            @Override
            public void onBrowserEvent(Cell.Context context, Element parent, GroupedDeviceBinding value, 
                    NativeEvent event, ValueUpdater<GroupedDeviceBinding> valueUpdater) {
                super.onBrowserEvent(context, parent, value, event, valueUpdater);
                if("click".equals(event.getType())) {
                    EventTarget eventTarget = event.getEventTarget();
                    if(parent.isOrHasChild(Element.as(eventTarget))) {
                        Element el = Element.as(eventTarget);
                        if(el.getNodeName().equalsIgnoreCase("IMG"))
                            onClick(el.getParentElement().getAttribute("name"), value);
                    }
                }
            }
            
            protected void onClick(String name, GroupedDeviceBinding binding) {
                if("alarms".equals(name)) {
                    final Device device = binding.getDevice();
                    if(device.hasUnreadAlarms())
                        deviceHandler.onShowAlarms(device);
                }
            }
            
            @Override
            public void render(Cell.Context context, GroupedDeviceBinding value, SafeHtmlBuilder sb) {
                if(value == null)
                    return;
                value.bindIcons(sb);
            }
            
        });
        colStatus.setFixed(true);
        colStatus.setResizable(false);
        columnConfigList.add(colStatus);
                
        // 'Follow' column
        ColumnConfig<GroupedDevice, Boolean> colFollow = new ColumnConfig<>(new ValueProvider<GroupedDevice, Boolean>() {
            
            @Override
            public Boolean getValue(GroupedDevice node) {
                if (deviceStore.isDevice(node)) {
                    Device device = deviceStore.getDevice(node);
                    return ApplicationContext.getInstance().isFollowing(deviceStore.getDevice(device));
                }
                return false;
            }

            @Override
            public void setValue(GroupedDevice node, Boolean value) {
                if (deviceStore.isDevice(node)) {
                    Device device = deviceStore.getDevice(node);
                    if (value) {
                        ApplicationContext.getInstance().follow(device);
                    } else {
                        ApplicationContext.getInstance().stopFollowing(device);
                    }
                }
            }

            @Override
            public String getPath() {
                return "follow";
            }
        }, 50, i18n.follow());
        colFollow.setCell(new DeviceOnlyCheckBoxCell(deviceStore));
        colFollow.setFixed(true);
        colFollow.setResizable(true);
        colFollow.setWidth(65);
        colFollow.setToolTip(new SafeHtmlBuilder().appendEscaped(i18n.follow()).toSafeHtml());
        columnConfigList.add(colFollow);

        // 'Record trace' column
        ColumnConfig<GroupedDevice, Boolean> colRecordTrace = new ColumnConfig<>(new ValueProvider<GroupedDevice, Boolean>() {
            @Override
            public Boolean getValue(GroupedDevice node) {
                if (deviceStore.isDevice(node)) {
                    Device device = deviceStore.getDevice(node);
                    return ApplicationContext.getInstance().isRecordingTrace(device);
                }
                return false;
            }

            @Override
            public void setValue(GroupedDevice node, Boolean value) {
                if (deviceStore.isDevice(node)) {
                    Device device = deviceStore.getDevice(node);
                    if (value) {
                        ApplicationContext.getInstance().recordTrace(device);
                    } else {
                        ApplicationContext.getInstance().stopRecordingTrace(device);
                    }
                }
            }

            @Override
            public String getPath() {
                return "recordTrace";
            }
        }, 50, i18n.recordTrace());
        colRecordTrace.setCell(new DeviceOnlyCheckBoxCell(deviceStore));
        colRecordTrace.setFixed(true);
        colRecordTrace.setResizable(true);
        colRecordTrace.setWidth(45);
        colRecordTrace.setToolTip(new SafeHtmlBuilder().appendEscaped(i18n.recordTrace()).toSafeHtml());
        columnConfigList.add(colRecordTrace);

        for (ColumnConfig<GroupedDevice, ?> col : columnConfigList) {
            col.setSortable(false);
        }

        view = new DeviceGridView(deviceVisibilityHandler, groupStore);
        view.setStripeRows(true);
        new GroupsHandler(globalDeviceStore, deviceStore, groupStore);

        columnModel = new ColumnModel<>(columnConfigList);

        grid = new TreeGrid<GroupedDevice>(deviceStore, columnModel, colName) {
            @Override
            protected void onRightClick(Event event) {
                EventTarget eventTarget = event.getEventTarget();
                List<GroupedDevice> selectedItems = getSelectionModel().getSelectedItems();
                boolean onSelectedRow = false;
                for (GroupedDevice selectedItem : selectedItems) {
                    if (deviceStore.isDevice(selectedItem)) {
                        int index = store.indexOf(selectedItem);
                        Element selectedRow = getView().getRow(index);
                        if (selectedRow.isOrHasChild(XElement.as(eventTarget))) {
                            onSelectedRow = true;
                            break;
                        }
                    }
                }

                if (onSelectedRow) {
                    super.onRightClick(event);
                }
            }
        };
        grid.setView(view);
        grid.setContextMenu(createDeviceGridContextMenu(reportHandler));
        
        grid.addHeaderClickHandler(new HeaderClickHandler() {
            private static final String CHECKED = "<input type=\"checkbox\" checked></input>";
            private static final String UNCHECKED = "<input type=\"checkbox\"></input>";
            boolean isChecked = false;
            
            @Override
            public void onHeaderClick(HeaderClickEvent event) {
                ColumnConfig<?,?> cc = grid.getColumnModel().getColumn(event.getColumnIndex());
                if(cc == colVisible) {
                    isChecked = !isChecked;
                    bind();
                    for(Device device:globalDeviceStore.getAll())
                        deviceVisibilityHandler.setVisible(device, isChecked);
                }
            }
           
            public void setIsChecked(boolean isChecked) {
                this.isChecked = isChecked;
                bind();
            }
            
            private void bind() {
                SafeHtmlBuilder shb = new SafeHtmlBuilder();
                shb.appendHtmlConstant(isChecked?CHECKED:UNCHECKED);
                grid.getView().getHeader().getHead(0).setHeader(shb.toSafeHtml());
            }
        });
        grid.addRowMouseDownHandler(this);
        grid.addCellDoubleClickHandler(this);
        grid.setAutoExpand(true);

        GridEditing<GroupedDevice> editing = new GridInlineEditing<>(grid);
        view.setShowDirtyCells(false);
        editing.addEditor(colFollow, new CheckBox());
        editing.addEditor(colRecordTrace, new CheckBox());
    }
    
    private void prepareRouteGrid(ListStore<Route> routeStore, 
            ReportsMenu.ReportHandler rHandler) {        
        List<ColumnConfig<Route, ?>> ccList = new ArrayList<>();
        ColumnConfig<Route, String> cName = new ColumnConfig<>(new ValueProvider<Route, String>() {
            @Override
            public String getValue(Route object) {
                return object.getName();
            }

            @Override
            public void setValue(Route object, String value) {
            }

            @Override
            public String getPath() {
                return "name";
            } 
        }, 1, "name");
        ccList.add(cName);
        ColumnConfig<Route, String> cProgress = new ColumnConfig<>(new ValueProvider<Route, String>() {
            @Override
            public String getValue(Route object) {
                return object.getDonePointsCount()+"/"+object.getRoutePoints().size();
            }

            @Override
            public void setValue(Route object, String value) {
            }

            @Override
            public String getPath() {
                return "progress";
            }
            
        }, 50, "progress");
        ccList.add(cProgress);
        
        ColumnConfig<Route, String> cStatus = new ColumnConfig<>(new ValueProvider<Route, String>() {
            @Override
            public String getValue(Route object) {
                return object.getStatus().name();
            }

            @Override
            public void setValue(Route object, String value) {
            }

            @Override
            public String getPath() {
                return "status";
            }
        }, 50, "status");
        ccList.add(cStatus);
        
        ColumnModel<Route> cm = new ColumnModel<>(ccList);
        routeGrid = new Grid<>(routeStore, cm);
        routeGrid.getView().setAutoExpandColumn(cName);
        routeGrid.setHideHeaders(true);
        
        routeGrid.getSelectionModel().addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler<Route>() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent<Route> event) {
                toggleManagementButtons(event.getSelection().isEmpty() ? null : event.getSelection().get(0));
                if (!event.getSelection().isEmpty()) {
                    routeHandler.onSelected(event.getSelection().get(0));
                }
            }
        });
        
        routeGrid.setContextMenu(createRouteContextMenu(rHandler));
    }

    final SelectionChangedEvent.SelectionChangedHandler<GroupedDevice> deviceSelectionHandler = new SelectionChangedEvent.SelectionChangedHandler<GroupedDevice>() {
        @Override
        public void onSelectionChanged(SelectionChangedEvent<GroupedDevice> event) {
            toggleManagementButtons(event.getSelection().isEmpty() ? null : event.getSelection().get(0));
        }
    };

    final SelectionChangedEvent.SelectionChangedHandler<GeoFence> geoFenceSelectionHandler = new SelectionChangedEvent.SelectionChangedHandler<GeoFence>() {
        @Override
        public void onSelectionChanged(SelectionChangedEvent<GeoFence> event) {
            toggleManagementButtons(event.getSelection().isEmpty() ? null : event.getSelection().get(0));
            geoFenceHandler.onSelected(event.getSelection().isEmpty() ? null : event.getSelection().get(0));
        }
    };

    @Override
    public void onRowMouseDown(RowMouseDownEvent event) {
        GroupedDevice node = grid.getSelectionModel().getSelectedItem();
        if (node != null && deviceStore.isDevice(node)) {
            deviceHandler.onSelected(deviceStore.getDevice(node), true);
        }
    }

    @Override
    public void onCellClick(CellDoubleClickEvent cellDoubleClickEvent) {
        GroupedDevice node = grid.getSelectionModel().getSelectedItem();
        if (deviceStore.isDevice(node)) {
            deviceHandler.doubleClicked(deviceStore.getDevice(node));
        }
    }

    @UiHandler("addButton")
    public void onAddClicked(SelectEvent event) {
        if(editingRoutes()) {
            routeHandler.onAdd();
        } else if (editingGeoFences()) {
            geoFenceHandler.onAdd();
        } else {
            deviceHandler.onAdd();
        }
    }

    private void editDevice() {
        GroupedDevice node = grid.getSelectionModel().getSelectedItem();
        if (deviceStore.isDevice(node)) {
            deviceHandler.onEdit(deviceStore.getDevice(node));
        }
    }

    private void shareDevice() {
        GroupedDevice node = grid.getSelectionModel().getSelectedItem();
        if (deviceStore.isDevice(node)) {
            deviceHandler.onShare(deviceStore.getDevice(node));
        }
    }

    private void removeDevice() {
        GroupedDevice node = grid.getSelectionModel().getSelectedItem();
        if (deviceStore.isDevice(node)) {
            deviceHandler.onRemove(deviceStore.getDevice(node));
        }
    }

    private void sendCommand() {
        commandHandler.onCommand(deviceStore.getDevice(grid.getSelectionModel().getSelectedItem()));
    }

    public void selectDevice(Device device) {
        GroupedDevice item = deviceStore.findModel(device);
        grid.getSelectionModel().select(item, false);
        grid.getView().focusRow(grid.getStore().indexOf(item));
        deviceHandler.onSelected((Device) grid.getSelectionModel().getSelectedItem());
    }

    @UiHandler("objectsTabs")
    public void onTabSelected(SelectionEvent<Widget> event) {
        grid.getSelectionModel().deselectAll();
        deviceHandler.onClearSelection();
        geoFenceListView.getSelectionModel().deselectAll();
        routeGrid.getSelectionModel().deselectAll();
        routeHandler.onSelected(null);
        toggleManagementButtons(null);
    }
    
    private boolean editingDevices() {
        return objectsTabs.getActiveWidget() == deviceList;
    }

    private boolean editingGeoFences() {
        return objectsTabs.getActiveWidget() == geoFenceList;
    }
    
    private boolean editingRoutes() {
        return objectsTabs.getActiveWidget() == routeList;
    }

    private void toggleManagementButtons(Object selection) {
        if (selection instanceof Group) {
            selection = null;
        }
        
        User user = ApplicationContext.getInstance().getUser();
        addButton.setEnabled((editingDevices() && user.hasPermission(UserPermission.DEVICE_EDIT))|| 
                (editingGeoFences() && user.hasPermission(UserPermission.GEOFENCE_EDIT)) || 
                (editingRoutes() && user.hasPermission(UserPermission.TRACK_EDIT)));
    }

    @UiHandler("showArchivedRoutes")
    public void onShowArchivedRoutesClicked(SelectEvent event) {
        routeHandler.onShowArchived();
    }
    
    interface HeaderIconTemplate extends XTemplates {
        @XTemplate("<div style=\"text-align:center;\">{img}</div>")
        SafeHtml render(SafeHtml img);
    }

    interface Resources extends ClientBundle {
        @Source("org/traccar/web/client/theme/icon/eye.png")
        ImageResource eye();

        @Source("org/traccar/web/client/theme/icon/follow.png")
        ImageResource follow();

        @Source("org/traccar/web/client/theme/icon/footprints.png")
        ImageResource footprints();
        
        @Source("org/traccar/web/client/theme/icon/ignition_enabled.png")
        ImageResource ignitionEnabled();

        @Source("org/traccar/web/client/theme/icon/ignition_disabled.png")
        ImageResource ignitionDisabled();
        
        @Source("org/traccar/web/client/theme/icon/alarm_enabled.png")
        ImageResource alarmEnabled();
        
        @Source("org/traccar/web/client/theme/icon/alarm_disabled.png")
        ImageResource alarmDisabled();
        
        @Source("org/traccar/web/client/theme/icon/speed_alarm_inactive.png")
        ImageResource speedAlarmInactive();
        
        @Source("org/traccar/web/client/theme/icon/speed_alarm_active.png")
        ImageResource speedAlarmActive();
    }

    private Menu createDeviceGridContextMenu(final ReportsMenu.ReportHandler reportHandler) {
        Menu menu = new Menu();
        User user = ApplicationContext.getInstance().getUser();
        if (user.hasPermission(UserPermission.DEVICE_EDIT)) {
            MenuItem edit = new MenuItem(i18n.edit());
            edit.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    editDevice();
                }
            });
            menu.add(edit);
        }
        if (user.hasPermission(UserPermission.DEVICE_SHARE)) {
            MenuItem share = new MenuItem(i18n.share());
            share.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    shareDevice();
                }
            });
            menu.add(share);
        }
        if (user.hasPermission(UserPermission.DEVICE_EDIT)) {
            MenuItem remove = new MenuItem(i18n.remove());
            remove.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    removeDevice();
                }
            });
            menu.add(remove);
        }
        if (user.hasPermission(UserPermission.COMMAND_TCP)) {
            MenuItem command = new MenuItem(i18n.command());
            command.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    sendCommand();
                }
            });
            menu.add(command);
        }
        if(user.hasPermission(UserPermission.REPORTS)) {
            MenuItem report = new MenuItem(i18n.report());
            report.setSubMenu(new ReportsMenu(reportHandler, new ReportsMenu.ReportSettingsHandler() {
                @Override
                public void setSettings(ReportsDialog dialog) {
                    GroupedDevice node = grid.getSelectionModel().getSelectedItem();
                    if (deviceStore.isDevice(node)) {
                        dialog.selectDevice((Device) node);
                    }
                }
            }));
            menu.add(report);
        }

        return menu;
    }
    
    private Menu createGfContextMenu() {
        Menu menu = new Menu();
        User user = ApplicationContext.getInstance().getUser();
        if(user.hasPermission(UserPermission.GEOFENCE_EDIT)) {
            MenuItem edit = new MenuItem(i18n.edit());
            edit.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    geoFenceHandler.onEdit(geoFenceListView.getSelectionModel().getSelectedItem());
                }
            });
            menu.add(edit);
        }
        
        if(user.hasPermission(UserPermission.GEOFENCE_SHARE)) {
            MenuItem share = new MenuItem(i18n.share());
            share.addSelectionHandler(new SelectionHandler<Item>(){
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    geoFenceHandler.onShare(geoFenceListView.getSelectionModel().getSelectedItem());
                }
            });
            menu.add(share);
        }
        
        if(user.hasPermission(UserPermission.GEOFENCE_EDIT)) {
            MenuItem remove = new MenuItem(i18n.remove());
            remove.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    geoFenceHandler.onRemove(geoFenceListView.getSelectionModel().getSelectedItem());
                }
            });
            menu.add(remove);
        }
        
        return menu;
    }
    
    private Menu createRouteContextMenu(final ReportsMenu.ReportHandler reportHandler) {
        Menu menu = new Menu();
        User user = ApplicationContext.getInstance().getUser();
        
        final MenuItem edit = new MenuItem(i18n.edit());
        if (user.hasPermission(UserPermission.TRACK_EDIT)) {
            edit.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    routeHandler.onEdit(routeGrid.getSelectionModel().getSelectedItem());
                }
            });
            menu.add(edit);
        }
        
        final MenuItem remove = new MenuItem(i18n.remove());
        if (user.hasPermission(UserPermission.TRACK_EDIT)) {
            remove.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    routeHandler.onRemove(routeGrid.getSelectionModel().getSelectedItem());
                }
            });
            menu.add(remove);
        }
        
        final MenuItem archive = new MenuItem(i18n.addToArchive());
        if(user.hasPermission(UserPermission.TRACK_EDIT)) {
            archive.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    Route route = routeGrid.getSelectionModel().getSelectedItem();
                    routeHandler.onArchivedChanged(route, true);
                }
            });
            menu.add(archive);
        }
        
        final MenuItem duplicate = new MenuItem(i18n.duplicateRoute());
        if(user.hasPermission(UserPermission.TRACK_EDIT)) {
            duplicate.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    Route r = new Route(routeGrid.getSelectionModel().getSelectedItem());
                    routeHandler.onEdit(r);
                }
            });
        }
        
        final MenuItem report = new MenuItem(i18n.report());
        if(user.hasPermission(UserPermission.REPORTS) && user.isPremium()) {
            report.addSelectionHandler(new SelectionHandler<Item>() {
                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    Route route = routeGrid.getSelectionModel().getSelectedItem();
                    ReportsDialog dialog = reportHandler.createDialog();
                    dialog.selectReportType(ReportType.TRACK);
                    dialog.selectRoute(route);
                    dialog.show();
                }
            });
            menu.add(report);
        }
        
        final MenuItem abort = new MenuItem(i18n.abortRoute());
        if(user.hasPermission(UserPermission.TRACK_EDIT)) {
            abort.addSelectionHandler(new SelectionHandler<Item>() {
                @Override 
                public void onSelection(SelectionEvent<Item> event) {
                    routeHandler.onAbort(routeGrid.getSelectionModel().getSelectedItem());
                }
            });
            menu.add(abort);
        }
        
        routeGrid.addBeforeShowContextMenuHandler(new BeforeShowContextMenuHandler() {
            @Override
            public void onBeforeShowContextMenu(BeforeShowContextMenuEvent event) {
                Route route = routeGrid.getSelectionModel().getSelectedItem();
                boolean inProgress = route.getStatus() == Route.Status.IN_PROGRESS_OK
                        || route.getStatus() == Route.Status.IN_PROGRESS_LATE;
                report.setEnabled(route.getStatus() != Route.Status.NEW && 
                        route.getDevice() != null &&
                        route.getDevice().getSubscriptionDaysLeft(new Date()) > 0);
                edit.setEnabled(inProgress || route.getStatus() == Route.Status.NEW);
                archive.setEnabled(!inProgress);
                remove.setEnabled(!inProgress);
                abort.setEnabled(inProgress);
            }
        });
        
        return menu;
    }
}
