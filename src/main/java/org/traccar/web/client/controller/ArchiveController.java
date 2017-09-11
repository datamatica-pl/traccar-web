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
package org.traccar.web.client.controller;

import pl.datamatica.traccar.model.PositionIconType;
import pl.datamatica.traccar.model.UserSettings;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.Device;
import java.util.*;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.*;
import com.sencha.gxt.widget.core.client.box.AutoProgressMessageBox;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.format.EncodedPolyline;
import org.traccar.web.client.*;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.BaseAsyncCallback;
import org.traccar.web.client.view.ArchiveView;
import org.traccar.web.client.view.FilterDialog;
import org.traccar.web.client.view.ReportsMenu;
import org.traccar.web.client.view.UserSettingsDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import org.traccar.web.client.model.api.ApiError;
import org.traccar.web.client.model.api.ApiRequestCallback;
import org.traccar.web.client.model.api.DevicePositionsService;
import org.traccar.web.client.widget.InfoMessageBox;
import pl.datamatica.traccar.model.UserPermission;

public class ArchiveController implements ContentController, ArchiveView.ArchiveHandler {

    public interface ArchiveHandler {
        void onSelected(Position position);
        void onClear(Device device);
        void onDrawTrack(Track track);
    }

    private final ArchiveHandler archiveHandler;

    private final UserSettingsDialog.UserSettingsHandler userSettingsHandler;

    private final ArchiveView archiveView;

    private final Messages i18n = GWT.create(Messages.class);

    private boolean snapToRoads;
    private final Map<Long, Track> originalTracks;
    private final Map<Long, Track> snappedTracks;
    private final ListStore<Device> deviceStore;

    public ArchiveController(ArchiveHandler archiveHandler,
                             UserSettingsDialog.UserSettingsHandler userSettingsHandler,
                             ListStore<Device> deviceStore,
                             ListStore<Report> reportStore,
                             ReportsMenu.ReportHandler reportHandler) {
        this.archiveHandler = archiveHandler;
        this.userSettingsHandler = userSettingsHandler;
        this.archiveView = new ArchiveView(this, deviceStore, reportStore, reportHandler);
        this.originalTracks = new HashMap<>();
        this.snappedTracks = new HashMap<>();
        this.deviceStore = deviceStore;
    }

    @Override
    public ContentPanel getView() {
        return archiveView.getView();
    }

    @Override
    public void run() {
    }

    @Override
    public void onSelected(Position position) {
        archiveHandler.onSelected(position);
    }

    @Override
    public void onLoad(final Device device, Date from, Date to, boolean filter, final ArchiveStyle style) {
        if (device != null && from != null && to != null) {
            if (!validateSubscription(device, from, to)) {
                return;
            }
            
            final AutoProgressMessageBox progress = new AutoProgressMessageBox(i18n.archive(), i18n.loadingData());
            progress.auto();
            progress.show();
            DevicePositionsService service = new DevicePositionsService();
            service.getPositions(device, from, to, filter, new ApiRequestCallback(i18n) {
                @Override
                public void onSuccess(String response) {
                    JSONValue v = JSONParser.parseStrict(response);
                    List<Position> result = Application.getDecoder()
                            .decodePositions(device, v.isObject());
                    archiveHandler.onClear(device);
                    if(result.isEmpty()) {
                        progress.hide();
                        new AlertMessageBox(i18n.error(), i18n.errNoResults()).show();
                    }
                    originalTracks.put(device.getId(), new Track(result, style));
                    snappedTracks.remove(device.getId());
                    if(snapToRoads) {
                        loadSnappedPointsAndShowTrack(device);
                    } else {
                        showArchive(device);
                    }
                    progress.hide();
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    super.onError(request, exception);
                    progress.hide();
                }
                
            });
        } else {
            new AlertMessageBox(i18n.error(), i18n.errFillFields()).show();
        }
    }
    
