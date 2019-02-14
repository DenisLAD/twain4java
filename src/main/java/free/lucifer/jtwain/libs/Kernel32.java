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

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author lucifer
 */
public interface Kernel32 extends Library {

    boolean Beep(int freq, int duration);

    int GetCurrentDirectoryA(int bufLen, byte buffer[]);

    boolean SetCurrentDirectoryA(String dir);

    int GetLastError();

    int GetCurrentProcess();

    int GetCurrentProcessId();

    int GetTickCount();

    int LoadLibraryA(String lib);

    Pointer GlobalLock(Pointer hdl);

    boolean GlobalUnlock(Pointer hdl);

    Pointer GlobalLock(int hdl);

    boolean GlobalUnlock(int hdl);

    Pointer GlobalAlloc(int flags, int size);

    int GlobalFree(Pointer hdl);

    int GlobalFree(int hdl);

    int GetLogicalDrives();

    int GetLogicalDriveStringsA(int bufLen, byte buf[]);

    boolean GetVolumeInformationA(String lpRootPathName, byte lpVolumeNameBuffer[], int nVolumeNameSize, int lpVolumeSerialNumber[], int lpMaximumComponentLength[], int lpFileSystemFlags[], byte lpFileSystemNameBuffer[], int nFileSystemNameSize);

    int GetDriveTypeA(String drive);

    void GetSystemTime(SYSTEMTIME st);

    void GetLocalTime(SYSTEMTIME st);

    void GetComputerName();

    boolean GetProcessTimes(int processHdl, FILETIME creation, FILETIME exit, FILETIME kernel, FILETIME user);

    boolean GetSystemTimes(FILETIME idle, FILETIME kernel, FILETIME user);

    int CreateFileA(String file, int access, int mode, SECURITY_ATTRIBUTES secAttrs, int disposition, int flagsAndAttribs, int hdlTemplate);

    boolean DeviceIoControl(int hdl, int opCode, byte inBuf[], int inBufSize, byte outBuf[], int outBufSize, int bytesReturned[], OVERLAPPED ol);

    boolean GetSystemPowerStatus(SYSTEM_POWER_STATUS sps);

    boolean GetDiskFreeSpaceA(String s, IntByReference r1, IntByReference r2, IntByReference r3, IntByReference r4);

    boolean GetDiskFreeSpaceEx(String s, IntByReference r1, IntByReference r2, IntByReference r3);

    public static class SYSTEMTIME extends Structure {

        public short wYear;
        public short wMonth;
        public short wDayOfWeek;
        public short wDay;
        public short wHour;
        public short wMinute;
        public short wSecond;
        public short wMilliseconds;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"wYear", "wMonth", "wDayOfWeek", "wDay", "wHour", "wMinute", "wSecond", "wMilliseconds"});
        }
    }

    public static class SYSTEM_POWER_STATUS extends Structure {

        public byte ACLineStatus;
        public byte BatteryFlag;
        public byte BatteryLifePercent;
        public byte Reserved1;
        public int BatteryLifeTime;
        public int BatteryFullLifeTime;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"ACLineStatus", "BatteryFlag", "BatteryLifePercent", "Reserved1", "BatteryLifeTime", "BatteryFullLifeTime"});
        }
    }

    public static class OVERLAPPED extends Structure {

        int Internal;
        int InternalHigh;
        int Offset;
        int OffsetHigh;
        int hEvent;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"Internal", "InternalHigh", "Offset", "OffsetHigh", "hEvent"});
        }
    }

    public static class SECURITY_ATTRIBUTES extends Structure {

        int nLength;
        Pointer lpSecurityDescriptor;
        boolean bInheritHandle;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"nLength", "lpSecurityDescriptor", "bInheritHandle"});
        }
    }

    public static class FILETIME extends Structure {

        public int dwLowDateTime;
        public int dwHighDateTime;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"dwLowDateTime", "dwHighDateTime"});
        }
    }
}
