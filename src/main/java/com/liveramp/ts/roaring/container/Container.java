package com.liveramp.ts.roaring.container;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * @author robgao
 * @date 2023/4/14 10:21
 * @className Container
 */
public interface Container {
    int readFrom(InputStream inputStream, int cardinality) throws IOException;
    int getCardinality();
    List<Short> toSet();
}
