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

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import free.lucifer.jtwain.exceptions.TwainException;
import free.lucifer.jtwain.libs.Kernel32;
import free.lucifer.jtwain.libs.Win32Twain;
import free.lucifer.jtwain.libs.Win32Twain.TW_IDENTITY;
import free.lucifer.jtwain.transfer.TwainMemoryTransfer;
import free.lucifer.jtwain.variable.TwainContainer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author lucifer
 */
public class Twain {

    // Based on http://www.twain.org/wp-content/uploads/2017/04/TWAIN.H.h
    public static final String[] ImageFileFormatExts = {
            ".tiff", ".pict", ".bmp", ".xbm", ".jpeg", ".fpx", ".tiff", ".png", ".spiff", ".exif"
    };
    static public final int STATE_UNDEFINED = 0;
    static public final int STATE_PRESESSION = 1;
    static public final int STATE_SRCMNGLOADED = 2;
    static public final int STATE_SRCMNGOPEN = 3;
    static public final int STATE_SRCOPEN = 4;
    static public final int STATE_SRCENABLED = 5;
    static public final int STATE_TRANSFERREADY = 6;
    static public final int STATE_TRANSFERRING = 7;

    public static final short MSG_NULL = 0;
    public static final short MSG_GET = 0x0001;
    public static final short MSG_GETCURRENT = 0x0002;
    public static final short MSG_GETDEFAULT = 0x0003;
    public static final short MSG_GETFIRST = 0x0004;
    public static final short MSG_GETNEXT = 0x0005;
    public static final short MSG_SET = 0x0006;
    public static final short MSG_RESET = 0x0007;
    public static final short MSG_QUERYSUPPORT = 0x0008;
    public static final short MSG_XFERREADY = 0x0101;
    public static final short MSG_CLOSEDSREQ = 0x0102;
    public static final short MSG_CLOSEDSOK = 0x0103;
    public static final short MSG_DEVICEEVENT = 0x0104;
    public static final short MSG_OPENDSM = 0x0301;
    public static final short MSG_CLOSEDSM = 0x0302;
    public static final short MSG_OPENDS = 0x0401;
    public static final short MSG_CLOSEDS = 0x0402;
    public static final short MSG_USERSELECT = 0x0403;
    public static final short MSG_DISABLEDS = 0x0501;
    public static final short MSG_ENABLEDS = 0x0502;
    public static final short MSG_ENABLEDSUIONLY = 0x0503;
    public static final short MSG_PROCESSEVENT = 0x0601;
    public static final short MSG_ENDXFER = 0x0701;
    public static final short MSG_CHANGEDIRECTORY = 0x0801;
    public static final short MSG_CREATEDIRECTORY = 0x0802;
    public static final short MSG_DELETE = 0x0803;
    public static final short MSG_FORMATMEDIA = 0x0804;
    public static final short MSG_GETCLOSE = 0x0805;
    public static final short MSG_GETFIRSTFILE = 0x0806;
    public static final short MSG_GETINFO = 0x0807;
    public static final short MSG_GETNEXTFILE = 0x0808;
    public static final short MSG_RENAME = 0x0809;
    public static final short MSG_COPY = 0x080A;
    public static final short MSG_AUTOMATICCAPTUREDIRECTORY = 0x080B;
    public static final short MSG_CUSTOMBASE = (short) 0x8000;

    public static final short DAT_NULL = 0x0000;
    public static final short DAT_CAPABILITY = 0x0001;
    public static final short DAT_EVENT = 0x0002;
    public static final short DAT_IDENTITY = 0x0003;
    public static final short DAT_PARENT = 0x0004;
    public static final short DAT_PENDINGXFERS = 0x0005;
    public static final short DAT_SETUPMEMXFER = 0x0006;
    public static final short DAT_SETUPFILEXFER = 0x0007;
    public static final short DAT_STATUS = 0x0008;
    public static final short DAT_USERINTERFACE = 0x0009;
    public static final short DAT_XFERGROUP = 0x000a;
    public static final short DAT_TWUNKIDENTITY = 0x000b;
    public static final short DAT_CUSTOMDSDATA = 0x000c;
    public static final short DAT_DEVICEEVENT = 0x000d;
    public static final short DAT_FILESYSTEM = 0x000e;
    public static final short DAT_PASSTHRU = 0x000f;
    public static final short DAT_IMAGEINFO = 0x0101;
    public static final short DAT_IMAGELAYOUT = 0x0102;
    public static final short DAT_IMAGEMEMXFER = 0x0103;
    public static final short DAT_IMAGENATIVEXFER = 0x0104;
    public static final short DAT_IMAGEFILEXFER = 0x0105;
    public static final short DAT_CIECOLOR = 0x0106;
    public static final short DAT_GRAYRESPONSE = 0x0107;
    public static final short DAT_RGBRESPONSE = 0x0108;
    public static final short DAT_JPEGCOMPRESSION = 0x0109;
    public static final short DAT_PALETTE8 = 0x010a;
    public static final short DAT_EXTIMAGEINFO = 0x010b;

    public static final short TWCP_NONE = 0;
    public static final short TWCP_PACKBITS = 1;
    public static final short TWCP_GROUP31D = 2;
    public static final short TWCP_GROUP31DEOL = 3;
    public static final short TWCP_GROUP32D = 4;
    public static final short TWCP_GROUP4 = 5;
    public static final short TWCP_JPEG = 6;
    public static final short TWCP_LZW = 7;
    public static final short TWCP_JBIG = 8;
    public static final short TWCP_PNG = 9;
    public static final short TWCP_RLE4 = 10;
    public static final short TWCP_RLE8 = 11;
    public static final short TWCP_BITFIELDS = 12;

    public static final short DG_CONTROL = 1;
    public static final short DG_IMAGE = 2;
    public static final short DG_AUDIO = 4;

    public static final short TWRC_SUCCESS = 0;
    public static final short TWRC_FAILURE = 1;
    public static final short TWRC_CHECKSTATUS = 2;
    public static final short TWRC_CANCEL = 3;
    public static final short TWRC_DSEVENT = 4;
    public static final short TWRC_NOTDSEVENT = 5;
    public static final short TWRC_XFERDONE = 6;
    public static final short TWRC_ENDOFLIST = 7;
    public static final short TWRC_INFONOTSUPPORTED = 8;
    public static final short TWRC_DATANOTAVAILABLE = 9;

    public static final short TWMF_APPOWNS = 0x0001;
    public static final short TWMF_DSMOWNS = 0x0002;
    public static final short TWMF_DSOWNS = 0x0004;
    public static final short TWMF_POINTER = 0x0008;
    public static final short TWMF_HANDLE = 0x0010;

    public static final int TWON_ARRAY = 3;
    public static final int TWON_ENUMERATION = 4;
    public static final int TWON_ONEVALUE = 5;
    public static final int TWON_RANGE = 6;

    public static final int TWTY_INT8 = 0x0000;
    public static final int TWTY_INT16 = 0x0001;
    public static final int TWTY_INT32 = 0x0002;

    public static final int TWTY_UINT8 = 0x0003;
    public static final int TWTY_UINT16 = 0x0004;
    public static final int TWTY_UINT32 = 0x0005;

    public static final int TWTY_BOOL = 0x0006;

    public static final int TWTY_FIX32 = 0x0007;

    public static final int TWTY_FRAME = 0x0008;

    public static final int TWTY_STR32 = 0x0009;
    public static final int TWTY_STR64 = 0x000a;
    public static final int TWTY_STR128 = 0x000b;
    public static final int TWTY_STR255 = 0x000c;
    public static final int TWTY_STR1024 = 0x000d;
    public static final int TWTY_UNI512 = 0x000e;

    public static final int CAP_CUSTOMBASE = 0x8000;

    public static final int CAP_XFERCOUNT = 0x0001;

    public static final int ICAP_COMPRESSION = 0x0100;
    public static final int ICAP_PIXELTYPE = 0x0101;
    public static final int ICAP_UNITS = 0x0102;
    public static final int ICAP_XFERMECH = 0x0103;

