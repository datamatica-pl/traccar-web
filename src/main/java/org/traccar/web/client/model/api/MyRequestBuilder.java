/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.client.model.api;

import com.google.gwt.http.client.RequestBuilder;

/**
 *
 * @author Lukasz
 */
public class MyRequestBuilder extends RequestBuilder{
    public MyRequestBuilder(String httpMethod, String url) {
        super(httpMethod, url);
    }
}
