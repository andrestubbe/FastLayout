package fastlayout;

import java.util.List;

/**
 * 2D Uniform Grid Layout (GridLayout).
 *
 * <p>Arranges elements into fixed rows and columns with uniform cell dimensions,
 * column-major or row-major flow, and zero-allocation execution.</p>
 */
public final class GridLayout implements LayoutAlgorithm {

    private final int columns;
    private final int rows;
    private final float rowGap;
    private final float colGap;

    public GridLayout(int columns, int rows) {
        this(columns, rows, 0f, 0f);
    }

    public GridLayout(int columns, int rows, float rowGap, float colGap) {
        this.columns = Math.max(1, columns);
        this.rows = Math.max(0, rows); // 0 = dynamic based on element count
        this.rowGap = rowGap;
        this.colGap = colGap;
    }

    public static GridLayout columns(int columns, float gap) {
        return new GridLayout(columns, 0, gap, gap);
    }

    public static GridLayout columns(int columns, float rowGap, float colGap) {
        return new GridLayout(columns, 0, rowGap, colGap);
    }

    public int columns() {
        return columns;
    }

    public int rows() {
        return rows;
    }

    public float rowGap() {
        return rowGap;
    }

    public float colGap() {
        return colGap;
    }

    private int computeRowCount(int itemCount) {
        if (rows > 0) return rows;
        return (itemCount + columns - 1) / columns;
    }

    @Override
    public void measure(
            List<Cell> cells,
            float containerWidth,
            float containerHeight,
            float gap,
            float paddingTop,
            float paddingRight,
            float paddingBottom,
            float paddingLeft,
            LayoutMeasure out
    ) {
        int n = cells.size();
        int rCount = computeRowCount(n);
        float rGap = (rowGap > 0) ? rowGap : gap;
        float cGap = (colGap > 0) ? colGap : gap;

        float maxCellW = 0f;
        float maxCellH = 0f;

        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            float w = c.width + c.totalMarginHorizontal();
            float h = c.height + c.totalMarginVertical();
            if (w > maxCellW) maxCellW = w;
            if (h > maxCellH) maxCellH = h;
        }

        float totalW = (maxCellW * columns) + (cGap * (columns - 1)) + paddingLeft + paddingRight;
        float totalH = (maxCellH * rCount) + (rGap * Math.max(0, rCount - 1)) + paddingTop + paddingBottom;

        if (containerWidth > 0 && totalW < containerWidth) totalW = containerWidth;
        if (containerHeight > 0 && totalH < containerHeight) totalH = containerHeight;