    public static final int CAP_AUTHOR = 0x01000;
    public static final int CAP_CAPTION = 0x01001;
    public static final int CAP_FEEDERENABLED = 0x01002;
    public static final int CAP_FEEDERLOADED = 0x01003;
    public static final int CAP_TIMEDATE = 0x01004;
    public static final int CAP_SUPPORTEDCAPS = 0x01005;
    public static final int CAP_EXTENDEDCAPS = 0x01006;
    public static final int CAP_AUTOFEED = 0x01007;
    public static final int CAP_CLEARPAGE = 0x01008;
    public static final int CAP_FEEDPAGE = 0x01009;
    public static final int CAP_REWINDPAGE = 0x0100a;
    public static final int CAP_INDICATORS = 0x0100b;
    public static final int CAP_SUPPORTEDCAPSEXT = 0x0100c;
    public static final int CAP_PAPERDETECTABLE = 0x0100d;
    public static final int CAP_UICONTROLLABLE = 0x0100e;
    public static final int CAP_DEVICEONLINE = 0x0100f;
    public static final int CAP_AUTOSCAN = 0x01010;
    public static final int CAP_THUMBNAILSENABLED = 0x01011;
    public static final int CAP_DUPLEX = 0x01012;
    public static final int CAP_DUPLEXENABLED = 0x01013;
    public static final int CAP_ENABLEDSUIONLY = 0x01014;
    public static final int CAP_CUSTOMDSDATA = 0x01015;
    public static final int CAP_ENDORSER = 0x01016;
    public static final int CAP_JOBCONTROL = 0x01017;
    public static final int CAP_ALARMS = 0x01018;
    public static final int CAP_ALARMVOLUME = 0x01019;
    public static final int CAP_AUTOMATICCAPTURE = 0x0101a;
    public static final int CAP_TIMEBEFOREFIRSTCAPTURE = 0x0101b;
    public static final int CAP_TIMEBETWEENCAPTURES = 0x0101c;
    public static final int CAP_CLEARBUFFERS = 0x0101d;
    public static final int CAP_MAXBATCHBUFFERS = 0x0101e;
    public static final int CAP_DEVICETIMEDATE = 0x0101f;
    public static final int CAP_POWERSUPPLY = 0x01020;
    public static final int CAP_CAMERAPREVIEWUI = 0x01021;
    public static final int CAP_DEVICEEVENT = 0x01022;
    public static final int CAP_PAGEMULTIPLEACQUIRE = 0x01023;
    public static final int CAP_SERIALNUMBER = 0x01024;
    public static final int CAP_FILESYSTEM = 0x01025;
    public static final int CAP_PRINTER = 0x01026;
    public static final int CAP_PRINTERENABLED = 0x01027;
    public static final int CAP_PRINTERINDEX = 0x01028;
    public static final int CAP_PRINTERMODE = 0x01029;
    public static final int CAP_PRINTERSTRING = 0x0102a;
    public static final int CAP_PRINTERSUFFIX = 0x0102b;
    public static final int CAP_LANGUAGE = 0x0102c;
    public static final int CAP_FEEDERALIGNMENT = 0x0102d;
    public static final int CAP_FEEDERORDER = 0x0102e;
    public static final int CAP_PAPERBINDING = 0x0102f;
    public static final int CAP_REACQUIREALLOWED = 0x01030;
    public static final int CAP_PASSTHRU = 0x01031;
    public static final int CAP_BATTERYMINUTES = 0x01032;
    public static final int CAP_BATTERYPERCENTAGE = 0x01033;
    public static final int CAP_POWERDOWNTIME = 0x01034;

    public static final int ICAP_AUTOBRIGHT = 0x01100;
    public static final int ICAP_BRIGHTNESS = 0x01101;
    public static final int ICAP_CONTRAST = 0x01103;
    public static final int ICAP_CUSTHALFTONE = 0x01104;
    public static final int ICAP_EXPOSURETIME = 0x01105;
    public static final int ICAP_FILTER = 0x01106;
    public static final int ICAP_FLASHUSED = 0x01107;
    public static final int ICAP_GAMMA = 0x01108;
    public static final int ICAP_HALFTONES = 0x01109;
    public static final int ICAP_HIGHLIGHT = 0x0110a;
    public static final int ICAP_IMAGEFILEFORMAT = 0x0110c;
    public static final int ICAP_LAMPSTATE = 0x0110d;
    public static final int ICAP_LIGHTSOURCE = 0x0110e;
    public static final int ICAP_ORIENTATION = 0x01110;
    public static final int ICAP_PHYSICALWIDTH = 0x01111;
    public static final int ICAP_PHYSICALHEIGHT = 0x01112;
    public static final int ICAP_SHADOW = 0x01113;
    public static final int ICAP_FRAMES = 0x01114;
    public static final int ICAP_XNATIVERESOLUTION = 0x01116;
    public static final int ICAP_YNATIVERESOLUTION = 0x01117;
    public static final int ICAP_XRESOLUTION = 0x01118;
    public static final int ICAP_YRESOLUTION = 0x01119;
    public static final int ICAP_MAXFRAMES = 0x0111a;
    public static final int ICAP_TILES = 0x0111b;
    public static final int ICAP_BITORDER = 0x0111c;
    public static final int ICAP_CCITTKFACTOR = 0x0111d;
    public static final int ICAP_LIGHTPATH = 0x0111e;
    public static final int ICAP_PIXELFLAVOR = 0x0111f;
    public static final int ICAP_PLANARCHUNKY = 0x01120;
    public static final int ICAP_ROTATION = 0x01121;
    public static final int ICAP_SUPPORTEDSIZES = 0x01122;
    public static final int ICAP_THRESHOLD = 0x01123;
    public static final int ICAP_XSCALING = 0x01124;
    public static final int ICAP_YSCALING = 0x01125;
    public static final int ICAP_BITORDERCODES = 0x01126;
    public static final int ICAP_PIXELFLAVORCODES = 0x01127;
    public static final int ICAP_JPEGPIXELTYPE = 0x01128;
    public static final int ICAP_TIMEFILL = 0x0112a;
    public static final int ICAP_BITDEPTH = 0x0112b;
    public static final int ICAP_BITDEPTHREDUCTION = 0x0112c;
    public static final int ICAP_UNDEFINEDIMAGESIZE = 0x0112d;
    public static final int ICAP_IMAGEDATASET = 0x0112e;
    public static final int ICAP_EXTIMAGEINFO = 0x0112f;
    public static final int ICAP_MINIMUMHEIGHT = 0x01130;
    public static final int ICAP_MINIMUMWIDTH = 0x01131;
    public static final int ICAP_AUTODISCARDBLANKPAGES = 0x01134;
    public static final int ICAP_FLIPROTATION = 0x01136;
    public static final int ICAP_BARCODEDETECTIONENABLED = 0x01137;
    public static final int ICAP_SUPPORTEDBARCODETYPES = 0x01138;
    public static final int ICAP_BARCODEMAXSEARCHPRIORITIES = 0x01139;
    public static final int ICAP_BARCODESEARCHPRIORITIES = 0x0113a;
    public static final int ICAP_BARCODESEARCHMODE = 0x0113b;
    public static final int ICAP_BARCODEMAXRETRIES = 0x0113c;
    public static final int ICAP_BARCODETIMEOUT = 0x0113d;
    public static final int ICAP_ZOOMFACTOR = 0x0113e;
    public static final int ICAP_PATCHCODEDETECTIONENABLED = 0x0113f;
    public static final int ICAP_SUPPORTEDPATCHCODETYPES = 0x01140;
    public static final int ICAP_PATCHCODEMAXSEARCHPRIORITIES = 0x01141;
    public static final int ICAP_PATCHCODESEARCHPRIORITIES = 0x01142;
    public static final int ICAP_PATCHCODESEARCHMODE = 0x01143;
    public static final int ICAP_PATCHCODEMAXRETRIES = 0x01144;
    public static final int ICAP_PATCHCODETIMEOUT = 0x01145;
    public static final int ICAP_FLASHUSED2 = 0x01146;
    public static final int ICAP_IMAGEFILTER = 0x01147;
    public static final int ICAP_NOISEFILTER = 0x01148;
    public static final int ICAP_OVERSCAN = 0x01149;
    public static final int ICAP_AUTOMATICBORDERDETECTION = 0x01150;
    public static final int ICAP_AUTOMATICDESKEW = 0x01151;
    public static final int ICAP_AUTOMATICROTATE = 0x01152;

