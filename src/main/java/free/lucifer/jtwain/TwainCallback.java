/*
 * Copyright 2018 lucifer.
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

import free.lucifer.jtwain.libs.Win32Twain;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

/**
 *
 * @author lucifer
 */
public class TwainCallback implements StdCallLibrary.StdCallCallback {

    public int callback(Win32Twain.TW_IDENTITY orign, Win32Twain.TW_IDENTITY dest, int dg, short dat, short msg, Pointer data) {
//        System.out.println("MSG!!!!!!!!!!!!!!!!!!!!!!!!!!!" + msg);
        try {
            TwainIdentity orig = new TwainIdentity(Twain.sourceManager, orign);
            TwainSource source = Twain.sourceManager.getSource();

            if (orig.getId() != source.getId()) {
                return Twain.TWRC_FAILURE;
            }
            System.out.println(String.format("CALLBACK ----------------------> DG:%d, DAT:%d, MSG:%d", dg, dat, msg));
            return source.callback(dg, dat, msg, data);
        } catch (Throwable e) {
            Twain.signalException(e.getMessage());
            e.printStackTrace();
            return Twain.TWRC_FAILURE;
        }
    }
}
