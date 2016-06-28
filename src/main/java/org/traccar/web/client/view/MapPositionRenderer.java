/*
 * Copyright 2013 Anton Tananaev (anton.tananaev@gmail.com)
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
import java.util.*;
import java.util.Map;

import com.google.gwt.i18n.client.DateTimeFormat;
import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Pixel;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.control.SelectFeature;
import org.gwtopenmaps.openlayers.client.control.SelectFeatureOptions;
import org.gwtopenmaps.openlayers.client.event.*;
import org.gwtopenmaps.openlayers.client.event.EventObject;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.LineString;
import org.gwtopenmaps.openlayers.client.geometry.MultiLineString;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.layer.RendererOptions;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.layer.VectorOptions;
import org.gwtopenmaps.openlayers.client.util.Attributes;
import org.gwtopenmaps.openlayers.client.util.JSObject;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.Track;
import org.traccar.web.client.TrackSegment;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.state.DeviceVisibilityProvider;
import org.traccar.web.shared.model.*;

public class MapPositionRenderer {

    public interface SelectHandler {
        void onSelected(Position position);
    }

    public interface MouseHandler {
        void onMouseOver(Position position);
        void onMouseOut(Position position);
    }
    
    public static class LayersFactory {
        public static enum LayerFlags {
            LIMIT_VISIBILITY
        }
        
        //http://wiki.openstreetmap.org/wiki/MinScaleDenominator
        private static final int[] SCALE_DENOMINATORS = {559082264, 279541132, 139770566,
            69885283, 34942642, 17471321, 8735660, 4367830, 2183915, 1091958, 545979,
            272989, 136495, 68247, 34124, 17062, 8531, 4265, 2133, 1066, 533
        }; 

        private final Messages i18n = GWT.create(Messages.class);
        private final List<UserSettings.OverlayType> userOverlays;
        private final List<Vector> selectableLayers;
        private final org.gwtopenmaps.openlayers.client.Map map;
        public LayersFactory(org.gwtopenmaps.openlayers.client.Map map) {
            this.selectableLayers = new ArrayList<>();
            this.map = map;
            this.userOverlays = ApplicationContext.getInstance().getUserSettings().overlays();
        }
        
        private Vector createMarkerLayer(UserSettings.OverlayType type, EnumSet<LayerFlags> flags,
                VectorFeatureSelectedListener vfsListener, 
                FeatureHighlightedListener fhListener,
                FeatureUnhighlightedListener fuhListener) {
            VectorOptions markersOptions = new VectorOptions();
            RendererOptions rendererOptions = new RendererOptions();
            rendererOptions.setZIndexing(true);
            markersOptions.setRendererOptions(rendererOptions);
            if(flags.contains(LayerFlags.LIMIT_VISIBILITY))
                markersOptions.setMaxScale(SCALE_DENOMINATORS[5]);
            final String name = i18n.overlayType(type);
            final Vector layer = new Vector(name, markersOptions);
            map.addLayer(layer);
            if(fhListener != null && fuhListener != null)
                addHoverListener(layer, fhListener, fuhListener);
            if(vfsListener != null) {
                addSelectionListener(layer, vfsListener);
                selectableLayers.add(layer);
            }
            layer.setIsVisible(userOverlays.contains(type));
            return layer;
        }
        
        private SelectFeature addHoverListener(final Vector layer,
                FeatureHighlightedListener fhListener,
                FeatureUnhighlightedListener fuhListener) {
            SelectFeatureOptions selectFeatureHoverOptions = new SelectFeatureOptions();
            selectFeatureHoverOptions.setHighlightOnly(true);
            selectFeatureHoverOptions.setHover();
            SelectFeature selectFeatureHover = new SelectFeature(layer, selectFeatureHoverOptions);
            selectFeatureHover.setAutoActivate(true);
            selectFeatureHover.setHover(true);
            selectFeatureHover.setClickOut(false);
            map.addControl(selectFeatureHover);
            selectFeatureHover.addFeatureHighlightedListener(fhListener);
            selectFeatureHover.addFeatureUnhighlightedListener(fuhListener);
            return selectFeatureHover;
        }

        private void addSelectionListener(final Vector layer, 
                VectorFeatureSelectedListener vfsListener) {
            layer.addVectorFeatureSelectedListener(vfsListener);
        }
        
        public void initClickSelection() {
            SelectFeature selectFeature = new SelectFeature(selectableLayers.toArray(new Vector[0]));
            selectFeature.setAutoActivate(true);
            map.addControl(selectFeature);
        }
    }

    private static final int MINIMAL_LEAP_DISTANCE = 50;
    
    private final MapView mapView;
    private final Vector markerLayer;
    private final Vector arrowLayer;

    protected Vector getVectorLayer() {
        return mapView.getVectorLayer();
    }

    protected Vector getMarkerLayer() {
        return markerLayer;
    }
    
    protected Vector getArrowLayer() {
        return arrowLayer;
    }

    private final SelectHandler selectHandler;
    private final MouseHandler mouseHandler;
    private final DeviceVisibilityProvider visibilityProvider;

    public MapPositionRenderer(MapView mapView,
                               LayersFactory layersFactory,
                               final SelectHandler selectHandler,
                               final MouseHandler mouseHandler,
                               DeviceVisibilityProvider visibilityProvider,
                               UserSettings.OverlayType arrowsType,
                               UserSettings.OverlayType markersType) {
        this.mapView = mapView;
        this.selectHandler = selectHandler;
        this.mouseHandler = mouseHandler;
        this.visibilityProvider = visibilityProvider;

        VectorFeatureSelectedListener vfsListener = null;
        if(selectHandler != null)
            vfsListener = new VectorFeatureSelectedListener() {
                @Override
                public void onFeatureSelected(VectorFeatureSelectedListener.FeatureSelectedEvent eventObject) {
                    Position position = getMouseEventPosition(eventObject.getVectorFeature());
                    if(position != null) {
                        selectHandler.onSelected(position);
                    }
                }
            };
        
        FeatureHighlightedListener fhListener = null;
        FeatureUnhighlightedListener fuhListener = null;
        if(mouseHandler != null) {
            fhListener = new FeatureHighlightedListener() {
                @Override
                public void onFeatureHighlighted(VectorFeature vectorFeature) {
                    Position position = getMouseEventPosition(vectorFeature);
                    if(position != null) {
                        mouseHandler.onMouseOver(position);
                    }
                }

            };
            fuhListener = new FeatureUnhighlightedListener() {
                @Override
                public void onFeatureUnhighlighted(VectorFeature vectorFeature) {
                    Position position = getMouseEventPosition(vectorFeature);
                    if (position != null) {
                        mouseHandler.onMouseOut(position);
                    }
                }
            };
        }
        
        arrowLayer = layersFactory.createMarkerLayer(arrowsType,
                EnumSet.of(LayersFactory.LayerFlags.LIMIT_VISIBILITY),
                vfsListener,
                fhListener, fuhListener);
        if(markersType != null)
            markerLayer = layersFactory.createMarkerLayer(markersType,
                    EnumSet.noneOf(LayersFactory.LayerFlags.class),
                    vfsListener,
                    fhListener, fuhListener);
        else
            markerLayer = null;
    }

    private Position getMouseEventPosition(EventObject eventObject) {
        JSObject object = eventObject.getJSObject().getProperty("feature");
        VectorFeature marker = object == null ? null : VectorFeature.narrowToVectorFeature(object);
        return marker == null ? null : getMouseEventPosition(marker);
    }

    private Position getMouseEventPosition(VectorFeature marker) {
        Attributes attributes = marker.getAttributes();
        Long deviceId = Long.valueOf(attributes.getAttributeAsString("d_id"));
        DeviceData deviceData = deviceMap.get(deviceId);
        if (deviceData != null) {
            Long positionId = Long.valueOf(attributes.getAttributeAsString("p_id"));
            Position position = deviceData.positionMap.get(positionId);
            if (position != null) {
                return position;
            }
        }
        return null;
    }

    private void setUpEvents(VectorFeature marker, Position position) {
        if (selectHandler != null || mouseHandler != null) {
            Attributes attributes = marker.getAttributes();
            attributes.setAttribute("d_id", Long.toString(position.getDevice().getId()));
            attributes.setAttribute("p_id", Long.toString(position.getId()));
        }
    }

    private void changeMarkerIcon(Position position, boolean selected) {
        DeviceData deviceData = getDeviceData(position.getDevice());
        VectorFeature oldMarker = deviceData.markerMap.get(position.getId());
        Point point = Point.narrowToPoint(oldMarker.getJSObject().getProperty("geometry"));
        VectorFeature newMarker = new VectorFeature(point, createStyle(position, selected));
        setUpEvents(newMarker, position);
        deviceData.markerMap.put(position.getId(), newMarker);
        getMarkerLayer().removeFeature(oldMarker);
        oldMarker.destroy();
        getMarkerLayer().addFeature(newMarker);
    }
    
    private void changeArrowIcon(Position position, boolean selected) {
        DeviceData deviceData = getDeviceData(position.getDevice());
        VectorFeature oldArrow = deviceData.arrows.get(position.getId());
        Point point = Point.narrowToPoint(oldArrow.getJSObject().getProperty("geometry"));
        VectorFeature newArrow = new VectorFeature(point, createArrowStyle(position, selected));
        setUpEvents(newArrow, position);
        deviceData.arrows.put(position.getId(), newArrow);
        getArrowLayer().removeFeature(oldArrow);
        oldArrow.destroy();
        getArrowLayer().addFeature(newArrow);
    }

    private static class SnappingHandler extends EventHandler {
        // minimum distance in pixels for snapping to occur
        static final int TOLERANCE = 15;
        final DeviceData deviceData;
        final MapView mapView;
        final Vector vectorLayer;
        final MouseHandler mouseHandler;

        VectorFeature feature;
        Style pointStyle;
        Position position;

        double resolution;
        Double cachedTolerance;

        SnappingHandler(DeviceData deviceData, MapView mapView, Vector vectorLayer, MouseHandler mouseHandler) {
            this.deviceData = deviceData;
            this.mapView = mapView;
            this.vectorLayer = vectorLayer;
            this.mouseHandler = mouseHandler;
            mapView.getMap().getEvents().register("mousemove", mapView.getMap(), this);
        }

        @Override
        public void onHandle(EventObject eventObject) {
            JSObject xy = eventObject.getJSObject().getProperty("xy");
            Pixel px = Pixel.narrowToPixel(xy);
            LonLat lonLat = mapView.getMap().getLonLatFromPixel(px);

            Position closestPosition = null;
            double closestSquaredDistance = 0;

            double mouseX = lonLat.lon();
            double mouseY = lonLat.lat();

            MultiLineString lineString = deviceData.trackLine;
            // check bounds
            Bounds bounds = lineString.getBounds();
            if (mouseX >= bounds.getLowerLeftX() - getTolerance() && mouseX <= bounds.getUpperRightX() + getTolerance() &&
                mouseY >= bounds.getLowerLeftY() - getTolerance() && mouseY <= bounds.getUpperRightY() + getTolerance()) {
                // check all points
                for (int j = 0; j < lineString.getNumberOfComponents(); j++) {
                    JSObject jsObject = lineString.getComponent(j);
                    double dX = jsObject.getPropertyAsDouble("x") - mouseX;
                    double dY = jsObject.getPropertyAsDouble("y") - mouseY;

                    double squaredDistance = dX * dX + dY * dY;
                    if (j < deviceData.positions.size()
                            && (closestPosition == null || squaredDistance < closestSquaredDistance)) {
                        closestPosition = deviceData.positions.get(j);
                        closestSquaredDistance = squaredDistance;
                    }
                }
            }

            double distance = Math.sqrt(closestSquaredDistance);
            if (closestPosition != null && distance < getTolerance()) {
                LonLat posLonLat = mapView.createLonLat(closestPosition.getLongitude(), closestPosition.getLatitude());

                if (feature == null) {
                    feature = new VectorFeature(new Point(posLonLat.lon(), posLonLat.lat()), getPointStyle());
                    vectorLayer.addFeature(feature);
                } else {
                    feature.move(new LonLat(posLonLat.lon(), posLonLat.lat()));
                }
            } else {
                if (feature != null) {
                    vectorLayer.removeFeature(feature);
                    feature = null;
                }
            }

            if (position != null &&
                (closestPosition == null || distance > getTolerance() || closestPosition.getId() != position.getId())) {
                mouseHandler.onMouseOut(position);
                position = null;
            }

            if (closestPosition != null && distance < getTolerance()) {
                position = closestPosition;
                mouseHandler.onMouseOver(closestPosition);
            }
        }

        /**
         * @return tolerance in map units
         */
        private double getTolerance() {
            if (cachedTolerance == null || resolution != mapView.getMap().getResolution()) {
                resolution = mapView.getMap().getResolution();
                cachedTolerance = TOLERANCE * resolution;
            }
            return cachedTolerance;
        }

        private Style getPointStyle() {
            if (pointStyle == null) {
                pointStyle = new Style();
                pointStyle.setPointRadius(5d);
                pointStyle.setFillOpacity(1d);
            }
            return pointStyle;
        }

        void destroy() {
            mapView.getMap().getEvents().unregister("mousemove", mapView.getMap(), this);
            if (feature != null) {
                vectorLayer.removeFeature(feature);
                feature.destroy();
                feature = null;
                position = null;
            }
        }
    }

    private static class DeviceMarker {
        final Position position;
        final VectorFeature marker;

        private DeviceMarker(Position position, VectorFeature marker) {
            this.position = position;
            this.marker = marker;
        }
    }

    private static class DeviceData {
        Map<Long, VectorFeature> markerMap = new HashMap<>(); // Position.id -> Marker
        List<Position> positions;
        Map<Long, Position> positionMap = new HashMap<>();
        VectorFeature track;
        VectorFeature alert;
        MultiLineString trackLine;
        List<VectorFeature> trackPoints = new ArrayList<>();
        List<VectorFeature> labels = new ArrayList<>();
        Map<Position, VectorFeature> pauseAndStops = new HashMap<>();
        Map<Position, VectorFeature> timeLabels = new HashMap<>();
        Map<Long, VectorFeature> arrows = new HashMap<>(); // Position.id -> Arrow

        SnappingHandler snappingHandler;

        Position getLatestPosition() {
            return positions == null || positions.isEmpty() ? null : positions.get(positions.size() - 1);
        }
    }

    private Map<Long, DeviceData> deviceMap = new HashMap<>(); // Device.id -> Device Data

    private final DateTimeFormat timeFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.HOUR24_MINUTE);

    private Position selectedPosition;
    private Long selectedDeviceId;

    private DeviceData getDeviceData(List<Position> positions) {
        return positions.isEmpty() ? null : getDeviceData(positions.get(0).getDevice());
    }

    private DeviceData getDeviceData(Device device) {
        return getDeviceData(device.getId());
    }

    private DeviceData getDeviceData(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        DeviceData deviceData = deviceMap.get(deviceId);
        if (deviceData == null) {
            deviceData = new DeviceData();
            deviceMap.put(deviceId, deviceData);
        }
        return deviceData;
    }

    public void clear(Device device) {
        clear(getDeviceData(device));
    }

    public void clear(Long deviceId) {
        clear(getDeviceData(deviceId));
    }

    private void clearMarkersAndTitleAndAlert(DeviceData deviceData) {
        if(getMarkerLayer() != null) {
            getMarkerLayer().destroyFeatures();
            deviceData.markerMap.clear();
        }
        if (deviceData.alert != null) {
            getVectorLayer().removeFeature(deviceData.alert);
            deviceData.alert.destroy();
            deviceData.alert = null;
        }
    }

    private void clear(DeviceData deviceData) {      
        // clear markers and title
        clearMarkersAndTitleAndAlert(deviceData);
        // clear labels
        for (VectorFeature label : deviceData.labels) {
            getVectorLayer().removeFeature(label);
            label.destroy();
        }
        deviceData.labels.clear();
        // clear time labels
        for (VectorFeature label : deviceData.timeLabels.values()) {
            getVectorLayer().removeFeature(label);
            label.destroy();
        }
        deviceData.timeLabels.clear();
        clearArrows(deviceData);
        // clear tracks
        if (deviceData.track != null) {
            getVectorLayer().removeFeature(deviceData.track);
            deviceData.track.destroy();
        }
        deviceData.track = null;
        deviceData.trackLine = null;
        for (VectorFeature trackPoint : deviceData.trackPoints) {
            getVectorLayer().removeFeature(trackPoint);
            trackPoint.destroy();
        }
        deviceData.trackPoints.clear();
        // clear pause and stop icons
        for (VectorFeature pauseOrStop : deviceData.pauseAndStops.values()) {
            getVectorLayer().removeFeature(pauseOrStop);
            pauseOrStop.destroy();
        }
        deviceData.pauseAndStops.clear();

        setSnapToTrack(deviceData, false);
    }
    
    public void clearPositionsAndTitlesAndAlerts() {
        for (DeviceData deviceData : deviceMap.values()) {
            clearMarkersAndTitleAndAlert(deviceData);
            clearArrows(deviceData);
        }
    }

    private void clearArrows(DeviceData deviceData) {
        getArrowLayer().destroyFeatures();
        deviceData.arrows.clear();
    }

    public void clear() {
        for (DeviceData deviceData : deviceMap.values()) {
            clear(deviceData);
        }
        deviceMap.clear();
    }

    public void showPositions(List<Position> positions) {
        showArrows(positions);
        
        DeviceData deviceData = getDeviceData(positions);
        deviceData.positions = positions;
        for (Position position : positions) {
            if (visibilityProvider.isVisible(position.getDevice())) {
                VectorFeature marker = new VectorFeature(
                        mapView.createPoint(position.getLongitude(), position.getLatitude()),
                        createStyle(position, false));
                deviceData.markerMap.put(position.getId(), marker);
                deviceData.positionMap.put(position.getId(), position);

                setUpEvents(marker, position);
                getMarkerLayer().addFeature(marker);
            }
        }

        if (!selectPosition(null, selectedPosition, false)) {
            this.selectedPosition = null;
        }

        if (positions.size() == 1 && selectedDeviceId != null && selectedDeviceId.equals(positions.get(0).getDevice().getId())
                && !selectPosition(null, positions.get(0), false)) {
            selectedDeviceId = null;
        }
    }

    public void showTime(List<Position> positions, boolean abovePoint) {
        DeviceData deviceData = getDeviceData(positions);
        for (Position position : positions) {
            if (visibilityProvider.isVisible(position.getDevice())) {
                org.gwtopenmaps.openlayers.client.Style st = new org.gwtopenmaps.openlayers.client.Style();
                st.setLabel(timeFormat.format(position.getTime()));
                st.setLabelXOffset(0);
                st.setLabelYOffset(abovePoint ? 12 : -12);
                st.setLabelAlign("cb");
                st.setFontColor("#FF4D00");
                st.setFontSize("11");
                st.setFill(false);
                st.setStroke(false);

                final VectorFeature point = new VectorFeature(mapView.createPoint(position.getLongitude(), position.getLatitude()), st);
                getVectorLayer().addFeature(point);
                deviceData.timeLabels.put(position, point);
            }
        }
    }

    public void showTrack(Track track, boolean breakOnLeaps) {
        List<TrackSegment> segments = track.getSegments();
        if (!segments.isEmpty()
                && visibilityProvider.isVisible(segments.get(0).getPositions().get(0).getDevice())) {
            DeviceData deviceData = getDeviceData(segments.get(0).getPositions());

            List<List<Point>> polylines = new ArrayList<>();
            List<Point> currentLine = new ArrayList<>();
            
            for (TrackSegment segment : segments) {
                if (segment.getGeometry() == null) {
                    for (Position position : segment.getPositions()) {
                        if(position.getDistance() > MINIMAL_LEAP_DISTANCE && breakOnLeaps) {
                            polylines.add(currentLine);
                            currentLine = new ArrayList<>();
                        }
                        currentLine.add(mapView.createPoint(position.getLongitude(), position.getLatitude()));
                    }
                    polylines.add(currentLine);
                } else {
                    for (VectorFeature feature : segment.getGeometry()) {
                        LineString lineString = LineString.narrowToLineString(feature.getJSObject().getProperty("geometry"));
                        for (int i = 0; i < lineString.getNumberOfComponents(); i++) {
                            Point point = Point.narrowToPoint(lineString.getComponent(i));
                            currentLine.add(mapView.createPoint(point.getX() / 10, point.getY() / 10));
                        }
                    }
                }
            }

            List<LineString> lineStrings = new ArrayList<>();

            if(deviceData.track == null) {
                for(List<Point> polyline : polylines) {
                    lineStrings.add(new LineString(polyline.toArray(new Point[polyline.size()])));
                    deviceData.positions = track.getPositions();
                }
                deviceData.trackLine = new MultiLineString(lineStrings.toArray(new LineString[lineStrings.size()]));
            } else {
                List<Position> trackPositions = track.getPositions();
                if(deviceData.positions.get(deviceData.positions.size() - 1).equals(trackPositions.get(0))) {
                    trackPositions.remove(0);
                    List<Point> polyline = polylines.get(0);
                    polylines.remove(0);
                    LineString lastSegment = LineString.narrowToLineString(
                        deviceData.trackLine.getComponent(deviceData.trackLine.getNumberOfComponents() -1));
                    for(int i = 1; i < polyline.size(); ++i) {
                        lastSegment.addPoint(polyline.get(i), lastSegment.getNumberOfComponents());
                    }
                }
                for(List<Point> polyline : polylines) {
                    lineStrings.add(new LineString(polyline.toArray(new Point[polyline.size()])));
                }
                deviceData.positions.addAll(trackPositions);
                deviceData.trackLine.addComponents(lineStrings.toArray(new LineString[lineStrings.size()]));
            }

            // Assigns color to style
            Style style = new Style();
            Style defaultStyle = mapView.getVectorLayer().getStyle();
            style.setStrokeColor("#" + track.getStyle().getTrackColor());
            style.setStrokeOpacity(defaultStyle.getStrokeOpacity());
            style.setStrokeWidth(defaultStyle.getStrokeWidth());
            style.setStrokeDashstyle(defaultStyle.getStrokeDashstyle());
            style.setStrokeLinecap(defaultStyle.getStrokeLinecap());

            VectorFeature mapTrack = new VectorFeature(deviceData.trackLine, style);
            getVectorLayer().addFeature(mapTrack);
            deviceData.track = mapTrack;

            if (track.getStyle().getZoomToTrack())
                mapView.getMap().zoomToExtent(deviceData.trackLine.getBounds());
        }
    }

    public void setSnapToTrack(Device device, boolean snap) {
        setSnapToTrack(getDeviceData(device), snap);
    }

    private void setSnapToTrack(DeviceData deviceData, boolean snap) {
        if (snap) {
            if (deviceData.snappingHandler == null) {
                deviceData.snappingHandler = new SnappingHandler(deviceData, mapView, getVectorLayer(), mouseHandler);
            }
        } else {
            if (deviceData.snappingHandler != null) {
                deviceData.snappingHandler.destroy();
                deviceData.snappingHandler = null;
            }
        }
    }

    public void selectPosition(Position position, boolean center) {
        if (selectPosition(selectedPosition, position, center)) {
            selectedPosition = position;
        } else {
            selectedPosition = null;
        }
    }

    public void selectDevice(Device device, boolean center) {
        if (!visibilityProvider.isVisible(device)) {
            return;
        }
        DeviceData oldDeviceData = getDeviceData(selectedDeviceId);
        Position oldPosition = oldDeviceData == null ? null : oldDeviceData.getLatestPosition();

        DeviceData newDeviceData = getDeviceData(device);
        Position newPosition = newDeviceData == null ? null : newDeviceData.getLatestPosition();
        if (selectPosition(oldPosition, newPosition, center)) {
            selectedDeviceId = device.getId();
        } else {
            selectedDeviceId = null;
        }
    }

    public void zoomIn(Device device) {
        if (!visibilityProvider.isVisible(device)) {
            return;
        }

        DeviceData deviceData = getDeviceData(device);
        if (deviceData.positions.size() > 0) {
            UserSettings userSettings = ApplicationContext.getInstance().getUserSettings();
            Short zoomLevel = userSettings.getFollowedDeviceZoomLevel();
            if (zoomLevel != null) {
                mapView.getMap().zoomTo(zoomLevel);
            }
        }
    }

    private boolean selectPosition(Position oldPosition, Position newPosition, boolean center) {
        if (oldPosition != null) {
            DeviceData deviceData = getDeviceData(oldPosition.getDevice());
            if (deviceData.arrows.containsKey(oldPosition.getId())) {
                changeArrowIcon(oldPosition, false);
            }
            if (deviceData.markerMap.containsKey(oldPosition.getId())) {
                changeMarkerIcon(oldPosition, false);
            }
        }
        if (newPosition != null) {
            DeviceData deviceData = getDeviceData(newPosition.getDevice());
            Point point = null;
            if (deviceData.arrows.containsKey(newPosition.getId())) {
                changeArrowIcon(newPosition, true);
                point = Point.narrowToPoint(deviceData.arrows.get(newPosition.getId()).getJSObject().getProperty("geometry"));
            }
            if (deviceData.markerMap.containsKey(newPosition.getId())) {
                changeMarkerIcon(newPosition, true);
                VectorFeature marker = deviceData.markerMap.get(newPosition.getId());
                point = Point.narrowToPoint(marker.getJSObject().getProperty("geometry"));
            }
            if(center) {
                mapView.getMap().panTo(new LonLat(point.getX(), point.getY()));
            }
            return point != null;
        }
        return false;
    }

    public void catchPosition(Position position) {
        if (visibilityProvider.isVisible(position.getDevice())
            && !mapView.getMap().getExtent().containsLonLat(mapView.createLonLat(position.getLongitude(), position.getLatitude()), true)) {
            selectPosition(position, true);
        }
    }

    public void clearTrackPositions(Device device, Date before) {                
        DeviceData deviceData = getDeviceData(device);
        if (deviceData.track != null) {
            boolean updated = false;
            MultiLineString trackLine = deviceData.trackLine;
            LineString currentPolyline = LineString.narrowToLineString(trackLine.getComponent(0));
            trackLine.removeComponent(currentPolyline);

            while (deviceData.positions.size() > 0) {
                if (deviceData.positions.get(0).getTime().after(before)) {
                    break;
                }
                Position position = deviceData.positions.remove(0);
                
                if(currentPolyline.getVertices(true).length > 2)
                    currentPolyline.removePoint(Point.narrowToPoint(currentPolyline.getComponent(0)));
                else if(trackLine != null) {
                    if(trackLine.getNumberOfComponents() == 0) {
                        trackLine = null;
                        deviceData.trackLine = null;
                    } else {
                        currentPolyline = LineString.narrowToLineString(trackLine.getComponent(0));
                        trackLine.removeComponent(currentPolyline);
                    }
                }
                if(!deviceData.trackPoints.isEmpty())
                    getVectorLayer().removeFeature(deviceData.trackPoints.remove(0));
                
                updated = true;

                VectorFeature timeLabel = deviceData.timeLabels.remove(position);
                if (timeLabel != null) {
                    getVectorLayer().removeFeature(timeLabel);
                    timeLabel.destroy();
                }

                VectorFeature arrow = deviceData.arrows.remove(position.getId());
                if (arrow != null) {
                    getVectorLayer().removeFeature(arrow);
                    arrow.destroy();
                }

                VectorFeature pauseOrStop = deviceData.pauseAndStops.remove(position);
                if (pauseOrStop != null) {
                    getVectorLayer().removeFeature(pauseOrStop);
                    pauseOrStop.destroy();
                }
            }
            if (updated) {
                getVectorLayer().removeFeature(deviceData.track);
                deviceData.track.destroy();
                if(trackLine != null) {
                    VectorFeature track = new VectorFeature(trackLine, deviceData.track.getStyle());
                    getVectorLayer().addFeature(track);
                    deviceData.track = track;
                }
            }
        }
    }

    public void showTrackPositions(List<Position> positions) {
        DeviceData deviceData = getDeviceData(positions);
        for (Position position : positions) {
            if (visibilityProvider.isVisible(position.getDevice())) {
                VectorFeature point = new VectorFeature(mapView.createPoint(position.getLongitude(), position.getLatitude()), getTrackPointStyle());
                getVectorLayer().addFeature(point);
                setUpEvents(point, position);
                deviceData.trackPoints.add(point);
                deviceData.markerMap.put(position.getId(), point);
                deviceData.positionMap.put(position.getId(), position);
            }
        }
    }

    org.gwtopenmaps.openlayers.client.Style trackPointStyle;

    private org.gwtopenmaps.openlayers.client.Style getTrackPointStyle() {
        if (trackPointStyle == null) {
            trackPointStyle = new org.gwtopenmaps.openlayers.client.Style();
            trackPointStyle.setPointRadius(5d);
            trackPointStyle.setFillOpacity(1d);
        }
        return trackPointStyle;
    }

    public void updateIcon(Device device) {
        if (visibilityProvider.isVisible(device)) {
            DeviceData deviceData = getDeviceData(device);
            Position position = deviceData.positions == null || deviceData.positions.size() != 1 ? null : deviceData.positions.get(0);
            if (position != null) {
                position.setDevice(device);
                position.setIcon(MarkerIcon.create(position).setName(device.isShowName()));
                boolean selected = selectedPosition != null && selectedPosition.getId() == position.getId();
                changeMarkerIcon(position, selected);
            }
        }
    }

    public void showAlerts(Collection<Position> positions) {
        if (positions != null) {
            for (Position position : positions) {
                if (visibilityProvider.isVisible(position.getDevice())) {
                    drawAlert(position);
                }
            }
        }
    }

    private void drawAlert(Position position) {
        DeviceData deviceData = getDeviceData(position.getDevice());

        int iconWidthHalf = (position.getIcon().isArrow()
                ? (5 + (int) Math.floor(position.getDevice().getIconArrowRadius()))
                : position.getIcon().getWidth()) / 2;
        int iconHeight = position.getIcon().isArrow()
                ? (5 + (int) Math.floor(position.getDevice().getIconArrowRadius()))
                : position.getIcon().getHeight();

        Style alertCircleStyle = new org.gwtopenmaps.openlayers.client.Style();
        alertCircleStyle.setPointRadius(Math.sqrt(iconWidthHalf * iconWidthHalf + iconHeight * iconHeight) + 1);
        alertCircleStyle.setFillOpacity(0d);
        alertCircleStyle.setStrokeWidth(2d);
        alertCircleStyle.setStrokeColor("#ff0000");

        VectorFeature alertCircle = new VectorFeature(mapView.createPoint(position.getLongitude(), position.getLatitude()), alertCircleStyle);
        getVectorLayer().addFeature(alertCircle);
        deviceData.alert = alertCircle;
    }

    public void updateAlert(Device device, boolean show) {
        DeviceData deviceData = getDeviceData(device);
        if (deviceData.alert != null) {
            getVectorLayer().removeFeature(deviceData.alert);
            deviceData.alert.destroy();
        }
        if (show && visibilityProvider.isVisible(device)) {
            Position latestPosition = deviceData.getLatestPosition();
            if (latestPosition != null) {
                drawAlert(latestPosition);
            }
        }
    }

    private static final int IDLE_ICON_WIDTH = 10;
    private static final int IDLE_ICON_HEIGHT = 10;

    private Style createStyle(Position position, boolean selected) {
        Style style = position.getIcon().isArrow()
                ? createArrowStyle(position, getBgColor(position))
                : createIconStyle(position, selected);

        if (position.getIcon().isName()) {
            style.setLabel(position.getDevice().getName());
            style.setLabelXOffset(0);
            // Calculate Y offset
            int yOffset;
            if (position.getIcon().isArrow()) {
                yOffset = -10 - (int) Math.floor(position.getDevice().getIconArrowRadius());
            } else {
                yOffset = -12;
                if (position.getCourse() != null
                    && position.getDevice().isIconRotation()) {
                    double sin = Math.sin(position.getCourse() * Math.PI / 180);
                    double cos = Math.cos(position.getCourse() * Math.PI / 180);
                    double yIconTop = cos * (selected ? position.getIcon().getSelectedHeight() : position.getIcon().getHeight());
                    double yHalfWidth = sin * (selected ? position.getIcon().getSelectedWidth() : position.getIcon().getWidth()) / 2;

                    if (yIconTop >= 0) {
                        yOffset -= Math.abs(yHalfWidth);
                    } else {
                        yOffset += yIconTop - Math.abs(yHalfWidth);
                    }
                }
            }
            style.setLabelYOffset(yOffset);
            style.setLabelAlign("cb");
            style.setFontColor("#0000FF");
            style.setFontSize("12");
        }

        return style;
    }
    
    private Style createArrowStyle(Position position, boolean selected) {
        if(selected)
            return createArrowStyle(position, "00FF00");
        else
            return createArrowStyle(position, getBgColor(position));
    }

    private Style createIconStyle(Position position, boolean selected) {
        PositionIcon icon = position.getIcon();

        Style style = new Style();
        int width = selected ? icon.getSelectedWidth() : icon.getWidth();
        int height = selected ? icon.getSelectedHeight() : icon.getHeight();

        style.setExternalGraphic(selected ? icon.getSelectedURL() : icon.getURL());
        style.setGraphicSize(width, height);
        style.setGraphicOffset(-width / 2, -height);
        style.setGraphicOpacity(1.0);
        style.setGraphicZIndex(10);

        String graphic = getIdleIcon(position);
        if (graphic != null) {
            style.setBackgroundGraphic(graphic);
            style.setBackgroundGraphicSize(IDLE_ICON_WIDTH, IDLE_ICON_HEIGHT);
            style.setBackgroundOffset(width / 2 - IDLE_ICON_WIDTH / 2, -height - 2);
            style.setBackgroundGraphicZIndex(11);
        }

        if (position.getDevice().isIconRotation() && position.getCourse() != null) {
            style.setRotation(position.getCourse().toString());
        }

        return style;
    }

    private String getIdleIcon(Position position) {
        if (position.getIdleStatus() != null && position.getIdleStatus() != Position.IdleStatus.MOVING) {
            switch (position.getIdleStatus()) {
                case PAUSED:
                    return "img/paused.svg";
                case IDLE:
                    return "img/stopped.svg";
            }
        }
        return null;
    }

    public void showPauseAndStops(List<Position> positions) {
        DeviceData deviceData = getDeviceData(positions);
        for (Position position : positions) {
            String graphic = getIdleIcon(position);
            if (graphic != null) {
                Style style = new Style();
                style.setExternalGraphic(graphic);
                style.setGraphicSize(IDLE_ICON_WIDTH * 3 / 2, IDLE_ICON_HEIGHT * 3 / 2);
                style.setGraphicOpacity(1.0);
                VectorFeature pauseOrStop = new VectorFeature(
                        mapView.createPoint(position.getLongitude(), position.getLatitude()),
                        style);
                getVectorLayer().addFeature(pauseOrStop);
                deviceData.pauseAndStops.put(position, pauseOrStop);
            }
        }
    }

    private String getBgColor(Position position) {
        String bgColor = position.getDevice().getIconArrowStoppedColor();
        if (position.getStatus() == Position.Status.OFFLINE || position.getIdleStatus() == null) {
            bgColor = position.getDevice().getIconArrowOfflineColor();
        } else {
            switch (position.getIdleStatus()) {
                case MOVING:
                    bgColor = position.getDevice().getIconArrowMovingColor();
                    break;
                case PAUSED:
                    bgColor = position.getDevice().getIconArrowPausedColor();
                    break;
                case IDLE:
                    bgColor = position.getDevice().getIconArrowStoppedColor();
                    break;
            }
        }
        return bgColor;
    }

    private Style createArrowStyle(Position position, String bgColor) {
        Style style = new Style();
        style.setExternalGraphic("/MapMarker?color="+bgColor);
        style.setGraphicOpacity(1.0);

        style.setFillColor("#" + bgColor);
        style.setFill(true);
        style.setPointRadius(position.getDevice().getIconArrowRadius());
        if (position.getCourse() != null) {
            style.setRotation(position.getCourse().toString());
        }
        return style;
    }

    public void showArrows(List<Position> positions) {
        DeviceData deviceData = getDeviceData(positions);
        for (Position position : positions) {
            if (visibilityProvider.isVisible(position.getDevice())
                    && !deviceData.arrows.containsKey(position.getId())) {
                VectorFeature arrow = new VectorFeature(
                        mapView.createPoint(position.getLongitude(), position.getLatitude()),
                        createArrowStyle(position, getBgColor(position)));
                deviceData.arrows.put(position.getId(), arrow);
                deviceData.positionMap.put(position.getId(), position);
                setUpEvents(arrow, position);
                getArrowLayer().addFeature(arrow);
            }
        }
    }
}
