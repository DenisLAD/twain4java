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

import free.lucifer.jtwain.exceptions.TwainCancelException;
import free.lucifer.jtwain.exceptions.TwainCheckStatusException;
import free.lucifer.jtwain.exceptions.TwainDSException;
import free.lucifer.jtwain.exceptions.TwainEndOfListException;
import free.lucifer.jtwain.exceptions.TwainException;
import free.lucifer.jtwain.exceptions.TwainNotDSException;
import free.lucifer.jtwain.exceptions.TwainResultException;
import free.lucifer.jtwain.exceptions.TwainTransferDoneException;
import free.lucifer.jtwain.utils.TwainUtils;
import com.sun.jna.platform.win32.WinDef;
import java.util.List;

/**
 *
 * @author lucifer
 */
public class TwainSourceManager {

    public static final String[] INFO = {
        "Success",
        "Failure due to unknown causes",
        "Not enough memory to perform operation",
        "No Data Source",
        "DS is connected to max possible applications",
        "DS or DSM reported internal error",
        "Unknown capability",
        "",
        "",
        "Unrecognized MSG DG DAT combination",
        "Data parameter out of range",
        "DG DAT MSG out of expected sequence",
        "Unknown destination Application/Source in DSM_Entry",
        "Capability not supported by source",
        "Operation not supported by capability",
        "Capability has dependancy on other capability",
        "File System operation is denied (file is protected)",
        "Operation failed because file already exists.",
        "File not found",
        "Operation failed because directory is not empty",
        "The feeder is jammed",
        "The feeder detected multiple pages",
        "Error writing the file (i.e. disk full conditions)",
        "The device went offline prior to or during this operation"
    };
    private TwainSource source;

    public TwainSourceManager(WinDef.HWND hwnd) {
        source = new TwainSource(this, hwnd, false);
        source.getDefault();
    }

    String getConditionCodeStr() throws TwainException {
        return INFO[getConditionCode()];
    }

    int getConditionCode() throws TwainException {
        byte[] status = new byte[4];
        int rc = Twain.callSourceManager(Twain.DG_CONTROL, Twain.DAT_STATUS, Twain.MSG_GET, status);
        if (rc != Twain.TWRC_SUCCESS) {
            throw new TwainResultException("Cannot retrieve twain source manager's status. RC = " + rc);
        }
        return TwainUtils.getINT16(status, 0);
    }

    void call(int dg, int id, int msg, Object obj) throws TwainException {
        int rc = Twain.callSourceManager(dg, id, msg, obj);
        switch (rc) {
            case Twain.TWRC_SUCCESS:
                return;
            case Twain.TWRC_FAILURE:
                throw new TwainException(getClass().getName() + ".call error: " + getConditionCodeStr());
            case Twain.TWRC_CHECKSTATUS:
                throw new TwainCheckStatusException();
            case Twain.TWRC_CANCEL:
                throw new TwainCancelException();
            case Twain.TWRC_DSEVENT:
                throw new TwainDSException();
            case Twain.TWRC_NOTDSEVENT:
                throw new TwainNotDSException();
            case Twain.TWRC_XFERDONE:
                throw new TwainTransferDoneException();
            case Twain.TWRC_ENDOFLIST:
                throw new TwainEndOfListException();

            default:
                throw new TwainException("Failed to call source. RC = " + rc);
        }
    }

    TwainSource getSource() {
        return source;
    }

    TwainSource selectSource() throws TwainException {
        source.checkState(3);
        source.setBusy(true);
        try {
            source.userSelect();
            return source;
        } catch (TwainException trec) {
            return source;
        } finally {
            source.setBusy(false);
        }
    }

    void getIdentities(List<TwainIdentity> identities) throws TwainException {
        source.checkState(3);
        source.setBusy(true);
        try {
            TwainIdentity identity = new TwainIdentity(this);
            identity.getFirst();
            identities.add(identity);
            while (true) {
                identity = new TwainIdentity(this);
                identity.getNext();
                identities.add(identity);
            }
        } catch (TwainEndOfListException treeol) {
        } catch (TwainException tioe) {
        } finally {
            source.setBusy(false);
        }
    }

    public TwainSource selectSource(String name) throws TwainException {
        source.checkState(3);
        source.setBusy(true);
        try {
            source.select(name);
            return source;
        } finally {
            source.setBusy(false);
        }
    }

    TwainSource openSource() throws TwainException {
        source.checkState(3);
        source.setBusy(true);
        try {
            source.open();
            
            if (!source.isDeviceOnline()) {
                source.close();
                throw new TwainException("Selected twain source is not online.");
            }

            source.setState(4);
            return source;
        } catch (TwainCancelException trec) {
            source.setBusy(false);
            return source;
        } catch (TwainException tioe) {
            source.setBusy(false);
            throw tioe;
        }
    }
}
