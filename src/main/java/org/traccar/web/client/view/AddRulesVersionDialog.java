/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.TextField;
import org.fusesource.restygwt.client.Method;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.api.ApiRulesVersion;
import org.traccar.web.client.model.api.RulesService;
import org.traccar.web.client.model.api.ApiJsonCallback;

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
    
    @UiField
    Messages i18n;
    
    public AddRulesVersionDialog() {
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
        ApiRulesVersion arv = new ApiRulesVersion(url.getText(), startDate.getCurrentValue());
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
