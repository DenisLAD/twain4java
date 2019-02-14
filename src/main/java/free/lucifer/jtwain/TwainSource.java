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

import free.lucifer.jtwain.transfer.TwainTransfer;
import free.lucifer.jtwain.exceptions.TwainException;
import free.lucifer.jtwain.libs.Win32Twain;
import free.lucifer.jtwain.exceptions.TwainCancelException;
import free.lucifer.jtwain.exceptions.TwainCheckStatusException;
import free.lucifer.jtwain.exceptions.TwainDataNotAvailableException;
import free.lucifer.jtwain.exceptions.TwainEndOfListException;
import free.lucifer.jtwain.exceptions.TwainInfNotSupportedException;
import free.lucifer.jtwain.exceptions.TwainNotDSException;
import free.lucifer.jtwain.exceptions.TwainTransferDoneException;
import free.lucifer.jtwain.exceptions.TwainUserCancelException;
import free.lucifer.jtwain.utils.TwainUtils;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author lucifer
 */
public class TwainSource extends TwainIdentity {

    private boolean busy;
    private int state;
    private WinDef.HWND hwnd;

    private int showUI = 1;
    private int modalUI = 0;
    private int iff = Twain.TWFF_BMP;

    private boolean userCancelled;
    private TwainTransferFactory transferFactory;
    private Semaphore twSemaphore = null;
    private boolean twHaveImage = false;

    short callback(int dg, short dat, short msg, Pointer data) {
        switch (msg) {
            case Twain.MSG_XFERREADY:
//                twHaveImage = true;
//                twSemaphore.release();
                try {
                    transferImage();
                } catch (Exception e) {
                    return Twain.TWRC_FAILURE;
                }
                break;
            case Twain.MSG_CLOSEDSOK:
            case Twain.MSG_CLOSEDSREQ:
//                twHaveImage = false;
//                twSemaphore.release();
                try {
                    disable();
                    close();
                } catch (Exception e) {
                    return Twain.TWRC_FAILURE;
                }
                break;
            case Twain.MSG_DEVICEEVENT:
            case Twain.MSG_NULL:
                break;
            default:
                System.out.println("Unknown message");
                return Twain.TWRC_FAILURE;
        }
        return Twain.TWRC_SUCCESS;
    }

    public TwainSource(TwainSourceManager manager, WinDef.HWND hwnd, boolean busy) {
        super(manager);
        this.busy = busy;
        this.state = 3;
        this.hwnd = hwnd;
        this.userCancelled = false;
        this.transferFactory = new TwainDefaultTransferFactory();
    }

    public Win32Twain.TW_IDENTITY getIdentity() {
        return identity;
    }

    public boolean isBusy() {
        return busy;
    }

    protected void setBusy(boolean b) {
        busy = b;
        Twain.signalStateChange(this);
    }

    public int getState() {
        return state;
    }

    public void setState(int s) {
        state = s;
        Twain.signalStateChange(this);
    }

    public boolean getCancel() {
        return userCancelled;
    }

    public void setCancel(boolean c) {
        userCancelled = c;
    }

    protected void checkState(int state) throws TwainException {
        if (this.state == state) {
            return;
        }
        throw new TwainException(getClass().getName() + ".checkState: Source not in state " + state + " but in state " + this.state + ".");
    }

    private boolean isTwain20() {
        return false;
//        return (identity.SupportedGroups & 0x40000000) != 0;
    }

    @Override
    public void open() throws TwainException {
        super.open();
        if (isTwain20()) {
            Win32Twain.TW_CALLBACK cb = new Win32Twain.TW_CALLBACK();
            cb.Message = 0;
            cb.Proc = new TwainCallback();

            call(Twain.DG_CONTROL, (short) 0x12, (short) 0x902, cb);
        }
    }

    protected int getConditionCode() throws TwainException {
        Win32Twain.TW_FIX32 status = new Win32Twain.TW_FIX32();
        int rc = Twain.callSource(identity, Twain.DG_CONTROL, Twain.DAT_STATUS, Twain.MSG_GET, status);
        if (rc != Twain.TWRC_SUCCESS) {
            throw new TwainException("Cannot retrive twain source's status. RC = " + rc);
        }
//        System.out.println(status.Whole + " " + status.Frac);
        return status.Whole;
    }

