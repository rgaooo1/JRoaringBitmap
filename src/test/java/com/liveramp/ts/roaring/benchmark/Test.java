package com.liveramp.ts.roaring.benchmark;

import com.liveramp.ts.roaring.TestRoaring64;
import com.liveramp.ts.roaring64.Roaring64Bitmap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author robgao
 * @date 2023/4/18 09:53
 * @className Test
 */
@BenchmarkMode(Mode.All)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 5)
@Threads(4)
@Fork(1)
@State(value = Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class Test {


    List<Long> values = new ArrayList<>();
    Roaring64Bitmap roaring64Bitmap = new Roaring64Bitmap();
    @Setup(Level.Trial) // 初始化方法，在全部Benchmark运行之前进行
    public void init() {
        for (int i = 0; i < 100000; i++) {
            long value = (long) (Math.random() * Long.MAX_VALUE);
            values.add(value);
        }

    }

    @Benchmark
    public void f1(Blackhole blackhole) throws IOException {

            roaring64Bitmap.add(1);

    }
    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder().include(Test.class.getSimpleName()).build();
        new Runner(options).run();
    }
}
