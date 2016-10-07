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
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private Date from;

    @Before
    public void initialize() throws ParseException {
        device = new Device();
        device.setName("Test device");
        from = dateTimeFormat.parse("2016-09-29 08:00:00");
    }

    @Test
    public void testSubscriptionLeftEqualDates() throws Exception {
        device.setValidTo(from);

        assertEquals(1, device.getSubscriptionDaysLeft(from));
    }

    @Test
    public void testSubscriptionLeftEqualDatesDifferentHours() throws Exception {
        device.setValidTo(dateFormat.parse("2016-09-29"));

        assertEquals(1, device.getSubscriptionDaysLeft(from));
    }

    @Test
    public void testSubscriptionLeftEqualDatesDifferentHours2() throws Exception {
        device.setValidTo(dateFormat.parse("2016-09-29"));

        assertEquals(1, device.getSubscriptionDaysLeft(dateTimeFormat.parse("2016-09-29 00:00:00")));
    }

    @Test
    public void testSubscriptionLeftEqualDatesDifferentHours3() throws Exception {
        device.setValidTo(dateFormat.parse("2016-09-29"));

        assertEquals(1, device.getSubscriptionDaysLeft(dateTimeFormat.parse("2016-09-29 23:59:59")));
    }

    @Test
    public void testSubscriptionLeftPastDate() throws Exception {
        device.setValidTo(dateFormat.parse("2016-09-25"));

        assertEquals(0, device.getSubscriptionDaysLeft(from));
    }

    @Test
    public void testSubscriptionLeftDayAfter() throws Exception {
        device.setValidTo(dateFormat.parse("2016-09-30"));

        assertEquals(2, device.getSubscriptionDaysLeft(from));
    }

    @Test
    public void testSubscriptionLeftAfterEndDST() throws Exception {
        device.setValidTo(dateFormat.parse("2016-10-30"));

        assertEquals(32, device.getSubscriptionDaysLeft(from));
    }

    @Test
    public void testSubscriptionLeftIfVAlidToNull() throws Exception {
        device.setValidTo(null);

        assertEquals(0, device.getSubscriptionDaysLeft(from));
    }

    @Test
    public void testCloseToExpireTrue() throws Exception {
        device.setValidTo(dateFormat.parse("2016-10-05"));

        assertTrue(device.isCloseToExpire(from));
    }

    @Test
    public void testCloseToExpireFalse() throws Exception {
        device.setValidTo(dateFormat.parse("2016-10-06"));

        assertFalse(device.isCloseToExpire(from));
    }

    @Test
    public void testCloseToExpireSameDay() throws Exception {
        device.setValidTo(dateFormat.parse("2016-09-29"));

        assertTrue(device.isCloseToExpire(from));
    }

    @Test
    public void testCloseToExpireDeviceExpired() throws Exception {
        device.setValidTo(dateFormat.parse("2016-09-28"));

        assertFalse(device.isCloseToExpire(from));
    }

    @Test
    public void testLastAvailableDateDeviceExpired() throws ParseException {
        Date validTo = dateTimeFormat.parse("2016-09-28 23:45:00");
        device.setValidTo(validTo);
        Date expectedLastAvailableDate = dateTimeFormat.parse("2016-09-27 00:00:00");

        assertEquals(expectedLastAvailableDate, device.getLastAvailablePositionDate(from));
    }

    @Test
    public void testLastAvailableDateDeviceValid5DaysHistory() throws ParseException {
        Date validTo = dateTimeFormat.parse("2016-09-29 00:00:01");
        device.setValidTo(validTo);
        device.setHistoryLength(5);
        Date expectedLastAvailableDate = dateTimeFormat.parse("2016-09-24 00:00:00");

        assertEquals(expectedLastAvailableDate, device.getLastAvailablePositionDate(from));
    }

    @Test
    public void testLastAvailableDateDeviceValid60DaysHistoryAfterDST() throws ParseException {
        Date validTo = dateTimeFormat.parse("2016-12-02 00:00:00");
        device.setValidTo(validTo);
        device.setHistoryLength(60);
        Date fromDate = dateTimeFormat.parse("2016-12-01 00:15:00");
        Date expectedLastAvailableDate = dateTimeFormat.parse("2016-10-02 00:00:00");

        assertEquals(expectedLastAvailableDate, device.getLastAvailablePositionDate(fromDate));
    }

    @Test
    public void testIsValidTrue() throws ParseException {
        device.setValidTo(dateFormat.parse("2016-10-05"));
        assertTrue(device.isValid(from));
    }

    @Test
    public void testIsValidTrueSameDayLateEvening() throws ParseException {
        device.setValidTo(dateFormat.parse("2016-10-30"));
        assertTrue(device.isValid(dateTimeFormat.parse("2016-10-30 23:59:59")));
    }

    @Test
    public void testIsValidTrueSameDayMidnight() throws ParseException {
        device.setValidTo(dateFormat.parse("2016-10-30"));
        assertTrue(device.isValid(dateTimeFormat.parse("2016-10-30 00:00:00")));
    }

    @Test
    public void testIsValidFalse() throws ParseException {
        device.setValidTo(dateFormat.parse("2016-10-30"));
        assertFalse(device.isValid(dateTimeFormat.parse("2016-10-31 00:00:00")));
    }

}
