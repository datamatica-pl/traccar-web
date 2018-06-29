/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.google.gwt.uibinder.client.UiHandler;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.TextField;

public class ImeiDialog {
    public interface ImeiHandler {
        void onImei(String imei, Boolean marketingCheck);
    }
    
    private static ImeiDialogUiBinder uiBinder = GWT.create(ImeiDialogUiBinder.class);

    interface ImeiDialogUiBinder extends UiBinder<Widget, ImeiDialog> {
    }
    
    @UiField
    Window window;
    
    @UiField
    TextField imei;
    
    @UiField
    CheckBox marketingCheck;
    
    private ImeiHandler handler;
    
    public ImeiDialog(ImeiHandler handler) {
        this.handler = handler;
        uiBinder.createAndBindUi(this);
    }
    
    public void show() {
        window.show();
    }
    
    @UiHandler("okButton")
    public void onOkClicked(SelectEvent event) {
        window.hide();
        handler.onImei(imei.getText(), this.marketingCheck.getValue());
    }
    
    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }
}
