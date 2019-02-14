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
public class TwainArray extends TwainContainer {

    int count;
    List<Object> items = new ArrayList<>();

    public TwainArray(int cap, byte[] container) {
        super(cap, container);
        count = TwainUtils.getINT32(container, 2);

        for (int i = 0, off = 6; i < count; i++) {
            items.add(getObjectAt(container, off));
            off += TYPE_SIZES[type];
        }
    }

    @Override
    public int getType() {
        return Twain.TWON_ARRAY;
    }

    @Override
    public byte[] getBytes() {
        int count = items.size();
        int len = 6 + count * TYPE_SIZES[type];
        byte[] container = new byte[len];
        TwainUtils.setINT16(container, 0, type);
        TwainUtils.setINT32(container, 2, count);

        for (int i = 0, off = 6; i < count; i++) {
            setObjectAt(container, off, items.get(i));
            off += TYPE_SIZES[type];
        }
        return container;
    }

    @Override
    public Object getCurrentValue() throws TwainException {
        throw new TwainException(getClass().getName() + ".getCurrentValue:\n\tnot applicable");
    }

    @Override
    public void setCurrentValue(Object obj) throws TwainException {
        throw new TwainException(getClass().getName() + ".setCurrentValue:\n\tnot applicable");
    }

    @Override
    public Object getDefaultValue() throws TwainException {
        throw new TwainException(getClass().getName() + ".getDefaultValue:\n\tnot applicable");
    }

    @Override
    public void setDefaultValue(Object obj) throws TwainException {
        throw new TwainException(getClass().getName() + ".setDefaultValue:\n\tnot applicable");
    }

    @Override
    public <T> T[] getItems() {
        return (T[]) items.toArray();
    }

}
