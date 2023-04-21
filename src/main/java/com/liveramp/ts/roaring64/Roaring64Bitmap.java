package com.liveramp.ts.roaring64;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.primitives.UnsignedLong;
import com.liveramp.ts.common.ByteNumUtils;
import lombok.extern.slf4j.Slf4j;
import org.roaringbitmap.RoaringBitmap;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * @author robgao
 * @date 2023/4/17 10:27
 * @className Roaring64Bitmap
 * TODO: 1. And Or Xor AndNot 2 Go file test case
 */
@Slf4j
public class Roaring64Bitmap {
    RoaringArray64 highlowcontainer;

    public Roaring64Bitmap() {
        highlowcontainer = new RoaringArray64();
    }

    public UnsignedLong getCardinality() {
        UnsignedLong cardinality = UnsignedLong.ZERO;
        for (Map.Entry<Integer, RoaringBitmap> entry : highlowcontainer.map.entrySet()) {
            cardinality.plus(UnsignedLong.valueOf(entry.getValue().getLongCardinality()));
        }
        return cardinality;
    }

    public long getLongCardinality() {
        long cardinality = 0;
        for (Map.Entry<Integer, RoaringBitmap> entry : highlowcontainer.map.entrySet()) {
            cardinality += entry.getValue().getLongCardinality();
        }

        // 超过long最大值,第一位符号位变成了1 ,会变成负数, 一般也用不了这么大
        if (cardinality < 0) {
            log.warn("the cardinality is too large, if you want to get the right cardinality, please use getCardinality()");
        }
        return cardinality;
    }

    public void add(long value) {
        int high = (int) (value >> 32);
        int low = (int) value;
        RoaringBitmap bitmap = highlowcontainer.map.get(high);
        if (bitmap == null) {
            bitmap = new RoaringBitmap();
            highlowcontainer.add(high, bitmap);
        }
        bitmap.add(low);
    }

    public boolean contains(long value) {
        int high = (int) (value >> 32);
        int low = (int) value;
        RoaringBitmap bitmap = highlowcontainer.map.get(high);
        if (bitmap == null) {
            return false;
        }
        return bitmap.contains(low);
    }

    public void readExternal(String path) throws IOException {
        File file = new File(path);
        readExternal(file);
    }

    public void readExternal(File file) throws IOException {
        if (!file.exists()) {
            throw new RuntimeException("file not exist");
        }
        if (!file.isFile()) {
            throw new RuntimeException("path is not a file");
        }
        log.info("read bitmap file from : {}", file.getAbsolutePath());
        ByteSource byteSource = Files.asByteSource(file);
        readExternal(byteSource);
    }

    public void writeExternal(String path, boolean overwrite) throws IOException {
        File file = new File(path);
        writeExternal(file, overwrite);
    }

    public void readExternal(ByteSource byteSource) throws IOException {
        BufferedInputStream stream = (BufferedInputStream) byteSource.openBufferedStream();
        readExternal(stream);
    }