    public static final int TWBO_LSBFIRST = 0;
    public static final int TWBO_MSBFIRST = 1;

    public static final int TWFF_TIFF = 0;
    public static final int TWFF_PICT = 1;
    public static final int TWFF_BMP = 2;
    public static final int TWFF_XBM = 3;
    public static final int TWFF_JFIF = 4;
    public static final int TWFF_FPX = 5;
    public static final int TWFF_TIFFMULTI = 6;
    public static final int TWFF_PNG = 7;
    public static final int TWFF_SPIFF = 8;
    public static final int TWFF_EXIF = 9;

    public static final int TWUN_INCHES = 0;
    public static final int TWUN_CENTIMETERS = 1;
    public static final int TWUN_PICAS = 2;
    public static final int TWUN_POINTS = 3;
    public static final int TWUN_TWIPS = 4;
    public static final int TWUN_PIXELS = 5;

    public static final int TWSX_NATIVE = 0;
    public static final int TWSX_FILE = 1;
    public static final int TWSX_MEMORY = 2;
    public static final int TWSX_FILE2 = 3;

    public static final int HWND_DESKTOP = 0x10014;
    public static final int WS_POPUPWINDOW = 0x80000000 | 0x00800000 | 0x00080000;
    public static final int CW_USEDEFAULT = 0x80000000;
    public static final int HWND_TOPMOST = -1;
    public static final int SWP_NOSIZE = 1;

    protected static Kernel32 kernel32;
    //    protected static User32 user32;
    protected static Win32Twain twain;
    protected static String libraryName;

    static TW_IDENTITY g_AppID;
    private static TW_IDENTITY c_AppID;
    static WinDef.HWND hwnd;
    private static WeakReference<TwainScanner> scanner;
    static TwainSourceManager sourceManager;
    private static Map<Integer, String> mapCapCodeToName;
    private static Map<String, Integer> mapCapNameToCode;

