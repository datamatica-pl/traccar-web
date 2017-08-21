/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;
import java.util.EnumSet;
import org.traccar.web.client.model.UserGroupProperties;
import pl.datamatica.traccar.model.Group;
import pl.datamatica.traccar.model.UserGroup;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author ŁŁ
 */
public class UserGroupDialog {
    
    private static UserGroupDilogUiBinder uiBinder = GWT.create(UserGroupDilogUiBinder.class);
    
    interface UserGroupDilogUiBinder extends UiBinder<Widget, UserGroupDialog> {
    }
    
    @UiField
    Window window;
    
    @UiField
    TextField name;
    
    @UiField
    FieldLabel srcGroupLbl;
    
    @UiField(provided = true)
    ComboBox srcUserGroup;
    
    private UserGroup group;
    private UserGroupHandler handler;
    
    public UserGroupDialog(UserGroup group, ListStore<UserGroup> groups, UserGroupHandler handler) {
        this.handler = handler;
        this.group = group;
        if(groups != null) {
            srcUserGroup.setVisible(true);
            UserGroupProperties ugProps = new UserGroupProperties();
            this.srcUserGroup = new ComboBox<>(groups, ugProps.label());
            group.setPermissions(EnumSet.noneOf(UserPermission.class));
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
        for(UserPermission p : ((UserGroup)srcUserGroup.getValue()).getPermissions())
            group.getPermissions().add(p);
        handler.onSave(group);
        
    }
    
    @UiHandler("cancelButton")
    public void onCancelClicked(SelectEvent event) {
        window.hide();
    }
    
    public interface UserGroupHandler {
        void onSave(UserGroup group);
    }
}
