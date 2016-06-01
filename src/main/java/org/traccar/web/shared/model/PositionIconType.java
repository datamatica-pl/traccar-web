/*
 * Copyright 2014 Vitaly Litvak (vitavaque@gmail.com)
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
package org.traccar.web.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum PositionIconType implements IsSerializable {
    iconLatest("marker-green.png", "marker.png", 21, 25),
    iconOffline("marker-green.png", "marker-white.png", 21, 25),
    iconArchive("marker-gold.png", "marker-blue.png", 21, 25),

    humanLatest("GTS_pointer_human.png", "GTS_pointer_human.png", 36, 48),
    humanOffline("GTS_pointer_human.png", "GTS_pointer_human.png", 36, 48),

    containerLatest("GTS_pointer_container.png", "GTS_pointer_container.png", 36, 48),
    containerOffline("GTS_pointer_container.png", "GTS_pointer_container.png", 36, 48),

    motoLatest("GTS_pointer_moto.png", "GTS_pointer_moto.png", 36, 48),
    motoOffline("GTS_pointer_moto.png", "GTS_pointer_moto.png", 36, 48),

    bicycleLatest("GTS_pointer_bike.png", "GTS_pointer_bike.png", 36, 48),
    bicycleOffline("GTS_pointer_bike.png", "GTS_pointer_bike.png", 36, 48),

    petLatest("GTS_pointer_pet.png", "GTS_pointer_pet.png", 36, 48),
    petOffline("GTS_pointer_pet.png", "GTS_pointer_pet.png", 36, 48),

    carTruckLatest("GTS_pointer_car.png", "GTS_pointer_car.png", 36, 48),
    carTruckOffline("GTS_pointer_car.png", "GTS_pointer_car.png", 36, 48),

    longTruckLatest("GTS_pointer_truck.png", "GTS_pointer_truck.png", 36, 48),
    longTruckOffline("GTS_pointer_truck.png", "GTS_pointer_truck.png", 36, 48),

    pickupLatest("GTS_pointer_pickup.png", "GTS_pointer_pickup.png", 36, 48),
    pickupOffline("GTS_pointer_pickup.png", "GTS_pointer_pickup.png", 36, 48),

    shipLatest("GTS_pointer_boat.png", "GTS_pointer_boat.png", 36, 48),
    shipOffline("GTS_pointer_boat.png", "GTS_pointer_boat.png", 36, 48),

    quadLatest("GTS_pointer_quad.png", "GTS_pointer_quad.png", 36, 48),
    quadOffline("GTS_pointer_quad.png", "GTS_pointer_quad.png", 36, 48),

    tracktorLatest("GTS_pointer_tractor.png", "GTS_pointer_tractor.png", 36, 48),
    tractorOffline("GTS_pointer_tractor.png", "GTS_pointer_tractor.png", 36, 48),

    dotArchive("dot-orange.png", "dot-orange.png", 13, 14);

    private final String selectedURL;
    private final String notSelectedURL;
    private final int width;
    private final int height;

    PositionIconType(String selectedURL, String notSelectedURL, int width, int height) {
        this.selectedURL = "img/" + selectedURL;
        this.notSelectedURL = "img/" + notSelectedURL;
        this.width = width;
        this.height = height;
    }

    public String getURL(boolean selected) {
        return selected ? selectedURL : notSelectedURL;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
