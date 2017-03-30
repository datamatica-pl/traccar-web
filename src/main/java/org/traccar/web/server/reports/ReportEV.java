/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
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

import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.Maintenance;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.DeviceEvent;
import pl.datamatica.traccar.model.Device;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.traccar.web.server.reports.MapBuilder.MarkerStyle;
import pl.datamatica.traccar.model.DeviceEventType;
import static pl.datamatica.traccar.model.DeviceEventType.*;

public class ReportEV extends ReportGenerator {
    @Override
    void generateImpl(Report report) throws IOException {
        h2(report.getName());

        for (Device device : getDevices(report)) {
            List<DeviceEvent> events = entityManager.createQuery("SELECT e FROM DeviceEvent e" +
                    " INNER JOIN FETCH e.position" +
                    " WHERE e.device=:device AND e.time BETWEEN :from AND :to "
                    + "AND e.type in (:validTypes)"
                    + "ORDER BY e.time", DeviceEvent.class)
                    .setParameter("device", device)
                    .setParameter("from", report.getFromDate())
                    .setParameter("to", report.getToDate())
                    .setParameter("validTypes", EnumSet.of(GEO_FENCE_ENTER, GEO_FENCE_EXIT, OVERSPEED))
                    .getResultList();
            panelStart();

            // heading
            panelHeadingStart();
            text(device.getName());
            panelHeadingEnd();

            // body
            panelBodyStart();
            // period
            paragraphStart();
            bold(message("timePeriod") + ": ");
            text(formatDate(report.getFromDate()) + " - " + formatDate(report.getToDate()));
            paragraphEnd();
            // device details
            deviceDetails(device);
            // data table
            if (!events.isEmpty()) {
                drawTable(getGeoFences(report, device), events);
                if(report.isIncludeMap())
                    drawMap(events);
            }

            panelBodyEnd();

            panelEnd();

        }
    }
    
    private void drawMap(List<DeviceEvent> events) {
        MapBuilder builder = getMapBuilder();
        for(DeviceEvent ev : events) {
            if(isVisible(ev))
                builder.marker(ev.getPosition(), 
                        MarkerStyle.event(ev.getType(), getLabel(ev)));
        }
        html(builder.bindWithTable("table", 1).create());
    }

    private boolean isVisible(DeviceEvent ev) {
        return ev.getType() == DeviceEventType.GEO_FENCE_ENTER 
                || ev.getType() == DeviceEventType.GEO_FENCE_EXIT
                || ev.getType() == DeviceEventType.OVERSPEED;
                
    }
    
    private String getLabel(DeviceEvent ev) {
        switch(ev.getType()) {
            case OVERSPEED:
                return String.format("V: %.0f km/h T: %s", 
                        ev.getPosition().getSpeedInKmh(), ev.getTime());
            case GEO_FENCE_ENTER:
            case GEO_FENCE_EXIT:
                return String.format("%s T: %s", 
                        ev.getGeoFence().getName(), ev.getTime());
        }
        return "";
    }

    static class Stats {
        int offline;
        Map<GeoFence, Integer> geoFenceEnter = new HashMap<>();
        Map<GeoFence, Integer> geoFenceExit = new HashMap<>();
        Map<Maintenance, Integer> maintenances = new HashMap<>();

        void update(DeviceEvent event) {
            switch (event.getType()) {
                case GEO_FENCE_ENTER:
                    update(geoFenceEnter, event.getGeoFence());
                    break;
                case GEO_FENCE_EXIT:
                    update(geoFenceExit, event.getGeoFence());
                    break;
                case OFFLINE:
                    offline++;
                    break;
                case MAINTENANCE_REQUIRED:
                    update(maintenances, event.getMaintenance());
                    break;
            }
        }

        <T> void update(Map<T, Integer> map, T entity) {
            if (entity != null) {
                Integer current = map.get(entity);
                map.put(entity, current == null ? 1 : (current + 1));
            }
        }
    }

    void drawTable(List<GeoFence> geoFences, List<DeviceEvent> events) {
        tableStart("table", hover().condensed());

        // header
        tableHeadStart();
        tableRowStart();

        for (String header : new String[] {"time", "event", "eventPosition"}) {
            tableHeadCellStart();
            text(message(header));
            tableHeadCellEnd();
        }

        tableHeadEnd();

        Stats stats = new Stats();
        // body
        tableBodyStart();

        for (DeviceEvent event : events) {
            if (event.getGeoFence() != null && !geoFences.contains(event.getGeoFence())) {
                continue;
            }

            tableRowStart();
            tableCell(formatDate(event.getTime()));
            String eventText = message("deviceEventType[" + event.getType() + "]");
            if (event.getGeoFence() != null) {
                eventText += " (" + event.getGeoFence().getName() + ")";
            }
            if (event.getMaintenance() != null) {
                eventText += " (" + event.getMaintenance().getName() + ")";
            }
            tableCell(eventText);
            tableCellStart();
            mapLink(event.getPosition().getLatitude(), event.getPosition().getLongitude());
            tableCellEnd();
            extentCell(event.getPosition(), event.getPosition());
            tableRowEnd();

            stats.update(event);
        }

        tableBodyEnd();
        tableEnd();

        // summary
        tableStart();
        tableBodyStart();

        if (stats.offline > 0) {
            dataRow(message("totalOffline"), Integer.toString(stats.offline));
        }
        for (GeoFence geoFence : geoFences) {
            Integer enterCount = stats.geoFenceEnter.get(geoFence);
            if (enterCount != null) {
                dataRow(message("totalGeoFenceEnters") + " (" + geoFence.getName() + ")", enterCount.toString());
            }
        }
        for (GeoFence geoFence : geoFences) {
            Integer enterCount = stats.geoFenceExit.get(geoFence);
            if (enterCount != null) {
                dataRow(message("totalGeoFenceExits") + " (" + geoFence.getName() + ")", enterCount.toString());
            }
        }
        if (!stats.maintenances.isEmpty()) {
            for (Map.Entry<Maintenance, Integer> entry : stats.maintenances.entrySet()) {
                dataRow(message("totalMaintenanceRequired") + " (" + entry.getKey().getName() + ")", entry.getValue().toString());
            }
        }

        tableBodyEnd();
        tableEnd();
    }
}
