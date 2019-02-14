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
package free.lucifer.jtwain.utils;

/**
 *
 * @author lucifer
 */
public class TwainUtils {

    public static int getINT16(byte[] buf, int off) {
        return (buf[off++] & 0x00FF) | (buf[off] << 8);
    }

    public static void setINT16(byte[] buf, int off, int i) {
        buf[off++] = (byte) i;
        buf[off++] = (byte) (i >> 8);
    }

    public static int getINT32(byte[] buf, int off) {
        return (buf[off++] & 0x00FF) | ((buf[off++] & 0x00FF) << 8) | ((buf[off++] & 0x00FF) << 16) | (buf[off] << 24);
    }

    public static void setINT32(byte[] buf, int off, int i) {
        buf[off++] = (byte) i;
        buf[off++] = (byte) (i >> 8);
        buf[off++] = (byte) (i >> 16);
        buf[off++] = (byte) (i >> 24);
    }

    public static long getINT64(byte[] buf, int off) {
        return (buf[off++] & 0x00FF) | ((buf[off++] & 0x00FF) << 8) | ((buf[off++] & 0x00FF) << 16) | ((buf[off++] & 0x00FF) << 24)
                | ((buf[off++] & 0x00FF) << 32) | ((buf[off++] & 0x00FF) << 40) | ((buf[off++] & 0x00FF) << 48) | (buf[off] << 56);
    }

    public static void setINT64(byte[] buf, int off, long i) {
        buf[off++] = (byte) i;
        buf[off++] = (byte) (i >> 8);
        buf[off++] = (byte) (i >> 16);
        buf[off++] = (byte) (i >> 24);
        buf[off++] = (byte) (i >> 32);
        buf[off++] = (byte) (i >> 40);
        buf[off++] = (byte) (i >> 48);
        buf[off++] = (byte) (i >> 56);
    }

    public static double getFIX32(byte[] buf, int off) {
        int whole = ((buf[off++] & 0x00FF) | (buf[off++] << 8));
        int frac = ((buf[off++] & 0x00FF) | ((buf[off] & 0x00FF) << 8));
        return ((double) whole) + ((double) frac) / 65536.0;
    }

    public static void setFIX32(byte[] buf, int off, double d) {
        int value = (int) (d * 65536.0 + ((d < 0) ? (-0.5) : 0.5));
        setINT16(buf, off, value >> 16);
        setINT16(buf, off + 2, value & 0x0000FFFF);
    }

    public static void setString(byte[] buf, int off, String s) {
        System.arraycopy(s.getBytes(), 0, buf, 0, s.length());
    }

}
