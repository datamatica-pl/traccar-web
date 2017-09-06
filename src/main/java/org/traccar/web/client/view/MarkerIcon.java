/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
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

import org.traccar.web.client.model.api.Resources;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.PositionIcon;
import pl.datamatica.traccar.model.DeviceIcon;
import pl.datamatica.traccar.model.DeviceIconType;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.DeviceIconMode;

public abstract class MarkerIcon {
    abstract Long getId();
    abstract String getKey();

    abstract String getDefaultURL();
    
    int getDefaultWidth() {
        return 36;
    }
    int getDefaultHeight() {
        return 48;
    }

    String getSelectedURL() {
        return getDefaultURL();
    }
    int getSelectedWidth() {
        return getDefaultWidth();
    }
    int getSelectedHeight() {
        return getDefaultHeight();
    }

    String getOfflineURL() {
        return getDefaultURL();
    }
    int getOfflineWidth() {
        return getDefaultWidth();
    }
    int getOfflineHeight() {
        return getDefaultHeight();
    }

    DeviceIconType getBuiltInIcon() {
        return null;
    }
    DeviceIcon getDatabaseIcon() {
        return null;
    }

    static class BuiltIn extends MarkerIcon {
        final Long id;

        BuiltIn(Long id) {
            this.id = id;
        }

        @Override
        Long getId() {
            return id;
        }
        
        @Override
        String getKey() {
            return id == null ? "default" : ""+id;
        }
        
        
        @Override
        String getDefaultURL() {
            return Resources.getInstance().icon(id);
        }
    }
    
    static class Custom extends MarkerIcon {
        long id;
        
        Custom(long id) {
            this.id = id;
        }
        
        @Override
        Long getId() {
            return id;
        }
        
        @Override
        String getKey() {
            return ""+id;
        }
        
        @Override
        String getDefaultURL() {
            return "../api/v1/resources/markers/custom/"+id;
        }
    }
    
    public static MarkerIcon create(Device device) {
        if ((device.getIconId() == null || device.getIconId() == -1) 
                && device.getCustomIconId() != null) {
            return new MarkerIcon.Custom(device.getCustomIconId());
        } else {
            return new MarkerIcon.BuiltIn(device.getIconId());
        }
    }

    public static PositionIcon create(Position position) {
        MarkerIcon deviceIcon = create(position.getDevice());
        String url = position.getStatus() == Position.Status.OFFLINE ? deviceIcon.getOfflineURL() : deviceIcon.getDefaultURL();
        int width = position.getStatus() == Position.Status.OFFLINE ? deviceIcon.getOfflineWidth() : deviceIcon.getDefaultWidth();
        int height = position.getStatus() == Position.Status.OFFLINE ? deviceIcon.getOfflineHeight() : deviceIcon.getDefaultHeight();
        return new PositionIcon(position.getDevice().getIconMode() == DeviceIconMode.ARROW, url, width, height,
                deviceIcon.getSelectedURL(), deviceIcon.getSelectedWidth(), deviceIcon.getSelectedHeight());
    }
}
