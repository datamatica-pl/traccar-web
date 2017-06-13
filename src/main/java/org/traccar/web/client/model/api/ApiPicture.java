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

import pl.datamatica.traccar.model.Picture;

public class ApiPicture extends Picture {
    private final String url;

    public ApiPicture(String url) {
        this.url = url;
    }
    
    @Override
    public String getMimeType() {
        return "image/png";
    }

    @Override
    public int getHeight() {
        return 48;
    }

    @Override
    public int getWidth() {
        return 36;
    }

    @Override
    public long getId() {
        return 0;
    }
    
    @Override
    public String getUrl() {
        return url;
    }
    
    
}
