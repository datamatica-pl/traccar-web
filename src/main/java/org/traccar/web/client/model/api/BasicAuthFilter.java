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
import com.google.gwt.thirdparty.guava.common.io.BaseEncoding;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.dispatcher.DispatcherFilter;

public class BasicAuthFilter implements DispatcherFilter {    
    private Credentials credentials;
    private static final BasicAuthFilter INSTANCE = new BasicAuthFilter();
    
    private BasicAuthFilter() {}
    
    @Override
    public boolean filter(Method method, RequestBuilder builder) {
        if(credentials == null)
            return true;
        
        builder.setHeader("Authorization", "Basic "+credentials.encode());
        credentials = null;
        return true;
    }
    
    public void pushCredentials(String login, String password) {
        this.credentials = new Credentials(login, password);
    }
    
    public static BasicAuthFilter getInstance() {
        return INSTANCE;
    }
    
    public static class Credentials{
        private String login;
        private String password;
        
        public Credentials(String login, String password) {
            this.login = login;
            this.password = password;
        }
        
        public String encode() {
            return btoa(login+":"+password);
        }
        
        private native String btoa(String bytes) /*-{
            return window.btoa(bytes);
        }-*/;
    }
}