    public static Map<String, Integer> getMapCapNameToCode() {
        if (mapCapNameToCode != null) {
            return mapCapNameToCode;
        }

        Map<String, Integer> map = new HashMap<>();
        map.put("CAP_CUSTOMBASE", 0x8000);
        /* Base of custom capabilities */

        /* all data sources are REQUIRED to support these caps */
        map.put("CAP_XFERCOUNT", 0x0001);

        /* image data sources are REQUIRED to support these caps */
        map.put("ICAP_COMPRESSION", 0x0100);
        map.put("ICAP_PIXELTYPE", 0x0101);
        map.put("ICAP_UNITS", 0x0102);
        map.put("ICAP_XFERMECH", 0x0103);

        /* all data sources MAY support these caps */
        map.put("CAP_AUTHOR", 0x1000);
        map.put("CAP_CAPTION", 0x1001);
        map.put("CAP_FEEDERENABLED", 0x1002);
        map.put("CAP_FEEDERLOADED", 0x1003);
        map.put("CAP_TIMEDATE", 0x1004);
        map.put("CAP_SUPPORTEDCAPS", 0x1005);
        map.put("CAP_EXTENDEDCAPS", 0x1006);
        map.put("CAP_AUTOFEED", 0x1007);
        map.put("CAP_CLEARPAGE", 0x1008);
        map.put("CAP_FEEDPAGE", 0x1009);
        map.put("CAP_REWINDPAGE", 0x100a);
        map.put("CAP_INDICATORS", 0x100b);
        map.put("CAP_PAPERDETECTABLE", 0x100d);
        map.put("CAP_UICONTROLLABLE", 0x100e);
        map.put("CAP_DEVICEONLINE", 0x100f);
        map.put("CAP_AUTOSCAN", 0x1010);
        map.put("CAP_THUMBNAILSENABLED", 0x1011);
        map.put("CAP_DUPLEX", 0x1012);
        map.put("CAP_DUPLEXENABLED", 0x1013);
        map.put("CAP_ENABLEDSUIONLY", 0x1014);
        map.put("CAP_CUSTOMDSDATA", 0x1015);
        map.put("CAP_ENDORSER", 0x1016);
        map.put("CAP_JOBCONTROL", 0x1017);
        map.put("CAP_ALARMS", 0x1018);
        map.put("CAP_ALARMVOLUME", 0x1019);
        map.put("CAP_AUTOMATICCAPTURE", 0x101a);
        map.put("CAP_TIMEBEFOREFIRSTCAPTURE", 0x101b);
        map.put("CAP_TIMEBETWEENCAPTURES", 0x101c);
        map.put("CAP_CLEARBUFFERS", 0x101d);
        map.put("CAP_MAXBATCHBUFFERS", 0x101e);
        map.put("CAP_DEVICETIMEDATE", 0x101f);
        map.put("CAP_POWERSUPPLY", 0x1020);
        map.put("CAP_CAMERAPREVIEWUI", 0x1021);
        map.put("CAP_DEVICEEVENT", 0x1022);
        map.put("CAP_SERIALNUMBER", 0x1024);
        map.put("CAP_PRINTER", 0x1026);
        map.put("CAP_PRINTERENABLED", 0x1027);
        map.put("CAP_PRINTERINDEX", 0x1028);
        map.put("CAP_PRINTERMODE", 0x1029);
        map.put("CAP_PRINTERSTRING", 0x102a);
        map.put("CAP_PRINTERSUFFIX", 0x102b);
        map.put("CAP_LANGUAGE", 0x102c);
        map.put("CAP_FEEDERALIGNMENT", 0x102d);
        map.put("CAP_FEEDERORDER", 0x102e);
        map.put("CAP_REACQUIREALLOWED", 0x1030);
        map.put("CAP_BATTERYMINUTES", 0x1032);
        map.put("CAP_BATTERYPERCENTAGE", 0x1033);
        map.put("CAP_CAMERASIDE", 0x1034);
        map.put("CAP_SEGMENTED", 0x1035);
        map.put("CAP_CAMERAENABLED", 0x1036);
        map.put("CAP_CAMERAORDER", 0x1037);
        map.put("CAP_MICRENABLED", 0x1038);
        map.put("CAP_FEEDERPREP", 0x1039);
        map.put("CAP_FEEDERPOCKET", 0x103a);
        map.put("CAP_AUTOMATICSENSEMEDIUM", 0x103b);
        map.put("CAP_CUSTOMINTERFACEGUID", 0x103c);
        map.put("CAP_SUPPORTEDCAPSSEGMENTUNIQUE", 0x103d);
        map.put("CAP_SUPPORTEDDATS", 0x103e);
        map.put("CAP_DOUBLEFEEDDETECTION", 0x103f);
        map.put("CAP_DOUBLEFEEDDETECTIONLENGTH", 0x1040);
        map.put("CAP_DOUBLEFEEDDETECTIONSENSITIVITY", 0x1041);
        map.put("CAP_DOUBLEFEEDDETECTIONRESPONSE", 0x1042);
        map.put("CAP_PAPERHANDLING", 0x1043);
        map.put("CAP_INDICATORSMODE", 0x1044);
        map.put("CAP_PRINTERVERTICALOFFSET", 0x1045);
        map.put("CAP_POWERSAVETIME", 0x1046);
        map.put("CAP_PRINTERCHARROTATION", 0x1047);
        map.put("CAP_PRINTERFONTSTYLE", 0x1048);
        map.put("CAP_PRINTERINDEXLEADCHAR", 0x1049);
        map.put("CAP_PRINTERINDEXMAXVALUE", 0x104A);
        map.put("CAP_PRINTERINDEXNUMDIGITS", 0x104B);
        map.put("CAP_PRINTERINDEXSTEP", 0x104C);
        map.put("CAP_PRINTERINDEXTRIGGER", 0x104D);
        map.put("CAP_PRINTERSTRINGPREVIEW", 0x104E);

        /* image data sources MAY support these caps */
        map.put("ICAP_AUTOBRIGHT", 0x1100);
        map.put("ICAP_BRIGHTNESS", 0x1101);
        map.put("ICAP_CONTRAST", 0x1103);
        map.put("ICAP_CUSTHALFTONE", 0x1104);
        map.put("ICAP_EXPOSURETIME", 0x1105);
        map.put("ICAP_FILTER", 0x1106);
        map.put("ICAP_FLASHUSED", 0x1107);
        map.put("ICAP_GAMMA", 0x1108);
        map.put("ICAP_HALFTONES", 0x1109);
        map.put("ICAP_HIGHLIGHT", 0x110a);
        map.put("ICAP_IMAGEFILEFORMAT", 0x110c);
        map.put("ICAP_LAMPSTATE", 0x110d);
        map.put("ICAP_LIGHTSOURCE", 0x110e);
        map.put("ICAP_ORIENTATION", 0x1110);
        map.put("ICAP_PHYSICALWIDTH", 0x1111);
        map.put("ICAP_PHYSICALHEIGHT", 0x1112);
        map.put("ICAP_SHADOW", 0x1113);
        map.put("ICAP_FRAMES", 0x1114);
        map.put("ICAP_XNATIVERESOLUTION", 0x1116);
        map.put("ICAP_YNATIVERESOLUTION", 0x1117);
        map.put("ICAP_XRESOLUTION", 0x1118);
        map.put("ICAP_YRESOLUTION", 0x1119);
        map.put("ICAP_MAXFRAMES", 0x111a);
        map.put("ICAP_TILES", 0x111b);
        map.put("ICAP_BITORDER", 0x111c);
        map.put("ICAP_CCITTKFACTOR", 0x111d);
        map.put("ICAP_LIGHTPATH", 0x111e);
        map.put("ICAP_PIXELFLAVOR", 0x111f);
        map.put("ICAP_PLANARCHUNKY", 0x1120);
        map.put("ICAP_ROTATION", 0x1121);
        map.put("ICAP_SUPPORTEDSIZES", 0x1122);
        map.put("ICAP_THRESHOLD", 0x1123);
        map.put("ICAP_XSCALING", 0x1124);
        map.put("ICAP_YSCALING", 0x1125);
        map.put("ICAP_BITORDERCODES", 0x1126);
        map.put("ICAP_PIXELFLAVORCODES", 0x1127);
        map.put("ICAP_JPEGPIXELTYPE", 0x1128);
        map.put("ICAP_TIMEFILL", 0x112a);
        map.put("ICAP_BITDEPTH", 0x112b);
        map.put("ICAP_BITDEPTHREDUCTION", 0x112c);
        map.put("ICAP_UNDEFINEDIMAGESIZE", 0x112d);
        map.put("ICAP_IMAGEDATASET", 0x112e);
        map.put("ICAP_EXTIMAGEINFO", 0x112f);
        map.put("ICAP_MINIMUMHEIGHT", 0x1130);
        map.put("ICAP_MINIMUMWIDTH", 0x1131);
        map.put("ICAP_AUTODISCARDBLANKPAGES", 0x1134);
        map.put("ICAP_FLIPROTATION", 0x1136);
        map.put("ICAP_BARCODEDETECTIONENABLED", 0x1137);
        map.put("ICAP_SUPPORTEDBARCODETYPES", 0x1138);
        map.put("ICAP_BARCODEMAXSEARCHPRIORITIES", 0x1139);
        map.put("ICAP_BARCODESEARCHPRIORITIES", 0x113a);
        map.put("ICAP_BARCODESEARCHMODE", 0x113b);
        map.put("ICAP_BARCODEMAXRETRIES", 0x113c);
        map.put("ICAP_BARCODETIMEOUT", 0x113d);
        map.put("ICAP_ZOOMFACTOR", 0x113e);
        map.put("ICAP_PATCHCODEDETECTIONENABLED", 0x113f);
        map.put("ICAP_SUPPORTEDPATCHCODETYPES", 0x1140);
        map.put("ICAP_PATCHCODEMAXSEARCHPRIORITIES", 0x1141);
        map.put("ICAP_PATCHCODESEARCHPRIORITIES", 0x1142);
        map.put("ICAP_PATCHCODESEARCHMODE", 0x1143);
        map.put("ICAP_PATCHCODEMAXRETRIES", 0x1144);
        map.put("ICAP_PATCHCODETIMEOUT", 0x1145);
        map.put("ICAP_FLASHUSED2", 0x1146);
        map.put("ICAP_IMAGEFILTER", 0x1147);
        map.put("ICAP_NOISEFILTER", 0x1148);
        map.put("ICAP_OVERSCAN", 0x1149);
        map.put("ICAP_AUTOMATICBORDERDETECTION", 0x1150);
        map.put("ICAP_AUTOMATICDESKEW", 0x1151);
        map.put("ICAP_AUTOMATICROTATE", 0x1152);
        map.put("ICAP_JPEGQUALITY", 0x1153);
        map.put("ICAP_FEEDERTYPE", 0x1154);
        map.put("ICAP_ICCPROFILE", 0x1155);
        map.put("ICAP_AUTOSIZE", 0x1156);
        map.put("ICAP_AUTOMATICCROPUSESFRAME", 0x1157);
        map.put("ICAP_AUTOMATICLENGTHDETECTION", 0x1158);
        map.put("ICAP_AUTOMATICCOLORENABLED", 0x1159);
        map.put("ICAP_AUTOMATICCOLORNONCOLORPIXELTYPE", 0x115a);
        map.put("ICAP_COLORMANAGEMENTENABLED", 0x115b);
        map.put("ICAP_IMAGEMERGE", 0x115c);
        map.put("ICAP_IMAGEMERGEHEIGHTTHRESHOLD", 0x115d);
        map.put("ICAP_SUPPORTEDEXTIMAGEINFO", 0x115e);
        map.put("ICAP_FILMTYPE", 0x115f);
        map.put("ICAP_MIRROR", 0x1160);
        map.put("ICAP_JPEGSUBSAMPLING", 0x1161);

        map.put("CAP_SUPPORTEDCAPSEXT", 0x100c);
        map.put("CAP_PAGEMULTIPLEACQUIRE", 0x1023);
        map.put("CAP_PAPERBINDING", 0x102f);
        map.put("CAP_PASSTHRU", 0x1031);
        map.put("CAP_POWERDOWNTIME", 0x1034);

        mapCapNameToCode = map;
        return mapCapNameToCode;
    }