    private boolean validateSubscription(Device device, Date from, Date to) {
        
        if (ApplicationContext.getInstance().getUser().hasPermission(UserPermission.ALL_HISTORY))
            return true;
       
        int sub = device.getHistoryLength();
        if (device.getValidTo() != null && device.getValidTo().before(new Date()))
            sub = 2;
        
        Date dev = new Date();
        CalendarUtil.addDaysToDate(dev, -1 * sub);
       
        new InfoMessageBox("LOG", "DEV: " + dev + " FROM: " + from + " TO: " + to + " SUB: " + sub).show();
        
        if (CalendarUtil.getDaysBetween(from, dev) > 0 && CalendarUtil.getDaysBetween(to, dev) > 0) {
            new InfoMessageBox(i18n.errNoSubscriptionTitle(), i18n.errNoSubscriptionMessage()).show();
            return false;
        }
        return true;
    }

    private void showArchive(Device device) {
        archiveHandler.onClear(device);
        Track track = snapToRoads ? snappedTracks.get(device.getId()) : originalTracks.get(device.getId());
        archiveHandler.onDrawTrack(track);
        archiveView.showPositions(device, track.getPositions());
    }

    @Override
    public void onSnapToRoads(boolean snapToRoads) {
        this.snapToRoads = snapToRoads;
        for (Map.Entry<Long, Track> entry : originalTracks.entrySet()) {
            Long deviceId = entry.getKey();
            Device device = deviceStore.findModelWithKey(deviceId.toString());
            Track snappedTrack = snappedTracks.get(deviceId);
            if (snapToRoads && snappedTrack == null) {
                loadSnappedPointsAndShowTrack(device);
            } else {
                showArchive(device);
            }
        }
    }

    @Override
    public void onClear(Device device) {
        originalTracks.remove(device.getId());
        snappedTracks.remove(device.getId());
        archiveHandler.onClear(device);
    }

    @Override
    public void onFilterSettings() {
        new FilterDialog(ApplicationContext.getInstance().getUserSettings(), userSettingsHandler).show();
    }

    @Override
    public void onChangeArchiveMarkerType(PositionIconType newMarkerType) {
        UserSettings settings = ApplicationContext.getInstance().getUserSettings();
        settings.setArchiveMarkerType(newMarkerType);
        userSettingsHandler.onSave(settings);
    }

    public void selectPosition(Position position) {
        archiveView.selectPosition(position);
    }

    public void selectDevice(Device device) {
        archiveView.selectDevice(device);
    }

    public static class Matchings extends JavaScriptObject {
        protected Matchings() {
        }

        public final native Matching[] getMatchings() /*-{
            return this.matchings;
        }-*/;
        
        public final native Tracepoint[] getTracepoints() /*-{
            return this.tracepoints;    
        }-*/;
    }
    
    public static class Tracepoint extends JavaScriptObject {
        protected Tracepoint() {
        }
        
        public final native double[] getLocation() /*-{
            return this.location;   
        }-*/;
        
        public final native int getWaypointIndex() /*-{
            return this.waypoint_index;
        }-*/;
        
        public final native int getMatchingIndex() /*-{
                return this.matchings_index;
        }-*/;
    }

    public static class Matching extends JavaScriptObject {        
        protected Matching() {
        }

        public final native String getGeometry() /*-{
            return this.geometry;
        }-*/;
    }
    
    public static class MatchingWrapper {
        private final List<Integer> indices = new ArrayList<>();
        private final List<List<Double>> matchedPoints = new ArrayList<>();
        private final Matching matching;
        
        public MatchingWrapper(Matching matching) {
            this.matching = matching;
        }
        
        public String getGeometry() {
            return matching.getGeometry();
        }
        
        public final double[] getMatchedPoint(int i) {
            List<Double> list = matchedPoints.get(i);
            double[] array = new double[2];
            array[0] = list.get(0);
            array[1] = list.get(1);
            return array;
        }
        
