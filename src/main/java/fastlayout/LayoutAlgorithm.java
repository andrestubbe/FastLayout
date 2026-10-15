package fastlayout;

import java.util.List;

/**
 * Common interface for pure zero-allocation layout algorithms.
 */
public interface LayoutAlgorithm {

    /**
     * Measures the content bounding box into a newly created LayoutMeasure.
     */
    default LayoutMeasure measure(
            List<Cell> cells,
            float containerWidth,
            float containerHeight,
            float gap,
            float paddingTop,
            float paddingRight,
            float paddingBottom,
            float paddingLeft
    ) {
        LayoutMeasure out = new LayoutMeasure();
        measure(cells, containerWidth, containerHeight, gap, paddingTop, paddingRight, paddingBottom, paddingLeft, out);
        return out;
    }

    /**
     * Measures the content bounding box into a pre-allocated LayoutMeasure (zero allocations).
     */
    void measure(
            List<Cell> cells,
            float containerWidth,
            float containerHeight,
            float gap,
            float paddingTop,
            float paddingRight,
            float paddingBottom,
            float paddingLeft,
            LayoutMeasure out
    );

    /**
     * Positions and dimensions each cell into pre-allocated Rect objects (AoS format).
     */
    void arrange(
            List<Cell> cells,
            float containerWidth,
            float containerHeight,
            float gap,
            float paddingTop,
            float paddingRight,
            float paddingBottom,
            float paddingLeft,
            Rect[] out
    );

    /**
     * Positions and dimensions each cell into a flat primitive float buffer (SoA / stride format: x, y, w, h).
     * Ideal for direct blitting, OpenGL/Vulkan/DirectX vertex buffers, or cache-aligned batched processing.
     *
     * @param cells input cells
     * @param containerWidth actual container width
     * @param containerHeight actual container height
     * @param gap spacing between consecutive cells
     * @param paddingTop padding top of the container
     * @param paddingRight padding right of the container
     * @param paddingBottom padding bottom of the container
     * @param paddingLeft padding left of the container
     * @param outBounds flat float array of at least offset + cells.size() * 4 elements
     * @param offset starting index in outBounds
     */
    void arrange(
            List<Cell> cells,
            float containerWidth,
            float containerHeight,
            float gap,
            float paddingTop,
            float paddingRight,
            float paddingBottom,
            float paddingLeft,
            float[] outBounds,
            int offset
    );
}
