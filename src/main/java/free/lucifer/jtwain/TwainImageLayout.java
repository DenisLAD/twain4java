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

import static free.lucifer.jtwain.Twain.DAT_IMAGELAYOUT;
import static free.lucifer.jtwain.Twain.DG_IMAGE;
import static free.lucifer.jtwain.Twain.MSG_GET;
import static free.lucifer.jtwain.Twain.MSG_GETDEFAULT;
import static free.lucifer.jtwain.Twain.MSG_RESET;
import static free.lucifer.jtwain.Twain.MSG_SET;
import free.lucifer.jtwain.exceptions.TwainException;
import free.lucifer.jtwain.utils.TwainUtils;

/**
 *
 * @author lucifer
 */
public class TwainImageLayout {

    TwainSource source;
    byte[] buf = new byte[28];

    public TwainImageLayout(TwainSource source) {
        this.source = source;
    }

    public void get() throws TwainException {
        source.call(DG_IMAGE, DAT_IMAGELAYOUT, MSG_GET, buf);
    }

    public void getDefault() throws TwainException {
        source.call(DG_IMAGE, DAT_IMAGELAYOUT, MSG_GETDEFAULT, buf);
    }

    public void set() throws TwainException {
        source.call(DG_IMAGE, DAT_IMAGELAYOUT, MSG_SET, buf);
    }

    public void reset() throws TwainException {
        source.call(DG_IMAGE, DAT_IMAGELAYOUT, MSG_RESET, buf);
    }

    public double getLeft() {
        return TwainUtils.getFIX32(buf, 0);
    }

    public void setLeft(double v) {
        TwainUtils.setFIX32(buf, 0, v);
    }

    public double getTop() {
        return TwainUtils.getFIX32(buf, 4);
    }

    public void setTop(double v) {
        TwainUtils.setFIX32(buf, 4, v);
    }

    public double getRight() {
        return TwainUtils.getFIX32(buf, 8);
    }

    public void setRight(double v) {
        TwainUtils.setFIX32(buf, 8, v);
    }

    public double getBottom() {
        return TwainUtils.getFIX32(buf, 12);
    }

    public void setBottom(double v) {
        TwainUtils.setFIX32(buf, 12, v);
    }

    public int getDocumentNumber() {
        return TwainUtils.getINT32(buf, 16);
    }

    public void setDocumentNumber(int v) {
        TwainUtils.setINT32(buf, 16, v);
    }

    public int getPageNumber() {
        return TwainUtils.getINT32(buf, 20);
    }

    public void setPageNumber(int v) {
        TwainUtils.setINT32(buf, 20, v);
    }

    public int getFrameNumber() {
        return TwainUtils.getINT32(buf, 24);
    }

    public void setFrameNumber(int v) {
        TwainUtils.setINT32(buf, 24, v);
    }

}
