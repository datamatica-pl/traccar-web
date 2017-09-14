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
package org.traccar.web.client.model.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.ReportType;

/**
 *
 * @author ŁŁ
 */
public class ApiReport {
    public long id;
    public String name;
    public String reportType;
    public List<Long> deviceIds;
    public List<Long> geofenceIds;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ",
                timezone="GMT")
    public Date fromDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ",
                timezone="GMT")
    public Date toDate;
    public boolean includeMap;
    public boolean disableFilter;
    
    public ApiReport() {
    }
    
    public ApiReport(Report report) {
        this.id = report.getId();
        this.name = report.getName();
        this.reportType = report.getType().name();
        this.fromDate = report.getFromDate();
        this.toDate = report.getToDate();
        this.includeMap = report.isIncludeMap();
        this.disableFilter = report.isDisableFilter();
        this.deviceIds = new ArrayList<>();
        for(Device d : report.getDevices())
            this.deviceIds.add(d.getId());
        this.geofenceIds = new ArrayList<>();
        for(GeoFence gf : report.getGeoFences())
            this.geofenceIds.add(gf.getId());
    }
    
    public Report toReport(Map<Long, Device> devices, Map<Long, GeoFence> geofences) {
        Report report = new Report(id);
        report.setName(name);
        report.setType(ReportType.valueOf(reportType));
        report.setFromDate(fromDate);
        report.setToDate(toDate);
        report.setIncludeMap(includeMap);
        report.setDisableFilter(disableFilter);
        report.setDevices(new HashSet<Device>());
        for(long did : deviceIds)
            report.getDevices().add(devices.get(did));
        report.setGeoFences(new HashSet<GeoFence>());
        for(long gid : geofenceIds) 
            report.getGeoFences().add(geofences.get(gid));
        return report;
    }
}
