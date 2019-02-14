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
public class TwainNativeTransfer extends TwainTransfer {

    private final byte[] imageHandle = new byte[8];

    public TwainNativeTransfer(TwainSource source) {
        super(source);
    }

    @Override
    public void initiate() throws TwainException {
        super.initiate();
        source.call(Twain.DG_IMAGE, Twain.DAT_IMAGENATIVEXFER, Twain.MSG_GET, imageHandle);
    }

    @Override
    public void finish() throws TwainException {
        int handle = TwainUtils.getINT32(imageHandle, 0);
        Twain.transferNativeImage(handle);
    }

}
