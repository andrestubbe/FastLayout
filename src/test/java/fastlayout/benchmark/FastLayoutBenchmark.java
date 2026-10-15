package fastlayout.benchmark;

import fastlayout.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(0)
public class FastLayoutBenchmark {

    @Param({"10", "100", "1000"})
    private int elementCount;

    private LinearLayout horizontalLayout;
    private LinearLayout verticalLayout;

    private List<Cell> cells;
    private Rect[] outRects;
    private float[] outFloatBuffer;
    private LayoutMeasure reusableMeasure;

    @Setup(Level.Trial)
    public void setup() {
        horizontalLayout = LinearLayout.horizontal(Alignment.CENTER);
        verticalLayout = LinearLayout.vertical(Alignment.STRETCH);

        cells = new ArrayList<>(elementCount);
        outRects = new Rect[elementCount];
        outFloatBuffer = new float[elementCount * 4];
        reusableMeasure = new LayoutMeasure();

        for (int i = 0; i < elementCount; i++) {
            Cell c = new Cell(32, 32);
            c.margin(2);
            if (i % 5 == 0) {
                c.weight = 1.0f; // some flex items
            }
            cells.add(c);
            outRects[i] = new Rect();
        }
    }

    @Benchmark
    public void benchmarkMeasureZeroAlloc(Blackhole bh) {
        horizontalLayout.measure(cells, 0, 0, 4, 8, 8, 8, 8, reusableMeasure);
        bh.consume(reusableMeasure.width);
        bh.consume(reusableMeasure.height);
    }

    @Benchmark
    public void benchmarkArrangeAoS_RectArray(Blackhole bh) {
        horizontalLayout.arrange(cells, 1000, 50, 4, 8, 8, 8, 8, outRects);
        bh.consume(outRects);
    }

    @Benchmark
    public void benchmarkArrangeBuffer_FlatFloats(Blackhole bh) {
        horizontalLayout.arrange(cells, 1000, 50, 4, 8, 8, 8, 8, outFloatBuffer, 0);
        bh.consume(outFloatBuffer);
    }

    @Benchmark
    public void benchmarkVerticalArrangeAoS_RectArray(Blackhole bh) {
        verticalLayout.arrange(cells, 200, 5000, 4, 8, 8, 8, 8, outRects);
        bh.consume(outRects);
    }

    @Benchmark
    public void benchmarkVerticalArrangeBuffer_FlatFloats(Blackhole bh) {
        verticalLayout.arrange(cells, 200, 5000, 4, 8, 8, 8, 8, outFloatBuffer, 0);
        bh.consume(outFloatBuffer);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FastLayoutBenchmark.class.getSimpleName())
                .forks(0)
                .warmupIterations(1)
                .measurementIterations(2)
                .build();
        new Runner(opt).run();
    }
}
