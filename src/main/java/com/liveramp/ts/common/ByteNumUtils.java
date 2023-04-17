package com.liveramp.ts.common;

import com.google.common.primitives.UnsignedInteger;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/**
 * @author robgao
 * @date 2023/4/13 20:27
 * @className Utils
 */
@Slf4j
public class ByteNumUtils {
    public static int ReadInt(byte[] bytes, boolean isLittleEndian) {
        if (bytes.length != 4) {
            throw new RuntimeException("bytes length is not 4");
        }
        int value;
        if (!isLittleEndian) {
            value = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
        } else {
            value = (bytes[3] & 0xFF) << 24 | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
        }
        return value;
    }

    public static long ReadLong(byte[] bytes) {
        if (bytes.length != 8) {
            throw new RuntimeException("bytes length is not 8");
        }
        // read little endian long
        ByteBuffer bf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        return bf.getLong();
    }

    public static short ReadShort(byte[] bytes) {
        if (bytes.length != 2) {
            throw new RuntimeException("bytes length is not 2");
        }
        // read little endian long
        ByteBuffer bf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        return bf.getShort();
    }

    public static long ReadUint32(byte[] bytes) {
        if (bytes.length != 4) {
            throw new RuntimeException("bytes length is not 4");
        }
        // read little endian long
        ByteBuffer bf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        return UnsignedInteger.fromIntBits(bf.getInt()).longValue();
    }

    public static int ReadUint16(byte[] bytes) {
        if (bytes.length != 2) {
            throw new RuntimeException("bytes length is not 2");
        }
        byte[] intBytes = ByteBuffer.allocate(4).put(bytes).put(new byte[]{0, 0}).array();
        // read little endian long
        ByteBuffer bf = ByteBuffer.wrap(intBytes).order(ByteOrder.LITTLE_ENDIAN);
        return UnsignedInteger.fromIntBits(bf.getInt()).intValue();
    }

    public static long[] ByteArrayToLongArray(byte[] data) {
        if (data.length % 8 != 0) {
            throw new IllegalArgumentException("Array length should be divisible by 8");
        }

        //        long[] result = new long[data.length / 8];
        //        for (int i = 0; i < result.length; i++) {
        //            byte[] subarray = Arrays.copyOfRange(data, i * 8, i * 8 + 8);
        //            result[i] = ReadLong(subarray);
        //        }


        int numLongs = data.length / Long.BYTES; // 计算long数组的大小
        ByteBuffer buffer = ByteBuffer.wrap(data); // 创建ByteBuffer实例
        LongBuffer longBuffer = buffer.asLongBuffer(); // 将ByteBuffer转换为LongBuffer
        long[] result = new long[numLongs]; // 创建结果数组
        for (int i = 0; i < result.length; i++) {
            result[i] = Long.reverseBytes(longBuffer.get(i));
        }
        return result;
    }


    public static short[] ByteArrayToShortArray(byte[] data) {
        if (data.length % 2 != 0) {
            throw new IllegalArgumentException("Array length should be divisible by 2");
        }

        short[] result = new short[data.length / Short.BYTES];
        ByteBuffer buffer = ByteBuffer.wrap(data); // 创建ByteBuffer实例
        ShortBuffer shortBuffer = buffer.asShortBuffer(); // 将ByteBuffer转换为LongBuffer
        for (int i = 0; i < result.length; i++) {
            result[i] = Short.reverseBytes(shortBuffer.get(i));
        }
        //        for (int i = 0; i < result.length; i++) {
        //            short val = ReadShort(new byte[]{data[i * 2], data[i * 2 + 1]});
        //            if (val > (1 << 16) - 1) {
        //                log.error("val > 65535 , it is logically impossible");
        //            }
        //            result[i] = val;
        //        }
        return result;
    }

    public static int[] ByteArrayToUint16Array(byte[] array) {
        if (array.length % 2 != 0) {
            throw new IllegalArgumentException("Array length should be divisible by 2");
        }
        int[] result = new int[array.length / 2];
        for (int i = 0; i < result.length; i++) {
            int val = ReadUint16(new byte[]{array[i * 2], array[i * 2 + 1]});
            if (val > (1 << 16) - 1) {
                log.error("val > 65535 , it is logically impossible");
            }
            result[i] = val;
        }
        return result;
    }

    // source of com.google.common.primitives.Ints
    public static byte[] toByteArray(int value) {
        return new byte[]{(byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value};
    }

    // source of com.google.common.primitives.Longs
    public static byte[] toByteArray(long value) {
        byte[] result = new byte[8];

        for(int i = 7; i >= 0; --i) {
            result[i] = (byte)((int)(value & 255L));
            value >>= 8;
        }

        return result;
    }
    public static long AddWithLow16(long num1, long num2) {
        //0x 16进制
        //0b 二进制
        //F = 16 = 1111
        //L = long
        // 0xFFFFL = 1111 1111 1111 1111 = 0b0000000000000000000000000000000000000000000000001111111111111111
        return (num1 & 0xFFFFL) << 16 | (num2 & 0xFFFFL);
        // 与下面等价
        //return (num1 & 0b0000000000000000000000000000000000000000000000001111111111111111) << 16 | (num2 & 0b0000000000000000000000000000000000000000000000001111111111111111);
    }
}
