package com.liveramp.ts.roaring;

import com.liveramp.ts.roaring64.Roaring64Bitmap;
import lombok.extern.slf4j.Slf4j;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author robgao
 * @date 2023/4/17 16:25
 * @className TestRoaring64
 */
@Slf4j
public class TestRoaring64 extends junit.framework.TestCase {

    public boolean writeAndTest(long val, String path) throws IOException {
        Roaring64Bitmap roaring64Bitmap = new Roaring64Bitmap();
        roaring64Bitmap.add(val);
        roaring64Bitmap.writeExternal(path, true);
        Roaring64Bitmap roaring64BitmapRead = new Roaring64Bitmap();
        roaring64BitmapRead.readExternal(path);
        return roaring64BitmapRead.contains(val);
    }

    /**
     * 测试0
     *
     * @throws IOException
     */
    public void testZero() throws IOException {
        long value = 0L;
        String path = "./bin/roaring64_java_zero.bin";
        log.info("test zero");
        assertTrue(writeAndTest(value, path));
    }

    /**
     * 测试uint8最大值
     *
     * @throws IOException
     */
    public void testUint8Max() throws IOException {
        long value = 0xFFL;
        String path = "./bin/roaring64_java_uint8_max.bin";
        log.info("test uint8 max");
        assertTrue(writeAndTest(value, path));
    }

    /**
     * 测试uint16最大值
     *
     * @throws IOException
     */
    public void testUint16Max() throws IOException {
        long value = 0xFFFFL;
        String path = "./bin/roaring64_java_uint16_max.bin";
        log.info("test uint16 max");
        assertTrue(writeAndTest(value, path));
    }

    /**
     * 测试uint32最大值
     *
     * @throws IOException
     */
    public void testUint32Max() throws IOException {
        long value = 0xFFFFFFFFL;
        String path = "./bin/roaring64_java_uint32_max.bin";
        log.info("test uint32 max");
        assertTrue(writeAndTest(value, path));
    }

    /**
     * 测试uint64最大值
     *
     * @throws IOException
     */
    public void testUint64Max() throws IOException {
        long value = 0xFFFFFFFFFFFFFFFFL;
        String path = "./bin/roaring64_java_uint64_max.bin";
        log.info("test uint64 max");
        assertTrue(writeAndTest(value, path));
    }

    /**
     * 测试随机10w个值
     *
     * @throws IOException
     */
    public void testRandom() throws IOException {
        log.info("test random");
        Roaring64Bitmap roaring64Bitmap = new Roaring64Bitmap();
        List<Long> values = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            long value = (long) (Math.random() * Long.MAX_VALUE);
            values.add(value);
        }
        long start = System.currentTimeMillis();
        for (long value : values) {
            roaring64Bitmap.add(value);
        }
        long end = System.currentTimeMillis();
        log.info("add {} values cost {} ms", values.size(), end - start);

        String path = "./bin/roaring64_java_random.bin";
        roaring64Bitmap.writeExternal(path, true);

        Roaring64Bitmap roaring64BitmapRead = new Roaring64Bitmap();
        roaring64BitmapRead.readExternal("./bin/roaring64_java_random.bin");

