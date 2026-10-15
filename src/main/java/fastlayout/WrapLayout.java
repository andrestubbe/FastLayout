package fastlayout;

import java.util.List;

/**
 * Multi-line / Multi-column responsive wrapping layout (WrapLayout / FlexWrap).
 *
 * <p>Flows items along the main axis until the container boundary is exceeded,
 * then breaks into a new row/column along the cross axis. Ideal for tag clouds,
 * chip lists, icon grids, and responsive toolbars.</p>
 */
public final class WrapLayout implements LayoutAlgorithm {

    private final Axis axis;
    private final Alignment crossAlignment;
    private final float crossGap;

    public WrapLayout() {
        this(Axis.HORIZONTAL, Alignment.START, 0f);
    }

    public WrapLayout(Axis axis, Alignment crossAlignment, float crossGap) {
        this.axis = (axis != null) ? axis : Axis.HORIZONTAL;
        this.crossAlignment = (crossAlignment != null) ? crossAlignment : Alignment.START;
        this.crossGap = crossGap;
    }

    public static WrapLayout horizontal(float crossGap) {
        return new WrapLayout(Axis.HORIZONTAL, Alignment.START, crossGap);
    }

    public static WrapLayout vertical(float crossGap) {
        return new WrapLayout(Axis.VERTICAL, Alignment.START, crossGap);
    }

    public Axis axis() {
        return axis;
    }

    public Alignment crossAlignment() {
        return crossAlignment;
    }

