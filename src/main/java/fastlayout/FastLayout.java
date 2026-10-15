package fastlayout;

import java.util.ArrayList;
import java.util.List;

/**
 * FastLayout engine façade providing convenient entry points and zero-allocation execution.
 */
public final class FastLayout {

    private FastLayout() {}

    /**
     * Measures the minimal/ideal bounds required by the cells for the given layout.
     */
    public static LayoutMeasure measure(
            LayoutAlgorithm algo,
            List<Cell> cells,
            float containerWidth,
            float containerHeight,
            float gap,
            float padding
    ) {
        return algo.measure(cells, containerWidth, containerHeight, gap, padding, padding, padding, padding);
    }

    /**
     * Measures the minimal/ideal bounds required by the cells with separate horizontal/vertical padding.
     */
    public static LayoutMeasure measure(
            LayoutAlgorithm algo,
            List<Cell> cells,
            float containerWidth,
            float containerHeight,
            float gap,
            float paddingV,
            float paddingH
    ) {
        return algo.measure(cells, containerWidth, containerHeight, gap, paddingV, paddingH, paddingV, paddingH);
    }

    /**
     * Arranges cells into pre-allocated Rect bounds.
     */
    public static void arrange(
            LayoutAlgorithm algo,
            List<Cell> cells,
            float containerWidth,
            float containerHeight,
            float gap,
            float padding,
            Rect[] out
    ) {
        algo.arrange(cells, containerWidth, containerHeight, gap, padding, padding, padding, padding, out);
    }

    /**
     * Helper to arrange cells and return a new list of Rects (useful for quick setup).
     */
    public static List<Rect> arrange(
            LayoutAlgorithm algo,
            List<Cell> cells,
            float containerWidth,
            float containerHeight,
            float gap,
            float padding
    ) {
        Rect[] out = new Rect[cells.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = new Rect();
        }
        algo.arrange(cells, containerWidth, containerHeight, gap, padding, padding, padding, padding, out);
        List<Rect> list = new ArrayList<>(out.length);
        for (Rect r : out) list.add(r);
        return list;
    }
}
