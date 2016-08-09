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
package org.traccar.web.client.view;

import pl.datamatica.traccar.model.Sensor;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.PositionIcon;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Device;
import com.google.gwt.core.client.GWT;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.tips.ToolTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;
import org.gwtopenmaps.openlayers.client.Pixel;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.shared.model.*;

import java.util.*;
import org.traccar.web.client.utils.JsonXmlParser;

public class PositionInfoPopup {
    private final static Messages i18n = GWT.create(Messages.class);

    final ToolTip toolTip;
    final ListStore<Device> deviceStore;
    final Set<String> alwaysVisibleOthers;

    public PositionInfoPopup(ListStore<Device> deviceStore) {
        this.deviceStore = deviceStore;
        this.toolTip = new ToolTip(new ToolTipConfig());
        
        this.alwaysVisibleOthers = new HashSet<>();
        Collections.addAll(alwaysVisibleOthers, "ignition", "battery", "power");
    }

    public void show(int x, int y, final Position position) {
        long current = System.currentTimeMillis();

        String body = "<table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">" +
                (position.getDevice().getDescription() != null && !position.getDevice().getDescription().trim().isEmpty() ? "<tr><td style=\"border-width: 1px 0px 0px 0px; border-style: solid; border-color: #000000; padding: 3px 0px 3px 0px;\" width=\"100%\" colspan=\"2\">" + position.getDevice().getDescription() + "</td></tr>" : "") +
                "<tr><td style=\"border-width: 1px 0px 1px 0px; border-style: solid; border-color: #000000; padding: 3px 0px 3px 0px;\" width=\"100%\" colspan=\"2\">" + i18n.ago(formatDateTimeDiff(current - position.getTime().getTime())) + "<br>(" + ApplicationContext.getInstance().getFormatterUtil().getTimeFormat().format(position.getTime()) + ")</td></tr>" +
                (position.getIdleSince() == null ? "" : ("<tr><td style=\"font-size: 11pt; border-width: 0px 1px 1px 0px; border-style: solid; border-color: #000000; padding: 3px 10px 3px 0px;\" valign=\"center\">" + i18n.idle() + "</td><td style=\"border-width: 0px 0px 1px 0px; border-style: solid; border-color: #000000; padding: 3px 10px 3px 10px;\" colspan=\"2\">" + formatDateTimeDiff(current - position.getIdleSince().getTime()) + "<br>(" + i18n.since(ApplicationContext.getInstance().getFormatterUtil().getTimeFormat().format(position.getIdleSince())) + ")</td></tr>")) +
                (position.getAddress() == null || position.getAddress().isEmpty() ? "" : ("<tr><td style=\"border-width: 0px 0px 1px 0px; border-style: solid; border-color: #000000; padding: 3px 0px 3px 0px;\" colspan=\"2\">" + position.getAddress() + "</td></tr>")) +
                "<tr>" +
                (position.getSpeed() == null ? "" : ("<td style=\"font-size: 12pt; border-width: 0px 1px 1px 0px; border-style: solid; border-color: #000000; padding: 3px 10px 3px 0px;\" valign=\"bottom\">v: " + ApplicationContext.getInstance().getFormatterUtil().getSpeedFormat().format(position.getSpeed()) + "</td>")) +
                (position.getAltitude() == null ? "" : ("<td style=\"font-size: 10pt; border-bottom: 1px solid #000000; padding: 3px 10px 3px 10px;\" valign=\"bottom\"" + (position.getSpeed() == null ? " colspan=\"2\" align=\"right\"" : "") + ">h: " + position.getAltitude() + " " + i18n.meter() + "</td>")) +
                "</tr>";

        boolean admin = ApplicationContext.getInstance().getUser().getAdmin();
        boolean manager = ApplicationContext.getInstance().getUser().getManager(); 
        Device device = deviceStore.findModelWithKey(Long.toString(position.getDevice().getId()));

        if(manager || admin) {
            if (position.getDevice().getOdometer() > 0 && device.isShowOdometer()) {
                body += "<tr><td style=\"padding: 3px 0px 3px 0px;\">" + i18n.odometer() + "</td><td>" + ApplicationContext.getInstance().getFormatterUtil().getDistanceFormat().format(position.getDevice().getOdometer()) + "</td></tr>";
            }
            if (position.getProtocol() != null && device.isShowProtocol()) {
                body += "<tr><td style=\"padding: 3px 0px 3px 0px;\">" + i18n.protocol() + "</td><td>" + position.getProtocol() + "</td></tr>";
            }
        }
        String other = position.getOther();
        if (other != null) {
            Map<String, Sensor> sensors = new HashMap<>(device.getSensors().size());
            for (Sensor sensor : device.getSensors()) {
                sensors.put(sensor.getParameterName(), sensor);
            }

            Map<String, Object> sensorData = JsonXmlParser.parse(other);

            if(!admin && !manager) {
                sensorData.keySet().retainAll(alwaysVisibleOthers);
            }
            if(!admin && sensorData.containsKey("ip"))
                sensorData.remove("ip");
            if (!device.isShowProtocol() && sensorData.containsKey("protocol")) {
                sensorData.remove("protocol");
            }
            // Alarm is not synchronized properly, remove it from device pop-up temporary
            if (sensorData.containsKey("alarm")) {
                sensorData.remove("alarm");
            }

            // write values
            for (Map.Entry<String, Object> entry : sensorData.entrySet()) {
                String parameterName = entry.getKey();
                Object value = entry.getValue();
                String valueText = value.toString();
                Sensor sensor = sensors.get(parameterName);
                if (sensor != null) {
                    if (!sensor.isVisible()) {
                        continue;
                    }
                    parameterName = sensor.getName();
                    if (value instanceof Number || value.toString().matches("^[-+]?\\d+(\\.\\d+)?$")) {
                        double doubleValue;
                        if (value instanceof Number) {
                            doubleValue = ((Number) value).doubleValue();
                        } else {
                            doubleValue = Double.parseDouble(valueText);
                        }
                        List<SensorInterval> intervals = SensorsEditor.intervals(sensor);
                        if (!intervals.isEmpty()) {
                            valueText = intervalText(doubleValue, intervals);
                        }
                    }
                } else if (parameterName.equals("protocol")) {
                    parameterName = i18n.protocol();
                }
                if (!valueText.isEmpty()) {
                    body += "<tr><td style=\"padding: 3px 0px 3px 0px;\">" + parameterName + "</td><td>" + valueText + "</td></tr>";
                }
            }
        }
        
    if (position.getGeoFences() != null && !position.getGeoFences().isEmpty()) {
            body += "<tr><td style=\"border-width: 1px 0px 0px 0px; border-style: solid; border-color: #000000; padding: 3px 10px 3px 0px;\" colspan=\"2\">";
            for (GeoFence geoFence : position.getGeoFences()) {
                body += "<p><div style=\"" +
                        "    width: 10px;\n" +
                        "    height: 10px;\n" +
                        "    display: inline-block;\n" +
                        "    background-color: #" + geoFence.getColor() + ";\n" +
                        "    border: 1px solid;\n" +
                        "    left: 5px;\n" +
                        "    top: 5px;\"></div> " + geoFence.getName() + "</p>";
            }
            body += "</td></tr>";
        }

        body += "</table>";

        ToolTipConfig config = new ToolTipConfig();

        PositionIcon icon = position.getIcon() == null ? MarkerIcon.create(position) : position.getIcon();
        String deviceTitle = position.getDevice().getName() + (position.getStatus() == Position.Status.OFFLINE ? " (" + i18n.offline() + ")" : "");

        config.setTitleHtml(
                "<table height=\"100%\"><tr>" +
                "<td>" +"<img src=\"" + icon.getURL() + "\">&nbsp;</td>" +
                "<td valign=\"center\">" + deviceTitle + "</td>" +
                "<td valign=\"center\" align=\"right\"><img src=\"img/arrow" + (int)((position.getCourse()+45/2)%360)/45 + ".png\"/></td>" +
                "</tr></table>");

        config.setBodyHtml(body);
        config.setAutoHide(false);
        config.setDismissDelay(0);

        toolTip.update(config);
        toolTip.showAt(x + 15, y + 15);
    }