        public final void addMatchedPoint(double[] lonLat) {
            List<Double> list = new ArrayList<>();
            list.add(lonLat[0]);
            list.add(lonLat[1]);
            matchedPoints.add(list);
        }

        public final int getIndex(int i) {
            return indices.get(i);
        }
        
        public int getIndicesCount() {
            return indices.size();
        }
        
        public final void addIndex(int index) {
            indices.add(index);
        }
    }

    private void loadSnappedPointsAndShowTrack(final Device device) {
        final Track track = originalTracks.get(device.getId());

        final List<Position> originalPositions = track.getPositions();
        if(originalPositions.size() < 2)
            return;
        
        StringBuilder positions = new StringBuilder();
        StringBuilder timestamps = new StringBuilder();
        for (Position position : originalPositions) {
            positions.append(formatLonLat(position.getLongitude())).append(",")
                    .append(formatLonLat(position.getLatitude())).append(";");
            timestamps.append(position.getTime().getTime()/1000).append(";");
        }
        positions.deleteCharAt(positions.length()-1);
        timestamps.deleteCharAt(timestamps.length()-1);

        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, 
                ApplicationContext.getInstance().getApplicationSettings().getMatchServiceURL()
                +"/"+positions+"?timestamps="+timestamps);
        try {
            builder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (200 == response.getStatusCode()) {
                        Matchings jsMatchings = JsonUtils.safeEval(response.getText());
                        List<MatchingWrapper> matchings = new ArrayList<>(jsMatchings.getMatchings().length);
                        for(Matching m : jsMatchings.getMatchings())
                            matchings.add(new MatchingWrapper(m));
                        for(Tracepoint tp : jsMatchings.getTracepoints()) {
                            MatchingWrapper matching = matchings.get(tp.getMatchingIndex());
                            matching.addIndex(tp.getWaypointIndex());
                            matching.addMatchedPoint(tp.getLocation());
                        }
                        
                        Track snappedTrack = new Track();
                        int lastIndex = 0;
                        for (MatchingWrapper matching : matchings) {
                            // add original track segment
                            List<Position> originalTrack = lastIndex < matching.getIndex(0)
                                    ? Collections.<Position>emptyList()
                                    : originalPositions.subList(lastIndex, matching.getIndex(0));
                            // add snapped track segment
                            List<Position> snappedPositions = new ArrayList<>(matching.getIndicesCount());
                            for (int i = 0; i < matching.getIndicesCount(); i++) {
                                int snappedPositionIndex = matching.getIndex(i);
                                double[] lonLat = matching.getMatchedPoint(i);
                                Position snapped = new Position(originalPositions.get(snappedPositionIndex));
                                snapped.setLongitude(lonLat[0]);
                                snapped.setLatitude(lonLat[1]);
                                snappedPositions.add(snapped);
                            }
                            EncodedPolyline encodedPolyline = new EncodedPolyline();
                            VectorFeature[] geometry = encodedPolyline.read(matching.getGeometry());
                            snappedTrack.addSegment(originalTrack, null, track.getStyle());
                            snappedTrack.addSegment(snappedPositions, geometry, track.getStyle());
                            lastIndex = matching.getIndex(matching.getIndicesCount()-1) + 1;
                        }
                        if (lastIndex < originalPositions.size()) {
                            snappedTrack.addSegment(originalPositions.subList(lastIndex, originalPositions.size()), null, track.getStyle());
                        }
                        snappedTracks.put(device.getId(), snappedTrack);
                        showArchive(device);
                    } else {
                        new AlertMessageBox(i18n.error(), i18n.errSnapToRoads(response.getStatusCode(), response.getText())).show();
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    new AlertMessageBox(i18n.error(), i18n.errSnapToRoads(-1, exception.getLocalizedMessage())).show();
                }
            });
        } catch (RequestException re) {
            GWT.log("Request failed", re);
        }
    }

    static native String formatLonLat(double lonLat) /*-{
        return lonLat.toFixed(6);
    }-*/;
}
