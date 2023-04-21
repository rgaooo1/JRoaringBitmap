package com.liveramp.ts.roaring.container;

import com.google.common.collect.Sets;
import com.liveramp.ts.common.ByteNumUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.liveramp.ts.common.Consts.ARRAY_DEFAULT_MAX_SIZE;


/**
 * @author robgao
 * @date 2023/4/14 10:29
 * @className ArrayContainer
 */
@Slf4j
public class ArrayContainer implements Container {
    short[] content;
    @Override
    public int readFrom(InputStream inputStream, int cardinality) throws IOException {
        if (cardinality > ARRAY_DEFAULT_MAX_SIZE) {
            log.error("cardinality is too large, may be it is a bitmap container");
            throw new RuntimeException("cardinality is too large, may be it is a bitmap container");
        }
        // uint16 2 bytes
        byte[] data = new byte[cardinality * 2];
        inputStream.read(data);
        content = ByteNumUtils.ByteArrayToShortArray(data);
        return data.length;
    }

    @Override
    public int getCardinality() {
        return content.length;
    }

    @Override
    public List<Short> toSet() {
        List<Short> set = new ArrayList<>();
        for (short s : content) {
            set.add(s);
        }
        return set;
    }
}
