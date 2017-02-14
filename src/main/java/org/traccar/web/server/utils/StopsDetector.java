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
package org.traccar.web.server.utils;

import java.util.ArrayList;
import java.util.List;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Position;

public class StopsDetector {
    private List<Position> accepted = new ArrayList<>();
    private List<Position> waiting = new ArrayList<>();
    
    public List<Position> detectStops(List<Position> positions) {
        for(Position pos : positions)
            handle(pos);
        if(!waiting.isEmpty())
            flushWaiting(waiting.get(waiting.size()-1));
        List<Position> result = new ArrayList<>(accepted);
        accepted.clear();
        return result;
    }
    
    public void handle(Position current) {
        if(isIdle(current)) {
            waiting.add(current);
        } else {
            if(!waiting.isEmpty())
                flushWaiting(current);
            accepted.add(current);
        }
    }
    
    public void flushWaiting(Position current) {
        Position stopStart = waiting.get(0);
        int stopTime = getStopTime(stopStart, current);
        if(stopTime < stopStart.getDevice().getMinIdleTime())
            accepted.addAll(waiting);
        else {
            stopStart.increaseStopTime(stopTime);
            accepted.add(stopStart);
        }
        waiting.clear();
    }
    
    public int getStopTime(Position first, Position last) {
        return (int)((last.getTime().getTime() - first.getTime().getTime())/1000);
    }
    
    public boolean isWaitingIdle() {
        Position first = waiting.get(0);
        Position last = waiting.get(waiting.size()-1);
        Device device = first.getDevice();
        long dt = last.getTime().getTime() - first.getTime().getTime();
        return dt >= device.getMinIdleTime()*1000;
    }
    
    public boolean isIdle(Position p) {
        return p.getSpeed() == null || p.getSpeed() <= p.getDevice().getIdleSpeedThreshold();
    }
}
