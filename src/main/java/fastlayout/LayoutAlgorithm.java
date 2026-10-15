package fastlayout;

import java.util.List;

/**
 * Common interface for pure zero-allocation layout algorithms.
 */
public interface LayoutAlgorithm {

    /**
     * Measures the minimum/content bounding box required by the cells given constraints.
     *
     * @param cells input cells
     * @param containerWidth available container width (or &lt;= 0 for unconstrained / fit-content)
     * @param containerHeight available container height (or &lt;= 0 for unconstrained / fit-content)
     * @param gap spacing between consecutive cells
     * @param paddingTop padding top of the container
     * @param paddingRight padding right of the container
     * @param paddingBottom padding bottom of the container
     * @param paddingLeft padding left of the container
     * @return LayoutMeasure with computed width and height
     */
    LayoutMeasure measure(
            List<Cell> cells,
            float containerWidth,
            float containerHeight,
            float gap,
            float paddingTop,
            float paddingRight,
            float paddingBottom,
            float paddingLeft
    );

    /**
     * Positions and dimensions each cell into the output bounds.
     *
     * @param cells input cells
     * @param containerWidth actual container width
     * @param containerHeight actual container height
     * @param gap spacing between consecutive cells
     * @param paddingTop padding top of the container
     * @param paddingRight padding right of the container
     * @param paddingBottom padding bottom of the container
     * @param paddingLeft padding left of the container
     * @param out pre-allocated array of Rects where results are written
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
}
