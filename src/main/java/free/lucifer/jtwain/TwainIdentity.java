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

import free.lucifer.jtwain.exceptions.TwainException;
import free.lucifer.jtwain.libs.Win32Twain;
import com.sun.jna.Pointer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lucifer
 */
public class TwainIdentity {

    private TwainSourceManager manager;
    protected Win32Twain.TW_IDENTITY identity;

    public TwainIdentity(TwainSourceManager manager) {
        this.manager = manager;
        this.identity = new Win32Twain.TW_IDENTITY();
    }

    TwainIdentity(TwainSourceManager sourceManager, Win32Twain.TW_IDENTITY orign) {
        this.manager = manager;
        this.identity = orign;
    }

    protected void getDefault() {
        try {
            manager.call(Twain.DG_CONTROL, Twain.DAT_IDENTITY, Twain.MSG_GETDEFAULT, identity);
        } catch (TwainException e) {
        }
    }

    void userSelect() throws TwainException {
        manager.call(Twain.DG_CONTROL, Twain.DAT_IDENTITY, Twain.MSG_USERSELECT, identity);
    }

    public void open() throws TwainException {
        manager.call(Twain.DG_CONTROL, Twain.DAT_IDENTITY, Twain.MSG_OPENDS, identity);
    }

    public void close() throws TwainException {
        manager.call(Twain.DG_CONTROL, Twain.DAT_IDENTITY, Twain.MSG_CLOSEDS, identity);
    }

    void getFirst() throws TwainException {
        manager.call(Twain.DG_CONTROL, Twain.DAT_IDENTITY, Twain.MSG_GETFIRST, identity);
    }

    void getNext() throws TwainException {
        manager.call(Twain.DG_CONTROL, Twain.DAT_IDENTITY, Twain.MSG_GETNEXT, identity);
    }

    public int getId() {
        return identity.Id;
    }

    public int getMajorNum() {
        return identity.Version.MajorNum;
    }

    public int getMinorNum() {
        return identity.Version.MinorNum;
    }

    public int getLanguage() {
        return identity.Version.Language;
    }

    public int getCountry() {
        return identity.Version.Country;
    }

    public int getProtocolMajor() {
        return identity.ProtocolMajor;
    }

    public int getProtocolMinor() {
        return identity.ProtocolMinor;
    }

    public int getSupportedGroups() {
        return identity.SupportedGroups;
    }

    public String getManufacturer() {
        return identity.getManufacturer();
    }

    public String getProductFamily() {
        return identity.getProductFamily();
    }

    public String getProductName() {
        return identity.getProductName();
    }

    public static TwainIdentity[] getIdentities() throws TwainException {
        TwainSourceManager manager = Twain.getSourceManager();
        List<TwainIdentity> identities = new ArrayList<>();
        try {
            TwainIdentity identity = new TwainIdentity(manager);
            identity.getFirst();
            identities.add(identity);
            while (true) {
                identity = new TwainIdentity(manager);
                identity.getNext();
                identities.add(identity);
            }
        } catch (TwainException e) {
        }

        return identities.toArray(new TwainIdentity[0]);
    }

    public static String[] getProductNames() throws TwainException {
        TwainSourceManager manager = Twain.getSourceManager();
        List<String> identities = new ArrayList<>();
        try {
            TwainIdentity identity = new TwainIdentity(manager);
            identity.getFirst();
            identities.add(identity.getProductName());
            while (true) {
                identity = new TwainIdentity(manager);
                identity.getNext();
                identities.add(identity.getProductName());
            }
        } catch (TwainException e) {
        }

        return identities.toArray(new String[0]);
    }

    public String toString() {
        String s = "TwainIdentity\n";
        s += "\tid               = 0x" + Integer.toHexString(getId()) + "\n";
        s += "\tmajorNum         = 0x" + Integer.toHexString(getMajorNum()) + "\n";
        s += "\tminorNum         = 0x" + Integer.toHexString(getMinorNum()) + "\n";
        s += "\tlanguage         = 0x" + Integer.toHexString(getLanguage()) + "\n";
        s += "\tcountry          = 0x" + Integer.toHexString(getCountry()) + "\n";
        s += "\tprotocol major   = 0x" + Integer.toHexString(getProtocolMajor()) + "\n";
        s += "\tprotocol minor   = 0x" + Integer.toHexString(getProtocolMinor()) + "\n";
        s += "\tsupported groups = 0x" + Integer.toHexString(getSupportedGroups()) + "\n";
        s += "\tmanufacturer     = " + getManufacturer() + "\n";
        s += "\tproduct family   = " + getProductFamily() + "\n";
        s += "\tproduct name     = " + getProductName() + "\n";
        return s;
    }
  
}
