/*
 * Copyright 2018 Datamatica (dev@datamatica.pl)
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

import java.util.ArrayList;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.geometry.LineString;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.traccar.web.client.view.GeoFenceRenderer.IMapView;
import org.traccar.web.client.view.RouteDialog;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.GeoFence.LonLat;

public class PolylineDecoder {    
    public static LineString decode(IMapView map, String polyStr) {
        ArrayList<Point> linePoints = new ArrayList<>();
        Object[] pts = decode(polyStr);
        for(int i=0;i<pts.length;++i) {
            String[] latLon=pts[i].toString().split(",");
            if(latLon.length != 2 || latLon[0] == null  || latLon[0].isEmpty()
                    || latLon[0].equals("undefined") || latLon[1] == null
                    || latLon[1].equals("undefined") || latLon[1].isEmpty())
                continue;
            Point pt;
            if(map == null)
                pt = new Point(Double.parseDouble(latLon[1]), Double.parseDouble(latLon[0]));
            else
                pt = map.createPoint(Double.parseDouble(latLon[1]), Double.parseDouble(latLon[0]));
            linePoints.add(pt);
        }
        return new LineString(linePoints.toArray(new Point[0]));
    }
    
    public static LonLat[] decodeToLonLat(String polyStr) {
        LineString ls = PolylineDecoder.decode(null, polyStr);
        Point[] pts = ls.getVertices(true);
        GeoFence.LonLat[] gll = new GeoFence.LonLat[pts.length];
        for(int i=0;i < pts.length;++i)
            gll[i] = new GeoFence.LonLat(pts[i].getX(), pts[i].getY());
        return gll;
    }
    
    private static native Object[] decode(String polyStr) /*-{
        return $wnd.polyline.decode(polyStr);
    }-*/;
}