        for (long value : values) {
            assertTrue(roaring64Bitmap.contains(value));
        }
    }


    /**
     * 测试读取随机数文件
     * @throws IOException
     */
    public void testRead() throws IOException {
        log.info("test read");
        Roaring64Bitmap roaring64BitmapRead = new Roaring64Bitmap();
        roaring64BitmapRead.readExternal("./bin/roaring64_java_random3.bin");
        assertTrue(roaring64BitmapRead.getLongCardinality() == 100000);
    }

    /**
     * 测试获取字节
     * @throws IOException
     */
    public void testGetBytes() throws IOException {
        log.info("测试获取字节");
        Roaring64Bitmap roaring64Bitmap = new Roaring64Bitmap();
        List<Long> values = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            long value = (long) (Math.random() * Long.MAX_VALUE);
            values.add(value);
        }
        long start = System.currentTimeMillis();
        for (long value : values) {
            roaring64Bitmap.add(value);
        }
        long end = System.currentTimeMillis();
        log.info("add {} values cost {} ms", values.size(), end - start);

        byte[] bytes = roaring64Bitmap.getBytes();

        // save bytes to file

        FileOutputStream fos = new FileOutputStream("./bin/roaring64_java_random3.bin");
        fos.write(bytes);
        fos.flush();
        fos.close();

        Roaring64Bitmap roaring64BitmapRead = new Roaring64Bitmap();
        roaring64BitmapRead.readExternal("./bin/roaring64_java_random3.bin");
        assertTrue(roaring64BitmapRead.getLongCardinality() == values.size());


    }

    /**
     * 测试迭代器输出的顺序
     */
    public void testOrder() throws IOException {
        Roaring64Bitmap roaring64Bitmap = new Roaring64Bitmap();
        for (int i = 0; i <= 100; i++) {
            roaring64Bitmap.add(i);
        }
        roaring64Bitmap.add(0xFFL);
        roaring64Bitmap.add(0xFFFFL);
        roaring64Bitmap.add(0xFFFFFFFFL);
        roaring64Bitmap.add(0xFFFFFFFFFFFFFFFFL);
        roaring64Bitmap.writeExternal("./bin/roaring64_java_order.bin", true);
        AtomicInteger count = new AtomicInteger();
        roaring64Bitmap.iterator().forEachRemaining(x -> {
            count.getAndIncrement();
            log.info("{}", Roaring64Bitmap.toUint64Str(x));
        });
        log.info("count: {}", count.get());
        assertTrue(count.get() == 105);
    }


    public void testReadGo() throws IOException {
        log.info("test read go bitmap 64");
        Roaring64Bitmap roaring64Bitmap = new Roaring64Bitmap();
        roaring64Bitmap.readExternal("./bin/bitmap64go.bin");
        roaring64Bitmap.iterator().forEachRemaining(x -> {
            log.info("{}", Roaring64Bitmap.toUint64Str(x));
        });
    }


    /**
     * 测试交集
     */
    public void testAnd() {
        log.info("测试交集");
        Roaring64Bitmap roaring64Bitmap1 = new Roaring64Bitmap();
        roaring64Bitmap1.add(1L);
        roaring64Bitmap1.add(2L);
        roaring64Bitmap1.add(3L);
        roaring64Bitmap1.add((1L << 32) + 1L);

        Roaring64Bitmap roaring64Bitmap2 = new Roaring64Bitmap();
        roaring64Bitmap2.add(2L);
        roaring64Bitmap2.add(3L);
        roaring64Bitmap1.and(roaring64Bitmap2);
        roaring64Bitmap1.iterator().forEachRemaining(x -> {
            log.info("{}", Roaring64Bitmap.toUint64Str(x));
        });


        assertTrue(roaring64Bitmap1.contains(2L));
        assertTrue(roaring64Bitmap1.contains(3L));
        assertFalse(roaring64Bitmap1.contains(1L));
        assertFalse(roaring64Bitmap1.contains((1L << 32) + 1L));
    }

    /**
     * 测试并集
     */
    public void testOr() {
        log.info("测试并集");
        Roaring64Bitmap roaring64Bitmap1 = new Roaring64Bitmap();
        roaring64Bitmap1.add(1L);
        roaring64Bitmap1.add(2L);
        roaring64Bitmap1.add(3L);
        roaring64Bitmap1.add((1L << 32) + 1L);

        Roaring64Bitmap roaring64Bitmap2 = new Roaring64Bitmap();
        roaring64Bitmap2.add(2L);
        roaring64Bitmap2.add(3L);

        roaring64Bitmap1.or(roaring64Bitmap2);
        roaring64Bitmap1.iterator().forEachRemaining(x -> {
            log.info("{}", Roaring64Bitmap.toUint64Str(x));
        });
        assertTrue(roaring64Bitmap1.contains(1L));
        assertTrue(roaring64Bitmap1.contains(2L));
        assertTrue(roaring64Bitmap1.contains(3L));
        assertTrue(roaring64Bitmap1.contains((1L << 32) + 1L));
    }

    /**
     * 测试差集
     */
    public void testNot() {
        log.info("测试差集");
        Roaring64Bitmap roaring64Bitmap1 = new Roaring64Bitmap();
        roaring64Bitmap1.add(1L);
        roaring64Bitmap1.add(2L);
        roaring64Bitmap1.add(3L);
        roaring64Bitmap1.add((1L << 32) + 1L);
        roaring64Bitmap1.add((1L << 32) + (1L << 32) + 1L);

        Roaring64Bitmap roaring64Bitmap2 = new Roaring64Bitmap();
        roaring64Bitmap2.add(2L);
        roaring64Bitmap2.add(3L);
        roaring64Bitmap1.not(roaring64Bitmap2);

        roaring64Bitmap1.iterator().forEachRemaining(x -> {
            log.info("{}", Roaring64Bitmap.toUint64Str(x));
        });
    }

    /**
     * 测试读取生产go的bitmap
     */
    public void testReadSegments() throws IOException {
        log.info("测试读取生产go的bitmap");
        Roaring64Bitmap roaring64Bitmap = new Roaring64Bitmap();
        roaring64Bitmap.readExternal("./bin/68c4f633-4bec-45ce-915d-f0218590c820");
        System.out.println(roaring64Bitmap.getLongCardinality());
        assertTrue(roaring64Bitmap.getLongCardinality() == 6846797);
    }

    /**
     * 测试读取生产go的bitmap
     */
    public void testReadSegments2() throws IOException {
        log.info("测试读取生产go的bitmap");
        Roaring64Bitmap roaring64Bitmap = new Roaring64Bitmap();
        roaring64Bitmap.readExternal("./bin/50c0e997-41c1-41ae-ac7e-0cb827124fd8");
        assertTrue(roaring64Bitmap.getLongCardinality() == 6616427);
    }

}
