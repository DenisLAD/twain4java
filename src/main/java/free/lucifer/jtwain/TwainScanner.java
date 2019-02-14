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
import free.lucifer.jtwain.transfer.TwainMemoryTransfer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author lucifer
 */
public class TwainScanner {

    private TwainIOMetadata metadata;
    private List<TwainListener> listeners = new ArrayList<>();

    public TwainScanner() {
        metadata = new TwainIOMetadata();
        Twain.setScanner(this);
    }

    public void select() throws TwainException {
        Twain.select(this);
    }

    public TwainIdentity[] getIdentities() {
        List<TwainIdentity> identities = new ArrayList<>();
        try {
            Twain.getIdentities(this, identities);
        } catch (Exception e) {
            metadata.setException(e);
            fireListenerUpdate(metadata.EXCEPTION);
        }
        return (TwainIdentity[]) identities.toArray(new TwainIdentity[identities.size()]);
    }

    public String[] getDeviceNames() throws TwainException {
        List<TwainIdentity> identities = new ArrayList<>();

        Twain.getIdentities(this, identities);

        String[] names = new String[identities.size()];
        Iterator<TwainIdentity> ids = identities.iterator();
        for (int i = 0; ids.hasNext(); i++) {
            TwainIdentity id = (TwainIdentity) ids.next();
            names[i] = id.getProductName();
        }
        return names;
    }

    public void select(String name) throws TwainException {
        Twain.select(this, name);
    }

    public void acquire() throws TwainException {
        Twain.acquire(this);
    }

    public void setCancel(boolean c) throws TwainException {
        Twain.setCancel(this, c);
    }

    void setImage(BufferedImage image) {
        try {
            metadata.setImage(image);
            fireListenerUpdate(metadata.ACQUIRED);
        } catch (Exception e) {
            metadata.setException(e);
            fireListenerUpdate(metadata.EXCEPTION);
        }
    }

    void setImage(File file) {
        try {
            metadata.setFile(file);
            fireListenerUpdate(metadata.FILE);
        } catch (Exception e) {
            metadata.setException(e);
            fireListenerUpdate(metadata.EXCEPTION);
        }
    }

    void setImageBuffer(TwainMemoryTransfer.Info info) {
        try {
            ((TwainIOMetadata) metadata).setMemory(info);
            fireListenerUpdate(metadata.MEMORY);
        } catch (Exception e) {
            metadata.setException(e);
            fireListenerUpdate(metadata.EXCEPTION);
        }
    }

    protected void negotiateCapabilities(TwainSource source) {
        ((TwainIOMetadata) metadata).setSource(source);
        fireListenerUpdate(metadata.NEGOTIATE);
        if (metadata.getCancel()) {
            try {
                source.close();
            } catch (Exception e) {
                metadata.setException(e);
                fireListenerUpdate(metadata.EXCEPTION);
            }
        }
    }

    void setState(TwainSource source) {
        metadata.setState(source.getState());
        ((TwainIOMetadata) metadata).setSource(source);
        fireListenerUpdate(metadata.STATECHANGE);
    }

    void signalInfo(String msg, int val) {
        metadata.setInfo(msg + " [0x" + Integer.toHexString(val) + "]");
        fireListenerUpdate(metadata.INFO);
    }

    void signalException(String msg) {
        Exception e = new TwainException(getClass().getName() + ".setException:\n    " + msg);
        metadata.setException(e);
        fireListenerUpdate(metadata.EXCEPTION);
    }

    public TwainIOMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(TwainIOMetadata metadata) {
        this.metadata = metadata;
    }

    public void addListener(TwainListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TwainListener listener) {
        listeners.remove(listener);
    }

    public void fireExceptionUpdate(Exception e) {
        metadata.setException(e);
        fireListenerUpdate(metadata.EXCEPTION);
    }

    public void fireListenerUpdate(TwainIOMetadata.Type type) {
        for (Iterator<TwainListener> e = new ArrayList<>(listeners).iterator(); e.hasNext();) {
            TwainListener listener = (TwainListener) e.next();
            listener.update(type, metadata);
        }
    }

    public static TwainScanner getScanner() {
        return new TwainScanner();
    }
}
