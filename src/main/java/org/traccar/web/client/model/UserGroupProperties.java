/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.client.model;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import pl.datamatica.traccar.model.UserGroup;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author ŁŁ
 */
public class UserGroupProperties {
    public ModelKeyProvider<UserGroup> id() {
        return new ModelKeyProvider<UserGroup>() {
            @Override
            public String getKey(UserGroup item) {
                return item.getId()+"";
            }
        };
    }
    
    public ValueProvider<UserGroup, String> name() {
            return new ValueProvider<UserGroup, String>() {
                @Override
                public String getValue(UserGroup object) {
                    return object.getName();
                }

                @Override
                public void setValue(UserGroup object, String value) {
                    object.setName(value);
                }

                @Override
                public String getPath() {
                    return "name";
                }
            };
        }
    
    public ValueProvider<UserGroup, Boolean> permission(UserPermission permission) {
        return new PermissionProvider(permission);
    }
    
    public LabelProvider<UserGroup> label() {
        return new LabelProvider<UserGroup>() {
            @Override
            public String getLabel(UserGroup item) {
                return item.getName();
            }
        };
    }
    
    public static class PermissionProvider implements ValueProvider<UserGroup, Boolean> {
        private final UserPermission permission;
        
        public PermissionProvider(UserPermission permission) {
            this.permission = permission;
        }
        
        @Override
        public Boolean getValue(UserGroup object) {
            return object.getPermissions().contains(permission);
        }

        @Override
        public void setValue(UserGroup object, Boolean value) {
            if(value)
                object.getPermissions().add(permission);
            else
                object.getPermissions().remove(permission);
        }

        @Override
        public String getPath() {
            return permission.name();
        }
    }
}