    public static Map<Integer, String> getMapCapCodeToName() {
        if (mapCapCodeToName != null) {
            return mapCapCodeToName;
        }

        Map<Integer, String> map = new HashMap<>();
        map.put(0x8000, "CAP_CUSTOMBASE");
        /* Base of custom capabilities */

        /* all data sources are REQUIRED to support these caps */
        map.put(0x0001, "CAP_XFERCOUNT");

        /* image data sources are REQUIRED to support these caps */
        map.put(0x0100, "ICAP_COMPRESSION");
        map.put(0x0101, "ICAP_PIXELTYPE");
        map.put(0x0102, "ICAP_UNITS");
        map.put(0x0103, "ICAP_XFERMECH");

        /* all data sources MAY support these caps */
        map.put(0x1000, "CAP_AUTHOR");
        map.put(0x1001, "CAP_CAPTION");
        map.put(0x1002, "CAP_FEEDERENABLED");
        map.put(0x1003, "CAP_FEEDERLOADED");
        map.put(0x1004, "CAP_TIMEDATE");
        map.put(0x1005, "CAP_SUPPORTEDCAPS");
        map.put(0x1006, "CAP_EXTENDEDCAPS");
        map.put(0x1007, "CAP_AUTOFEED");
        map.put(0x1008, "CAP_CLEARPAGE");
        map.put(0x1009, "CAP_FEEDPAGE");
        map.put(0x100a, "CAP_REWINDPAGE");
        map.put(0x100b, "CAP_INDICATORS");
        map.put(0x100d, "CAP_PAPERDETECTABLE");
        map.put(0x100e, "CAP_UICONTROLLABLE");
        map.put(0x100f, "CAP_DEVICEONLINE");
        map.put(0x1010, "CAP_AUTOSCAN");
        map.put(0x1011, "CAP_THUMBNAILSENABLED");
        map.put(0x1012, "CAP_DUPLEX");
        map.put(0x1013, "CAP_DUPLEXENABLED");
        map.put(0x1014, "CAP_ENABLEDSUIONLY");
        map.put(0x1015, "CAP_CUSTOMDSDATA");
        map.put(0x1016, "CAP_ENDORSER");
        map.put(0x1017, "CAP_JOBCONTROL");
        map.put(0x1018, "CAP_ALARMS");
        map.put(0x1019, "CAP_ALARMVOLUME");
        map.put(0x101a, "CAP_AUTOMATICCAPTURE");
        map.put(0x101b, "CAP_TIMEBEFOREFIRSTCAPTURE");
        map.put(0x101c, "CAP_TIMEBETWEENCAPTURES");
        map.put(0x101d, "CAP_CLEARBUFFERS");
        map.put(0x101e, "CAP_MAXBATCHBUFFERS");
        map.put(0x101f, "CAP_DEVICETIMEDATE");
        map.put(0x1020, "CAP_POWERSUPPLY");
        map.put(0x1021, "CAP_CAMERAPREVIEWUI");
        map.put(0x1022, "CAP_DEVICEEVENT");
        map.put(0x1024, "CAP_SERIALNUMBER");
        map.put(0x1026, "CAP_PRINTER");
        map.put(0x1027, "CAP_PRINTERENABLED");
        map.put(0x1028, "CAP_PRINTERINDEX");
        map.put(0x1029, "CAP_PRINTERMODE");
        map.put(0x102a, "CAP_PRINTERSTRING");
        map.put(0x102b, "CAP_PRINTERSUFFIX");
        map.put(0x102c, "CAP_LANGUAGE");
        map.put(0x102d, "CAP_FEEDERALIGNMENT");
        map.put(0x102e, "CAP_FEEDERORDER");
        map.put(0x1030, "CAP_REACQUIREALLOWED");
        map.put(0x1032, "CAP_BATTERYMINUTES");
        map.put(0x1033, "CAP_BATTERYPERCENTAGE");
        map.put(0x1034, "CAP_CAMERASIDE");
        map.put(0x1035, "CAP_SEGMENTED");
        map.put(0x1036, "CAP_CAMERAENABLED");
        map.put(0x1037, "CAP_CAMERAORDER");
        map.put(0x1038, "CAP_MICRENABLED");
        map.put(0x1039, "CAP_FEEDERPREP");
        map.put(0x103a, "CAP_FEEDERPOCKET");
        map.put(0x103b, "CAP_AUTOMATICSENSEMEDIUM");
        map.put(0x103c, "CAP_CUSTOMINTERFACEGUID");
        map.put(0x103d, "CAP_SUPPORTEDCAPSSEGMENTUNIQUE");
        map.put(0x103e, "CAP_SUPPORTEDDATS");
        map.put(0x103f, "CAP_DOUBLEFEEDDETECTION");
        map.put(0x1040, "CAP_DOUBLEFEEDDETECTIONLENGTH");
        map.put(0x1041, "CAP_DOUBLEFEEDDETECTIONSENSITIVITY");
        map.put(0x1042, "CAP_DOUBLEFEEDDETECTIONRESPONSE");
        map.put(0x1043, "CAP_PAPERHANDLING");
        map.put(0x1044, "CAP_INDICATORSMODE");
        map.put(0x1045, "CAP_PRINTERVERTICALOFFSET");
        map.put(0x1046, "CAP_POWERSAVETIME");
        map.put(0x1047, "CAP_PRINTERCHARROTATION");
        map.put(0x1048, "CAP_PRINTERFONTSTYLE");
        map.put(0x1049, "CAP_PRINTERINDEXLEADCHAR");
        map.put(0x104A, "CAP_PRINTERINDEXMAXVALUE");
        map.put(0x104B, "CAP_PRINTERINDEXNUMDIGITS");
        map.put(0x104C, "CAP_PRINTERINDEXSTEP");
        map.put(0x104D, "CAP_PRINTERINDEXTRIGGER");
        map.put(0x104E, "CAP_PRINTERSTRINGPREVIEW");

        /* image data sources MAY support these caps */
        map.put(0x1100, "ICAP_AUTOBRIGHT");
        map.put(0x1101, "ICAP_BRIGHTNESS");
        map.put(0x1103, "ICAP_CONTRAST");
        map.put(0x1104, "ICAP_CUSTHALFTONE");
        map.put(0x1105, "ICAP_EXPOSURETIME");
        map.put(0x1106, "ICAP_FILTER");
        map.put(0x1107, "ICAP_FLASHUSED");
        map.put(0x1108, "ICAP_GAMMA");
        map.put(0x1109, "ICAP_HALFTONES");
        map.put(0x110a, "ICAP_HIGHLIGHT");
        map.put(0x110c, "ICAP_IMAGEFILEFORMAT");
        map.put(0x110d, "ICAP_LAMPSTATE");
        map.put(0x110e, "ICAP_LIGHTSOURCE");
        map.put(0x1110, "ICAP_ORIENTATION");
        map.put(0x1111, "ICAP_PHYSICALWIDTH");
        map.put(0x1112, "ICAP_PHYSICALHEIGHT");
        map.put(0x1113, "ICAP_SHADOW");
        map.put(0x1114, "ICAP_FRAMES");
        map.put(0x1116, "ICAP_XNATIVERESOLUTION");
        map.put(0x1117, "ICAP_YNATIVERESOLUTION");
        map.put(0x1118, "ICAP_XRESOLUTION");
        map.put(0x1119, "ICAP_YRESOLUTION");
        map.put(0x111a, "ICAP_MAXFRAMES");
        map.put(0x111b, "ICAP_TILES");
        map.put(0x111c, "ICAP_BITORDER");
        map.put(0x111d, "ICAP_CCITTKFACTOR");
        map.put(0x111e, "ICAP_LIGHTPATH");
        map.put(0x111f, "ICAP_PIXELFLAVOR");
        map.put(0x1120, "ICAP_PLANARCHUNKY");
        map.put(0x1121, "ICAP_ROTATION");
        map.put(0x1122, "ICAP_SUPPORTEDSIZES");
        map.put(0x1123, "ICAP_THRESHOLD");
        map.put(0x1124, "ICAP_XSCALING");
        map.put(0x1125, "ICAP_YSCALING");
        map.put(0x1126, "ICAP_BITORDERCODES");
        map.put(0x1127, "ICAP_PIXELFLAVORCODES");
        map.put(0x1128, "ICAP_JPEGPIXELTYPE");
        map.put(0x112a, "ICAP_TIMEFILL");
        map.put(0x112b, "ICAP_BITDEPTH");
        map.put(0x112c, "ICAP_BITDEPTHREDUCTION");
        map.put(0x112d, "ICAP_UNDEFINEDIMAGESIZE");
        map.put(0x112e, "ICAP_IMAGEDATASET");
        map.put(0x112f, "ICAP_EXTIMAGEINFO");
        map.put(0x1130, "ICAP_MINIMUMHEIGHT");
        map.put(0x1131, "ICAP_MINIMUMWIDTH");
        map.put(0x1134, "ICAP_AUTODISCARDBLANKPAGES");
        map.put(0x1136, "ICAP_FLIPROTATION");
        map.put(0x1137, "ICAP_BARCODEDETECTIONENABLED");
        map.put(0x1138, "ICAP_SUPPORTEDBARCODETYPES");
        map.put(0x1139, "ICAP_BARCODEMAXSEARCHPRIORITIES");
        map.put(0x113a, "ICAP_BARCODESEARCHPRIORITIES");
        map.put(0x113b, "ICAP_BARCODESEARCHMODE");
        map.put(0x113c, "ICAP_BARCODEMAXRETRIES");
        map.put(0x113d, "ICAP_BARCODETIMEOUT");
        map.put(0x113e, "ICAP_ZOOMFACTOR");
        map.put(0x113f, "ICAP_PATCHCODEDETECTIONENABLED");
        map.put(0x1140, "ICAP_SUPPORTEDPATCHCODETYPES");
        map.put(0x1141, "ICAP_PATCHCODEMAXSEARCHPRIORITIES");
        map.put(0x1142, "ICAP_PATCHCODESEARCHPRIORITIES");
        map.put(0x1143, "ICAP_PATCHCODESEARCHMODE");
        map.put(0x1144, "ICAP_PATCHCODEMAXRETRIES");
        map.put(0x1145, "ICAP_PATCHCODETIMEOUT");
        map.put(0x1146, "ICAP_FLASHUSED2");
        map.put(0x1147, "ICAP_IMAGEFILTER");
        map.put(0x1148, "ICAP_NOISEFILTER");
        map.put(0x1149, "ICAP_OVERSCAN");
        map.put(0x1150, "ICAP_AUTOMATICBORDERDETECTION");
        map.put(0x1151, "ICAP_AUTOMATICDESKEW");
        map.put(0x1152, "ICAP_AUTOMATICROTATE");
        map.put(0x1153, "ICAP_JPEGQUALITY");
        map.put(0x1154, "ICAP_FEEDERTYPE");
        map.put(0x1155, "ICAP_ICCPROFILE");
        map.put(0x1156, "ICAP_AUTOSIZE");
        map.put(0x1157, "ICAP_AUTOMATICCROPUSESFRAME");
        map.put(0x1158, "ICAP_AUTOMATICLENGTHDETECTION");
        map.put(0x1159, "ICAP_AUTOMATICCOLORENABLED");
        map.put(0x115a, "ICAP_AUTOMATICCOLORNONCOLORPIXELTYPE");
        map.put(0x115b, "ICAP_COLORMANAGEMENTENABLED");
        map.put(0x115c, "ICAP_IMAGEMERGE");
        map.put(0x115d, "ICAP_IMAGEMERGEHEIGHTTHRESHOLD");
        map.put(0x115e, "ICAP_SUPPORTEDEXTIMAGEINFO");
        map.put(0x115f, "ICAP_FILMTYPE");
        map.put(0x1160, "ICAP_MIRROR");
        map.put(0x1161, "ICAP_JPEGSUBSAMPLING");

        map.put(0x100c, "CAP_SUPPORTEDCAPSEXT");
        map.put(0x1023, "CAP_PAGEMULTIPLEACQUIRE");
        map.put(0x102f, "CAP_PAPERBINDING");
        map.put(0x1031, "CAP_PASSTHRU");
        map.put(0x1034, "CAP_POWERDOWNTIME");

        mapCapCodeToName = map;
        return mapCapCodeToName;
    }

