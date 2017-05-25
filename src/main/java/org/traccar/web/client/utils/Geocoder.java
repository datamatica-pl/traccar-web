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
                            +address);
            builder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    JsResult pos = JsonUtils.safeEval(response.getText());
                    LonLat result = pos.getResults()[0];
                    GWT.log(address+"->("+result.lon()+","+result.lat()+")");
                    callback.onResult(result.lon(), result.lat());
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
        void onResult(float lon, float lat);
    }
    
    public static class JsResult extends JavaScriptObject {
        protected JsResult() {}
        
        public final native LonLat[] getResults() /*-{
            return this;
        }-*/;
    }
    
    public static class LonLat extends JavaScriptObject{
        protected LonLat() {}
        
        public final native float lon() /*-{
            return this.lon;
        }-*/;
        
        public final native float lat() /*-{
            return this.lat;
        }-*/;
    }
}
