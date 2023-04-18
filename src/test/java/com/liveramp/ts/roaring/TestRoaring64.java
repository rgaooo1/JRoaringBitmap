package com.liveramp.ts.roaring;

import com.liveramp.ts.roaring64.Roaring64Bitmap;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.Benchmark;

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
     * @throws IOException
     */
    public void testRandom() throws IOException {
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

        for (long value : values) {
            assertTrue(roaring64Bitmap.contains(value));
        }
    }

    /**
     * 测试迭代器输出的顺序
     */
    public void testOrder() {
//        RoaringBitmap roaringBitmap = new RoaringBitmap();

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
        Roaring64Bitmap roaring64Bitmap = new Roaring64Bitmap();
        roaring64Bitmap.readExternal("./bin/bitmap64go.bin");
        roaring64Bitmap.iterator().forEachRemaining(x -> {
            log.info("{}", Roaring64Bitmap.toUint64Str(x));
        });
    }

}