    public void call(short dg, short dat, short msg, Object data) throws TwainCheckStatusException, TwainCancelException, TwainNotDSException, TwainTransferDoneException, TwainException {
        int rc = Twain.callSource(identity, dg, dat, msg, data);
        switch (rc) {
            case Twain.TWRC_SUCCESS:
                return;
            case Twain.TWRC_FAILURE:
                int cc = getConditionCode();
                if (cc == 4) {
                } else {
                    throw new TwainException(getClass().getName() + ".call error: " + TwainSourceManager.INFO[cc]);
                }
            case Twain.TWRC_CHECKSTATUS:
                throw new TwainCheckStatusException();
            case Twain.TWRC_CANCEL:
                throw new TwainCancelException();
            case Twain.TWRC_DSEVENT:
                return;
            case Twain.TWRC_NOTDSEVENT:
                throw new TwainNotDSException();
            case Twain.TWRC_XFERDONE:
                throw new TwainTransferDoneException();
            case Twain.TWRC_ENDOFLIST:
                throw new TwainEndOfListException();
            case Twain.TWRC_INFONOTSUPPORTED:
                throw new TwainInfNotSupportedException();
            case Twain.TWRC_DATANOTAVAILABLE:
                throw new TwainDataNotAvailableException();
            default:
                throw new TwainException("Failed to call source. RC = " + rc);
        }
    }

    public TwainCapability[] getCapabilities() throws TwainException {
        return TwainCapability.getCapabilities(this);
    }

    public TwainCapability getCapability(int cap) throws TwainException {              // use only in state 4
        return new TwainCapability(this, cap);
    }

    public TwainCapability getCapability(int cap, int mode) throws TwainException {
        return new TwainCapability(this, cap, mode);
    }

    public TwainTransferFactory getTransferFactory() {
        return transferFactory;
    }

    public void setTransferFactory(TwainTransferFactory transferFactory) {
        if (transferFactory == null) {
            throw new IllegalArgumentException(getClass().getName() + ".setTransferFactory\n\tTwain transfer factory cannot be null.");
        }
        this.transferFactory = transferFactory;
    }

    public void setShowUI(boolean enable) {
        showUI = (enable) ? 1 : 0;
    }

    public boolean isModalUI() {
        return (modalUI == 1);
    }

    public void setCapability(int cap, boolean v) throws TwainException {
        TwainCapability tc = getCapability(cap, Twain.MSG_GETCURRENT);
        if (tc.booleanValue() != v) {
            tc.setCurrentValue(v);
            if (getCapability(cap).booleanValue() != v) {
                throw new TwainException(getClass().getName() + ".setCapability:\n\tCannot set capability " + cap + " to " + v);
            }
        }
    }

    public void setCapability(int cap, int v) throws TwainException {
        TwainCapability tc = getCapability(cap, Twain.MSG_GETCURRENT);
        if (tc.intValue() != v) {
            tc.setCurrentValue(v);
            if (getCapability(cap).intValue() != v) {
                throw new TwainException(getClass().getName() + ".setCapability:\n\tCannot set capability " + cap + " to " + v);
            }
        }
    }

    public void setCapability(int cap, double v) throws TwainException {
        TwainCapability tc = getCapability(cap, Twain.MSG_GETCURRENT);
        if (tc.doubleValue() != v) {
            tc.setCurrentValue(v);
            if (getCapability(cap).doubleValue() != v) {
                throw new TwainException(getClass().getName() + ".setCapability:\n\tCannot set capability " + cap + " to " + v);
            }
        }
    }

    public boolean isUIControllable() {
        try {
            return getCapability(Twain.CAP_UICONTROLLABLE).booleanValue();
        } catch (Exception e) {
            Twain.signalException(getClass().getName() + ".isUIControllable:\n\t" + e);
            return false;
        }
    }

    public boolean isDeviceOnline() {
        try {
            return getCapability(Twain.CAP_DEVICEONLINE).booleanValue();
        } catch (Exception e) {
            Twain.signalException(getClass().getName() + ".isOnline:\n\t" + e);
            return true;
        }
    }

    public void setShowUserInterface(boolean show) throws TwainException {
        setShowUI(show);
    }

    public void setShowProgressBar(boolean show) throws TwainException {
        setCapability(Twain.CAP_INDICATORS, show);
    }

    public void setResolution(double dpi) throws TwainException {
        setCapability(Twain.ICAP_UNITS, Twain.TWUN_INCHES);
        setCapability(Twain.ICAP_XRESOLUTION, dpi);
        setCapability(Twain.ICAP_YRESOLUTION, dpi);
    }

    public void setRegionOfInterest(int x, int y, int w, int h) throws TwainException {
        if ((x == -1) && (y == -1) && (w == -1) && (h == -1)) {
            new TwainImageLayout(this).reset();
        } else {
            setCapability(Twain.ICAP_UNITS, Twain.TWUN_PIXELS);
            TwainImageLayout til = new TwainImageLayout(this);
            til.get();
            til.setLeft(x);
            til.setTop(y);
            til.setRight(x + w);
            til.setBottom(y + h);
            til.set();
        }
    }

