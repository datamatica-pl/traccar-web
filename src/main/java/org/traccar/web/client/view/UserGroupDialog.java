/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.UserGroupProperties;
import org.traccar.web.client.model.api.ApiUserGroup;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author ŁŁ
 */
public class UserGroupDialog {
    
    private static UserGroupDialogUiBinder uiBinder = GWT.create(UserGroupDialogUiBinder.class);
    
    interface UserGroupDialogUiBinder extends UiBinder<Widget, UserGroupDialog> {
    }
    
    @UiField
    Window window;
    
    @UiField
    TextField name;
    
    @UiField
    FieldLabel srcGroupLbl;
    
    @UiField(provided = true)
    ComboBox<ApiUserGroup> srcUserGroup;
    
    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);
    
    private ApiUserGroup group;
    private UserGroupHandler handler;
    private boolean editing;
    
    public UserGroupDialog(ApiUserGroup group, ListStore<ApiUserGroup> groups, UserGroupHandler handler) {
        this.handler = handler;
        this.group = group;
        if(groups != null) {
            editing = false;
            UserGroupProperties ugProps = new UserGroupProperties();
            ListStore<ApiUserGroup> groupsCopy = new ListStore<>(ugProps.id());
            groupsCopy.addAll(groups.getAll());
            this.srcUserGroup = new ComboBox<>(groupsCopy, ugProps.label());
            srcUserGroup.setForceSelection(true);
            srcUserGroup.setTriggerAction(TriggerAction.ALL);
        } else {
            editing = true;
        }
        uiBinder.createAndBindUi(this);
        if(groups == null)
            srcGroupLbl.setVisible(false);
        name.setText(group.getName());
    }
    
    public void show() {
        window.show();
    }
    
    public void hide() {
        window.hide();
    }
    
    @UiHandler("saveButton")
    public void onSaveClicked(SelectEvent event) {
        group.setName(name.getText());
        if(!editing) {
            group.clearPermissions();
            for(UserPermission p : ((ApiUserGroup)srcUserGroup.getValue()).permissions)
                group.grantPermission(p);
        }
        window.hide();
        handler.onSave(group);
    }
    
    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }
    
    public interface UserGroupHandler {
        void onSave(ApiUserGroup group);
    }
}
