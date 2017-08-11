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

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import java.util.Date;
import org.traccar.web.client.ApplicationContext;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.UserSettings;

/**
 *
 * @author ŁŁ
 */
public class DevicePositionsService {
    DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    
    public void getPositions(Device d, Date from, Date to, boolean filter,
            RequestCallback callback) {
        if(d == null || from == null || to == null)
            throw new IllegalArgumentException();
        StringBuilder url = new StringBuilder("../api/v1/devices/"+d.getId()+"/positions");
        url.append("?fromDate=").append(dateFormat.format(from))
                .append("&toDate=").append(dateFormat.format(to))
                .append("&all");
        UserSettings us = ApplicationContext.getInstance().getUserSettings();
        if(us.isHideDuplicates())
            url.append("&hideDup");
        if(us.isHideInvalidLocations())
            url.append("&hideInvalid");
        if(us.isHideZeroCoordinates())
            url.append("&hideZero");
        if(us.getMinDistance() != null)
            url.append("&minDistance=").append(us.getMinDistance()*1000);
        if(us.getSpeedForFilter() != null && us.getSpeedModifier() != null) {
            String speedComp = speedCompFromSpeedModifier(us.getSpeedModifier());
            url.append("&speedComp=").append(speedComp)
                    .append("&speedValue=").append(us.getSpeedForFilter());
        }
        String addr = url.toString().replace("+", "%2B");
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, addr);
        try {
            rb.sendRequest(null, callback);
        } catch (RequestException ex) {
            callback.onError(null, ex);
        }
    }
    
    private String speedCompFromSpeedModifier(String modifier) {
        if("<".equals(modifier))
            return "lt";
        else if("<=".equals(modifier))
            return "lte";
        else if("=".equals(modifier))
            return "eq";
        else if(">=".equals(modifier))
            return "gte";
        else if(">".equals(modifier))
            return "gt";
        throw new IllegalArgumentException();
    }
}
