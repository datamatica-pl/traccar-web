/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.web.client.model;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import org.traccar.web.shared.model.MaintenanceBase;

public interface IMaintenanceProperties<T extends MaintenanceBase> extends PropertyAccess<T> {
    
    ModelKeyProvider<T> indexNo();
    
    ValueProvider<T, String> name();
}
