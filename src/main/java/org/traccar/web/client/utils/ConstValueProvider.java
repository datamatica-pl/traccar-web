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
package org.traccar.web.client.utils;

import com.sencha.gxt.core.client.ValueProvider;

/**
 *
 * @author ŁŁ
 * @param <T1>
 * @param <T2>
 */
public class ConstValueProvider<T1, T2> implements ValueProvider<T1, T2> {
    private final T2 value;
    
    public ConstValueProvider(T2 value) {
        this.value = value;
    }
    
    @Override
    public T2 getValue(T1 object) {
        return value;
    }

    @Override
    public void setValue(T1 object, T2 value) {
    }

    @Override
    public String getPath() {
        return "";
    }
    
}