    public void setRegionOfInterest(double x, double y, double w, double h) throws TwainException {
        if ((x == -1) && (y == -1) && (w == -1) && (h == -1)) {
            new TwainImageLayout(this).reset();
        } else {
            setCapability(Twain.ICAP_UNITS, Twain.TWUN_CENTIMETERS);
            TwainImageLayout til = new TwainImageLayout(this);
            til.get();
            til.setLeft(x / 10.0);
            til.setTop(y / 10.0);
            til.setRight((x + w) / 10.0);
            til.setBottom((y + h) / 10.0);
            til.set();
        }
    }

    public void select(String name) throws TwainException {
        checkState(3);
        TwainSourceManager manager = Twain.getSourceManager();
        try {
            TwainIdentity device = new TwainIdentity(manager);
            device.getFirst();
            while (true) {
                if (device.getProductName().equals(name)) {
                    device.identity.copyTo(identity);
                    break;
                }
                device.getNext();
            }
        } catch (TwainEndOfListException treeol) {
            throw new TwainException(getClass().getName() + ".select(String name)\n\tCannot find twain data source: '" + name + "'");
        }
    }

    void enable() throws TwainException {
        checkState(4);
        Twain.negotiateCapabilities(this);
        if (getState() < 4) {
            return;
        }

        int xfer = new TwainCapability.XferMech(this).intValue();
        if (xfer == Twain.TWSX_NATIVE) {
        } else if (xfer == Twain.TWSX_FILE) {
            try {
                iff = getCapability(Twain.ICAP_IMAGEFILEFORMAT).intValue();
            } catch (Exception e) {
                iff = Twain.TWFF_BMP;
            }
        }

//        if (isTwain20()) {
//            twSemaphore = new Semaphore(0, true);
//            twHaveImage = false;
//        }

        modalUI = 0;
        Win32Twain.TW_USERINTERFACE ui = new Win32Twain.TW_USERINTERFACE();
        ui.ShowUI = showUI != 0;
        ui.hParent = hwnd;

        try {
            call(Twain.DG_CONTROL, Twain.DAT_USERINTERFACE, Twain.MSG_ENABLEDS, ui);
            modalUI = ui.ModalUI ? 1 : 0;
            setState(5);
        } catch (TwainCheckStatusException trecs) {
            setState(5);
        } catch (TwainCancelException trec) {
            disable();
            close();
        }

//        if (isTwain20()) {
//            try {
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            twSemaphore.tryAcquire(5, TimeUnit.MINUTES);
//                            twSemaphore.release();
//
//                            if (twHaveImage) {
//                                transferImage();
//                            } else {
////                                throw new TwainException("Scan timeout");
//                            }
//
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                throw new TwainException(e);
//            }
//        }
    }

    private void transfer(TwainTransfer tt) throws TwainException {
        try {
            byte[] pendingXfers = new byte[6];
            do {
                setState(6);
                TwainUtils.setINT16(pendingXfers, 0, 0);
                try {
                    tt.setCancel(userCancelled);
                    tt.initiate();
                } catch (TwainTransferDoneException tretd) {
                    setState(7);
                    tt.finish();
                    call(Twain.DG_CONTROL, Twain.DAT_PENDINGXFERS, Twain.MSG_ENDXFER, pendingXfers);
                    if (TwainUtils.getINT16(pendingXfers, 0) == 0) {
                        setState(5);
                    }
                } catch (TwainUserCancelException tuce) {
                    call(Twain.DG_CONTROL, Twain.DAT_PENDINGXFERS, Twain.MSG_RESET, pendingXfers);
                    setState(5);
                } catch (TwainCancelException trec) {
                    tt.cancel();

                    call(Twain.DG_CONTROL, Twain.DAT_PENDINGXFERS, Twain.MSG_ENDXFER, pendingXfers);
                    if (TwainUtils.getINT16(pendingXfers, 0) > 0) {
                        call(Twain.DG_CONTROL, Twain.DAT_PENDINGXFERS, Twain.MSG_RESET, pendingXfers);
                    }
                    setState(5);
                } catch (TwainException tfe) {
                    Twain.signalException(getClass().getName() + ".transfer:\n\t" + tfe);

                    call(Twain.DG_CONTROL, Twain.DAT_PENDINGXFERS, Twain.MSG_ENDXFER, pendingXfers);
                    if (TwainUtils.getINT16(pendingXfers, 0) > 0) {
                        call(Twain.DG_CONTROL, Twain.DAT_PENDINGXFERS, Twain.MSG_RESET, pendingXfers);
                    }
                    setState(5);
                } finally {
                    tt.cleanup();
                }
            } while (TwainUtils.getINT16(pendingXfers, 0) != 0);
        } finally {
            if (userCancelled || (showUI == 0)) {
                userCancelled = false;
                disable();
                close();
            }
        }
    }

