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
package org.traccar.web.client.model.api;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Resources {
    private static Resources instance;
    
    public static Resources getInstance() {
        if(instance == null)
            instance = new Resources();
        return instance;
    }
    
    private Map<Long, IconInfo> icons = new TreeMap<>();
    private Map<Long, ApiDeviceModel> models = new HashMap<>();
    private IconInfo defIcon = new IconInfo("img/GTS_pointer_car.png", false);
    
    public IconInfo icon(Long id) {
        if(id != null && icons.containsKey(id))
            return icons.get(id);
        return defIcon;
    }
    
    public void icon(long id, IconInfo info) {
        icons.put(id, info);
    }

    public Iterable<Long> icons() {
        return icons.keySet();
    }

    public void model(ApiDeviceModel m) {
        models.put(m.getId(), m);
    }
    
    public ApiDeviceModel model(long id) {
        return models.get(id);
    }
    
    public Iterable<Long> models() {
        return models.keySet();
    }
    
    public static class IconInfo {
        private final String url;
        private final boolean canRotate;
        
        public IconInfo(String url, boolean rotates) {
            this.url = url;
            this.canRotate = rotates;
        }
        
        public IconInfo(ApiDeviceIcon icon) {
            this.url = icon.getUrl().replace("/images/", "/markers/");
            this.canRotate = icon.isWithoutFrame;
        }
        
        public String getUrl() {
            return url;
        }
        
        public boolean canRotate() {
            return canRotate;
        }
    }
}
