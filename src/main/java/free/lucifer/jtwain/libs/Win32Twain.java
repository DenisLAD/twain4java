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
package free.lucifer.jtwain.libs;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author lucifer
 */
public interface Win32Twain extends Library {

    public short DSM_Entry(TW_IDENTITY origin, TW_IDENTITY destination, int dg, short dat, short msg, Object p);

    public Pointer DSM_Alloc(int len);

    public void DSM_Free(Pointer handle);

    public Pointer DSM_Lock(Pointer handle);

    public boolean DSM_Unlock(Pointer handle);

    public static class TW_CALLBACK extends Structure {

        public Callback Proc;
        public int RefCon;
        public short Message;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"Proc", "RefCon", "Message"});
        }
    }

    public static class TW_STATUS extends Structure {

        public short ConditionCode;
        public short Reserved;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"ConditionCode", "Reserved"});
        }
    }

    public static class TW_CAPABILITY extends Structure {

        public short Cap;
        public short ConType;
        public Long Container;
        public int reserved;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"Cap", "ConType", "Container", "reserved"});
        }

    }

    public static class TW_VERSION extends Structure implements Structure.ByValue {

        public short MajorNum;
        public short MinorNum;
        public short Language;
        public short Country;
        public byte Info[] = new byte[34];

        public TW_VERSION(int align) {
            super();
            setAlignType(align);
        }

        public TW_VERSION() {
            super();
            setAlignType(Structure.ALIGN_NONE);
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"MajorNum", "MinorNum", "Language", "Country", "Info"});
        }

        public void setInfo(String m) {
            byte mb[] = m.getBytes();
            for (int i = 0; i < Math.min(32, mb.length); ++i) {
                Info[i] = mb[i];
            }
        }

        public String getInfo() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 32; ++i) {
                if (Info[i] == 0) {
                    break;
                }
                sb.append((char) Info[i]);
            }
            return sb.toString();
        }

    }

    public static class TW_IDENTITY extends Structure {

        public int Id;
        public TW_VERSION Version = new TW_VERSION();
        public short ProtocolMajor;
        public short ProtocolMinor;
        public int SupportedGroups;
        public byte Manufacturer[] = new byte[34];
        public byte ProductFamily[] = new byte[34];
        public byte ProductName[] = new byte[34];

        public TW_IDENTITY(int align) {
            super();
            setAlignType(align);
        }

        public TW_IDENTITY() {
            super();
            setAlignType(Structure.ALIGN_NONE);
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"Id", "Version", "ProtocolMajor", "ProtocolMinor", "SupportedGroups", "Manufacturer", "ProductFamily", "ProductName"});
        }

        public void setManufacturer(String m) {
            byte mb[] = m.getBytes();
            for (int i = 0; i < Math.min(32, mb.length); ++i) {
                Manufacturer[i] = mb[i];
            }
        }

        public String getManufacturer() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 32; ++i) {
                if (Manufacturer[i] == 0) {
                    break;
                }
                sb.append((char) Manufacturer[i]);
            }
            return sb.toString();
        }

        public void setProductFamily(String m) {
            byte mb[] = m.getBytes();
            for (int i = 0; i < Math.min(32, mb.length); ++i) {
                ProductFamily[i] = mb[i];
            }
        }

        public String getProductFamily() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 32; ++i) {
                if (ProductFamily[i] == 0) {
                    break;
                }
                sb.append((char) ProductFamily[i]);
            }
            return sb.toString();
        }

        public void setProductName(String m) {
            byte mb[] = m.getBytes();
            for (int i = 0; i < Math.min(32, mb.length); ++i) {
                ProductName[i] = mb[i];
            }
        }

        public String getProductName() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 32; ++i) {
                if (ProductName[i] == 0) {
                    break;
                }
                sb.append((char) ProductName[i]);
            }
            return sb.toString();
        }

        public void copyTo(TW_IDENTITY identity) {
            identity.Id = Id;

            System.arraycopy(Manufacturer, 0, identity.Manufacturer, 0, Manufacturer.length);
            System.arraycopy(ProductFamily, 0, identity.ProductFamily, 0, ProductFamily.length);
            System.arraycopy(ProductName, 0, identity.ProductName, 0, ProductName.length);

            System.arraycopy(Version.Info, 0, identity.Version.Info, 0, Version.Info.length);
            identity.Version.Country = Version.Country;
            identity.Version.Language = Version.Language;
            identity.Version.MajorNum = Version.MajorNum;
            identity.Version.MinorNum = Version.MinorNum;

            identity.ProtocolMajor = ProtocolMajor;
            identity.ProtocolMinor = ProtocolMinor;
            identity.SupportedGroups = SupportedGroups;
        }

    }

    public static class TW_FIX32 extends Structure {

        public short Whole;
        public short Frac;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"Whole", "Frac"});
        }
    }

    public static class TW_USERINTERFACE extends Structure {

        public boolean ShowUI;
        public boolean ModalUI;
        public WinDef.HWND hParent;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"ShowUI", "ModalUI", "hParent"});
        }
    }

    public static class TW_PENDINGXFERS extends Structure {

        public int EOJ;
        public int Reserved;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"EOJ", "Reserved"});
        }
    }

    public static class TW_EVENT extends Structure {

        public Pointer pEvent;
        public short TWMessage;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"pEvent", "TWMessage"});
        }
    }

    public static class TW_MEMORY extends Structure {

        public int Flags;
        public int Length;
        public Pointer TheMem;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"Flags", "Length", "TheMem"});
        }
    }

    public static class TW_IMAGEMEMXFER extends Structure implements Structure.ByValue {

        public short Compression;
        public int BytesPerRow;
        public int Columns;
        public int Rows;
        public int XOffset;
        public int YOffset;
        public int BytesWritten;
        public TW_MEMORY Memory;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"Compression", "BytesPerRow", "Columns", "Rows", "XOffset", "YOffset", "BytesWritten", "Memory"});
        }

    }

}
