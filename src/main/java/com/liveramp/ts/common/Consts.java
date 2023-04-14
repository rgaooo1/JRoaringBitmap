package com.liveramp.ts.common;

/**
 * @author robgao
 * @date 2023/4/13 22:55
 * @className Consts
 */
public class Consts {
    public final static int serialCookieNoRunContainer = 12346; // only arrays and bitmaps
    public final static int serialCookie = 12347; //runs, arrays, and bitmaps

    public static final int MAX_KEY_SIZE = 1 << 16;
    public static final int MAX_KEY_VALUE = (1 << 16) - 1; // 65535
    public static final long ARRAY_DEFAULT_MAX_SIZE = 4096;
    public static final int BITMAP_CONTAINER_DEFAULT_MAX_SIZE = 1 << 16;// 65536

}