    private String formatDateTimeDiff(long diff) {
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        return diffDays > 0 ? diffDays + i18n.day() + " " + diffHours + i18n.hour() :
                diffHours > 0 ? diffHours + i18n.hour() + " " + diffMinutes + i18n.minute() :
                        diffMinutes > 0 ? diffMinutes + i18n.minute() + " " + diffSeconds + i18n.second() :
                                diffSeconds + i18n.second();
    }

    public void show(final MapView mapView, final Position position) {
        Pixel pixel = mapView.getMap().getPixelFromLonLat(mapView.createLonLat(position.getLongitude(), position.getLatitude()));
        show(mapView.getView().getAbsoluteLeft() + pixel.x(), mapView.getView().getAbsoluteTop() + pixel.y(), position);
    }

    public void hide() {
        ToolTipConfig config = toolTip.getToolTipConfig();
        config.setAutoHide(true);
        config.setDismissDelay(10);
        toolTip.update(config);
    }

    public static String intervalText(double value, List<SensorInterval> intervals) {
        String valueText = null;
        for (SensorInterval interval : intervals) {
            if (valueText == null) {
                valueText = interval.getText();
            }
            if (value < interval.getValue()) {
                break;
            }
            valueText = interval.getText();
        }
        return valueText;
    }
}
