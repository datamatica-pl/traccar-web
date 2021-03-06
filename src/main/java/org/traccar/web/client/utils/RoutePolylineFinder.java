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
package org.traccar.web.client.utils;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.api.ApiRequestCallback;

public class RoutePolylineFinder {
    public static void find(List<LonLat> pts, final Callback callback) {
        Messages i18n = GWT.create(Messages.class);
        StringBuilder rp = new StringBuilder();
        for(LonLat p : pts)
            rp.append(p.lon()).append(",").append(p.lat()).append("|");
        if(rp.length() > 0)
            rp.replace(rp.length()-1, rp.length(), "");
        try {
            RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,
                    "api/v1/routes/findPolyline?coordinates="
                    +rp.toString()+"&geometry=true&geometry_format=encodedpolyline"
                    +"&profile=driving-car&units=m");
            builder.sendRequest(null, new ApiRequestCallback(i18n) {
                @Override
                public void onSuccess(String response) {
                    Result r = JsonUtils.safeEval(response);
                    if(r == null)
                        callback.onResult(null, null);
                    else
                        callback.onResult(r.geometry(0), r.legDistances());
                }
                
            });
        } catch (RequestException ex) {
            Logger.getLogger(Geocoder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public interface Callback {
        void onResult(String points, double[] distances);
        void onError(int code);
    }
    
    static class Result extends JavaScriptObject {
        protected Result() {}
        
        public final native String geometry(int i) /*-{
            if(this.routes && this.routes.length > i && this.routes[i].geometry) 
                return this.routes[i].geometry;
            return null;
        }-*/;
        
        public final native double[] legDistances() /*-{
            var dists = null;
            if(this.routes && this.routes.length > 0 && this.routes[0].segments) {
                dists = [];
                for(var i=0;i < this.routes[0].segments.length;++i)
                    dists.push(this.routes[0].segments[i].distance);
            }
            return dists;
        }-*/;
    }
}
