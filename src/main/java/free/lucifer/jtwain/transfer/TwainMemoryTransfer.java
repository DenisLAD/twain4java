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
package free.lucifer.jtwain.transfer;

import free.lucifer.jtwain.Twain;
import free.lucifer.jtwain.TwainSource;
import free.lucifer.jtwain.exceptions.TwainException;
import free.lucifer.jtwain.utils.TwainUtils;

/**
 *
 * @author lucifer
 */
public class TwainMemoryTransfer extends TwainTransfer {

    private final byte[] imx = new byte[48];
    private Info info;

    protected int minBufSize = -1;
    protected int maxBufSize = -1;
    protected int preferredSize = -1;

    public TwainMemoryTransfer(TwainSource source) {
        super(source);
    }

    protected void retrieveBufferSizes() throws TwainException {
        byte[] setup = new byte[12];
        TwainUtils.setINT32(setup, 0, minBufSize);
        TwainUtils.setINT32(setup, 4, maxBufSize);
        TwainUtils.setINT32(setup, 8, preferredSize);
        source.call(Twain.DG_CONTROL, Twain.DAT_SETUPMEMXFER, Twain.MSG_GET, setup);
        minBufSize = TwainUtils.getINT32(setup, 0);
        maxBufSize = TwainUtils.getINT32(setup, 4);
        preferredSize = TwainUtils.getINT32(setup, 8);
    }

    @Override
    public void initiate() throws TwainException {
        super.initiate();
        retrieveBufferSizes();
        Twain.nnew(imx, preferredSize);
        byte[] buf = new byte[preferredSize];
        info = new Info(imx, buf);
        while (true) {
            source.call(Twain.DG_IMAGE, Twain.DAT_IMAGEMEMXFER, Twain.MSG_GET, imx);
            int bytesWritten = TwainUtils.getINT32(imx, 22);
            int bytesCopied = Twain.ncopy(buf, imx, bytesWritten);
            if (bytesCopied == bytesWritten) {
                Twain.transferMemoryBuffer(info);
            }
        }
    }

    @Override
    public void finish() throws TwainException {
        int bytesWritten = TwainUtils.getINT32(imx, 22);
        int bytesCopied = Twain.ncopy(info.getBuffer(), imx, bytesWritten);
        if (bytesCopied == bytesWritten) {
            Twain.transferMemoryBuffer(info);
        }
    }

    @Override
    public void cleanup() throws TwainException {
        Twain.ndelete(imx);
    }

    public static class Info {

        private byte[] imx;
        private byte[] buf;
        private int len;

        Info(byte[] imx, byte[] buf) {
            this.imx = imx;
            this.buf = buf;
        }

        public byte[] getBuffer() {
            return buf;
        }

        public int getCompression() {
            return TwainUtils.getINT16(imx, 0);
        }

        public int getBytesPerRow() {
            return TwainUtils.getINT32(imx, 2);
        }

        public int getWidth() {
            return TwainUtils.getINT32(imx, 6);
        }

        public int getHeight() {
            return TwainUtils.getINT32(imx, 10);
        }

        public int getTop() {
            return TwainUtils.getINT32(imx, 14);
        }

        public int getLeft() {
            return TwainUtils.getINT32(imx, 18);
        }

        public int getLength() {
            return TwainUtils.getINT32(imx, 22);
        }

    }
}