    public static short DSM_Entry(TW_IDENTITY origin, TW_IDENTITY destination, int dg, short dat, short msg, Object p) {
        return twain.DSM_Entry(origin, destination, dg, dat, msg, p);
    }

    public static Pointer DSM_Alloc(int len) {
        try {
            return twain.DSM_Alloc(len);
        } catch (UnsatisfiedLinkError e) {
            return kernel32.GlobalAlloc(0, len);
        }
    }

    public static void DSM_Free(Pointer handle) {
        try {
            twain.DSM_Free(handle);
        } catch (UnsatisfiedLinkError e) {
            kernel32.GlobalFree(handle);
        }
    }

    public static Pointer DSM_Lock(Pointer handle) {
        try {
            return twain.DSM_Lock(handle);
        } catch (UnsatisfiedLinkError e) {
            return kernel32.GlobalLock(handle);
        }
    }

    public static boolean DSM_Unlock(Pointer handle) {
        try {
            return twain.DSM_Unlock(handle);
        } catch (UnsatisfiedLinkError e) {
            return kernel32.GlobalUnlock(handle);
        }
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    public static String getArch() {
        return System.getProperty("os.name").contains("amd64") ? "64" : "32";
    }

    public static void init() throws TwainException {

        if (!isWindows()) {
            throw new TwainException("Library works only on Windows OS");
        }
        kernel32 = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);

//        user32 = (User32) Native.loadLibrary("user32", User32.class);
        try {
            twain = (Win32Twain) Native.loadLibrary("twaindsm", Win32Twain.class);
            libraryName = "twaindsm";
        } catch (UnsatisfiedLinkError e) {
            try {
                twain = (Win32Twain) Native.loadLibrary("twain_" + getArch(), Win32Twain.class);
                libraryName = "twain_32";
            } catch (UnsatisfiedLinkError ex) {
                throw new TwainException("Cannot load TWAIN_" + getArch() + ".DLL or TWAINDSM.DLL");
            }
        }

        if (!openDataSourceManager()) {
            throw new TwainException("Cannot open datasource manager.");
        }
    }

    public static void done() {
        User32.INSTANCE.PostMessage(hwnd, WinUser.WM_QUIT, null, null);
    }

    private static void setupAppId(TW_IDENTITY appID) {
        appID.Id = 0;
        appID.ProtocolMajor = 1;
        appID.ProtocolMinor = 9;
//        appID.ProtocolMajor = 2;
//        appID.ProtocolMinor = 4;
        appID.SupportedGroups = (DG_CONTROL | DG_IMAGE | 0x20000000);
        appID.setManufacturer("Smart-Consulting");
        appID.setProductFamily("JTwain agent");
        appID.setProductName("TWAIN-AGENT");
//        appID.Version.MajorNum = 2;
//        appID.Version.MinorNum = 4;
        appID.Version.MajorNum = 1;
        appID.Version.MinorNum = 9;
        appID.Version.Language = 17;
        appID.Version.Country = 1;
        appID.Version.setInfo("2017-01-17");
    }
//

    private static int OpenDSM(TW_IDENTITY application, WinDef.HWND winHdl) {
        int stat = twain.DSM_Entry(application, null, DG_CONTROL, DAT_PARENT, MSG_OPENDSM, new WinNT.HANDLEByReference(winHdl));
        return stat;
    }

    static boolean started = false;

    public static boolean openDataSourceManager() throws TwainException {
        if (g_AppID != null) {
            return true;
        }

        g_AppID = new TW_IDENTITY();

        final Object o = new Object();

        new Thread(new Runnable() {

            private WinDef.HWND owner;

            @Override
            public void run() {
                try {
                    String wNClass = "TWAIN" + System.currentTimeMillis();

                    WinDef.HMODULE hInst = null;//com.sun.jna.platform.win32.Kernel32.INSTANCE.GetModuleHandle("");
                    WinUser.WNDCLASSEX wClass = new WinUser.WNDCLASSEX();
                    wClass.hInstance = hInst;
                    wClass.lpszClassName = wNClass;
                    wClass.lpfnWndProc = new TwainWndProc();
                    User32.INSTANCE.RegisterClassEx(wClass);

                    owner = hwnd = User32.INSTANCE.CreateWindowEx(0, wNClass, wNClass, User32.WS_MAXIMIZE, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, new WinDef.HWND(new Pointer(HWND_DESKTOP)), null, hInst, null);

                    if (hwnd == null) {
                        throw new TwainException("Unable to create private window");
                    }
                    boolean ok = User32.INSTANCE.SetWindowPos(hwnd, new WinDef.HWND(Pointer.createConstant(-1)), 0, 0, 0, 0, (short) SWP_NOSIZE);
                    if (!ok) {
                        User32.INSTANCE.DestroyWindow(hwnd);
                        hwnd = null;
                        g_AppID = null;
                        throw new TwainException("Unable to position private window");
                    }

                    setupAppId(g_AppID);
                    int stat = OpenDSM(g_AppID, hwnd);

                    if (stat != TWRC_SUCCESS) {
                        User32.INSTANCE.DestroyWindow(hwnd);
                        hwnd = null;
                        g_AppID = null;

                        throw new TwainException("Unable to open DSM");
                    }

                    WinUser.MSG lpMsg = new WinUser.MSG();
                    started = true;
                    synchronized (o) {
                        o.notifyAll();
                    }

                    while (0 != User32.INSTANCE.GetMessage(lpMsg, null, 0, 0)) {
                        int rc = 0;
                        if (sourceManager != null) {
                            rc = handleMessage(lpMsg.getPointer());
                        }
                        if (rc != TWRC_DSEVENT) {
                            User32.INSTANCE.TranslateMessage(lpMsg);
                            User32.INSTANCE.DispatchMessage(lpMsg);
                        }
                    }

                } catch (Throwable e) {
                    started = false;
                    e.printStackTrace();
                }
            }

        }
        ).start();

        synchronized (o) {
            try {
                o.wait(TimeUnit.SECONDS.toMillis(10));
            } catch (InterruptedException ex) {
                throw new TwainException(ex);
            }
        }

        return started;
    }