    void transferImage() throws TwainException {
        switch (getXferMech()) {
            case Twain.TWSX_NATIVE:
                transfer(transferFactory.createNativeTransfer(this));
                break;
            case Twain.TWSX_FILE:
                transfer(transferFactory.createFileTransfer(this));
                break;
            case Twain.TWSX_MEMORY:
                transfer(transferFactory.createMemoryTransfer(this));
                break;
            default:
                break;
        }
    }

    void disable() throws TwainException {
        if (state < 5) {
            return;
        }

        byte[] gui = new byte[8];
        TwainUtils.setINT16(gui, 0, -1);
        TwainUtils.setINT16(gui, 2, 0);
        TwainUtils.setINT32(gui, 4, (int) Pointer.nativeValue(hwnd.getPointer()));

        call(Twain.DG_CONTROL, Twain.DAT_USERINTERFACE, Twain.MSG_DISABLEDS, gui);
        setState(4);
    }

    @Override
    public void close() throws TwainException {
        if (state != 4) {
            return;
        }

//        if (isTwain20()) {
//            Win32Twain.TW_CALLBACK cb = new Win32Twain.TW_CALLBACK();
//            cb.Message = 0;
//            cb.Proc = null;//new TwainCallback();
//
//            call(Twain.DG_CONTROL, (short) 0x12, (short) 0x902, cb);
//        }
        super.close();
        busy = false;
        setState(3);
    }

    int handleGetMessage(Pointer msgPtr) throws TwainException {
        if (state < 5) {
            return Twain.TWRC_NOTDSEVENT;
        }
        try {
            Win32Twain.TW_EVENT event = new Win32Twain.TW_EVENT();
            event.pEvent = msgPtr;
            event.TWMessage = 0;

            call(Twain.DG_CONTROL, Twain.DAT_EVENT, Twain.MSG_PROCESSEVENT, event);

            int message = event.TWMessage;
            switch (message) {
                case Twain.MSG_XFERREADY:
                    transferImage();
                    break;
                case Twain.MSG_CLOSEDSOK:
                case Twain.MSG_CLOSEDSREQ:
                    disable();
                    close();
                    break;
                case Twain.MSG_DEVICEEVENT:
                case Twain.MSG_NULL:
                default:
                    break;
            }
            return Twain.TWRC_DSEVENT;
        } catch (TwainNotDSException trendse) {
            return Twain.TWRC_NOTDSEVENT;
        }
    }

    public int getXferMech() throws TwainException {
        return new TwainCapability.XferMech(this).intValue();
    }

    public void setXferMech(int mech) {
        try {
            switch (mech) {
                case Twain.TWSX_NATIVE:
                case Twain.TWSX_FILE:
                case Twain.TWSX_MEMORY:
                    break;
                default:
                    mech = Twain.TWSX_NATIVE;
                    break;
            }
            TwainCapability tc;
            tc = getCapability(Twain.ICAP_XFERMECH, Twain.MSG_GETCURRENT);
            if (tc.intValue() != mech) {
                tc.setCurrentValue(mech);
                if (getCapability(Twain.ICAP_XFERMECH).intValue() != mech) {
                    Twain.signalException(getClass().getName() + ".setXferMech:\n\tCannot change transfer mechanism to mode=" + mech);
                }
            }
        } catch (TwainException e) {
            Twain.signalException(getClass().getName() + ".setXferMech:\n\t" + e);
        }
    }

    public int getImageFileFormat() {
        return iff;
    }

    public void setImageFileFormat(int iff) {
        try {
            TwainCapability tc;
            switch (iff) {
                case Twain.TWFF_TIFF:
                case Twain.TWFF_BMP:
                case Twain.TWFF_JFIF:
                case Twain.TWFF_TIFFMULTI:
                case Twain.TWFF_PNG:
                    break;
                default:
                    iff = Twain.TWFF_BMP;
                    break;
            }
            tc = getCapability(Twain.ICAP_IMAGEFILEFORMAT, Twain.MSG_GETCURRENT);
            if (tc.intValue() != iff) {
                tc.setCurrentValue(iff);
                if (getCapability(Twain.ICAP_IMAGEFILEFORMAT).intValue() != iff) {
                    Twain.signalException(getClass().getName() + ".setImageFileFormat:\n\tCannot change file format to format=" + iff);
                }
            }
        } catch (Exception e) {
            Twain.signalException(getClass().getName() + ".setImageFileFormat:\n\t" + e);
        }
    }

}
