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
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import java.util.List;
import java.util.Set;
import org.fusesource.restygwt.client.MethodCallback;

/**
 *
 * @author ŁŁ
 */
public class GeofencesService {
   public static interface LLongMapper extends ObjectMapper<List<Long>>{}
   
   private LLongMapper llMapper = GWT.create(LLongMapper.class);
   private IGeofencesService service = GWT.create(IGeofencesService.class);
    
   public void getGeofenceShare(long id, MethodCallback<Set<Long>> callback) {
       service.getGeofenceShare(id, callback);
   }
   
   public void updateGeofenceShare(long id, List<Long> uids, RequestCallback callback){
       RequestBuilder rb = new RequestBuilder(RequestBuilder.PUT,
                "../api/v1/geofences/"+id+"/share");
       try {
           rb.sendRequest(llMapper.write(uids), callback);
       } catch (RequestException ex) {
           callback.onError(null, ex);
       }
   }
}
