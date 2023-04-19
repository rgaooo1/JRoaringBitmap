package com.liveramp.ts.roaring64;

import lombok.extern.slf4j.Slf4j;
import org.roaringbitmap.RoaringBitmap;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;


/**
 * @author robgao
 * @date 2023/4/17 10:29
 * @className RoaringArray64
 */
@Slf4j
public class RoaringArray64 {
    Map<Integer, RoaringBitmap> map;
    boolean[] needCopyOnWrite;
    boolean copyOnWrite;

    public RoaringArray64() {
        map = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer k1, Integer k2) {
//                byte[] k1Bytes = Ints.toByteArray(k1);
//                byte[] k2Bytes = Ints.toByteArray(k2);
//                byte[] k1LongBytes = {
//                        0, 0, 0, 0, k1Bytes[0], k1Bytes[1], k1Bytes[2], k1Bytes[3]
//                };
//                byte[] k2LongBytes = {
//                        0, 0, 0, 0, k2Bytes[0], k2Bytes[1], k2Bytes[2], k2Bytes[3]
//                };
//                return (int) (Longs.fromByteArray(k2LongBytes) - Longs.fromByteArray(k1LongBytes));
                return k2 - k1;
            }
        });
    }

    public void add(int key, RoaringBitmap bitmap) {
        map.put(key, bitmap);
    }

    public long size() {
        log.warn("the key size in java max is {}, If  contains more than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE", Integer.MAX_VALUE);
        return map.size();
    }
}
