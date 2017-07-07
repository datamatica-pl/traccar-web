package org.traccar.web.client.controller;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import java.util.ArrayList;
import java.util.List;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.api.ApiDeviceIcon;
import org.traccar.web.client.model.api.Decoder;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Position;
import org.traccar.web.client.model.api.ResourcesService;

public class UpdatesController {
    public interface LatestPositionsListener {
        void onPositionsUpdated(List<Position> result);
    }
    public interface DevicesListener {
        void onDevicesUpdated(List<Device> devices);
    }
    
    private static final int MAX_UPDATE_FAILURE_COUNT = 3;
    private final Messages i18n = GWT.create(Messages.class);
    
    private Timer updateTimer;
    private int updateFailureCount;
    
    private List<LatestPositionsListener> latestPositionsListeners;
    private List<DevicesListener> devicesListeners;
    
    public UpdatesController() {
        latestPositionsListeners = new ArrayList<>();
        devicesListeners = new ArrayList<>();
    }
    
    public void update() {
        updateTimer.cancel();
        Application.getDevicesService().getDevices(new JsonCallback() {
            @Override
            public void onFailure(Method method, Throwable caught) {
                onUpdateFailed(caught);
            }
            
            @Override
            public void onSuccess(Method method, JSONValue response) {
                Decoder dec = Application.getDecoder();
                List<Device> dev = dec.decodeDevices(response.isObject());
                devicesLoaded(dev);
            }
        });
    }
    
    public void devicesLoaded(List<Device> dev) {
        List<Position> pos = new ArrayList<>();
        for(Device d : dev) {
            if(d.getLatestPosition() != null)
                pos.add(d.getLatestPosition());
        }
        for(LatestPositionsListener listener : latestPositionsListeners)
            listener.onPositionsUpdated(pos);
        for(DevicesListener listener : devicesListeners)
            listener.onDevicesUpdated(dev);
        updateFailureCount = 0;
        updateTimer.schedule(ApplicationContext.getInstance()
                .getApplicationSettings().getUpdateInterval());
    }
    
    private void onUpdateFailed(Throwable caught) {
        if (++updateFailureCount >= MAX_UPDATE_FAILURE_COUNT) {
            updateTimer.cancel();
            String msg = i18n.errUserDisconnected();
            if (caught instanceof StatusCodeException) {
                StatusCodeException e = (StatusCodeException) caught;
                if (e.getStatusCode() == 500) {
                    msg = i18n.errUserSessionExpired();
                }
            }
            AlertMessageBox msgBox = new AlertMessageBox(i18n.error(), msg);
            msgBox.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
                @Override
                public void onDialogHide(DialogHideEvent event) {
                    Window.Location.reload();
                }
            });
            msgBox.show();
        }
   }
    
    public void run() {       
        updateTimer = new Timer() {
            @Override
            public void run() {
                update();
            }
        };
        updateTimer.schedule(ApplicationContext.getInstance()
                        .getApplicationSettings().getUpdateInterval());
    }
    
    public void addLatestPositionsListener(LatestPositionsListener listener) {
        latestPositionsListeners.add(listener);
    }
    
    public void addDevicesListener(DevicesListener listener) {
        devicesListeners.add(listener);
    }
}
