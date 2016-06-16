package org.traccar.web.server.utils;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


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
    
    private static Map<String, Object> parseXml(String data) throws Exception {
        Map<String, Object> parsedData = new HashMap<>();
        NodeList nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(data))).getFirstChild().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String parameterName = node.getNodeName();
            String valueText = node.getFirstChild().getNodeValue();
            parsedData.put(parameterName, parseValue(valueText));
        }
        return parsedData;
    }
    
    private static Object parseValue(String value) {
        try {
            return Double.parseDouble(value);
        } catch(NumberFormatException ex) {}
        boolean parsed = Boolean.parseBoolean(value);
        if(parsed || value.equalsIgnoreCase("false"))
            return parsed;
        return value;
    }

    private static Map<String, Object> parseJson(String data) throws Exception {
        Map<String, Object> parsedData = new HashMap<>();
        JSONParser parser = new JSONParser();
        Object parsed = parser.parse(data);
        JSONObject object = parsed instanceof JSONObject ? (JSONObject)parsed : null;
        if (object != null) {
            for(Object key : object.keySet()) {
                Object value = object.get(key);
                parsedData.put(key.toString(), value);
            }
        }
        return parsedData;
    }
    
}
