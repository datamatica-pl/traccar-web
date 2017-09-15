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

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorDelegate;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.StringLabelProvider;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.Validator;
import com.sencha.gxt.widget.core.client.form.validator.AbstractValidator;
import com.sencha.gxt.widget.core.client.form.validator.MaxLengthValidator;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinLengthValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.traccar.web.client.ApplicationContext;
import org.traccar.web.client.i18n.Messages;
import org.traccar.web.client.model.api.ApiCommandType;
import org.traccar.web.client.model.api.ApiCommandType.ParameterConstraint;

public class CommandArgumentsBinder {    
    private final VerticalLayoutContainer container;
    private final Map<String, IFieldAdapter> params;
    
    public CommandArgumentsBinder(VerticalLayoutContainer container) {    
        this.container = container;
        this.params = new HashMap<>();
    }
    
    public void bind(ApiCommandType type) {
        VerticalLayoutData vld = new VerticalLayoutData(1, -1);
        ApplicationContext ctx = ApplicationContext.getInstance();
        container.clear();
        params.clear();
        for(ApiCommandType.Parameter param: type.getCommandParameters()) {
            IFieldAdapter fa = createInput(param);
            FieldLabel lbl = new FieldLabel();
            lbl.setLabelWidth(120);
            lbl.setText(ctx.getMessage(param.getDescription()));
            lbl.add(fa.getWidget());
            lbl.setLayoutData(vld);
            container.add(lbl);
            params.put(param.getParameterName(), fa);
        }
    }
    
    private IFieldAdapter createInput(ApiCommandType.Parameter parameter) {
        String valueType = parameter.getValueType();
        if("integer".equals(valueType)) {
            return new IntegerAdapter(parameter.getConstraints());
        } else if("interval".equals(valueType)) {
            return new IntervalAdapter(parameter.getConstraints());
        } else if("string".equals(valueType)) {
            return new StringAdapter(parameter.getConstraints());
        }
        return null;
    }
    
    public Map<String, String> getParamMap() {
        Map<String, String> paramMap = new HashMap<>();
        for(String param : params.keySet()) {
            paramMap.put(param, params.get(param).getValue());
        }
        return paramMap;
    }

    public boolean validate() {
        for(IFieldAdapter fa : params.values()) {
            if(fa.hasError())
                return false;
        }
        return true;
    }
    
    
    private interface IFieldAdapter {
        Widget getWidget();
        String getValue();
        boolean hasError();
    }
    
    private static class IntegerAdapter implements IFieldAdapter{
        private NumberField field;
        
        IntegerAdapter(List<ParameterConstraint> constraints) {
            NumberPropertyEditor npe = new NumberPropertyEditor.IntegerPropertyEditor();
            field = new NumberField(npe);
            field.setAllowBlank(false);
            for(ParameterConstraint c: constraints) {
                int val = Integer.parseInt(c.getConstraintValue());
                if("GTE".equalsIgnoreCase(c.getConstraintType())) {
                    field.addValidator(new MinNumberValidator(val));
                } else if("LTE".equalsIgnoreCase(c.getConstraintType())) {
                    field.addValidator(new MaxNumberValidator(val));
                } else if("DEFAULT".equalsIgnoreCase(c.getConstraintType())) {
                    field.setValue(val);
                } else if("STEP".equalsIgnoreCase(c.getConstraintType())) {
                    npe.setIncrement(val);
                }
            }
        }
        
        @Override
        public Widget getWidget() {
            return field;
        }

        @Override
        public String getValue() {
            if(field.getValue() == null)
                return null;
            return field.getValue().toString();
        }

        @Override
        public boolean hasError() {
            field.validate();
            return field.getErrors() != null && !field.getErrors().isEmpty();
        }
        
        
    }
    
    private static class IntervalAdapter implements IFieldAdapter {
        private final Messages i18n = GWT.create(Messages.class);
        
        private NumberField<Integer> nf;
        private MaxNumberValidator maxnv;
        private MinNumberValidator minnv;
        private Validator stepValidator;
        private ComboBox<String> cb;
        private HorizontalLayoutContainer hlc;
        
