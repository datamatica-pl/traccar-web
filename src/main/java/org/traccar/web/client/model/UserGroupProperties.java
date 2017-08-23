/*
 * Copyright 2017 Datamatica (dev@datamatica.pl)
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
package org.traccar.web.client.model;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import org.traccar.web.client.model.api.ApiUserGroup;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author ŁŁ
 */
public class UserGroupProperties {
    public ModelKeyProvider<ApiUserGroup> id() {
        return new ModelKeyProvider<ApiUserGroup>() {
            @Override
            public String getKey(ApiUserGroup item) {
                return item.getId()+"";
            }
        };
    }
    
    public ValueProvider<ApiUserGroup, String> name() {
            return new ValueProvider<ApiUserGroup, String>() {
                @Override
                public String getValue(ApiUserGroup object) {
                    return object.getName();
                }

                @Override
                public void setValue(ApiUserGroup object, String value) {
                    object.setName(value);
                }

                @Override
                public String getPath() {
                    return "name";
                }
            };
        }
    
    public ValueProvider<ApiUserGroup, Boolean> permission(UserPermission permission) {
        return new PermissionProvider(permission);
    }
    
    public LabelProvider<ApiUserGroup> label() {
        return new LabelProvider<ApiUserGroup>() {
            @Override
            public String getLabel(ApiUserGroup item) {
                return item.getName();
            }
        };
    }
    
    public static class PermissionProvider implements ValueProvider<ApiUserGroup, Boolean> {
        private final UserPermission permission;
        
        public PermissionProvider(UserPermission permission) {
            this.permission = permission;
        }
        
        @Override
        public Boolean getValue(ApiUserGroup object) {
            return object.hasPermission(permission);
        }

        @Override
        public void setValue(ApiUserGroup object, Boolean value) {
            if(value)
                object.grantPermission(permission);
            else
                object.revokePermission(permission);
        }

        @Override
        public String getPath() {
            return permission.name();
        }
    }
}
