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
package free.lucifer.jtwain;

import free.lucifer.jtwain.variable.TwainRange;
import free.lucifer.jtwain.variable.TwainEnumeration;
import free.lucifer.jtwain.variable.TwainArray;
import free.lucifer.jtwain.variable.TwainOneValue;
import free.lucifer.jtwain.variable.TwainContainer;
import free.lucifer.jtwain.libs.Win32Twain;
import free.lucifer.jtwain.exceptions.TwainCheckStatusException;
import free.lucifer.jtwain.exceptions.TwainException;
import free.lucifer.jtwain.utils.TwainUtils;
import com.sun.jna.Pointer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lucifer
 */
public class TwainCapability {

    protected TwainSource source;
    public int cap;
    protected byte[] capability = new byte[16];
    protected TwainContainer container;

    public TwainCapability(TwainSource source, int cap) throws TwainException {
        this.source = source;
        this.cap = cap;
        this.container = get();
    }

    public TwainCapability(TwainSource source, int cap, int mode) throws TwainException {
        this.source = source;
        this.cap = cap;
        switch (mode) {
            case Twain.MSG_GETCURRENT:
                container = getCurrent();
                break;
            case Twain.MSG_GETDEFAULT:
                container = getDefault();
                break;
            case Twain.MSG_GET:
            default:
                container = get();
                break;
        }
    }

    private TwainContainer get(int msg, int contype) throws TwainException {
        Win32Twain.TW_CAPABILITY cp = new Win32Twain.TW_CAPABILITY();

        cp.Cap = (short) cap;
        cp.ConType = (short) contype;
        cp.Container = 0l;

        TwainUtils.setINT16(capability, 0, cap);
        TwainUtils.setINT16(capability, 2, contype);
        TwainUtils.setINT64(capability, 4, 0);

        System.out.print("TWAIN GET: ");
        dumpHex(capability);
        System.out.print(",");
        source.call(Twain.DG_CONTROL, Twain.DAT_CAPABILITY, (short) msg, capability);

        int containerType = TwainUtils.getINT16(capability, 2);
        Pointer containerPtr = new Pointer(TwainUtils.getINT64(capability, 4));

        dumpHex(capability);
        System.out.print(",");
        byte[] container = Twain.getContainer(containerType, containerPtr);
        dumpHex(container);
        System.out.println();

        switch (containerType) {
            case Twain.TWON_ARRAY:
                return new TwainArray(cap, container);
            case Twain.TWON_ENUMERATION:
                return new TwainEnumeration(cap, container);
            case Twain.TWON_ONEVALUE:
                return new TwainOneValue(cap, container);
            case Twain.TWON_RANGE:
                return new TwainRange(cap, container);
            default:
                throw new TwainException("Unknown container type " + containerType);
        }
    }

    private TwainContainer get(int msg) throws TwainException {
        return get(msg, -1);
    }

    public TwainContainer get() throws TwainException {
        return container = get(Twain.MSG_GET);
    }

    public TwainContainer getCurrent() throws TwainException {
        return get(Twain.MSG_GETCURRENT);
    }

    public TwainContainer getDefault() throws TwainException {
        return get(Twain.MSG_GETDEFAULT);
    }

    public int querySupport() throws TwainException {
        return get(Twain.MSG_QUERYSUPPORT, Twain.TWON_ONEVALUE).intValue();
    }

    public boolean querySupport(int flagMask) {
        try {
            int flags = querySupport();
            return (flags & flagMask) != 0;
        } catch (TwainException e) {
            return false;
        }
    }

    public TwainContainer reset() throws TwainException {
        return container = get(Twain.MSG_RESET);
    }

    public TwainContainer set() throws TwainException {
        return container = set(container);
    }

