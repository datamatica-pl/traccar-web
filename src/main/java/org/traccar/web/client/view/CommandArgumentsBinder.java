/*
 * Copyright 2016 Datamatica (dev@datamatica.pl)
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

import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.Map;
import pl.datamatica.traccar.model.CommandType;

public class CommandArgumentsBinder {    
    protected static final Map<CommandType, String[]> visibilityInfo = new HashMap<>();
    private final Map<String, Widget> widgetMap = new HashMap<>();
    
    public static CommandArgumentsBinder getInstance(String protocol, CommandDialog commandDialog) {
        if(protocol.equalsIgnoreCase("h02")) {
            return new H02CommandArgumentsBinder(commandDialog);
        } else if (protocol.equalsIgnoreCase("minifinder")) {
            return new MiniFinderCommandArgumentsBinder(commandDialog);
        } else if (protocol.equalsIgnoreCase("gt06")) {
            return new GT06CommandArgumentsBinder(commandDialog);
        } else {
            return new CommandArgumentsBinder(commandDialog);
        }
    }
    
    public CommandArgumentsBinder(CommandDialog commandDialog) {    
        initWidgetMap(commandDialog);
        initVisibilityInfo(); 
    }
    
    private void initWidgetMap(CommandDialog commandDialog) {
        widgetMap.put("lblFrequency", commandDialog.lblFrequency);
        widgetMap.put("frequency", commandDialog.frequency);
        widgetMap.put("frequencyUnit", commandDialog.frequencyUnit);
        
        widgetMap.put("lblFrequencyStop", commandDialog.lblFrequencyStop);
        widgetMap.put("frequencyStop", commandDialog.frequencyStop);
        widgetMap.put("frequencyUnitStop", commandDialog.frequencyUnitStop);
        
        widgetMap.put("lblTimeZone", commandDialog.lblTimeZone);
        widgetMap.put("timeZone", commandDialog.timeZone);
        
        widgetMap.put("lblRadius", commandDialog.lblRadius);
        widgetMap.put("radius", commandDialog.radius);
        
        widgetMap.put("lblCustomMessage", commandDialog.lblCustomMessage);
        widgetMap.put("customMessage", commandDialog.customMessage);
        
        widgetMap.put("lblPhoneNumber", commandDialog.lblPhoneNumber);
        widgetMap.put("phoneNumber", commandDialog.phoneNumber);
        
        widgetMap.put("lblMessage", commandDialog.lblMessage);
        widgetMap.put("message", commandDialog.message);
        
        widgetMap.put("lblDefenseTime", commandDialog.lblDefenseTime);
        widgetMap.put("defenseTime", commandDialog.defenseTime);
        
        widgetMap.put("lblSOSNumber1", commandDialog.lblSOSNumber1);
        widgetMap.put("lblSOSNumber2", commandDialog.lblSOSNumber2);
        widgetMap.put("lblSOSNumber3", commandDialog.lblSOSNumber3);
        widgetMap.put("SOSNumber1", commandDialog.SOSNumber1);
        widgetMap.put("SOSNumber2", commandDialog.SOSNumber2);
        widgetMap.put("SOSNumber3", commandDialog.SOSNumber3);
        
        widgetMap.put("lblSOSNumber", commandDialog.lblSOSNumber);
        widgetMap.put("SOSNumber", commandDialog.SOSNumber);
        
        widgetMap.put("lblCenterNumber", commandDialog.lblCenterNumber);
        widgetMap.put("centerNumber", commandDialog.centerNumber);
    }

    private void initVisibilityInfo() {
        visibilityInfo.put(CommandType.positionPeriodic,
                new String[]{"lblFrequency", "frequency", "frequencyUnit"});
        visibilityInfo.put(CommandType.positionStop,
                new String[]{"lblFrequency", "frequency", "frequencyUnit"});
        visibilityInfo.put(CommandType.custom,
                new String[]{"lblCustomMessage", "customMessage"});
        visibilityInfo.put(CommandType.extendedCustom,
                new String[]{"lblCustomMessage", "customMessage"});
        visibilityInfo.put(CommandType.setTimezone,
                new String[]{"lblTimeZone", "timeZone"});
        visibilityInfo.put(CommandType.movementAlarm,
                new String[]{"lblRadius", "radius"});
        visibilityInfo.put(CommandType.sendSms,
                new String[]{"lblPhoneNumber", "phoneNumber", "lblMessage", "message"});
        visibilityInfo.put(CommandType.setDefenseTime,
                new String[]{"lblDefenseTime", "defenseTime"});
        visibilityInfo.put(CommandType.setSOSNumbers,
                new String[]{"lblSOSNumber1", "lblSOSNumber2", "lblSOSNumber3", 
                    "SOSNumber1", "SOSNumber2", "SOSNumber3"});
        visibilityInfo.put(CommandType.setCenterNumber,
                new String[]{"lblCenterNumber", "centerNumber"});
        visibilityInfo.put(CommandType.deleteSOSNumber,
                new String[]{"lblSOSNumber", "SOSNumber"});
    }
    
    public void bind(CommandType type) {
        for(Widget widget : widgetMap.values())
            widget.setVisible(false);
        if(visibilityInfo.containsKey(type))
            for(String key : visibilityInfo.get(type))
                widgetMap.get(key).setVisible(true);
    }
}

class H02CommandArgumentsBinder extends CommandArgumentsBinder{
    
    public H02CommandArgumentsBinder(CommandDialog commandDialog) {
        super(commandDialog);
        visibilityInfo.remove(CommandType.setSOSNumbers);
        visibilityInfo.put(CommandType.setSOSNumbers, 
                new String[]{"lblSOSNumber1", "SOSNumber1", "lblSOSNumber2", "SOSNumber2"});
    }
}

class MiniFinderCommandArgumentsBinder extends CommandArgumentsBinder {
    
    public MiniFinderCommandArgumentsBinder(CommandDialog commandDialog) {
        super(commandDialog);
        visibilityInfo.remove(CommandType.setSOSNumbers);
        visibilityInfo.put(CommandType.setSOSNumbers, 
                new String[]{"lblSOSNumber1", "SOSNumber1"});
    }
}

class GT06CommandArgumentsBinder extends CommandArgumentsBinder {
    public GT06CommandArgumentsBinder(CommandDialog commandDialog) {
        super(commandDialog);
        visibilityInfo.remove(CommandType.positionPeriodic);
        visibilityInfo.put(CommandType.positionPeriodic,
                new String[]{"lblFrequency", "frequency", "frequencyUnit",
                            "lblFrequencyStop", "frequencyStop", "frequencyUnitStop"});
    }
}
