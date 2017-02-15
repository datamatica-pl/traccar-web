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
package org.traccar.web.server.reports;

import java.util.ArrayList;
import java.util.List;
import pl.datamatica.traccar.model.Position;

public class MapBuilder {
    public static final String IMG_ROUTE_START = "new ol.style.Icon({ "
            + "anchor: [0.5, 25], anchorXUnits: 'fraction', anchorYUnits: 'pixels', "
            + "opacity: 0.75, src: 'https://cdnjs.cloudflare.com/ajax/libs/openlayers/2.13.1/img/marker.png' })";
    
    public static final String IMG_ROUTE_END = "new ol.style.Icon({ "
            + "anchor: [0.5, 25], anchorXUnits: 'fraction', anchorYUnits: 'pixels', "
            + "opacity: 0.75, src: 'https://cdnjs.cloudflare.com/ajax/libs/openlayers/2.13.1/img/marker-blue.png' })";
    
    private final List<String> vectors = new ArrayList<>();
    private final String width, height;
    
    public MapBuilder(String width, String height) {
        this.width = width;
        this.height = height;
    }
    
    public MapBuilder polyline(List<Position> positions, String color, int width) {
        String id = "v"+vectors.size();
        StringBuilder sb = new StringBuilder();
        sb.append("var ").append(id).append(" = polyline('").append(PolylineEncoder.encode(positions)).append("');\r\n");
        sb.append(id).append(".setStyle(new ol.style.Style({ stroke: new ol.style.Stroke({color: '").append(color).append("', width: ").append(width).append("})}));\r\n");
        
        vectors.add(sb.toString());
        return this;
    }
    
    public MapBuilder marker(Position position, String name, String icon) {
        String id = "v"+vectors.size();
        StringBuilder sb = new StringBuilder();
        sb.append("var ").append(id).append(" = marker([").append(position.getLongitude())
                .append(", ").append(position.getLatitude()).append("], '").append(name).append("');\r\n");
        sb.append(id).append(".setStyle(new ol.style.Style({\r\n")
                .append("image: ").append(icon).append("\r\n")
                .append("}));");
        
        vectors.add(sb.toString());
        return this;
    }
    
    public String create() {
        StringBuilder output = new StringBuilder();
        output.append("<div id=\"map\" style=\"width: ").append(width)
                .append("; height: ").append(height).append(";\"></div>\r\n");
        output.append("<script type=\"text/javascript\">\r\n");
        
        output.append("//helper functions\r\n");
        output.append(helperFunctions()).append("\r\n");
        output.append("//features\r\n");
        for(String v : vectors)
            output.append(v).append("\r\n");
        
        output.append("var source = new ol.source.Vector({\r\n");
        output.append("  features: [\r\n");
        for(int i=0;i<vectors.size();++i) {
            output.append("          v").append(i);
            if(i != vectors.size()-1)
                output.append(", ");
            else
                output.append("\r\n");
        }
        output.append("  ]\r\n");
        output.append("});\r\n\r\n");
        
        
        output.append("//the map\r\n");
        output.append("var map = new ol.Map({\r\n");
        output.append("  target: 'map',\r\n");
        output.append("  layers: [\r\n");
        output.append("    new ol.layer.Tile({source: new ol.source.OSM()}),\r\n");
        output.append("    new ol.layer.Vector({\r\n");
        output.append("      source: source\r\n");
        output.append("    })\r\n");
        output.append("  ],\r\n");
        output.append("  view: new ol.View()\r\n");
        output.append("});\r\n");
        output.append("map.getView().fit(source.getExtent(), map.getSize());");
        output.append("</script>");
        return output.toString();
    }
    
    private String helperFunctions() {
        return "function polyline(polyString) {\r\n"
                + "  var routeGeom = new ol.format.Polyline().readGeometry(polyString, "
                + "    {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});\r\n"
                + "  return new ol.Feature({ geometry: routeGeom, name: 'Route'});\r\n"  
                + "}\r\n"
                + "function marker(coords, name) {\r\n"
                + "  var geom = new ol.geom.Point(ol.proj.transform(coords, 'EPSG:4326', 'EPSG:3857'));\r\n"
                + "  return new ol.Feature({ geometry: geom, name: name});\r\n"
                + "}\r\n";
    }
}
