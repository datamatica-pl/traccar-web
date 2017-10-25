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

import org.traccar.web.shared.model.AccessDeniedException;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.Report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.traccar.web.server.reports.MapBuilder.MarkerStyle;
import pl.datamatica.traccar.model.DeviceEventType;

public class ReportDS extends ReportGenerator {
    @Override
    void generateImpl(Report report) throws IOException {
        h2(report.getName());

        for (Device device : getDevices(report)) {
            List<Position> positions;
            try {
                positions = dataService.getPositions(device, report.getFromDate(), report.getToDate(), !report.isDisableFilter());
            } catch (AccessDeniedException ade) {
                continue;
            }

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
            if (!positions.isEmpty()) {
                List<Data> datas = calculate(positions);
                drawTable(datas);
                if(!datas.isEmpty() && report.isIncludeMap())
                    drawMap(datas, positions);
            } else {
                drawSummary(0d, 0, 0, 0d, 0d);
            }

            panelBodyEnd();

            panelEnd();
        }
    }

    private void drawMap(List<Data> datas, List<Position> positions) {
        MapBuilder builder = getMapBuilder();
        
        builder.polyline(positions, "#00f", 2);
        for(Position p : positions)
            builder.marker(p, MarkerStyle.arrow(p.getCourse()));
        
        for(Data data : datas) {
            if(data.idle) {
                String label = String.format("D: %d m", data.getDuration()/60000);
                builder.marker(data.start, MarkerStyle.event(DeviceEventType.STOPPED, label));
            }
        }
        
        Position latest = positions.get(positions.size()-1);
        builder.marker(latest, MarkerStyle.deviceMarker(latest));
        html(builder.bindWithTable("table", 2).create());
    }

    static class Data {
        final boolean idle;

        Position start;
        Position end;
        double topSpeed;
        double totalSpeed;
        double distance;
        int positionsWithSpeed;

        Data(boolean idle, Position start) {
            this.start = start;
            this.idle = idle;
        }

        long getDuration() {
            return end.getTime().getTime() - start.getTime().getTime();
        }

        double getAverageSpeed() {
            return totalSpeed / positionsWithSpeed;
        }
    }

    List<Data> calculate(List<Position> positions) {
        Position prevPosition = null;
        List<Data> datas = new ArrayList<>();
        Data currentData = null;

        for (Iterator<Position> it = positions.iterator(); it.hasNext(); ) {
            Position position = it.next();

            if (currentData == null) {
                currentData = new Data(isIdle(position), position);
            }

            if (prevPosition != null && isIdle(position) != isIdle(prevPosition)) {
                currentData.end = position;
                datas.add(currentData);
                currentData = new Data(isIdle(position), position);
            }

            if (position.getSpeed() != null && !isIdle(position)) {
                currentData.topSpeed = Math.max(currentData.topSpeed, position.getSpeed());
                currentData.totalSpeed += position.getSpeed();
                currentData.positionsWithSpeed++;
            }
            currentData.distance += position.getDistance();

            prevPosition = position;
        }
        currentData.end = positions.get(positions.size() - 1);
        datas.add(currentData);

        // filter 'idle' data, which duration is less than the setting from device profile
        Device device = positions.get(0).getDevice();
        Data prevData = null;
        for (Iterator<Data> it = datas.iterator(); it.hasNext(); ) {
            Data data = it.next();
            long minIdleTime = (long) device.getMinIdleTime() * 1000;
            if (isIdle(data.start) && data.getDuration() < minIdleTime
                    && (prevData != null || it.hasNext())) {
                Data nonIdleData = prevData == null ? it.next() : prevData;
                if (prevData == null) {
                    nonIdleData.start = data.start;
                } else {
                    nonIdleData.end = data.end;
                }
                nonIdleData.distance += data.distance;
                nonIdleData.positionsWithSpeed += data.positionsWithSpeed;
                nonIdleData.topSpeed = Math.max(data.topSpeed, nonIdleData.topSpeed);
                nonIdleData.totalSpeed += data.totalSpeed;
                it.remove();
            }
            if (!data.idle) {
                prevData = data;
            }
        }
        // merge sequential 'moving' datas into one
        prevData = null;
        for (Iterator<Data> it = datas.iterator(); it.hasNext(); ) {
            Data data = it.next();
            if (prevData != null && !prevData.idle && !data.idle) {
                prevData.distance += data.distance;
                prevData.positionsWithSpeed += data.positionsWithSpeed;
                prevData.topSpeed = Math.max(data.topSpeed, prevData.topSpeed);
                prevData.totalSpeed += data.totalSpeed;
                prevData.end = data.end;
                it.remove();
                continue;
            }
            prevData = data;
        }
        return datas;
    }

    void drawTable(List<Data> datas) {
        tableStart("table", hover().condensed().height(DEFAULT_TABLE_HEIGHT));

        // header
        tableHeadStart();
        tableRowStart();

        for (String header : new String[] {"status", "start", "end", "duration"}) {
            tableHeadCellStart(rowspan(2));
            text(message(header));
            tableHeadCellEnd();
        }
        
        for (String header : new String[] {"distance", "topSpeed", "averageSpeed"}) {
            tableHeadCellStart();
            text(message(header));
            tableHeadCellEnd();
        }
        tableRowEnd();

        tableHeadEnd();

        // body
        tableBodyStart();

        long totalStopDuration = 0;
        long totalMoveDuration = 0;
        double totalDistance = 0;
        int totalMovingPositionCount = 0;
        double totalSpeed = 0;
        double totalTopSpeed = 0;

        for (Data data : datas) {
            tableRowStart();
            tableCell(message(data.idle ? "stopped" : "moving"));
            tableCell(formatDate(data.start.getTime()));
            tableCell(formatDate(data.end.getTime()));
            // update total counters
            if (data.idle) {
                totalStopDuration += data.getDuration();
            } else {
                totalMoveDuration += data.getDuration();
            }
            tableCell(formatDuration(data.getDuration()));

            if(!data.idle) {
                tableCell(formatDistance(data.distance));
                tableCell(formatSpeed(data.topSpeed));
                tableCell(formatSpeed(data.getAverageSpeed()));
                // update total counters
                totalMovingPositionCount += data.positionsWithSpeed;
                totalSpeed += data.totalSpeed;
                totalTopSpeed = Math.max(totalTopSpeed, data.topSpeed);
            }
            extentCell(data.start, data.end);
            totalDistance += data.distance;
            tableRowEnd();
        }

        tableBodyEnd();

        tableEnd();

        drawSummary(totalDistance,
                totalMoveDuration,
                totalStopDuration,
                totalTopSpeed,
                totalMovingPositionCount == 0 ? 0d : totalSpeed / totalMovingPositionCount);
    }

    private void drawSummary(double routeLength,
                             long moveDuration,
                             long stopDuration,
                             double topSpeed,
                             double averageSpeed) {
        tableStart();
        tableBodyStart();

        dataRow(message("routeLength"), formatDistance(routeLength));
        dataRow(message("moveDuration"), formatDuration(moveDuration));
        dataRow(message("stopDuration"), formatDuration(stopDuration));
        dataRow(message("topSpeed"), formatSpeed(topSpeed));
        dataRow(message("averageSpeed"), formatSpeed(averageSpeed));

        tableBodyEnd();
        tableEnd();
    }

    private boolean isIdle(Position position) {
        return position.getSpeed() == null || position.getSpeed() <= position.getDevice().getIdleSpeedThreshold();
    }
}
