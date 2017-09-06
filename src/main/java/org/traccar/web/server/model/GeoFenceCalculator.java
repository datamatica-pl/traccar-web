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
package org.traccar.web.server.model;

public class GeoFenceCalculator {
    private static final double radKoef = Math.PI / 180;
    private static final double earthRadius = 6371.01; // Radius of the earth in km

    static double getDistance(double lonX, double latX, double lonY, double latY) {
        double dLat = (latX - latY) * radKoef;
        double dLon = (lonX - lonY) * radKoef;
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(latX * radKoef) * Math.cos(latY * radKoef) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c; // Distance in km
    }
}