    public float crossGap() {
        return crossGap;
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
        boolean isH = (axis == Axis.HORIZONTAL);
        float maxMain = isH ? (containerWidth - paddingLeft - paddingRight)
                            : (containerHeight - paddingTop - paddingBottom);
        if (maxMain <= 0) maxMain = Float.MAX_VALUE;

        float currentLineMain = 0f;
        float currentLineCross = 0f;
        float totalCross = 0f;
        float widestLine = 0f;
        boolean firstInLine = true;
        boolean firstLine = true;

        for (int i = 0; i < cells.size(); i++) {
            Cell c = cells.get(i);
            float itemMain = isH ? (c.width + c.totalMarginHorizontal()) : (c.height + c.totalMarginVertical());
            float itemCross = isH ? (c.height + c.totalMarginVertical()) : (c.width + c.totalMarginHorizontal());

            if (!firstInLine && (currentLineMain + gap + itemMain > maxMain)) {
                // Break line
                if (currentLineMain > widestLine) widestLine = currentLineMain;
                totalCross += (firstLine ? 0 : crossGap) + currentLineCross;
                firstLine = false;

                currentLineMain = itemMain;
                currentLineCross = itemCross;
                firstInLine = false;
            } else {
                if (!firstInLine) currentLineMain += gap;
                currentLineMain += itemMain;
                if (itemCross > currentLineCross) currentLineCross = itemCross;
                firstInLine = false;
            }
        }

        if (currentLineMain > widestLine) widestLine = currentLineMain;
        totalCross += (firstLine ? 0 : crossGap) + currentLineCross;

        float totalW = isH ? (widestLine + paddingLeft + paddingRight) : (totalCross + paddingLeft + paddingRight);
        float totalH = isH ? (totalCross + paddingTop + paddingBottom) : (widestLine + paddingTop + paddingBottom);

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
        boolean isH = (axis == Axis.HORIZONTAL);
        float maxMain = isH ? (containerWidth - paddingLeft - paddingRight)
                            : (containerHeight - paddingTop - paddingBottom);
        if (maxMain <= 0) maxMain = Float.MAX_VALUE;

        float currentLineMain = 0f;
        float currentLineCross = 0f;
        float totalCross = 0f;
        float widestLine = 0f;
        boolean firstInLine = true;
        boolean firstLine = true;
        int end = offset + count;

        for (int i = offset; i < end; i++) {
            float itemMain = isH ? (arena.width[i] + arena.marginLeft[i] + arena.marginRight[i])
                                 : (arena.height[i] + arena.marginTop[i] + arena.marginBottom[i]);
            float itemCross = isH ? (arena.height[i] + arena.marginTop[i] + arena.marginBottom[i])
                                  : (arena.width[i] + arena.marginLeft[i] + arena.marginRight[i]);

            if (!firstInLine && (currentLineMain + gap + itemMain > maxMain)) {
                if (currentLineMain > widestLine) widestLine = currentLineMain;
                totalCross += (firstLine ? 0 : crossGap) + currentLineCross;
                firstLine = false;

                currentLineMain = itemMain;
                currentLineCross = itemCross;
                firstInLine = false;
            } else {
                if (!firstInLine) currentLineMain += gap;
                currentLineMain += itemMain;
                if (itemCross > currentLineCross) currentLineCross = itemCross;
                firstInLine = false;
            }
        }

        if (currentLineMain > widestLine) widestLine = currentLineMain;
        totalCross += (firstLine ? 0 : crossGap) + currentLineCross;

        float totalW = isH ? (widestLine + paddingLeft + paddingRight) : (totalCross + paddingLeft + paddingRight);
        float totalH = isH ? (totalCross + paddingTop + paddingBottom) : (widestLine + paddingTop + paddingBottom);

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

        boolean isH = (axis == Axis.HORIZONTAL);
        float maxMain = isH ? (containerWidth - paddingLeft - paddingRight)
                            : (containerHeight - paddingTop - paddingBottom);
        if (maxMain <= 0) maxMain = Float.MAX_VALUE;

        float curMain = isH ? paddingLeft : paddingTop;
        float curCross = isH ? paddingTop : paddingLeft;
        float lineCrossMax = 0f;
        boolean firstInLine = true;
        boolean firstLine = true;

        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            float itemMain = isH ? (c.width + c.totalMarginHorizontal()) : (c.height + c.totalMarginVertical());
            float itemCross = isH ? (c.height + c.totalMarginVertical()) : (c.width + c.totalMarginHorizontal());

            if (!firstInLine && (curMain + gap + itemMain > (isH ? paddingLeft : paddingTop) + maxMain)) {
                // Break to next line
                curCross += lineCrossMax + crossGap;
                firstLine = false;
                curMain = isH ? paddingLeft : paddingTop;
                lineCrossMax = 0f;
                firstInLine = true;
            }

            if (!firstInLine) curMain += gap;
            firstInLine = false;

            float x = isH ? (curMain + c.marginLeft) : (curCross + c.marginLeft);
            float y = isH ? (curCross + c.marginTop) : (curMain + c.marginTop);
            float w = c.width;
            float h = c.height;

            if (out != null && i < out.length && out[i] != null) {
                out[i].set(x, y, w, h);
            }

            c.outer.set(
                    isH ? curMain : curCross,
                    isH ? curCross : curMain,
                    isH ? itemMain : itemCross,
                    isH ? itemCross : itemMain
            );
            c.inner.set(x, y, w, h);

            curMain += itemMain;
            if (itemCross > lineCrossMax) lineCrossMax = itemCross;
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

        boolean isH = (axis == Axis.HORIZONTAL);
        float maxMain = isH ? (containerWidth - paddingLeft - paddingRight)
                            : (containerHeight - paddingTop - paddingBottom);
        if (maxMain <= 0) maxMain = Float.MAX_VALUE;

        float curMain = isH ? paddingLeft : paddingTop;
        float curCross = isH ? paddingTop : paddingLeft;
        float lineCrossMax = 0f;
        boolean firstInLine = true;
        boolean firstLine = true;
        int dest = offset;

        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            float itemMain = isH ? (c.width + c.totalMarginHorizontal()) : (c.height + c.totalMarginVertical());
            float itemCross = isH ? (c.height + c.totalMarginVertical()) : (c.width + c.totalMarginHorizontal());

            if (!firstInLine && (curMain + gap + itemMain > (isH ? paddingLeft : paddingTop) + maxMain)) {
                curCross += lineCrossMax + crossGap;
                firstLine = false;
                curMain = isH ? paddingLeft : paddingTop;
                lineCrossMax = 0f;
                firstInLine = true;
            }

            if (!firstInLine) curMain += gap;
            firstInLine = false;

            float x = isH ? (curMain + c.marginLeft) : (curCross + c.marginLeft);
            float y = isH ? (curCross + c.marginTop) : (curMain + c.marginTop);
            float w = c.width;
            float h = c.height;

            if (outBounds != null && dest + 3 < outBounds.length) {
                outBounds[dest]     = x;
                outBounds[dest + 1] = y;
                outBounds[dest + 2] = w;
                outBounds[dest + 3] = h;
            }
            dest += 4;

            c.outer.set(
                    isH ? curMain : curCross,
                    isH ? curCross : curMain,
                    isH ? itemMain : itemCross,
                    isH ? itemCross : itemMain
            );
            c.inner.set(x, y, w, h);

            curMain += itemMain;
            if (itemCross > lineCrossMax) lineCrossMax = itemCross;
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

        boolean isH = (axis == Axis.HORIZONTAL);
        float maxMain = isH ? (containerWidth - paddingLeft - paddingRight)
                            : (containerHeight - paddingTop - paddingBottom);
        if (maxMain <= 0) maxMain = Float.MAX_VALUE;

        float curMain = isH ? paddingLeft : paddingTop;
        float curCross = isH ? paddingTop : paddingLeft;
        float lineCrossMax = 0f;
        boolean firstInLine = true;
        boolean firstLine = true;
        int dest = outOffset;
        int end = slotOffset + count;

        for (int i = slotOffset; i < end; i++) {
            float itemMain = isH ? (arena.width[i] + arena.marginLeft[i] + arena.marginRight[i])
                                 : (arena.height[i] + arena.marginTop[i] + arena.marginBottom[i]);
            float itemCross = isH ? (arena.height[i] + arena.marginTop[i] + arena.marginBottom[i])
                                  : (arena.width[i] + arena.marginLeft[i] + arena.marginRight[i]);

            if (!firstInLine && (curMain + gap + itemMain > (isH ? paddingLeft : paddingTop) + maxMain)) {
                curCross += lineCrossMax + crossGap;
                firstLine = false;
                curMain = isH ? paddingLeft : paddingTop;
                lineCrossMax = 0f;
                firstInLine = true;
            }

            if (!firstInLine) curMain += gap;
            firstInLine = false;

            float x = isH ? (curMain + arena.marginLeft[i]) : (curCross + arena.marginLeft[i]);
            float y = isH ? (curCross + arena.marginTop[i]) : (curMain + arena.marginTop[i]);
            float w = arena.width[i];
            float h = arena.height[i];

            if (outBounds != null && dest + 3 < outBounds.length) {
                outBounds[dest]     = x;
                outBounds[dest + 1] = y;
                outBounds[dest + 2] = w;
                outBounds[dest + 3] = h;
            }
            dest += 4;

            curMain += itemMain;
            if (itemCross > lineCrossMax) lineCrossMax = itemCross;
        }
    }
}
