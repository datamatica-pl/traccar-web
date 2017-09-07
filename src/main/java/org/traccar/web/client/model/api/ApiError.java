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

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import org.traccar.web.client.ApplicationContext;

public class ApiError {
    private static final Mapper mapper = GWT.create(Mapper.class);
    
    public String localizedMessage;
    public String messageKey;
    
    public String getMessage() {
        if(localizedMessage != null)
            return localizedMessage;
        return ApplicationContext.getInstance().getMessage(messageKey);
    }
    
    public static ApiError fromJson(String body) {
        return mapper.read(body);
    }
    
    public interface Mapper extends ObjectMapper<ApiError> {}
}