    public static TwainSourceManager getSourceManager() throws TwainException {
        if (sourceManager != null) {
            return sourceManager;
        }
        init();
        return sourceManager = new TwainSourceManager(hwnd);
    }

    static int callSourceManager(int dg, int id, int msg, Object obj) {
        int rc = DSM_Entry(g_AppID, null, dg, (short) id, (short) msg, obj);

        if (dg == DG_CONTROL && id == DAT_IDENTITY && msg == MSG_USERSELECT) {
            // Set foreground window
        }

        return rc;
    }

    static int callSource(TW_IDENTITY identity, int dg, int id, int msg, Object obj) {
//        int rc = DSM_Entry(g_AppID, identity, dg, (short) id, (short) msg, obj);
        int rc = DSM_Entry(g_AppID, identity, dg, (short) id, (short) msg, obj);

        if (dg == DG_CONTROL && id == DAT_IDENTITY && msg == MSG_USERSELECT) {
            // Set foreground window
        }

        return rc;
    }

    static byte[] getContainer(int containerType, Pointer containerPtr) {
        if (containerPtr == Pointer.NULL) {
            return null;
        }
        switch (containerType) {
            case TWON_ARRAY: {
                Pointer p = DSM_Lock(containerPtr);
                byte[] ret = null;
                int size = 0;
                int type = 0;
                if (p != Pointer.NULL) {
                    type = p.getShort(0);
                    size = p.getInt(2);

                    if (type <= TWTY_UNI512) {
                        size = 6 + size * TwainContainer.TYPE_SIZES[type];
                    } else {
                        size = 6;
                        p.setInt(2, 0);
                    }
                    ret = p.getByteArray(0, size);
                }
                DSM_Unlock(containerPtr);
                return ret;
            }
            case TWON_ENUMERATION: {
                Pointer p = DSM_Lock(containerPtr);
                byte[] ret = null;
                int size = 0;
                int type = 0;
                if (p != Pointer.NULL) {
                    type = p.getShort(0);
                    size = p.getInt(2);

                    if (type <= TWTY_UNI512) {
                        size = 14 + size * TwainContainer.TYPE_SIZES[type];
                    } else {
                        size = 14;
                        p.setInt(2, 0);
                    }
                    ret = p.getByteArray(0, size);
                }
                DSM_Unlock(containerPtr);
                return ret;
            }
            case TWON_ONEVALUE: {
                Pointer p = DSM_Lock(containerPtr);
                byte[] ret = null;
                if (p != Pointer.NULL) {
                    ret = p.getByteArray(0, 6);
                }
                DSM_Unlock(containerPtr);
                return ret;
            }

            case TWON_RANGE: {
                Pointer p = DSM_Lock(containerPtr);
                byte[] ret = p.getByteArray(0, 22);
                DSM_Unlock(containerPtr);
                return ret;

            }
            default:
                return null;
        }
    }

    static Pointer setContainer(int containerType, byte[] containerBytes) {
        Pointer p = DSM_Alloc(containerBytes.length);

        Pointer buf = DSM_Lock(p);

        if (buf == Pointer.NULL) {
            DSM_Unlock(p);
            DSM_Free(p);

            return Pointer.NULL;
        }

        buf.write(0, containerBytes, 0, containerBytes.length);

        DSM_Unlock(p);

        return p;
    }

    static void free(Pointer containerHandle) {
        DSM_Free(containerHandle);
    }

    static void signalException(String string) {
        TwainScanner scanner = getScanner();
        if (scanner != null) {
            scanner.signalException(string);
        }
    }

    static void signalStateChange(TwainSource aThis) {
        TwainScanner scanner = getScanner();
        if (scanner != null) {
            scanner.setState(aThis);
        }
    }

    static void negotiateCapabilities(TwainSource aThis) {
        TwainScanner scanner = getScanner();
        if (scanner != null) {
            scanner.negotiateCapabilities(aThis);
        }
    }

    static public void setScanner(TwainScanner s) {
        scanner = new WeakReference<>(s);
    }

    static private TwainScanner getScanner() {
        return (TwainScanner) scanner.get();
    }

    static void select(TwainScanner sc) throws TwainException {
        setScanner(sc);
        TwainSourceManager sm = getSourceManager();
        sm.getSource().checkState(3);
        trigger(sc, 1);
    }

