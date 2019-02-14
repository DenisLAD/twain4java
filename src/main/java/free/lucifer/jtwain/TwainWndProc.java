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

import static free.lucifer.jtwain.Twain.callMapper;
import static free.lucifer.jtwain.Twain.execute;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author lucifer
 */
public class TwainWndProc implements WinUser.WindowProc {

    private ExecutorService exec = Executors.newCachedThreadPool();

    @Override
    public WinDef.LRESULT callback(WinDef.HWND hwnd, int uMsg, final WinDef.WPARAM wParam, WinDef.LPARAM lParam) {
        switch (uMsg) {
            case WinUser.WM_CLOSE:
            case WinUser.WM_DESTROY:
                if (hwnd.equals(Twain.hwnd)) {
                    User32.INSTANCE.DestroyWindow(hwnd);
                    hwnd = null;
                    Twain.g_AppID = null;
                    Twain.done();
                }
                break;
            case WinUser.WM_USER: {
                final Object o = callMapper.get(lParam.intValue());
                if (o != null) {
//                    exec.submit(new Runnable() {
//                        @Override
//                        public void run() {
                    execute(o, wParam.intValue());
//                        }
//                    });

                }

            }
            break;
            default:
                return User32.INSTANCE.DefWindowProc(hwnd, uMsg, wParam, lParam);
        }
        return new WinDef.LRESULT(0);
    }
}
