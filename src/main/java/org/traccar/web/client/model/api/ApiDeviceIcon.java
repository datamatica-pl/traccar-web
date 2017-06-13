/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.client.model.api;

public class ApiDeviceIcon {
    long id;
    String iconUrl;
    boolean isDeleted;
    
    public long getId() {
        return id;
    }
    
    public String getUrl() {
        return iconUrl;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }
}
