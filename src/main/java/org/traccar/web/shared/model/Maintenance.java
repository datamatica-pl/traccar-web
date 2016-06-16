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
package org.traccar.web.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import javax.persistence.*;

@Entity
@Table(name = "maintenances",
       indexes = { @Index(name = "maintenances_pkey", columnList = "id") })
public class Maintenance extends MaintenanceBase implements IsSerializable  {

    public Maintenance() {}

    public Maintenance(Maintenance maintenance) {
        copyFrom(maintenance);
    }

    public Maintenance(String name) {
        this.name = name;
    }

    // how often to perform service - value in kilometers
    private double serviceInterval;

    public double getServiceInterval() {
        return serviceInterval;
    }

    public void setServiceInterval(double serviceInterval) {
        this.serviceInterval = serviceInterval;
    }

    // odometer value when service was last performed
    private double lastService;

    public double getLastService() {
        return lastService;
    }

    public void setLastService(double lastService) {
        this.lastService = lastService;
    }
    
    public void copyFrom(Maintenance other) {
        super.copyFrom(other);
        this.serviceInterval = other.serviceInterval;
        this.lastService = other.lastService;
    }
}
