/*
 * Copyright 2016 Datamatica
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
import java.util.Date;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import pl.datamatica.traccar.model.Device;

/**
 *
 * @author Jan Usarek
 */
public class DeviceTest {
    
    private Device device;
    private final Date from = new Date(1475128800000L); // Thu Sep 29 08:00:00 2016 GMT+2
    
    @Before
    public void initialize() {
        device = new Device();
        device.setName("Test device");
    }

    @Test
    public void testSubscriptionLeftEqualDates() throws Exception {
        device.setValidTo(from);
        
        assertEquals(1, device.getSubscriptionDaysLeft(from));
    }
    
    @Test
    public void testSubscriptionLeftEqualDatesDifferentHours() throws Exception {
        Date validTo = new Date(1475186399000L); // Thu Sep 29 23:59:59 2016 GMT+2
        device.setValidTo(validTo);
        
        assertEquals(1, device.getSubscriptionDaysLeft(from));
    }
    
    @Test
    public void testSubscriptionLeftEqualDatesDifferentHours2() throws Exception {
        Date validTo = new Date(1475100000000L); // Thu Sep 29 00:00:00 2016 GMT+2
        
        device.setValidTo(validTo);
        
        assertEquals(1, device.getSubscriptionDaysLeft(from));
    }
    
    @Test
    public void testSubscriptionLeftPastDate() throws Exception {
        Date validTo = new Date(1474839000000L); //Sun Sep 25 23:30:00 2016 GMT+2
        device.setValidTo(validTo);
        
        assertEquals(0, device.getSubscriptionDaysLeft(from));
    }
    
    @Test
    public void testSubscriptionLeftDayAfter() throws Exception {
        Date validTo = new Date(1475271000000L); // Fri Sep 30 23:30:00 2016 GMT+2
        device.setValidTo(validTo);
        
        assertEquals(2, device.getSubscriptionDaysLeft(from));
    }
    
    @Test
    public void testSubscriptionLeftAfterEndDST30Oct0030() throws Exception {
        Date validTo = new Date(1477783800000L); // Sun Oct 30 00:30:00 2016 GMT+1 (no DST)
        device.setValidTo(validTo);
        
        assertEquals(32, device.getSubscriptionDaysLeft(from));
    }
    
    @Test
    public void testSubscriptionLeftAfterEndDST30Oct0130() throws Exception {
        Date validTo = new Date(1477787400000L); // Sun Oct 30 01:30:00 2016 GMT+1 (no DST)
        device.setValidTo(validTo);
        
        assertEquals(32, device.getSubscriptionDaysLeft(from));
    }
    
    @Test
    public void testSubscriptionLeftAfterEndDST30Oct0200() throws Exception {
        Date validTo = new Date(1477789200000L); // Sun Oct 30 02:00:00 2016 GMT+1 (no DST)
        device.setValidTo(validTo);
        
        assertEquals(32, device.getSubscriptionDaysLeft(from));
    }
    
    @Test
    public void testSubscriptionLeftAfterEndDST30Oct0230() throws Exception {
        Date validTo = new Date(1477791000000L); // Sun Oct 30 02:30:00 2016 GMT+1 (no DST)
        device.setValidTo(validTo);
        
        assertEquals(32, device.getSubscriptionDaysLeft(from));
    }
    
    @Test
    public void testSubscriptionLeftAfterEndDST30Oct0300() throws Exception {
        Date validTo = new Date(1477792800000L); // Sun Oct 30 03:00:00 2016 GMT+1 (no DST)
        device.setValidTo(validTo);
        
        assertEquals(32, device.getSubscriptionDaysLeft(from));
    }
}
