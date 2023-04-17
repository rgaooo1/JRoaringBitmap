package com.liveramp.ts.roaring;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.liveramp.ts.common.Consts;
import com.liveramp.ts.common.ByteNumUtils;
import com.liveramp.ts.roaring.container.ArrayContainer;
import com.liveramp.ts.roaring.container.BitmapContainer;
import com.liveramp.ts.roaring.container.Container;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.liveramp.ts.common.Consts.ARRAY_DEFAULT_MAX_SIZE;
import static com.liveramp.ts.common.Consts.MAX_KEY_SIZE;


/**
 * @author robgao
 * @date 2023/4/13 18:57
 * @className RoaringBitmap
 */
@Slf4j
public class RoaringBitmap {

    static {
        log.error("这个是在学习时做的32位bitmap, 不要使用 \n com.liveramp.ts.roaring64.Roaring64Bitmap 可以兼容go生成的64位bitmap");
        System.exit(1);
    }
    RoaringArray highlowcontainer;

    public RoaringBitmap() {
        log.error("这个是在学习时做的32位bitmap, 请不要使用, com.liveramp.ts.roaring64.Roaring64Bitmap 可以兼容go生成的64位bitmap");
        System.exit(1);
        highlowcontainer = new RoaringArray();
    }

    public void fromFile(String path) throws IOException {
        File file = new File(path);
        fromFile(file);
    }

    public void fromFile(File file) throws IOException {
        if (!file.exists()) {
            throw new RuntimeException("file not exist");
        }
        if (!file.isFile()) {
            throw new RuntimeException("path is not a file");
        }
        log.info("read bitmap file from : {}", file.getAbsolutePath());
        ByteSource byteSource = Files.asByteSource(file);
        fromBytes(byteSource);
    }

    public List<Long> toSet() {
        List<Long> list = new ArrayList<>();
        for (int i = 0; i < highlowcontainer.containers.length; i++) {
            int key = highlowcontainer.keys[i];
            Container container = highlowcontainer.containers[i];
            List<Short> set = container.toSet();
            for (short v : set) {
                long value = ByteNumUtils.AddWithLow16(key, v);
                list.add(value);
            }
        }
        return list;
    }


    public long getCardinality() {
        long cardinality = 0;
        for (int i = 0; i < highlowcontainer.containers.length; i++) {
            Container container = highlowcontainer.containers[i];
            cardinality += container.getCardinality();
        }
        return cardinality;
    }

    /**
     * 4bytes cookie
     * 4bytes keySize
     * keySize * (2+2)  bytes (2bytes key 2bytes cardinality)
     * keySize * (4) bytes each container offset(uint32)
     * .........
     * ...data..
     * .........
     *
     * @param byteSource
     * @throws IOException
     */
    public void fromBytes(ByteSource byteSource) throws IOException {
        int n = 0;
        int cookie;
        // go uint32 max : 4294967295 int存不下, 用long
        long keySize;
        try (InputStream stream = byteSource.openBufferedStream()) {
            log.info("Available: {} bytes", stream.available());

            /** 读取cookie **/
            byte[] cookieBytes = stream.readNBytes(4);
            n += 4;
            cookie = ByteNumUtils.ReadInt(cookieBytes, true);
            cookie = cookie & 0x0000FFFF;

            if (cookie == Consts.serialCookie) {
                // TODO 当bitmap容器写满了,会自动转成RunContainer,这里还是需要支持的
                log.error("Cookie {{}}, not support runs currently", cookie);
                return;
            }
            byte[] sizeBytes = stream.readNBytes(4);
            n += 4;
            /** 读取KeySize **/
            keySize = ByteNumUtils.ReadUint32(sizeBytes);
            log.info("Cookie: {{}}   KeySize: {{}}", cookie, keySize);

            if (keySize > MAX_KEY_SIZE) {
                log.error("It is logically impossible to have more than (1<<16) containers");
                return;
            }

            /** 读取每个key和容器的容量cardinality **/
            // key : uint16
            // Cardinality : uint16
            // kc: means key and cardinality
            int[][] kc = new int[(int) keySize][2];
            for (int i = 0; i < keySize; i++) {
                int key = ByteNumUtils.ReadUint16(stream.readNBytes(2));
                int cardinality = ByteNumUtils.ReadUint16(stream.readNBytes(2));
                kc[i][0] = key;

                /**
                 这里需要加一
                 uint16的最大值是65535, 但是一个容器里最多可以存65536个元素
                 为了用uint16存储,需要减一
                 如果key存在,容器里就一定会有元素
                 **/
                kc[i][1] = cardinality + 1;
            }

            n += (2 + 2) * (int) keySize;
            /** 读取每个容器的offset(容器数据的开始位置) **/
            int[] offsets = new int[(int) keySize];
            for (int i = 0; i < keySize; i++) {
                offsets[i] = ByteNumUtils.ReadInt(stream.readNBytes(4), true);
                n += 4;
            }
            for (int i = 0; i < keySize; i++) {
                log.info("Key:{{}},Cardinality:{{}}, Offset: {{}}", kc[i][0], kc[i][1], offsets[i]);
            }

            this.highlowcontainer.keys = new int[(int) keySize];
            this.highlowcontainer.containers = new Container[(int) keySize];
            for (int i = 0; i < keySize; i++) {
                this.highlowcontainer.keys[i] = kc[i][0];
            }
            /** 读取每个容器的数据 **/
            for (int i = 0; i < keySize; i++) {
                int cardinality = kc[i][1];
                if (cardinality == 0) {
                    log.error("Cardinality is 0, it is logically impossible");
                    throw new RuntimeException("Cardinality is 0, it is logically impossible");
                } else if (cardinality <= ARRAY_DEFAULT_MAX_SIZE) {
                    this.highlowcontainer.containers[i] = new ArrayContainer();
                    n += this.highlowcontainer.containers[i].readFrom(stream, cardinality);
                } else {
                    this.highlowcontainer.containers[i] = new BitmapContainer();
                    n += this.highlowcontainer.containers[i].readFrom(stream, cardinality);
                }
            }
            log.info("Read {{}} bytes", n);
            int available = stream.available();
            log.info("Available: {{}}", available);
        }
    }
}
