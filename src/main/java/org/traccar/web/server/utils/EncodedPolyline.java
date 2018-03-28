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
package org.traccar.web.server.utils;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.ArrayList;
import java.util.List;
import pl.datamatica.traccar.model.Position;

public class EncodedPolyline {
    
    private static StringBuilder encodeSignedNumber(int num) {
        int sgn_num = num << 1;
        if (num < 0) {
            sgn_num = ~(sgn_num);
        }
        return encodeNumber(sgn_num);
    }

    private static StringBuilder encodeNumber(int num) {
        StringBuilder encodeString = new StringBuilder();
        while (num >= 0x20) {
            int nextValue = (0x20 | (num & 0x1f)) + 63;
            if (nextValue == 92) {
                encodeString.append((char)(nextValue));
            }
            encodeString.append((char)(nextValue));
            num >>= 5;
        }

        num += 63;
        if (num == 92) {
            encodeString.append((char)(num));
        }

        encodeString.append((char)(num));

        return encodeString;
    }

    /**
     * Encode a polyline with Google polyline encoding method
     * @param polyline the polyline
     * @return the encoded polyline, as a String
     */
    public static String encode(Coordinate[] polyline) {
        StringBuilder encodedPoints = new StringBuilder();
        int prev_lat = 0, prev_lng = 0;
        for (Coordinate trackpoint : polyline) {
            int lat = (int) Math.round(trackpoint.y * 1e5);
            int lng = (int) Math.round(trackpoint.x * 1e5);
            encodedPoints.append(encodeSignedNumber(lat - prev_lat));
            encodedPoints.append(encodeSignedNumber(lng - prev_lng));
            prev_lat = lat;
            prev_lng = lng;
        }
        return encodedPoints.toString();
    }
    
    public static Coordinate[] decode(String encoded) {
        PolylineParser parser = new PolylineParser(encoded);
        List<Coordinate> coords = new ArrayList<>();
        double lat = 0, lon = 0;
        while(parser.hasNext()) {
            int dLat = parser.parseNext();
            lat += dLat / 1.e5;
            int dLon = parser.parseNext();
            lon += dLon / 1.e5;
            coords.add(new Coordinate(lon, lat));
        }
        return coords.toArray(new Coordinate[0]);
    }
    
    private static class PolylineParser {
        private int i;
        private final String encoded;
        
        public PolylineParser(String encoded) {
            this.encoded = encoded;
        }
        
        public int parseNext() {
            int res =0, s =0;
            while((encoded.charAt(i) & 0x20) != 0) {
                res |= ((encoded.charAt(i) - 63) & 0x1F) << s;
                s+=5;
                ++i;
            }
            if((res & 1) != 0) {
                res = ~res;
            }
            res >>= 1;
            return res;
        }
        
        public boolean hasNext() {
            return encoded.length() - 1 > i;
        }
    }
}
