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
import free.lucifer.jtwain.exceptions.TwainUserCancelException;

/**
 *
 * @author lucifer
 */
public class TwainTransfer {

    protected TwainSource source;
    protected boolean isCancelled;

    public TwainTransfer(TwainSource source) {
        this.source = source;
        isCancelled = false;
    }

    public void initiate() throws TwainException {
        commitCancel();
    }

    public void setCancel(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    protected void commitCancel() throws TwainException {
        if (isCancelled && (source.getState() == Twain.STATE_TRANSFERREADY)) {
            throw new TwainUserCancelException();
        }
    }

    public void finish() throws TwainException {
    }

    public void cancel() throws TwainException {
    }

    public void cleanup() throws TwainException {
    }
}
