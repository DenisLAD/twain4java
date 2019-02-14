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
public class TwainOneValue extends TwainContainer {

    Object item;

    public TwainOneValue(int cap, byte[] container) {
        super(cap, container);
        item = get32BitObjectAt(container, 2);
    }

    @Override
    public int getType() {
        return Twain.TWON_ONEVALUE;
    }

    @Override
    public byte[] getBytes() {
        byte[] container = new byte[6];
        TwainUtils.setINT16(container, 0, type);
        set32BitObjectAt(container, 2, item);
        return container;
    }

    @Override
    public Object getCurrentValue() throws TwainException {
        return item;
    }

    @Override
    public void setCurrentValue(Object obj) throws TwainException {
        item = obj;
    }

    @Override
    public Object getDefaultValue() throws TwainException {
        return item;
    }

    @Override
    public void setDefaultValue(Object obj) throws TwainException {
        item = obj;
    }

    @Override
    public <T> T[] getItems() {
        Object[] items = new Object[1];
        items[0] = item;
        return (T[]) items;
    }
}
