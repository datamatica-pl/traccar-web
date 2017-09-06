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

import pl.datamatica.traccar.model.Group;
import pl.datamatica.traccar.model.Device;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.*;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.GroupProperties;
import org.traccar.web.client.model.GroupStore;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.TextField;
import org.traccar.web.client.Application;
import org.traccar.web.client.model.api.ApiDeviceModel;
import org.traccar.web.client.model.api.Resources;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

public class DeviceDialog implements Editor<Device> {

    private static DeviceDialogUiBinder uiBinder = GWT.create(DeviceDialogUiBinder.class);

    interface DeviceDialogUiBinder extends UiBinder<Widget, DeviceDialog> {
    }

    private DeviceDriver driver = GWT.create(DeviceDriver.class);

    interface DeviceDriver extends SimpleBeanEditorDriver<Device, DeviceDialog> {
    }

    public interface DeviceHandler {
        void setWindow(Window window);
        void onSave(Device device);
    }

    private DeviceHandler deviceHandler;

    @UiField
    Window window;

    @UiField
    TabPanel tabs;

    @UiField
    TextField name;

    @UiField
    TextField uniqueId;

    @UiField
    TextField description;

    @UiField
    TextField phoneNumber;

    @UiField
    TextField plateNumber;

    @UiField
    TextField vehicleInfo;

    @UiField
    TextField commandPassword;

    @UiField
    DateField validTo;

    @UiField(provided = true)
    NumberPropertyEditor<Integer> integerPropertyEditor = new NumberPropertyEditor.IntegerPropertyEditor();

    @UiField
    NumberField<Integer> timeout;

    @UiField(provided = true)
    NumberPropertyEditor<Double> doublePropertyEditor = new NumberPropertyEditor.DoublePropertyEditor();

    @UiField
    NumberField<Integer> historyLength;

    @UiField
    NumberField<Double> idleSpeedThreshold;

    @UiField
    NumberField<Integer> minIdleTime;

    @UiField
    NumberField<Double> speedLimit;

    @UiField
    NumberField<Double> fuelCapacity;

    @UiField
    NumberField<Integer> timezoneOffset;

    @UiField
    CheckBox showProtocol;

    @UiField
    CheckBox showOdometer;

    @UiField
    VerticalLayoutContainer iconTab;
    final DeviceIconEditor iconEditor;

    //@UiField
    //VerticalLayoutContainer sensorsTab;
    final SensorsEditor sensorsEditor;

    @UiField
    VerticalLayoutContainer technicalReviewTab;
    final TechnicalReviewEditor technicalReviewEditor;

    @UiField
    VerticalLayoutContainer registrationReviewTab;
    final RegistrationReviewEditor registrationReviewEditor;

    @UiField
    VerticalLayoutContainer insuranceValidityTab;

    @UiField(provided = true)
    ComboBox<Group> group;
    
    @UiField(provided = true)
    ComboBox<Long> deviceModelId;

    @UiField
    Messages i18n;

    final Device device;

    public DeviceDialog(Device device, ListStore<Device> deviceStore, final GroupStore groupStore, DeviceHandler deviceHandler) {
        this.device = device;
        this.deviceHandler = deviceHandler;

        GroupProperties groupProperties = GWT.create(GroupProperties.class);

        this.group = new ComboBox<>(groupStore.toListStore(), groupProperties.label(), new AbstractSafeHtmlRenderer<Group>() {
            @Override
            public SafeHtml render(Group group) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                for (int i = 0; i < groupStore.getDepth(group); i++) {
                    builder.appendHtmlConstant("&nbsp;&nbsp;&nbsp;");
                }
                return builder.appendEscaped(group.getName() == null ? "" : group.getName()).toSafeHtml();
            }
        });
        this.group.setForceSelection(false);
        
        ListStore<Long> modelStore = new ListStore<>(new ModelKeyProvider<Long>() {
            @Override
            public String getKey(Long item) {
                return item+"";
            }
        });
        for(long id : Resources.getInstance().models())
            modelStore.add(id);
        modelStore.add(-1L);
        this.deviceModelId = new ComboBox<>(modelStore, new LabelProvider<Long>() {
            @Override
            public String getLabel(Long id) {
                if(id == null)
                    return i18n.unknownModel();
                ApiDeviceModel m = Resources.getInstance().model(id);
                if(m == null)
                    return i18n.unknownModel();
                return m.getModelName();
            }
        });

        uiBinder.createAndBindUi(this);

        timeout.addValidator(new MinNumberValidator<>(1));
        timeout.addValidator(new MaxNumberValidator<>(7 * 24 * 60 * 60));

        driver.initialize(this);
        driver.edit(device);
        if(device.getUniqueId() != null)
            uniqueId.setReadOnly(true);

        idleSpeedThreshold.setValue(device.getIdleSpeedThreshold() * ApplicationContext.getInstance().getUserSettings().getSpeedUnit().getFactor());
        speedLimit.addValidator(new MaxNumberValidator<>(255.));
        if (device.getSpeedLimit() != null) {
            speedLimit.setValue(device.getSpeedLimit() * ApplicationContext.getInstance().getUserSettings().getSpeedUnit().getFactor());
        }

        fuelCapacity.addValidator(new MaxNumberValidator<>(9000.));
        fuelCapacity.addValidator(new MinNumberValidator<>(1.));

        User currentUser = ApplicationContext.getInstance().getUser();
        if (currentUser.hasPermission(UserPermission.ALL_DEVICES)) {
            validTo.setEnabled(true);
            historyLength.setEnabled(true);
        } else {
            validTo.setEnabled(false);
            historyLength.setEnabled(false);
        }

        sensorsEditor = new SensorsEditor(device, deviceStore);
        //sensorsTab.add(sensorsEditor.getPanel(), new VerticalLayoutContainer.VerticalLayoutData(1, 1));

        technicalReviewEditor = new TechnicalReviewEditor(device, deviceStore);
        technicalReviewTab.add(technicalReviewEditor.getPanel(), new VerticalLayoutContainer.VerticalLayoutData(1, 1));

        registrationReviewEditor = new RegistrationReviewEditor(device, deviceStore);
        registrationReviewTab.add(registrationReviewEditor.getPanel(), new VerticalLayoutContainer.VerticalLayoutData(1,1));

        iconEditor = new DeviceIconEditor(device);
        iconTab.add(iconEditor.getPanel(), new VerticalLayoutContainer.VerticalLayoutData(1, 1));

        tabs.addSelectionHandler(new SelectionHandler<Widget>() {
            @Override
            public void onSelection(SelectionEvent<Widget> event) {
                if (event.getSelectedItem() == iconTab) {
                    iconEditor.loadIcons();
                }
            }
        });
        
        deviceHandler.setWindow(window);
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        Device device = driver.flush();
        device.setIdleSpeedThreshold(ApplicationContext.getInstance().getUserSettings().getSpeedUnit().toKnots(device.getIdleSpeedThreshold()));
        if (device.getSpeedLimit() != null) {
            device.setSpeedLimit(ApplicationContext.getInstance().getUserSettings().getSpeedUnit().toKnots(device.getSpeedLimit()));
        }

        iconEditor.flush();
        technicalReviewEditor.flush();
        registrationReviewEditor.flush();
        sensorsEditor.flush();
        deviceHandler.onSave(device);
    }

    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }
}
