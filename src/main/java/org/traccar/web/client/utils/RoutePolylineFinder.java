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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.geometry.Point;

public class RoutePolylineFinder {
    public static void find(List<LonLat> pts, final Callback callback) {
        StringBuilder rp = new StringBuilder();
        for(LonLat p : pts)
            rp.append(p.lon()).append(",").append(p.lat()).append(";");
        if(rp.length() > 0)
            rp.replace(rp.length()-1, rp.length(), "");
        try {
            RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
                    "https://router.project-osrm.org/route/v1/driving/"
                    +rp.toString()+"?geometries=polyline");
            builder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    Result r = JsonUtils.safeEval(response.getText());
                    if(r.geometry() == null)
                        GWT.log("null!");
                    Object[] pts = r.geometry();
                    LonLat[] lonLat = new LonLat[pts.length];
                    for(int i=0;i<pts.length;++i) {
                        String[] latLon=pts[i].toString().split(",");
                        if(latLon.length != 2 || latLon[0] == null  || latLon[0].isEmpty()
                                || latLon[0].equals("undefined") || latLon[1] == null
                                || latLon[1].equals("undefined") || latLon[1].isEmpty())
                            continue;
                        lonLat[i] = new LonLat(Double.parseDouble(latLon[1]), 
                                Double.parseDouble(latLon[0]));
                    }
                    callback.onResult(lonLat);
                }
                
                @Override
                public void onError(Request request, Throwable exception) {
                    //todo
                }
                
            });
        } catch (RequestException ex) {
            Logger.getLogger(Geocoder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public interface Callback {
        void onResult(LonLat[] points);
    }
    
    static class Result extends JavaScriptObject {
        protected Result() {}
        
        public final native Object[] geometry() /*-{
            if(this.routes && this.routes.length > 0 && this.routes[0].geometry) 
                return $wnd.polyline.decode(this.routes[0].geometry);
            return null;
        }-*/;
    }
}
