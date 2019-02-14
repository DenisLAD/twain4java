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
import java.awt.geom.Rectangle2D;

/**
 *
 * @author lucifer
 */
public abstract class TwainContainer {

    public static final int[] TYPE_SIZES = {
        1, 2, 4,
        1, 2, 4,
        2, 4, 16,
        34, 66, 130, 256,
        1026, 1024
    };
    protected int cap;
    protected int type;

    TwainContainer(int cap, byte[] container) {
        this.cap = cap;
        this.type = TwainUtils.getINT16(container, 0);
    }

    TwainContainer(int cap, int type) {
        this.cap = cap;
        this.type = type;
    }

    public int getCapabilityId() {
        return cap;
    }

    public abstract int getType();

    public abstract byte[] getBytes();

    public int getItemType() {
        return type;
    }

    abstract public <T> T[] getItems();

    private boolean booleanValue(Object obj) throws TwainException {
        if (obj instanceof Number) {
            return (((Number) obj).intValue() != 0);
        } else if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        } else if (obj instanceof String) {
            return Boolean.valueOf((String) obj).booleanValue();
        }
        throw new TwainException(getClass().getName() + ".booleanValue:\n\tUnsupported data type: " + obj.getClass().getName());
    }

    private int intValue(Object obj) throws TwainException {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj instanceof Boolean) {
            return ((((Boolean) obj).booleanValue()) ? 1 : 0);
        } else if (obj instanceof String) {
            String s = (String) obj;
            try {
                return Integer.parseInt(s);
            } catch (Exception e) {
                throw new TwainException(getClass().getName() + ".intValue:\n\tCannot convert string [\"" + s + "\"] to int.");
            }
        }
        throw new TwainException(getClass().getName() + ".intValue:\n\tUnsupported data type: " + obj.getClass().getName());
    }

    private double doubleValue(Object obj) throws TwainException {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else if (obj instanceof Boolean) {
            return ((((Boolean) obj).booleanValue()) ? 1 : 0);
        } else if (obj instanceof String) {
            String s = (String) obj;
            try {
                return Double.parseDouble(s);
            } catch (Exception e) {
                throw new TwainException(getClass().getName() + ".doubleValue:\n\tCannot convert string [\"" + s + "\"] to double.");
            }
        }
        throw new TwainException(getClass().getName() + ".doubleValue:\n\tUnsupported data type: " + obj.getClass().getName());
    }

    abstract public Object getCurrentValue() throws TwainException;

    public boolean booleanValue() throws TwainException {
        return booleanValue(getCurrentValue());
    }

    public int intValue() throws TwainException {
        return intValue(getCurrentValue());
    }

    public double doubleValue() throws TwainException {
        return doubleValue(getCurrentValue());
    }

    abstract public void setCurrentValue(Object v) throws TwainException;

    public void setCurrentValue(boolean v) throws TwainException {
        setCurrentValue(new Boolean(v));
    }

    public void setCurrentValue(int v) throws TwainException {
        setCurrentValue(new Integer(v));
    }

    public void setCurrentValue(double v) throws TwainException {
        setCurrentValue(new Double(v));
    }

    abstract public Object getDefaultValue() throws TwainException;

    public boolean booleanDefaultValue() throws TwainException {
        return booleanValue(getDefaultValue());
    }

    public int intDefaultValue() throws TwainException {
        return intValue(getDefaultValue());
    }

    public double doubleDefaultValue() throws TwainException {
        return doubleValue(getDefaultValue());
    }

    abstract public void setDefaultValue(Object v) throws TwainException;

    public void setDefaultValue(boolean v) throws TwainException {
        setDefaultValue(new Boolean(v));
    }

    public void setDefaultValue(int v) throws TwainException {
        setDefaultValue(new Integer(v));
    }

    public void setDefaultValue(double v) throws TwainException {
        setDefaultValue(new Double(v));
    }

    protected Object get32BitObjectAt(byte[] container, int index) {
        switch (type) {
            case Twain.TWTY_INT8:
            case Twain.TWTY_INT16:
            case Twain.TWTY_INT32:
                return TwainUtils.getINT32(container, index);
            case Twain.TWTY_UINT8:
                return TwainUtils.getINT32(container, index) & 0x000000FF;
            case Twain.TWTY_UINT16:
                return TwainUtils.getINT32(container, index) & 0x0000FFFF;
            case Twain.TWTY_UINT32:
                return ((long) TwainUtils.getINT32(container, index)) & 0x00000000FFFFFFFFL;
            case Twain.TWTY_BOOL:
                return (TwainUtils.getINT32(container, index) != 0);
            case Twain.TWTY_FIX32:
                return TwainUtils.getFIX32(container, index);

            case Twain.TWTY_FRAME:
            case Twain.TWTY_STR32:
            case Twain.TWTY_STR64:
            case Twain.TWTY_STR128:
            case Twain.TWTY_STR255:
            case Twain.TWTY_STR1024:
            case Twain.TWTY_UNI512:
                return TwainUtils.getINT32(container, index);
            default:
        }
        return null;
    }

    protected void set32BitObjectAt(byte[] container, int index, Object item) {
        if (item instanceof Integer) {
            int v = ((Integer) item);
            switch (type) {
                case Twain.TWTY_FIX32:
                    TwainUtils.setFIX32(container, index, v);
                    break;
                case Twain.TWTY_BOOL:
                    TwainUtils.setINT32(container, index, (v == 0) ? 0 : 1);
                    break;
                default:
                    TwainUtils.setINT32(container, index, v);
                    break;
            }
        } else if (item instanceof Double) {
            double v = ((Double) item);
            switch (type) {
                case Twain.TWTY_FIX32:
                    TwainUtils.setFIX32(container, index, v);
                    break;
                case Twain.TWTY_BOOL:
                    TwainUtils.setINT32(container, index, (v == 0) ? 0 : 1);
                    break;
                default:
                    TwainUtils.setINT32(container, index, (int) v);
                    break;
            }
        } else if (item instanceof Boolean) {
            int v = (((Boolean) item)) ? 1 : 0;
            if (type == Twain.TWTY_FIX32) {
                TwainUtils.setFIX32(container, index, v);
            } else {
                TwainUtils.setINT32(container, index, v);
            }
        } else if (item instanceof String) {
            if (type == Twain.TWTY_FIX32) {
                this.set32BitObjectAt(container, index, new Double((String) item));
            } else {
                this.set32BitObjectAt(container, index, new Integer((String) item));
            }
        }
    }

    protected Object getObjectAt(byte[] container, int index) {
        switch (type) {
            case Twain.TWTY_INT8:
                return new Integer(container[index]);
            case Twain.TWTY_INT16:
                return TwainUtils.getINT16(container, index);
            case Twain.TWTY_INT32:
                return TwainUtils.getINT32(container, index);
            case Twain.TWTY_UINT8:
                return container[index] & 0x000000FF;
            case Twain.TWTY_UINT16:
                return TwainUtils.getINT16(container, index) & 0x0000FFFF;
            case Twain.TWTY_UINT32:
                return ((long) TwainUtils.getINT32(container, index)) & 0x00000000FFFFFFFFL;
            case Twain.TWTY_BOOL:
                return (TwainUtils.getINT16(container, index) != 0);
            case Twain.TWTY_FIX32:
                return TwainUtils.getFIX32(container, index);
            case Twain.TWTY_FRAME:
                double x = TwainUtils.getFIX32(container, index);          // left
                double y = TwainUtils.getFIX32(container, index + 4);        // top
                double w = TwainUtils.getFIX32(container, index + 8) - x;      // right
                double h = TwainUtils.getFIX32(container, index + 12) - y;     // bottom
                return new Rectangle2D.Double(x, y, w, h);
            case Twain.TWTY_STR32:
            case Twain.TWTY_STR64:
            case Twain.TWTY_STR128:
            case Twain.TWTY_STR255:
                String s = "";
                for (int i = 0; (container[index + i] != 0) && (i < TYPE_SIZES[type]); i++) {
                    s += (char) container[index + i];
                }
                return s;
            default:
        }
        return null;
    }

    private void set16BitObjectAt(byte[] container, int index, Object item) {
        if (item instanceof Number) {
            int v = (((Number) item).intValue());
            TwainUtils.setINT16(container, index, v);
        } else if (item instanceof Boolean) {
            int v = (((Boolean) item)) ? 1 : 0;
            TwainUtils.setINT16(container, index, v);
        }
    }

    protected void setObjectAt(byte[] container, int index, Object item) {
        switch (type) {
            case Twain.TWTY_INT16:
            case Twain.TWTY_UINT16:
                set16BitObjectAt(container, index, item);
                break;
            case Twain.TWTY_FIX32:
            case Twain.TWTY_INT32:
            case Twain.TWTY_UINT32:
                set32BitObjectAt(container, index, item);
                break;
            default:

        }
    }
}