        public IntervalAdapter(List<ParameterConstraint> constraints) {
            hlc = new HorizontalLayoutContainer();
            HorizontalLayoutData hld = new HorizontalLayoutData(-1, -1);
            
            final NumberPropertyEditor npe = new NumberPropertyEditor.IntegerPropertyEditor();
            nf = new NumberField(npe);
            nf.setAllowNegative(false);
            nf.setAllowDecimals(false);
            nf.setAllowBlank(false);
            nf.setWidth(70);
            nf.setLayoutData(hld);
            hlc.add(nf);
            
            ListStore<String> units = new ListStore<>(new ModelKeyProvider<String>(){
                @Override
                public String getKey(String item) {
                    return item;
                }
            });
            units.add(i18n.hour());
            units.add(i18n.minute());
            units.add(i18n.second());
            cb = new ComboBox(units, new StringLabelProvider<>());
            cb.setTriggerAction(TriggerAction.ALL);
            cb.setLayoutData(hld);
            hlc.add(cb);
            
            for(ParameterConstraint c : constraints) {
                final int val = Integer.parseInt(c.getConstraintValue());
                if("LTE".equalsIgnoreCase(c.getConstraintType())) {
                    final MaxNumberValidator hv = new MaxNumberValidator(val/3600);
                    final MaxNumberValidator mv = new MaxNumberValidator(val/60);
                    final MaxNumberValidator sv = new MaxNumberValidator(val);
                    maxnv = sv;
                    nf.addValidator(maxnv);
                    cb.addValueChangeHandler(new ValueChangeHandler<String>() {
                        @Override
                        public void onValueChange(ValueChangeEvent<String> event) {
                            nf.removeValidator(maxnv);
                            if(i18n.hour().equals(event.getValue())) {
                                maxnv = hv;
                            } else if(i18n.minute().equals(event.getValue())) {
                                maxnv = mv;
                            } else {
                                maxnv = sv;
                            }
                            nf.addValidator(maxnv);
                            nf.validate();
                        }
                    });
                } else if("GTE".equalsIgnoreCase(c.getConstraintType())) {
                    final MinNumberValidator hv = new MinNumberValidator((val-1)/3600+1);
                    final MinNumberValidator mv = new MinNumberValidator((val-1)/60+1);
                    final MinNumberValidator sv = new MinNumberValidator(val);
                    minnv = sv;
                    nf.addValidator(minnv);
                    cb.addValueChangeHandler(new ValueChangeHandler<String>() {
                        @Override
                        public void onValueChange(ValueChangeEvent<String> event) {
                            nf.removeValidator(minnv);
                            if(i18n.hour().equals(event.getValue())) {
                                minnv = hv;
                            } else if(i18n.minute().equals(event.getValue())) {
                                minnv = mv;
                            } else {
                                minnv = sv;
                            }
                            nf.addValidator(minnv);
                            nf.validate();
                        } 
                    });
                } else if("DEFAULT".equalsIgnoreCase(c.getConstraintType())) {
                    nf.setValue(val);
                    cb.setValue(i18n.second());
                } else if("STEP".equalsIgnoreCase(c.getConstraintType())) {
                    stepValidator = new AbstractValidator<Integer>() {
                        @Override
                        public List<EditorError> validate(Editor<Integer> editor, Integer value) {
                            if(value % val != 0)
                                return createError(editor, 
                                        i18n.errValMustBeDivisibleBy(val), null);
                            return null;
                        }  
                    };
                    npe.setIncrement(val);
                    nf.addValidator(stepValidator);
                    cb.addValueChangeHandler(new ValueChangeHandler<String>() {
                        @Override
                        public void onValueChange(ValueChangeEvent<String> event) {
                            nf.removeValidator(stepValidator);
                            if(i18n.second().equals(event.getValue())) {
                                npe.setIncrement(val);
                                nf.addValidator(stepValidator);
                                nf.addValidator(maxnv);
                            } else
                                npe.setIncrement(0);
                            nf.validate();
                        }
                    });
                }
            }
        }
        
        @Override
        public Widget getWidget() {
            return hlc;
        }

        @Override
        public String getValue() {
            if(nf.getValue() == null)
                return null;
            if(i18n.hour().equals(cb.getValue())) {
                return Integer.toString(nf.getValue()*3600);
            } else if(i18n.minute().equals(cb.getValue())) {
                return Integer.toString(nf.getValue()*60);
            } else {
                return Integer.toString(nf.getValue());
            }
        }

        @Override
        public boolean hasError() {
            nf.validate();
            return nf.getErrors() != null && !nf.getErrors().isEmpty();
        }
        
    }
    
    private static class StringAdapter implements IFieldAdapter {
        private TextField tf;
        
        public StringAdapter(List<ParameterConstraint> constraints) {
            tf = new TextField();
            for(ParameterConstraint c : constraints) {
                if("MIN_LENGTH".equalsIgnoreCase(c.getConstraintType())) {
                    int val = Integer.parseInt(c.getConstraintValue());
                    tf.addValidator(new MinLengthValidator(val));
                } else if("MAX_LENGTH".equalsIgnoreCase(c.getConstraintType())) {
                    int val = Integer.parseInt(c.getConstraintValue());
                    tf.addValidator(new MaxLengthValidator(val));
                } else if("DEFAULT".equalsIgnoreCase(c.getConstraintType())) {
                    tf.setValue(c.getConstraintValue());
                }
            }
        }
        
        @Override
        public Widget getWidget() {
            return tf;
        }

        @Override
        public String getValue() {
            return tf.getValue();
        }

        @Override
        public boolean hasError() {
            tf.validate();
            return tf.getErrors() != null && !tf.getErrors().isEmpty();
        }
        
    }
}