    // [0-7] 8 bytes key size
    // [8-11] 4 bytes key
    // [12-unknown] bytes roaring32 bitmap
    // 4 bytes key
    // unknown bytes roaring32 bitmap
    // 4 bytes key
    // unknown bytes roaring32 bitmap
    // ... ...
    public void readExternal(InputStream stream) throws IOException {
        try {
            DataInput input = new DataInputStream(stream);
            long keySize;
            log.info("Available: {} bytes", stream.available());

            keySize = Long.reverseBytes(input.readLong());
            log.info("keySize: {}", keySize);

            for (int i = 0; i < keySize; i++) {
                int key = Integer.reverseBytes(input.readInt());
                RoaringBitmap bitmap = new RoaringBitmap();
                bitmap.deserialize(input);
                highlowcontainer.add(key, bitmap);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            stream.close();
            log.info("read bitmap file done");
        }
    }

    public void writeExternal(File file, boolean overwrite) throws IOException {
        if (file.exists() && !overwrite) {
            throw new RuntimeException("file exist");
        }
        // file write stream
        RandomAccessFile dos = null;
        try {
            dos = new RandomAccessFile(file, "rw");
            long keySize = 0L;
            // 不能直接用 highlowcontainer.size()
            // 如果key的个数 超过int_max,值是不准确的
            // long keySize = highlowcontainer.size();

            // write key size 8 bytes
            byte[] bytes = {0, 0, 0, 0, 0, 0, 0, 0};
            dos.write(bytes);
            for (Map.Entry<Integer, RoaringBitmap> entry : highlowcontainer.map.entrySet()) {
                // write key 4 bytes
                byte[] keyBytes = ByteNumUtils.toByteArray(Integer.reverseBytes(entry.getKey()));
                // write bitmap ? bytes
                dos.write(keyBytes);
                entry.getValue().serialize(dos);

                keySize++;
            }
            // write key size 8 bytes Little Endian
            bytes = ByteNumUtils.toByteArray(Long.reverseBytes(keySize));
            dos.seek(0);
            dos.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            dos.close();
        }
    }

    public byte[] getBytes() {
        ByteArrayOutputStream first = new ByteArrayOutputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            long keySize = 0L;
            for (Map.Entry<Integer, RoaringBitmap> entry : highlowcontainer.map.entrySet()) {
                // write key 4 bytes
                byte[] keyBytes = ByteNumUtils.toByteArray(Integer.reverseBytes(entry.getKey()));
                // write bitmap ? bytes
                bos.write(keyBytes);
                ByteBuffer byteBuffer = ByteBuffer.allocate(entry.getValue().serializedSizeInBytes());
                entry.getValue().serialize(byteBuffer);
                bos.write(byteBuffer.array());
                keySize++;
            }
            first.write(ByteNumUtils.toByteArray(Long.reverseBytes(keySize)));
            first.write(bos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return first.toByteArray();
    }

    public static String toUint64Str(long value) {
        UnsignedLong unsignedLong = UnsignedLong.fromLongBits(value);
        return unsignedLong.toString();
    }

    public void or(Roaring64Bitmap other) {
        for (Map.Entry<Integer, RoaringBitmap> entry : other.highlowcontainer.map.entrySet()) {
            int high = entry.getKey();
            RoaringBitmap lowBitmap = entry.getValue();
            RoaringBitmap thisLowBitmap = highlowcontainer.map.get(high);
            if (thisLowBitmap != null) {
                thisLowBitmap.or(lowBitmap);
            } else {
                highlowcontainer.map.put(high, lowBitmap);
            }
        }
    }

    public void and(Roaring64Bitmap other) {
        Set<Integer> removeList = new HashSet<>();
        for (Map.Entry<Integer, RoaringBitmap> entry : other.highlowcontainer.map.entrySet()) {
            int high = entry.getKey();
            RoaringBitmap lowBitmap = entry.getValue();
            RoaringBitmap thisLowBitmap = highlowcontainer.map.get(high);
            if (thisLowBitmap != null) {
                thisLowBitmap.and(lowBitmap);
                if (thisLowBitmap.isEmpty()) {
                    removeList.add(high);
                }
            }
        }

        for (Map.Entry<Integer, RoaringBitmap> entry : highlowcontainer.map.entrySet()) {
            int high = entry.getKey();
            if (!other.highlowcontainer.map.containsKey(high)) {
                removeList.add(high);
            }
        }

        for (Integer high : removeList) {
            highlowcontainer.map.remove(high);
        }
    }

    public void not(Roaring64Bitmap other) {
        for (Map.Entry<Integer, RoaringBitmap> entry : other.highlowcontainer.map.entrySet()) {
            int high = entry.getKey();
            RoaringBitmap lowBitmap = entry.getValue();
            RoaringBitmap thisLowBitmap = highlowcontainer.map.get(high);
            if (thisLowBitmap != null) {
                thisLowBitmap.andNot(lowBitmap);
                if (thisLowBitmap.isEmpty()) {
                    highlowcontainer.map.remove(high);
                }
            }
        }
    }

    /**
     * iterator the bitmap
     *
     * @return
     */
    public Iterator<Long> iterator() {
        return new Iterator<Long>() {
            Iterator<Map.Entry<Integer, RoaringBitmap>> iterator = highlowcontainer.map.entrySet().iterator();
            Iterator<Integer> lowIterator = null;
            int high = 0;

            @Override
            public boolean hasNext() {
                if (lowIterator == null) {
                    if (iterator.hasNext()) {
                        Map.Entry<Integer, RoaringBitmap> entry = iterator.next();
                        high = entry.getKey();
                        lowIterator = entry.getValue().iterator();
                    } else {
                        return false;
                    }
                }
                if (lowIterator.hasNext()) {
                    return true;
                } else {
                    if (iterator.hasNext()) {
                        Map.Entry<Integer, RoaringBitmap> entry = iterator.next();
                        high = entry.getKey();
                        lowIterator = entry.getValue().iterator();
                        return hasNext();
                    } else {
                        return false;
                    }
                }
            }

            @Override
            public Long next() {
                if (hasNext()) {
                    int low = lowIterator.next();
                    // high 32 bit + low 32 bit
                    return ((long) high << 32) | (low & 0xFFFFFFFFL);
                } else {
                    return null;
                }
            }
        };
    }
}
