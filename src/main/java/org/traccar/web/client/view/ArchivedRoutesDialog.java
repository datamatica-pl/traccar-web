/*
 * Copyright 2017 Datamatica (dev@datamatica.pl)
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import java.util.List;
import org.traccar.web.client.Application;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.DataServiceAsync;
import pl.datamatica.traccar.model.Route;

/**
 *
 * @author ŁŁ
 */
public class ArchivedRoutesDialog {
    interface _UiBinder extends UiBinder<Widget, ArchivedRoutesDialog> {}
    private static _UiBinder uiBinder = GWT.create(_UiBinder.class);
    
    public interface RouteHandler {
        void onArchivedChanged(Route selectedItem, boolean archive);
    }
    
    @UiField
    Window window;
    
    @UiField(provided = true)
    ListView<Route, String> routesListView;
    
    ListStore<Route> routes;
    RouteHandler routeHandler;
    
    Messages i18n = GWT.create(Messages.class);
    
    public ArchivedRoutesDialog(ListStore<Route> routes, RouteHandler routeHandler) {
        this.routes = routes;
        this.routeHandler = routeHandler;
        routesListView = new ListView(routes, new ValueProvider<Route, String>() {
            @Override
            public String getValue(Route object) {
                return object.getName();
            }

            @Override
            public void setValue(Route object, String value) {
            }

            @Override
            public String getPath() {
                return "name";
            }
        });
        routesListView.setContextMenu(createContextMenu());
        uiBinder.createAndBindUi(this);
    }
    
    public Menu createContextMenu() {
        Menu menu = new Menu();
        
        MenuItem restore = new MenuItem(i18n.restoreRoute());
        restore.addSelectionHandler(new SelectionHandler<Item>() {
            @Override
            public void onSelection(SelectionEvent<Item> event) {
                Route r = routesListView.getSelectionModel().getSelectedItem();
                routes.remove(r);
                routeHandler.onArchivedChanged(r, false);
            }  
        });
        menu.add(restore);
        
        return menu;
    }
    
    public void show() {
        window.show();
    }
    
    public void hide() {
        window.hide();
    }
    
    @UiHandler("closeButton")
    public void close(SelectEvent selectEvent) {
        hide();
    }
}
