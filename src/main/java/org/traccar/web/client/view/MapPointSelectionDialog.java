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
package org.traccar.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.Marker;
import org.gwtopenmaps.openlayers.client.event.MapClickListener;
import org.gwtopenmaps.openlayers.client.layer.Markers;
import org.gwtopenmaps.openlayers.client.layer.OSM;

public class MapPointSelectionDialog {
    interface _UiBinder extends UiBinder<Widget, MapPointSelectionDialog> {}
    private static _UiBinder uiBinder = GWT.create(_UiBinder.class);
    
    @UiField
    Window window;
    
    private Map map;
    private Marker marker;
    private PointSelectedListener listener;
    
    public MapPointSelectionDialog(PointSelectedListener listener) { 
        this.listener = listener;
        
        uiBinder.createAndBindUi(this);
        
        MapOptions mapOptions = new MapOptions();
        mapOptions.setMaxExtent(new Bounds(-20037508.34, -20037508.34, 20037508.34, 20037508.34));
        MapWidget mapWidget = new MapWidget("100%", "100%", mapOptions);
        map = mapWidget.getMap();
        map.addLayer(OSM.Mapnik("OpenStreetMap"));
        LonLat center = new LonLat(19, 52);
        center.transform("EPSG:4326", map.getProjection());
        map.setCenter(center, 7);
        final Markers markers = new Markers("");
        map.addLayer(markers);
        map.addMapClickListener(new MapClickListener() {
            @Override
            public void onClick(MapClickListener.MapClickEvent ev) {
                if(marker != null)
                    markers.removeMarker(marker);
                marker = new Marker(ev.getLonLat());
                markers.addMarker(marker);
            }
            
        });
        window.add(mapWidget);
    }
    
    public void show() {
        window.show();
    }
    
    public void hide() {
        window.hide();
    }
    
    @UiHandler("saveButton")
    public void save(SelectEvent selectEvent) {
        window.hide();
        LonLat ll = marker.getLonLat();
        ll.transform(map.getProjection(), "EPSG:4326");
        listener.onPointSelected(ll);
    }
    
    @UiHandler("cancelButton")
    public void cancel(SelectEvent selectEvent) {
       window.hide(); 
    }
    
    public interface PointSelectedListener {
        void onPointSelected(LonLat lonLat);
    }
}
