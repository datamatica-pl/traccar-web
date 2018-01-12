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
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import java.util.ArrayList;
import java.util.List;
import org.fusesource.restygwt.client.Method;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.api.ApiJsonCallback;
import org.traccar.web.client.model.api.ApiRulesAcceptance;
import org.traccar.web.client.model.api.ApiRulesVersion;
import org.traccar.web.client.model.api.RulesService;

/**
 *
 * @author ŁŁ
 */
public class RulesDialog {
    @UiField
    Window window;
    @UiField
    VerticalLayoutContainer container;
    List<CheckBox> rulesCheckboxes;
    
    RulesHandler handler;
    boolean force;
    
    private static RulesDialogUiBinder uiBinder = GWT.create(RulesDialogUiBinder.class);
    
    interface RulesDialogUiBinder extends UiBinder<Widget, RulesDialog> {
    }
    
    public RulesDialog(List<ApiRulesVersion> rvs, boolean force, RulesHandler handler) {
        this.handler = handler;
        this.force = force;
        this.rulesCheckboxes = new ArrayList<>();
        
        uiBinder.createAndBindUi(this);
        for(ApiRulesVersion rv : rvs) {
            HorizontalLayoutContainer hlc = new HorizontalLayoutContainer();            
            CheckBox cb = new CheckBox();
            cb.setBoxLabel(rv.description + (rv.isObligatory ? " * " : ""));
            cb.setData("id", rv.id);
            rulesCheckboxes.add(cb);
            container.add(cb);
            InlineHTML p = new InlineHTML("<a href=\""+rv.url+"\">"+"treść tutaj"+"</a>");
            container.add(p);
            container.add(hlc, VerticalLayoutContainer.VerticalLayoutData(1, -1, new Margins(0, 8, 0, 8)));
        }
    }
    
    public void show() {
        window.show();
    }
    
    public void hide() {
        window.hide();
    }
    
    @UiHandler("acceptButton")
    public void onAcceptClicked(SelectEvent event) {
        RulesService service = GWT.create(RulesService.class);
        Messages i18n = GWT.create(Messages.class);
        ApiRulesAcceptance ra = new ApiRulesAcceptance();
        for(CheckBox cb : rulesCheckboxes) {
            long id = (Long)cb.getData("id");
            if(cb.getValue())
                ra.accepted.add(id);
            else
                ra.rejected.add(id);
        }
        service.accept(ra, new ApiJsonCallback(i18n) {
            @Override
            public void onSuccess(Method method, JSONValue response) {
                window.hide();
                handler.onRulesAccepted();
            }
            
        });
    }
    
    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        if(force)
            window.hide();
        else
            handler.onRulesAccepted();
    }
    
    public interface RulesHandler {
        void onRulesAccepted();
    }
}
