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

import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.form.*;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import org.traccar.web.client.model.ApplicationSettingsProperties;
import org.traccar.web.client.model.EnumKeyProvider;
import org.traccar.web.client.widget.LanguageComboBox;
import pl.datamatica.traccar.model.ApplicationSettings;

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
import pl.datamatica.traccar.model.PasswordHashMethod;

import java.util.Arrays;
import java.util.List;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.controller.SettingsController;
import org.traccar.web.client.model.api.ApiUserGroup;
import pl.datamatica.traccar.model.UserGroup;
import pl.datamatica.traccar.model.UserPermission;

public class ApplicationSettingsDialog implements Editor<ApplicationSettings> {

    private static ApplicationSettingsDialogUiBinder uiBinder = GWT.create(ApplicationSettingsDialogUiBinder.class);

    interface ApplicationSettingsDialogUiBinder extends UiBinder<Widget, ApplicationSettingsDialog> {
    }

    private ApplicationSettingsDriver driver = GWT.create(ApplicationSettingsDriver.class);

    interface ApplicationSettingsDriver extends SimpleBeanEditorDriver<ApplicationSettings, ApplicationSettingsDialog> {
    }

    public interface ApplicationSettingsHandler {
        void onSave(ApplicationSettings applicationSettings);
    }

    private ApplicationSettingsHandler applicationSettingsHandler;

    @UiField
    Window window;

    @UiField
    CheckBox registrationEnabled;
    
    @UiField
    CheckBox eventRecordingEnabled;

    @UiField(provided = true)
    NumberPropertyEditor<Integer> integerPropertyEditor = new NumberPropertyEditor.IntegerPropertyEditor();

    @UiField
    NumberField<Integer> notificationExpirationPeriod;

    @UiField(provided = true)
    NumberPropertyEditor<Short> shortPropertyEditor = new NumberPropertyEditor.ShortPropertyEditor();

    @UiField
    NumberField<Short> updateInterval;

    @UiField(provided = true)
    ComboBox<PasswordHashMethod> defaultHashImplementation;

    @UiField(provided = true)
    ComboBox<String> language;

    @UiField
    TextField bingMapsKey;

    @UiField
    TextField matchServiceURL;
    
    @UiField
    FieldLabel defaultGroupLbl;
    
    @UiField(provided = true)
    ComboBox<UserGroup> defaultGroup;
    
    @UiField
    NumberField<Integer> defaultIconId;
    
    @UiField
    NumberField<Integer> freeHistory;

    public ApplicationSettingsDialog(ApplicationSettings applicationSettings, 
            ApplicationSettingsHandler applicationSettingsHandler, 
            List<ApiUserGroup> userGroups) {
        this.applicationSettingsHandler = applicationSettingsHandler;

        ListStore<PasswordHashMethod> dhmStore = new ListStore<>(
                new EnumKeyProvider<PasswordHashMethod>());
        dhmStore.addAll(Arrays.asList(PasswordHashMethod.values()));
        defaultHashImplementation = new ComboBox<>(
                dhmStore, new ApplicationSettingsProperties.PasswordHashMethodLabelProvider());
        
        defaultHashImplementation.setForceSelection(true);
        defaultHashImplementation.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        
        boolean canManageGroups = ApplicationContext.getInstance().getUser().hasPermission(UserPermission.USER_GROUP_MANAGEMENT);
        if(canManageGroups) {
            prepareDefaultGroupCBox(userGroups, applicationSettings);
        }

        language = new LanguageComboBox();

        uiBinder.createAndBindUi(this);

        updateInterval.addValidator(new MinNumberValidator<>(ApplicationSettings.UPDATE_INTERVAL_MIN));
        updateInterval.addValidator(new MaxNumberValidator<>(ApplicationSettings.UPDATE_INTERVAL_MAX));
        
        defaultGroupLbl.setVisible(canManageGroups);

        driver.initialize(this);
        driver.edit(applicationSettings);
    }

    private void prepareDefaultGroupCBox(List<ApiUserGroup> userGroups, ApplicationSettings applicationSettings) {
        ListStore<UserGroup> uGroups = new ListStore<>(new ModelKeyProvider<UserGroup>() {
            @Override
            public String getKey(UserGroup item) {
                return item.getId()+"";
            }
        });
        for(ApiUserGroup aug : userGroups) {
            UserGroup ug = aug.toUserGroup();
            uGroups.add(ug);
            if(aug.getId() == applicationSettings.getDefaultGroupId())
                applicationSettings.setDefaultGroup(ug);
        }
        defaultGroup = new ComboBox(uGroups, new LabelProvider<UserGroup>() {
            @Override
            public String getLabel(UserGroup item) {
                return item.getName();
            }
        });
        defaultGroup.setForceSelection(true);
        defaultGroup.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }
    
    @UiHandler("updateRules")
    public void onUpdateRulesClicked(SelectEvent event) {
        AddRulesVersionDialog dialog = new AddRulesVersionDialog();
        dialog.show();
    }

    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        ApplicationSettings as = driver.flush();
        if(!driver.hasErrors()) {
            window.hide();
            applicationSettingsHandler.onSave(as);
        }
    }

    @UiHandler("cancelButton")
    public void onRegisterClicked(SelectEvent event) {
        window.hide();
    }

}
