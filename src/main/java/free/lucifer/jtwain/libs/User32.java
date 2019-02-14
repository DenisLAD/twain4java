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
package free.lucifer.jtwain.libs;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author lucifer
 */
public interface User32 extends Library {

    boolean MessageBeep(int uType);

    WinDef.HWND CreateWindowExA(int styleEx, String className, String windowName, int style, int x, int y, int width, int height, int hndParent, int hndMenu, int hndInst, Object parm);

    boolean SetWindowPos(WinDef.HWND hWnd, int hWndInsAfter, int x, int y, int cx, int cy, short uFlgs);

    boolean SetWindowPos(WinDef.HWND hWnd, WinDef.HWND hWndInsertAfter, int X, int Y, int cx, int cy, int uFlags);

    WinDef.ATOM RegisterClassEx(WinUser.WNDCLASSEX lpwcx);

    int DestroyWindow(int hdl);

    int DestroyWindow(WinDef.HWND hdl);

    boolean GetMessageA(MSG lpMsg, int hWnd, int wMsgFilterMin, int wMsgFilterMax);

    boolean TranslateMessage(MSG lpMsg);

    int DispatchMessageA(MSG lpMsg);

    void PostMessage(WinDef.HWND hWnd, int msg, WinDef.WPARAM wParam, WinDef.LPARAM lParam);

    public static class POINT extends Structure {

        public int x;
        public int y;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"x", "y"});
        }
    }

    public static class MSG extends Structure {

        public int hwnd;
        public int message;
        public short wParm;
        public int lParm;
        public int time;
        public POINT pt;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"hwnd", "message", "wParm", "lParm", "time", "pt"});
        }
    }
    public static HANDLE INVALID_HANDLE_VALUE = new HANDLE(Pointer.createConstant(Pointer.SIZE == 8 ? -1 : 0xFFFFFFFFL));

    public static class HANDLE extends PointerType {

        private boolean immutable;

        public HANDLE() {
        }

        public HANDLE(Pointer p) {
            setPointer(p);
            immutable = true;
        }

        @Override
        public Object fromNative(Object nativeValue, FromNativeContext context) {
            Object o = super.fromNative(nativeValue, context);
            if (INVALID_HANDLE_VALUE.equals(o)) {
                return INVALID_HANDLE_VALUE;
            }
            return o;
        }

        @Override
        public void setPointer(Pointer p) {
            if (immutable) {
                throw new UnsupportedOperationException("immutable reference");
            }

            super.setPointer(p);
        }

    }

}