    static void getIdentities(TwainScanner sc, List list) throws TwainException {
        setScanner(sc);
        TwainSourceManager sm = getSourceManager();
        sm.getSource().checkState(3);
        Semaphore s = new Semaphore(0, true);
        list.add(s);
        trigger(list, 2);
        try {
            s.tryAcquire(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            throw new TwainException(Twain.class.getName() + ".getIdentities\n\tCould not retrieve device names. Request timed out.");
        }
    }

    static void select(TwainScanner sc, String name) throws TwainException {
        setScanner(sc);
        TwainSourceManager sm = getSourceManager();
        sm.getSource().checkState(3);
        trigger(name, 3);
    }

    static void acquire(TwainScanner scanner) throws TwainException {
        setScanner(scanner);
        TwainSourceManager sm = getSourceManager();
        sm.getSource().checkState(3);
        trigger(scanner, 4);
    }

    static void setCancel(TwainScanner aThis, boolean c) throws TwainException {
        getSourceManager().getSource().setCancel(c);
    }

    public static void nnew(byte[] imx, int preferredSize) {
        Pointer p = kernel32.GlobalAlloc(0, preferredSize);
        Win32Twain.TW_IMAGEMEMXFER im = new Win32Twain.TW_IMAGEMEMXFER();
        im.getPointer().write(0, imx, 0, im.size());
        im.read();

        im.Memory.Flags = TWMF_APPOWNS | TWMF_HANDLE;
        im.Memory.Length = preferredSize;
        im.Memory.TheMem = p;

        im.write();
        im.getPointer().read(0, imx, 0, im.size());

        im.Memory.TheMem = null;
    }

    public static int ncopy(byte[] buffer, byte[] imx, int bytesWritten) {
        Win32Twain.TW_IMAGEMEMXFER ix = new Win32Twain.TW_IMAGEMEMXFER();
        ix.getPointer().write(0, imx, 0, ix.size());
        ix.read();

        Pointer p = kernel32.GlobalLock(ix.Memory.TheMem);

        p.read(0, buffer, 0, bytesWritten);

        kernel32.GlobalUnlock(p);

        ix.Memory.TheMem = null;

        return bytesWritten;
    }

    public static void transferMemoryBuffer(TwainMemoryTransfer.Info info) {
        TwainScanner scanner = getScanner();
        if (scanner != null) {
            scanner.setImageBuffer(info);
        }
    }

    public static void ndelete(byte[] imx) {

        Win32Twain.TW_IMAGEMEMXFER im = new Win32Twain.TW_IMAGEMEMXFER();
        im.getPointer().write(0, imx, 0, im.size());
        im.read();

        if (im.Memory.TheMem != Pointer.NULL) {
            kernel32.GlobalFree(im.Memory.TheMem);
            im.Memory.TheMem = Pointer.NULL;
        }

        im.write();
        im.getPointer().read(0, imx, 0, im.size());
    }

    public static void transferNativeImage(int handle) {

        BufferedImage image = (BufferedImage) ntransferImage(handle);
        if (image != null) {
            TwainScanner scanner = getScanner();
            if (scanner != null) {
                scanner.setImage(image);
            }
        }
    }

    public static void transferFileImage(File file) {
        if (file != null) {
            TwainScanner scanner = getScanner();
            if (scanner != null) {
                scanner.setImage(file);
            }
        }
    }

    private static void trigger(Object caller, int cmd) {
        ntrigger(caller, cmd);
    }

    protected static void execute(Object obj, int cmd) {
        TwainSource source;
        try {
            switch (cmd) {
                case 0: {
                    if (!(obj instanceof TwainScanner)) {
                        throw new TwainException(obj.getClass().getName() + " not a instance of TwainScanner class");
                    }
                    TwainScanner ti = (TwainScanner) obj;
                    source = sourceManager.getSource();
                    ti.setState(source);
                    break;
                }
                case 1:
                    sourceManager.selectSource();
                    break;
                case 2:
                    List list = (List) obj;
                    Semaphore s = (Semaphore) list.get(0);
                    list.remove(0);
                    sourceManager.getIdentities(list);
                    s.release();
                    break;
                case 3:
                    String name = (String) obj;
                    sourceManager.selectSource(name);
                    break;
                case 4:
                    final TwainSource src = sourceManager.openSource();

                    try {
                        src.enable();
                    } finally {
                        src.close();
                    }
                    break;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static int handleMessage(Pointer lpMsg) {
        try {
            return sourceManager.getSource().handleGetMessage(lpMsg);
        } catch (Throwable ex) {
            signalException(ex.getMessage());
            ex.printStackTrace();
            return TWRC_NOTDSEVENT;
        }
    }

    protected static Map<Integer, Object> callMapper = new ConcurrentHashMap<>();

    private static void ntrigger(Object caller, int cmd) {
        int id = (int) System.nanoTime();
        callMapper.put(id, caller);
//        User32.INSTANCE.PostMessage(hwnd, WinUser.WM_USER, new WinDef.WPARAM(cmd), new WinDef.LPARAM(id));
        User32.INSTANCE.PostMessage(hwnd, WinUser.WM_USER, new WinDef.WPARAM(cmd), new WinDef.LPARAM(id));
    }

    private static BufferedImage ntransferImage(int handle) {

        if (handle == 0) {
            return null;
        }

        Pointer HANDLE = new Pointer(handle);
        Pointer p = DSM_Lock(HANDLE);
        if (p == null) {
            return null;
        }
        WinGDI.BITMAPINFOHEADER bih = (WinGDI.BITMAPINFOHEADER) WinGDI.BITMAPINFOHEADER.newInstance(WinGDI.BITMAPINFOHEADER.class, p);
        bih.read();

        if (bih.biCompression != WinGDI.BI_RGB && bih.biCompression != WinGDI.BI_BITFIELDS) {
            signalException("Cannot deal with DIB header");
            DSM_Unlock(HANDLE);
            DSM_Free(HANDLE);
            return null;
        }
        BufferedImage image = null;
        switch (bih.biBitCount) {
            case 1:
                image = transfer01BitImage(bih);
                break;
            case 4:
                image = transfer04BitImage(bih);
                break;
            case 8:
                image = transfer08BitImage(bih);
                break;
            case 24:
                image = transfer24BitImage(bih);
                break;
            default:
                signalException("Unsupported bit size");
                break;
        }

        DSM_Unlock(HANDLE);
        DSM_Free(HANDLE);
        return image;
    }

    private static BufferedImage transfer01BitImage(WinGDI.BITMAPINFOHEADER bih) {
        BufferedImage img = new BufferedImage(bih.biWidth, bih.biHeight, BufferedImage.TYPE_BYTE_BINARY, readBitIndexModel(bih));
        DataBufferByte dbi = (DataBufferByte) img.getRaster().getDataBuffer();
        int size = (bih.biWidth + 7) >> 3;

        int offset = bih.size() + getColorsInPallete(bih) * 4;
        int coffest = dbi.getData().length - size;
        int bpl = ((bih.biWidth * bih.biBitCount + 31) >> 5) << 2;
        for (int i = 0; i < bih.biHeight; i++) {
            bih.getPointer().read(offset, dbi.getData(), coffest, size);
            offset += bpl;
            coffest -= size;

        }
        return img;
    }

    private static BufferedImage transfer04BitImage(WinGDI.BITMAPINFOHEADER bih) {
        BufferedImage img = new BufferedImage(bih.biWidth, bih.biHeight, BufferedImage.TYPE_BYTE_INDEXED, readBitIndexModel(bih));
        DataBufferByte dbi = (DataBufferByte) img.getRaster().getDataBuffer();
        int size = (bih.biWidth + 1) >> 1;

        int offset = bih.size() + getColorsInPallete(bih) * 4;
        int coffest = dbi.getData().length - size;
        int bpl = ((bih.biWidth * bih.biBitCount + 31) >> 5) << 2;
        for (int i = 0; i < bih.biHeight; i++) {
            bih.getPointer().read(offset, dbi.getData(), coffest, size);
            offset += bpl;
            coffest -= size;

        }

        return img;
    }

    private static BufferedImage transfer08BitImage(WinGDI.BITMAPINFOHEADER bih) {
        BufferedImage img = new BufferedImage(bih.biWidth, bih.biHeight, BufferedImage.TYPE_BYTE_INDEXED, readBitIndexModel(bih));
        DataBufferByte dbi = (DataBufferByte) img.getRaster().getDataBuffer();
        int size = bih.biWidth;

        int offset = bih.size() + getColorsInPallete(bih) * 4;
        int coffest = dbi.getData().length - size;
        int bpl = ((bih.biWidth * bih.biBitCount + 31) >> 5) << 2;
        for (int i = 0; i < bih.biHeight; i++) {
            bih.getPointer().read(offset, dbi.getData(), coffest, size);
            offset += bpl;
            coffest -= size;
        }

        return img;
    }

    private static BufferedImage transfer24BitImage(WinGDI.BITMAPINFOHEADER bih) {
        BufferedImage img = new BufferedImage(bih.biWidth, bih.biHeight, BufferedImage.TYPE_3BYTE_BGR);
        DataBufferByte dbi = (DataBufferByte) img.getRaster().getDataBuffer();
        int size = bih.biWidth * 3;

        int offset = bih.size();
        int coffest = dbi.getData().length - size;
        int bpl = ((bih.biWidth * bih.biBitCount + 31) >> 5) << 2;
        for (int i = 0; i < bih.biHeight; i++) {
            bih.getPointer().read(offset, dbi.getData(), coffest, size);
            offset += bpl;
            coffest -= size;
        }
        return img;
    }

    private static IndexColorModel readBitIndexModel(WinGDI.BITMAPINFOHEADER bih) {
        WinGDI.BITMAPINFO bi = (WinGDI.BITMAPINFO) WinGDI.BITMAPINFO.newInstance(WinGDI.BITMAPINFO.class, new Pointer(Pointer.nativeValue(bih.getPointer())));

        int size = getColorsInPallete(bih);

        bi.bmiColors = new WinGDI.RGBQUAD[size];
        bi.read();

        byte[] r = new byte[size];
        byte[] g = new byte[size];
        byte[] b = new byte[size];
        for (int i = 0; i < size; i++) {
            r[i] = bi.bmiColors[i].rgbRed;
            g[i] = bi.bmiColors[i].rgbGreen;
            b[i] = bi.bmiColors[i].rgbBlue;
        }
        IndexColorModel icm = new IndexColorModel(bih.biBitCount, size, r, g, b);

        return icm;
    }

    private static int getColorsInPallete(WinGDI.BITMAPINFOHEADER bih) {
        return bih.biClrUsed != 0 ? bih.biClrUsed : 1 << bih.biBitCount;
    }
}
