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
import java.util.logging.Level;
import java.util.logging.Logger;

public class Geocoder {
    public static void search(final String address, final SearchCallback callback) {
        try {
            RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
                    "https://nominatim.openstreetmap.org/search?format=json&q="
                            +address+"&addressdetails=1");
            builder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    JsResult pos = JsonUtils.safeEval(response.getText());
                    LonLatAddr result = pos.getResults()[0];
                    String name = result.getAddress().getCity().substring(0, 3)
                            + "-" + result.getAddress().getRoad().substring(0, 12);
                    GWT.log(address+"->("+result.lon()+","+result.lat()+","
                            + name+")");
                    callback.onResult(result.lon(), result.lat(), name);
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
    
    public interface SearchCallback {
        void onResult(float lon, float lat, String name);
    }
    
    public static class JsResult extends JavaScriptObject {
        protected JsResult() {}
        
        public final native LonLatAddr[] getResults() /*-{
            return this;
        }-*/;
    }
    
    public static class LonLatAddr extends JavaScriptObject{
        protected LonLatAddr() {}
        
        public final native float lon() /*-{
            return this.lon;
        }-*/;
        
        public final native float lat() /*-{
            return this.lat;
        }-*/;
        
        public final native Address getAddress() /*-{
            return this.address;
        }-*/;
    }
    
    public static class Address extends JavaScriptObject {
        protected Address() {}
        
        public final native String getCity() /*-{
            return this.city;
        }-*/;
        
        public final native String getRoad() /*-{
            return this.road;
        }-*/;
    }
}
