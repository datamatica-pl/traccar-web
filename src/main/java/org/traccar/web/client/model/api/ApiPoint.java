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

import pl.datamatica.traccar.model.GeoFence.LonLat;

/**
 *
 * @author ŁŁ
 */
public class ApiPoint {
    public double latitude;
    public double longitude;

    public ApiPoint() {}

    public LonLat toLonLat() {
        return new LonLat(longitude, latitude);
    }
    
    public static ApiPoint parsePoint(String pt) {
        String[] lonLat = pt.split(" ");
        if(lonLat.length != 2)
            throw new RuntimeException("Invalid string");
        double longitude = Double.parseDouble(lonLat[0]);
        double latitude = Double.parseDouble(lonLat[1]);
        ApiPoint point = new ApiPoint();
        point.latitude = latitude;
        point.longitude = longitude;
        return point;
    }
}
