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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import java.text.DateFormat;
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
    public Long routeId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ",
                timezone="GMT")
    public Date fromDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ",
                timezone="GMT")
    public Date toDate;
    public boolean includeMap;
    public boolean disableFilter;
    
    public String format;
    public boolean preview;
    
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
        if(report.getDevices() != null)
            for(Device d : report.getDevices())
                if(d != null)
                    this.deviceIds.add(d.getId());
        this.geofenceIds = new ArrayList<>();
        if(report.getGeoFences() != null)
            for(GeoFence gf : report.getGeoFences())
                if(gf != null)
                    this.geofenceIds.add(gf.getId());
        if(report.getRoute() != null)
            this.routeId = report.getRoute().getId();
        
        if(report.getFormat() != null)
            this.format = report.getFormat().name();
        this.preview = report.isPreview();
    }
    
    public ApiReport(JSONObject val) {
        DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        
        id = (long)val.get("id").isNumber().doubleValue();
        name = val.get("name").isString().stringValue();
        reportType = val.get("reportType").isString().stringValue();
        deviceIds = new ArrayList<Long>();
        JSONArray dids = val.get("deviceIds").isArray();
        for(int i=0;i<dids.size();++i)
            deviceIds.add((long)dids.get(i).isNumber().doubleValue());
        geofenceIds = new ArrayList<Long>();
        JSONArray gids = val.get("geofenceIds").isArray();
        for(int i=0;i<gids.size();++i) {
            if(gids.get(i).isNumber() != null)
                geofenceIds.add((long)gids.get(i).isNumber().doubleValue());
        }
        fromDate = dtf.parse(val.get("fromDate").isString().stringValue());
        toDate = dtf.parse(val.get("toDate").isString().stringValue());
        includeMap = val.get("includeMap").isBoolean().booleanValue();
        disableFilter = val.get("disableFilter").isBoolean().booleanValue();
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
            if(devices.containsKey(did))
                report.getDevices().add(devices.get(did));
        report.setGeoFences(new HashSet<GeoFence>());
        for(long gid : geofenceIds)
            if(geofences.containsKey(gid))
                report.getGeoFences().add(geofences.get(gid));
        return report;
    }
}