        out.set(totalW, totalH);
    }

    @Override
    public void measure(
            LayoutArena arena,
            int offset,
            int count,
            float containerWidth,
            float containerHeight,
            float gap,
            float paddingTop,
            float paddingRight,
            float paddingBottom,
            float paddingLeft,
            LayoutMeasure out
    ) {
        int rCount = computeRowCount(count);
        float rGap = (rowGap > 0) ? rowGap : gap;
        float cGap = (colGap > 0) ? colGap : gap;

        float maxCellW = 0f;
        float maxCellH = 0f;
        int end = offset + count;

        for (int i = offset; i < end; i++) {
            float w = arena.width[i] + arena.marginLeft[i] + arena.marginRight[i];
            float h = arena.height[i] + arena.marginTop[i] + arena.marginBottom[i];
            if (w > maxCellW) maxCellW = w;
            if (h > maxCellH) maxCellH = h;
        }

        float totalW = (maxCellW * columns) + (cGap * (columns - 1)) + paddingLeft + paddingRight;
        float totalH = (maxCellH * rCount) + (rGap * Math.max(0, rCount - 1)) + paddingTop + paddingBottom;

        if (containerWidth > 0 && totalW < containerWidth) totalW = containerWidth;
        if (containerHeight > 0 && totalH < containerHeight) totalH = containerHeight;

        out.set(totalW, totalH);
    }

    @Override
    public void arrange(
            List<Cell> cells,
            float containerWidth,
            float containerHeight,
            float gap,
            float paddingTop,
            float paddingRight,
            float paddingBottom,
            float paddingLeft,
            Rect[] out
    ) {
        int n = cells.size();
        if (n == 0) return;

        int rCount = computeRowCount(n);
        float rGap = (rowGap > 0) ? rowGap : gap;
        float cGap = (colGap > 0) ? colGap : gap;

        float availW = containerWidth - paddingLeft - paddingRight - (cGap * (columns - 1));
        float availH = containerHeight - paddingTop - paddingBottom - (rGap * Math.max(0, rCount - 1));

        float cellW = Math.max(0f, availW / columns);
        float cellH = Math.max(0f, availH / Math.max(1, rCount));

        for (int i = 0; i < n; i++) {
            int col = i % columns;
            int row = i / columns;

            float cellX = paddingLeft + (col * (cellW + cGap));
            float cellY = paddingTop + (row * (cellH + rGap));

            Cell c = cells.get(i);
            float x = cellX + c.marginLeft;
            float y = cellY + c.marginTop;
            float w = Math.max(0f, cellW - c.totalMarginHorizontal());
            float h = Math.max(0f, cellH - c.totalMarginVertical());

            if (out != null && i < out.length && out[i] != null) {
                out[i].set(x, y, w, h);
            }

            c.outer.set(cellX, cellY, cellW, cellH);
            c.inner.set(x, y, w, h);
        }
    }

    @Override
    public void arrange(
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
    ) {
        int n = cells.size();
        if (n == 0) return;

        int rCount = computeRowCount(n);
        float rGap = (rowGap > 0) ? rowGap : gap;
        float cGap = (colGap > 0) ? colGap : gap;

        float availW = containerWidth - paddingLeft - paddingRight - (cGap * (columns - 1));
        float availH = containerHeight - paddingTop - paddingBottom - (rGap * Math.max(0, rCount - 1));

        float cellW = Math.max(0f, availW / columns);
        float cellH = Math.max(0f, availH / Math.max(1, rCount));
        int dest = offset;

        for (int i = 0; i < n; i++) {
            int col = i % columns;
            int row = i / columns;

            float cellX = paddingLeft + (col * (cellW + cGap));
            float cellY = paddingTop + (row * (cellH + rGap));

            Cell c = cells.get(i);
            float x = cellX + c.marginLeft;
            float y = cellY + c.marginTop;
            float w = Math.max(0f, cellW - c.totalMarginHorizontal());
            float h = Math.max(0f, cellH - c.totalMarginVertical());

            if (outBounds != null && dest + 3 < outBounds.length) {
                outBounds[dest]     = x;
                outBounds[dest + 1] = y;
                outBounds[dest + 2] = w;
                outBounds[dest + 3] = h;
            }
            dest += 4;

            c.outer.set(cellX, cellY, cellW, cellH);
            c.inner.set(x, y, w, h);
        }
    }

    @Override
    public void arrange(
            LayoutArena arena,
            int slotOffset,
            int count,
            float containerWidth,
            float containerHeight,
            float gap,
            float paddingTop,
            float paddingRight,
            float paddingBottom,
            float paddingLeft,
            float[] outBounds,
            int outOffset
    ) {
        if (count == 0) return;

        int rCount = computeRowCount(count);
        float rGap = (rowGap > 0) ? rowGap : gap;
        float cGap = (colGap > 0) ? colGap : gap;

        float availW = containerWidth - paddingLeft - paddingRight - (cGap * (columns - 1));
        float availH = containerHeight - paddingTop - paddingBottom - (rGap * Math.max(0, rCount - 1));

        float cellW = Math.max(0f, availW / columns);
        float cellH = Math.max(0f, availH / Math.max(1, rCount));
        int dest = outOffset;
        int end = slotOffset + count;

        for (int i = slotOffset; i < end; i++) {
            int idx = i - slotOffset;
            int col = idx % columns;
            int row = idx / columns;

            float cellX = paddingLeft + (col * (cellW + cGap));
            float cellY = paddingTop + (row * (cellH + rGap));

            float x = cellX + arena.marginLeft[i];
            float y = cellY + arena.marginTop[i];
            float w = Math.max(0f, cellW - (arena.marginLeft[i] + arena.marginRight[i]));
            float h = Math.max(0f, cellH - (arena.marginTop[i] + arena.marginBottom[i]));

            if (outBounds != null && dest + 3 < outBounds.length) {
                outBounds[dest]     = x;
                outBounds[dest + 1] = y;
                outBounds[dest + 2] = w;
                outBounds[dest + 3] = h;
            }
            dest += 4;
        }
    }
}
