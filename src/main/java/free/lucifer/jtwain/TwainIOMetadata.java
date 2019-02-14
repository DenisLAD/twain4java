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

import free.lucifer.jtwain.transfer.TwainMemoryTransfer;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 *
 * @author lucifer
 */
public class TwainIOMetadata {

    static public Type INFO = new Type("INFO");
    static public Type EXCEPTION = new Type("EXCEPTION");
    static public Type SELECTED = new Type("SELECTED");
    static public Type ACQUIRED = new Type("ACQUIRED");
    static public Type FILE = new Type("FILE");
    static public Type MEMORY = new Type("MEMORY");
    static public Type NEGOTIATE = new Type("NEGOTIATE");
    static public Type STATECHANGE = new Type("STATECHANGE");

    private int laststate = 0, state = 0;
    private boolean cancel = false;
    private BufferedImage image = null;
    private File file = null;
    private String info = "";
    private Exception exception = null;

    public void setState(int s) {
        laststate = state;
        state = s;
    }

    public int getLastState() {
        return laststate;
    }

    public int getState() {
        return state;
    }

    public boolean isState(int state) {
        return this.state == state;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        this.file = null;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setFile(File file) {
        this.image = null;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public void setException(Exception ex) {
        this.exception = ex;
    }

    public Exception getException() {
        return exception;
    }

    public boolean getCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    static public final String[] TWAIN_STATE = {
        "",
        "Pre-Session",
        "Source Manager Loaded",
        "Source Manager Open",
        "Source Open",
        "Source Enabled",
        "Transfer Ready",
        "Transferring Data",};

    public String getStateStr() {
        return TWAIN_STATE[getState()];
    }

    private TwainSource source = null;

    void setSource(TwainSource source) {
        this.source = source;
    }

    public TwainSource getSource() {
        return source;
    }

    public TwainSource getDevice() {
        return source;
    }
    private TwainMemoryTransfer.Info memory = null;

    public void setMemory(TwainMemoryTransfer.Info info) {
        memory = info;
    }

    public TwainMemoryTransfer.Info getMemory() {
        return memory;
    }

    public boolean isFinished() {
        return (getState() == 3) && (getLastState() == 4);
    }

    static public class Type {

        String type;

        public Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

    }
}
