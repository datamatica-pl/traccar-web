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
package org.traccar.web.client.model.api;

import java.util.List;
import pl.datamatica.traccar.model.CommandType;

/**
 *
 * @author Lukasz
 */
public class ApiCommandType {
    String commandName;
    boolean isTCP;
    List<Parameter> commandParameters;
    
    public String getCommandName() {
        return commandName;
    }
    
    public boolean isTCP() {
        return isTCP;
    }
    
    public List<Parameter> getCommandParameters() {
        return commandParameters;
    }
    
    
    public static class Parameter {
        String parameterName;
        String valueType;
        String description;
        List<ParameterConstraint> constraints;
        
        public String getParameterName() {
            return parameterName;
        }
        
        public String getValueType() {
            return valueType;
        }
        
        public String getDescription() {
            return description;
        }
        
        public List<ParameterConstraint> getConstraints() {
            return constraints;
        }
    }
    
    public static class ParameterConstraint {
        String constraintType;
        String constraintValue;
        
        public String getConstraintType() {
            return constraintType;
        }
        
        public String getConstraintValue() {
            return constraintValue;
        }
    }
}
