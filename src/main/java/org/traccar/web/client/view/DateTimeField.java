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
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.event.BlurEvent;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.IsField;
import com.sencha.gxt.widget.core.client.form.TimeField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.traccar.web.client.editor.DateTimeEditor;

/**
 *
 * @author ŁŁ
 */
public class DateTimeField implements IsField<Date> {
    
    interface _UiBinder extends UiBinder<Widget, DateTimeField> {}
    public static final _UiBinder uiBinder = GWT.create(_UiBinder.class);
    
    @UiField
    DateField date;
    @UiField
    TimeField time;
    
    private Widget widget;
    private DateTimeEditor editor;
    
    public DateTimeField() {
        widget = uiBinder.createAndBindUi(this);
        editor = new DateTimeEditor(date, time);
    }
    
    @Override
    public void clear() {
        date.clear();
        time.clear();
    }

    @Override
    public void clearInvalid() {
        date.clearInvalid();
        time.clearInvalid();
    }

    @Override
    public void finishEditing() {
        date.finishEditing();
        time.finishEditing();
    }

    @Override
    public List<EditorError> getErrors() {
        List<EditorError> errors = new ArrayList<>();
        errors.addAll(date.getErrors());
        errors.addAll(time.getErrors());
        return errors;
    }

    @Override
    public boolean isValid(boolean preventMark) {
        return date.isValid(preventMark) && time.isValid(preventMark);
    }

    @Override
    public void reset() {
        date.reset();
        time.reset();
    }

    @Override
    public boolean validate(boolean preventMark) {
        return date.validate(preventMark) && time.validate(preventMark);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setValue(Date value) {
        editor.setValue(value);
    }

    @Override
    public Date getValue() {
        return editor.getValue();
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurEvent.BlurHandler handler) {
        time.addBlurHandler(handler);
        return date.addBlurHandler(handler);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
        time.addValueChangeHandler(handler);
        return date.addValueChangeHandler(handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        date.fireEvent(event);
        time.fireEvent(event);
    }
    
}
