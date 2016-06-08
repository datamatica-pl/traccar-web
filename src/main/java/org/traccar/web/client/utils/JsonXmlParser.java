/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.client.utils;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import java.util.HashMap;
import java.util.Map;


public class JsonXmlParser {

    public static Map<String, Object> parse(String data) {     
        try{
            return isXml(data) ? parseXml(data) : parseJson(data);
        } catch(Exception error) {
            return new HashMap<>();
        }
    }

    private static boolean isXml(String data) {
        return data.trim().startsWith("<");
    }
    
    private static Map<String, Object> parseXml(String data) {
        Map<String, Object> parsedData = new HashMap<>();
        NodeList nodes = XMLParser.parse(data).getFirstChild().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String parameterName = node.getNodeName();
            String valueText = node.getFirstChild().getNodeValue();
            parsedData.put(parameterName, valueText);
        }
        return parsedData;
    }

    private static Map<String, Object> parseJson(String data) {
        Map<String, Object> parsedData = new HashMap<>();
        JSONValue parsed = JSONParser.parseStrict(data);
        JSONObject object = parsed.isObject();
        if (object != null) {
            for (String parameterName : object.keySet()) {
                JSONValue value = object.get(parameterName);
                if (value.isNumber() != null) {
                    parsedData.put(parameterName, value.isNumber().doubleValue());
                } else if (value.isBoolean() != null) {
                    parsedData.put(parameterName, value.isBoolean().booleanValue());
                } else if (value.isString() != null) {
                    parsedData.put(parameterName, value.isString().stringValue());
                }
            }
        }
        return parsedData;
    }
    
}
