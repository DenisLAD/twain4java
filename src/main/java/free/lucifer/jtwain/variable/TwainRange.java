/*
 * Copyright 2018 (c) Denis Andreev (lucifer).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package free.lucifer.jtwain.variable;

import free.lucifer.jtwain.Twain;
import free.lucifer.jtwain.exceptions.TwainException;
import free.lucifer.jtwain.utils.TwainUtils;

/**
 *
 * @author lucifer
 */
public class TwainRange extends TwainContainer {

    private final Object minValue;
    private final Object maxValue;
    private final Object stepSize;
    private Object defaultValue;
    private Object currentValue;

    public TwainRange(int cap, byte[] container) {
        super(cap, container);
        minValue = get32BitObjectAt(container, 2);
        maxValue = get32BitObjectAt(container, 6);
        stepSize = get32BitObjectAt(container, 10);
        defaultValue = get32BitObjectAt(container, 14);
        currentValue = get32BitObjectAt(container, 18);
    }

    @Override
    public int getType() {
        return Twain.TWON_RANGE;
    }

    @Override
    public byte[] getBytes() {
        byte[] container = new byte[22];
        TwainUtils.setINT16(container, 0, type);
        set32BitObjectAt(container, 2, minValue);
        set32BitObjectAt(container, 6, maxValue);
        set32BitObjectAt(container, 10, stepSize);
        set32BitObjectAt(container, 14, defaultValue);
        set32BitObjectAt(container, 18, currentValue);
        return container;
    }

    @Override
    public Object getCurrentValue() throws TwainException {
        return currentValue;
    }

    @Override
    public void setCurrentValue(Object obj) throws TwainException {
        currentValue = obj;
    }

    @Override
    public Object getDefaultValue() throws TwainException {
        return defaultValue;
    }

    @Override
    public void setDefaultValue(Object obj) throws TwainException {
        defaultValue = obj;
    }

    @Override
    public Object[] getItems() {
        Object[] items = new Object[1];
        items[0] = currentValue;
        return items;
    }

}
