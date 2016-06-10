package org.traccar.web.client.view;

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.traccar.web.shared.model.CommandType;

public class CommandArgumentsBinder {    
    protected static final Map<CommandType, String[]> visibilityInfo = new HashMap<>();
    private final Map<String, Widget> widgetMap = new HashMap<>();
    
    public static CommandArgumentsBinder getInstance(String protocol, CommandDialog commandDialog) {
        if(protocol.equals("h02"))
            return new H02CommandArgumentsBinder(commandDialog);
        return new CommandArgumentsBinder(commandDialog);
    }
    
    public CommandArgumentsBinder(CommandDialog commandDialog) {    
        initWidgetMap(commandDialog);
        initVisibilityInfo(); 
    }
    
    private void initWidgetMap(CommandDialog commandDialog) {
        widgetMap.put("lblFrequency", commandDialog.lblFrequency);
        widgetMap.put("frequency", commandDialog.frequency);
        widgetMap.put("frequencyUnit", commandDialog.frequencyUnit);
        
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
        visibilityInfo.put(CommandType.custom,
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

