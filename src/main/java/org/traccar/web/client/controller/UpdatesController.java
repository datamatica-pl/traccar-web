package org.traccar.web.client.controller;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Position;

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
        Application.getDataService().getLatestPositions(new AsyncCallback<List<Position>>() {
            @Override
            public void onSuccess(List<Position> result) {                
                for(LatestPositionsListener listener : latestPositionsListeners)
                    listener.onPositionsUpdated(result);
                updateFailureCount = 0;
                
                Application.getDataService().getDevices(new AsyncCallback<List<Device>>() {
                    @Override
                    public void onSuccess(List<Device> result) {
                        for(DevicesListener listener : devicesListeners) {
                            listener.onDevicesUpdated(result);
                        }
                        updateFailureCount = 0;
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        onUpdateFailed(caught);
                    }
                });
                
                updateTimer.schedule(ApplicationContext.getInstance().getApplicationSettings().getUpdateInterval());
            }

            @Override
            public void onFailure(Throwable caught) {
                onUpdateFailed(caught);
            }
        });
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
        update();
    }
    
    public void addLatestPositionsListener(LatestPositionsListener listener) {
        latestPositionsListeners.add(listener);
    }
    
    public void addDevicesListener(DevicesListener listener) {
        devicesListeners.add(listener);
    }
}
