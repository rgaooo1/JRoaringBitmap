package com.liveramp.ts.roaring.container;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.liveramp.ts.common.ByteNumUtils.ByteArrayToLongArray;
import static com.liveramp.ts.common.Consts.ARRAY_DEFAULT_MAX_SIZE;
import static com.liveramp.ts.common.Consts.BITMAP_CONTAINER_DEFAULT_MAX_SIZE;

/**
 * @author robgao
 * @date 2023/4/14 10:30
 * @className BitmapContainer
 */
@Slf4j
public class BitmapContainer implements Container {
    int cardinality;
    long[] bitmap;

    @Override
    public int readFrom(InputStream inputStream, int cardinality) throws IOException {

        if (cardinality <= ARRAY_DEFAULT_MAX_SIZE) {
            log.error("cardinality is too small, may be it is a array container");
            throw new RuntimeException("cardinality is too small, may be it is a array container");
        } else {

            byte[] data = new byte[BITMAP_CONTAINER_DEFAULT_MAX_SIZE / 8];
            inputStream.read(data);
            bitmap = ByteArrayToLongArray(data);
            cardinality = cardinality;
            return data.length;
        }
    }

    @Override
    public int getCardinality() {
        return cardinality;
    }

    @Override
    public List<Short> toSet() {
        List set = new ArrayList<>();
        for (int i = 0; i < bitmap.length; i++) {
            long bitset = bitmap[i];
            while (bitset != 0L) {
                set.add((short) (i * 64 + Long.numberOfTrailingZeros(bitset)));
                bitset &= bitset - 1L;
            }
        }
        return set;
    }
}