    public static void dumpHex(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            System.out.print(String.format("%02x", bytes[i]));
        }
    }

    public TwainContainer set(TwainContainer container) throws TwainException {
        int containerType = container.getType();
        byte[] containerBytes = container.getBytes();
        Pointer containerHandle = Twain.setContainer(containerType, containerBytes);

        try {

            TwainUtils.setINT16(capability, 0, cap);
            TwainUtils.setINT16(capability, 2, containerType);
            TwainUtils.setINT64(capability, 4, Pointer.nativeValue(containerHandle));
            System.out.print("TWAIN SET: ");
            dumpHex(capability);
            System.out.print(",");
            dumpHex(containerBytes);
            System.out.println();
            source.call(Twain.DG_CONTROL, Twain.DAT_CAPABILITY, Twain.MSG_SET, capability);

        } catch (TwainCheckStatusException e) {
            e.printStackTrace();
            container = get();
        } finally {
            Twain.free(containerHandle);
        }

        return container;
    }

    public <T> T[] getItems() {
        return container.getItems();
    }

    public boolean booleanValue() throws TwainException {
        return getCurrent().booleanValue();
    }

    public int intValue() throws TwainException {
        return getCurrent().intValue();
    }

    public double doubleValue() throws TwainException {
        return getCurrent().doubleValue();
    }

    public void setCurrentValue(boolean v) throws TwainException {
        setCurrentValue(new Boolean(v));
    }

    public void setCurrentValue(int v) throws TwainException {
        setCurrentValue(new Integer(v));
    }

    public void setCurrentValue(double v) throws TwainException {
        setCurrentValue(new Double(v));
    }

    public void setCurrentValue(Object val) throws TwainException {
        container.setCurrentValue(val);
        set();
    }

    public boolean booleanDefaultValue() throws TwainException {
        return getDefault().booleanDefaultValue();
    }

    public int intDefaultValue() throws TwainException {
        return getDefault().intDefaultValue();
    }

    public double doubleDefaultValue() throws TwainException {
        return getDefault().doubleDefaultValue();
    }

    public void setDefaultValue(boolean v) throws TwainException {
        setDefaultValue(new Boolean(v));
    }

    public void setDefaultValue(int v) throws TwainException {
        setDefaultValue(new Integer(v));
    }

    public void setDefaultValue(double v) throws TwainException {
        setDefaultValue(new Double(v));
    }

    public void setDefaultValue(Object val) throws TwainException {
        container.setDefaultValue(val);
        set();
    }

    public static TwainCapability[] getCapabilities(TwainSource source) throws TwainException {
        TwainCapability tc = source.getCapability(Twain.CAP_SUPPORTEDCAPS);
        Object[] items = tc.getItems();
        List<TwainCapability> caps = new ArrayList<>();

        for (int i = 0; i < items.length; i++) {
            int capid = ((Number) items[i]).intValue();
            try {
                switch (capid) {
                    case Twain.ICAP_COMPRESSION:
                        caps.add(new Compression(source));
                        break;
                    case Twain.ICAP_XFERMECH:
                        caps.add(new XferMech(source));
                        break;
                    case Twain.ICAP_IMAGEFILEFORMAT:
                        caps.add(new ImageFileFormat(source));
                        break;
                    default:
                        caps.add(new TwainCapability(source, capid));
                        break;
                }
            } catch (TwainException e) {
            }
        }
        return (TwainCapability[]) caps.toArray(new TwainCapability[0]);
    }

    static public class ImageFileFormat extends TwainCapability {

        ImageFileFormat(TwainSource source) throws TwainException {
            super(source, Twain.ICAP_IMAGEFILEFORMAT);
        }

    }

    static public class Compression extends TwainCapability {

        Compression(TwainSource source) throws TwainException {
            super(source, Twain.ICAP_COMPRESSION);
        }

    }

    static public class XferMech extends TwainCapability {

        XferMech(TwainSource source) throws TwainException {
            super(source, Twain.ICAP_XFERMECH);
        }

        @Override
        public int intValue() {
            try {
                return super.intValue();
            } catch (Exception e) {
                return Twain.TWSX_NATIVE;
            }
        }
    }
}
