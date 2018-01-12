/*
 * Copyright 2018 Datamatica (dev@datamatica.pl)
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
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.TextField;
import java.util.Arrays;
import org.fusesource.restygwt.client.Method;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.EnumKeyProvider;
import org.traccar.web.client.model.api.ApiRulesVersion;
import org.traccar.web.client.model.api.RulesService;
import org.traccar.web.client.model.api.ApiJsonCallback;
import pl.datamatica.traccar.model.RulesVersion;

/**
 *
 * @author ŁŁ
 */
public class AddRulesVersionDialog {
    
    private static AddRulesVersionDialogUiBinder uiBinder = GWT.create(AddRulesVersionDialogUiBinder.class);
    
    interface AddRulesVersionDialogUiBinder extends UiBinder<Widget, AddRulesVersionDialog> {
    }
    
    @UiField
    Window window;    
    @UiField
    TextField url;
    @UiField
    DateField startDate;
    @UiField(provided=true)
    ComboBox<RulesVersion.Type> type;
    
    @UiField
    Messages i18n;
    
    public AddRulesVersionDialog() {
        ListStore<RulesVersion.Type> store = new ListStore<>(new EnumKeyProvider<RulesVersion.Type>());
        store.addAll(Arrays.asList(RulesVersion.Type.values()));
        type = new ComboBox(store,
            new LabelProvider<RulesVersion.Type>() {
                @Override
                public String getLabel(RulesVersion.Type item) {
                    return item.toString();
                }
        });
        uiBinder.createAndBindUi(this);
    }
    
    public void show() {
        window.show();
    }
    
    public void hide() {
        window.hide();
    }
    
    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        ApiRulesVersion arv = new ApiRulesVersion(url.getText(), 
                startDate.getCurrentValue(), type.getCurrentValue());
        RulesService service = GWT.create(RulesService.class);
        service.addNewVersion(arv, new ApiJsonCallback(i18n) {
            @Override
            public void onSuccess(Method method, JSONValue response) {
                window.hide();
            }
            
        });
    }
    
    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }
}
