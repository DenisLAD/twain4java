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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lucifer
 */
public class TwainEnumeration extends TwainContainer {

    int count;
    int currentIndex;
    int defaultIndex;
    List<Object> items = new ArrayList<>();

    public TwainEnumeration(int cap, byte[] container) {
        super(cap, container);
        count = TwainUtils.getINT32(container, 2);
        currentIndex = TwainUtils.getINT32(container, 6);
        defaultIndex = TwainUtils.getINT32(container, 10);

        for (int i = 0, off = 14; i < count; i++) {
            items.add(getObjectAt(container, off));
            off += TYPE_SIZES[type];
        }
    }

    @Override
    public int getType() {
        return Twain.TWON_ENUMERATION;
    }

    @Override
    public <T> T[] getItems() {
        return (T[]) items.toArray();
    }

    @Override
    public byte[] getBytes() {
        int count = items.size();
        int len = 14 + count * TYPE_SIZES[type];
        byte[] container = new byte[len];
        TwainUtils.setINT16(container, 0, type);
        TwainUtils.setINT32(container, 2, count);
        TwainUtils.setINT32(container, 6, currentIndex);
        TwainUtils.setINT32(container, 10, defaultIndex);

        for (int i = 0, off = 14; i < count; i++) {
            setObjectAt(container, off, items.get(i));
            off += TYPE_SIZES[type];
        }
        return container;
    }

    @Override
    public Object getCurrentValue() throws TwainException {
        return items.get(currentIndex);
    }

    @Override
    public void setCurrentValue(Object obj) throws TwainException {
        int count = items.size();
        for (int i = 0; i < count; i++) {
            Object item = items.get(i);
            if (obj.equals(item)) {
                currentIndex = i;
                return;
            }
        }
        throw new TwainException(getClass().getName() + ".setCurrentValue:\n\tCould not find " + obj.toString());
    }

    @Override
    public Object getDefaultValue() throws TwainException {
        return items.get(defaultIndex);
    }

    @Override
    public void setDefaultValue(Object obj) throws TwainException {
        int count = items.size();
        for (int i = 0; i < count; i++) {
            Object item = items.get(i);
            if (obj.equals(item)) {
                defaultIndex = i;
                return;
            }
        }
        throw new TwainException(getClass().getName() + ".setDefaultValue:\n\tCould not find " + obj.toString());
    }

}
