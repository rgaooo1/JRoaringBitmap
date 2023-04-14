package com.liveramp.ts.roaring;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.primitives.UnsignedInteger;
import com.liveramp.ts.common.ByteNumUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * Unit test for simple App.
 */
@Slf4j
public class TestCase extends junit.framework.TestCase {
    public void testMain() throws IOException {
        RoaringBitmap roaringBitmap = new RoaringBitmap();
        roaringBitmap.fromFile("bin/bitmap32_test_01.bin");
        List list = roaringBitmap.toSet();
        log.info("list: {}", list);
        assertTrue(true);
    }


    public void testUInt32() throws IOException {
        System.out.println(Integer.toBinaryString(65535));
        log.info("java int max value: {}", Integer.MAX_VALUE);
        log.info("go uint32 max value: {}", UnsignedInteger.MAX_VALUE);
        File file = new File("bin/maxUint32.bin");
        ByteSource byteSource = Files.asByteSource(file);
        byte[] bytes = byteSource.read();
        long readUInt32 = ByteNumUtils.ReadUint32(bytes);
        log.info("read MaxUint32 from bin/maxUint32.bin {}", readUInt32);
        assertTrue(readUInt32 == UnsignedInteger.MAX_VALUE.longValue());
    }

    public void testUInt16() throws IOException {
        log.info("java short max value: {}", Short.MAX_VALUE);
        log.info("go uint16 max value: {}", (1 << 16) - 1);
        File file = new File("bin/maxUint16.bin");
        ByteSource byteSource = Files.asByteSource(file);
        byte[] bytes = byteSource.read();
        int readUInt16 = ByteNumUtils.ReadUint16(bytes);
        log.info("read MaxUint16 from bin/maxUint16.bin {}", readUInt16);
        assertTrue(readUInt16 == 65535);
    }

    public void testAA() {
        int max = Integer.MAX_VALUE;
        String maxStr = Integer.toBinaryString(max);
        maxStr = String.format("%32s", maxStr).replace(' ', '0');
        log.info("maxStr: {}", maxStr);

        int min = Integer.MIN_VALUE;
        String minStr = Integer.toBinaryString(min);
        minStr = String.format("%32s", minStr).replace(' ', '0');
        log.info("minStr: {}", minStr);
    }

    public void testAddWithLow16() {
        long num1 = 65535;
        long num2 = -1;
        long result = ByteNumUtils.AddWithLow16(num1, num2);
        long expect = (((long) 1) << 32) - 1;//4294967295
        log.info("Test AddWithLow16: {} + {} = {}, expect:{}", num1, num2, result, expect);
        assertTrue(result == expect);
    }

    public void testReadInt() {
        int value = 123456;
        int lit = Integer.reverseBytes(value);
        byte[] bytes = ByteBuffer.allocate(4).putInt(lit).array();
        int readInt = ByteNumUtils.ReadInt(bytes, true);
        log.info("readInt: {}", readInt);
        assertTrue(readInt == value);
    }

    public void testReadFromBitmapContainer() {
        System.out.println(64 * 64);
        System.out.println(String.format("%64s", Long.toBinaryString(5)).replace(' ', '0'));
    }


    public void testOfficial() throws IOException {
        File file = new File("bin/bitmap32_test_01.bin");
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);

        org.roaringbitmap.RoaringBitmap bitmap = new org.roaringbitmap.RoaringBitmap();
        bitmap.deserialize(dis);

        dis.close();
        fis.close();
        int a[] = bitmap.toArray();
        Arrays.stream(bitmap.toArray()).sequential().forEach(System.out::println);
    }

    // 官方32位的也不兼容, go中可以保存的个数和最大值都会超过java
    public void testOfficialFull() throws IOException {
        File file = new File("bin/full_bitmap.bin");
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);

        org.roaringbitmap.RoaringBitmap bitmap = new org.roaringbitmap.RoaringBitmap();
        bitmap.deserialize(dis);

        dis.close();
        fis.close();
        int a[] = bitmap.toArray();
        System.out.println(a.length);
    }

    // TODO: 2019/12/3 0003  为什么这个测试用例会失败
    //  当一个bitmap被写满的时候会自动转换为runContainer
    //  目前还没有对runContainer进行支持
    public void testMyFull() throws IOException {
        RoaringBitmap roaringBitmap = new RoaringBitmap();
        roaringBitmap.fromFile("bin/full_bitmap.bin");
        List list = roaringBitmap.toSet();
        log.info("list: {}", list.size());
    }


}
