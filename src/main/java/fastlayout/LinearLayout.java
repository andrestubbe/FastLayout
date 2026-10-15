package fastlayout;

import java.util.List;

/**
 * High-performance 1D Linear Layout (Row / Column / ToolBar / Popup Menu / Settings).
 *
 * <p>Features monomorphic loops optimized for HotSpot C2 inlining, zero GC allocations,
 * and support for both AoS ({@code Rect[]}), Struct-of-Arrays ({@link LayoutArena}),
 * and flat contiguous float buffers ({@code float[]} stride: x, y, w, h).</p>
 */
public final class LinearLayout implements LayoutAlgorithm {

    private final Axis axis;
    private final Alignment crossAlignment;
    private final Alignment mainAlignment;

    public LinearLayout(Axis axis) {
        this(axis, (axis == Axis.HORIZONTAL) ? Alignment.CENTER : Alignment.STRETCH, Alignment.START);
    }

    public LinearLayout(Axis axis, Alignment crossAlignment) {
        this(axis, crossAlignment, Alignment.START);
    }

    public LinearLayout(Axis axis, Alignment crossAlignment, Alignment mainAlignment) {
        this.axis = (axis != null) ? axis : Axis.HORIZONTAL;
        this.crossAlignment = (crossAlignment != null) ? crossAlignment : Alignment.CENTER;
        this.mainAlignment = (mainAlignment != null) ? mainAlignment : Alignment.START;
    }

    public static LinearLayout horizontal() {
        return new LinearLayout(Axis.HORIZONTAL, Alignment.CENTER, Alignment.START);
    }

    public static LinearLayout horizontal(Alignment crossAlignment) {
        return new LinearLayout(Axis.HORIZONTAL, crossAlignment, Alignment.START);
    }

    public static LinearLayout vertical() {
        return new LinearLayout(Axis.VERTICAL, Alignment.STRETCH, Alignment.START);
    }

    public static LinearLayout vertical(Alignment crossAlignment) {
        return new LinearLayout(Axis.VERTICAL, crossAlignment, Alignment.START);
    }

    public Axis axis() {
        return axis;
    }

    public Alignment crossAlignment() {
        return crossAlignment;
    }

    public Alignment mainAlignment() {
        return mainAlignment;
    }

    // --- Cell List Measurement ---
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
        if (axis == Axis.HORIZONTAL) {
            measureHorizontal(cells, n, containerWidth, containerHeight, gap, paddingTop, paddingRight, paddingBottom, paddingLeft, out);
        } else {
            measureVertical(cells, n, containerWidth, containerHeight, gap, paddingTop, paddingRight, paddingBottom, paddingLeft, out);
        }
    }

    private void measureHorizontal(
            List<Cell> cells,
            int n,
            float containerWidth,
            float containerHeight,
            float gap,
            float paddingTop,
            float paddingRight,
            float paddingBottom,
            float paddingLeft,
            LayoutMeasure out
    ) {
        float mainAccum = 0f;
        float crossMax = 0f;
        boolean first = true;

        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            if (!first) mainAccum += gap;
            first = false;

            float mainSize = c.width + c.marginLeft + c.marginRight;
            float crossSize = c.height + c.marginTop + c.marginBottom;

            mainAccum += mainSize;
            if (crossSize > crossMax) crossMax = crossSize;
        }

        float totalW = mainAccum + paddingLeft + paddingRight;
        float totalH = crossMax + paddingTop + paddingBottom;

        if (containerWidth > 0 && totalW < containerWidth) totalW = containerWidth;
        if (containerHeight > 0 && totalH < containerHeight) totalH = containerHeight;

        out.set(totalW, totalH);
    }

    private void measureVertical(
            List<Cell> cells,
            int n,
            float containerWidth,
            float containerHeight,
            float gap,
            float paddingTop,
            float paddingRight,
            float paddingBottom,
            float paddingLeft,
            LayoutMeasure out
    ) {
        float mainAccum = 0f;
        float crossMax = 0f;
        boolean first = true;

        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            if (!first) mainAccum += gap;
            first = false;

            float crossSize = c.width + c.marginLeft + c.marginRight;
            float mainSize = c.height + c.marginTop + c.marginBottom;

            mainAccum += mainSize;
            if (crossSize > crossMax) crossMax = crossSize;
        }

        float totalW = crossMax + paddingLeft + paddingRight;
        float totalH = mainAccum + paddingTop + paddingBottom;

        if (containerWidth > 0 && totalW < containerWidth) totalW = containerWidth;
        if (containerHeight > 0 && totalH < containerHeight) totalH = containerHeight;

        out.set(totalW, totalH);
    }

    // --- LayoutArena Measurement (Pure SoA) ---
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
        if (axis == Axis.HORIZONTAL) {
            measureHorizontalArena(arena, offset, count, containerWidth, containerHeight, gap, paddingTop, paddingRight, paddingBottom, paddingLeft, out);
        } else {
            measureVerticalArena(arena, offset, count, containerWidth, containerHeight, gap, paddingTop, paddingRight, paddingBottom, paddingLeft, out);
        }
    }

    private void measureHorizontalArena(
            LayoutArena a, int offset, int count,
            float containerWidth, float containerHeight,
            float gap,
            float paddingTop, float paddingRight, float paddingBottom, float paddingLeft,
            LayoutMeasure out
    ) {
        float mainAccum = 0f;
        float crossMax = 0f;
        boolean first = true;
        int end = offset + count;

        for (int i = offset; i < end; i++) {
            if (!first) mainAccum += gap;
            first = false;

            float mainSize = a.width[i] + a.marginLeft[i] + a.marginRight[i];
            float crossSize = a.height[i] + a.marginTop[i] + a.marginBottom[i];

            mainAccum += mainSize;
            if (crossSize > crossMax) crossMax = crossSize;
        }

        float totalW = mainAccum + paddingLeft + paddingRight;
        float totalH = crossMax + paddingTop + paddingBottom;

        if (containerWidth > 0 && totalW < containerWidth) totalW = containerWidth;
        if (containerHeight > 0 && totalH < containerHeight) totalH = containerHeight;

        out.set(totalW, totalH);
    }

    private void measureVerticalArena(
            LayoutArena a, int offset, int count,
            float containerWidth, float containerHeight,
            float gap,
            float paddingTop, float paddingRight, float paddingBottom, float paddingLeft,
            LayoutMeasure out
    ) {
        float mainAccum = 0f;
        float crossMax = 0f;
        boolean first = true;
        int end = offset + count;

        for (int i = offset; i < end; i++) {
            if (!first) mainAccum += gap;
            first = false;

            float crossSize = a.width[i] + a.marginLeft[i] + a.marginRight[i];
            float mainSize = a.height[i] + a.marginTop[i] + a.marginBottom[i];

            mainAccum += mainSize;
            if (crossSize > crossMax) crossMax = crossSize;
        }

        float totalW = crossMax + paddingLeft + paddingRight;
        float totalH = mainAccum + paddingTop + paddingBottom;

        if (containerWidth > 0 && totalW < containerWidth) totalW = containerWidth;
        if (containerHeight > 0 && totalH < containerHeight) totalH = containerHeight;

        out.set(totalW, totalH);
    }

    // --- Cell List Arrangement (AoS) ---
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

        if (axis == Axis.HORIZONTAL) {
            arrangeHorizontalAoS(cells, n, containerWidth, containerHeight, gap, paddingTop, paddingRight, paddingBottom, paddingLeft, out);
        } else {
            arrangeVerticalAoS(cells, n, containerWidth, containerHeight, gap, paddingTop, paddingRight, paddingBottom, paddingLeft, out);
        }
    }

    // --- Cell List Arrangement (Flat Float Buffer) ---
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

        if (axis == Axis.HORIZONTAL) {
            arrangeHorizontalBuffer(cells, n, containerWidth, containerHeight, gap, paddingTop, paddingRight, paddingBottom, paddingLeft, outBounds, offset);
        } else {
            arrangeVerticalBuffer(cells, n, containerWidth, containerHeight, gap, paddingTop, paddingRight, paddingBottom, paddingLeft, outBounds, offset);
        }
    }

    // --- LayoutArena Arrangement (Pure SoA -> Flat Buffer Kernel) ---
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

        if (axis == Axis.HORIZONTAL) {
            arrangeHorizontalArena(arena, slotOffset, count, containerWidth, containerHeight, gap, paddingTop, paddingRight, paddingBottom, paddingLeft, outBounds, outOffset);
        } else {
            arrangeVerticalArena(arena, slotOffset, count, containerWidth, containerHeight, gap, paddingTop, paddingRight, paddingBottom, paddingLeft, outBounds, outOffset);
        }
    }

    // --- Monomorphic Horizontal (AoS) ---
    private void arrangeHorizontalAoS(
            List<Cell> cells, int n,
            float containerWidth, float containerHeight,
            float gap,
            float paddingTop, float paddingRight, float paddingBottom, float paddingLeft,
            Rect[] out
    ) {
        float availableMain = containerWidth - paddingLeft - paddingRight;
        float availableCross = containerHeight - paddingTop - paddingBottom;
        if (availableCross < 0) availableCross = 0;

        float totalWeight = 0f;
        float fixedMain = 0f;
        float gapTotal = 0f;
        boolean first = true;

        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            if (!first) gapTotal += gap;
            first = false;

            float marginH = c.marginLeft + c.marginRight;
            if (c.weight > 0) {
                totalWeight += c.weight;
                fixedMain += marginH;
            } else {
                fixedMain += c.width + marginH;
            }
        }

        float remainingMain = availableMain - fixedMain - gapTotal;
        if (remainingMain < 0) remainingMain = 0;

        float curMain = paddingLeft;
        if (totalWeight <= 0 && remainingMain > 0) {
            if (mainAlignment == Alignment.CENTER) {
                curMain += remainingMain / 2f;
            } else if (mainAlignment == Alignment.END) {
                curMain += remainingMain;
            }
        }

        first = true;
        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            if (!first) curMain += gap;
            first = false;

            float mainDim;
            if (c.weight > 0 && totalWeight > 0) {
                mainDim = (c.weight / totalWeight) * remainingMain;
            } else {
                mainDim = c.width;
            }

            if (c.minWidth > 0 && mainDim < c.minWidth) mainDim = c.minWidth;
            if (c.maxWidth > 0 && mainDim > c.maxWidth) mainDim = c.maxWidth;

            float availableForCross = availableCross - (c.marginTop + c.marginBottom);
            if (availableForCross < 0) availableForCross = 0;

            Alignment align = (c.crossAlignment != null) ? c.crossAlignment : this.crossAlignment;
            float crossDim = c.height;
            float crossPos;

            if (align == Alignment.STRETCH) {
                crossDim = availableForCross;
                crossPos = paddingTop + c.marginTop;
            } else if (align == Alignment.CENTER) {
                crossPos = paddingTop + c.marginTop + (availableForCross - crossDim) / 2f;
            } else if (align == Alignment.END) {
                crossPos = paddingTop + availableCross - c.marginBottom - crossDim;
            } else {
                crossPos = paddingTop + c.marginTop;
            }

            if (c.minHeight > 0 && crossDim < c.minHeight) crossDim = c.minHeight;
            if (c.maxHeight > 0 && crossDim > c.maxHeight) crossDim = c.maxHeight;

            float x = curMain + c.marginLeft;
            float y = crossPos;
            float w = mainDim;
            float h = crossDim;

            if (out != null && i < out.length && out[i] != null) {
                out[i].set(x, y, w, h);
            }

            c.outer.set(curMain, paddingTop, mainDim + c.marginLeft + c.marginRight, availableCross);
            c.inner.set(x, y, w, h);

            curMain += mainDim + c.marginLeft + c.marginRight;
        }
    }

    // --- Monomorphic Vertical (AoS) ---
    private void arrangeVerticalAoS(
            List<Cell> cells, int n,
            float containerWidth, float containerHeight,
            float gap,
            float paddingTop, float paddingRight, float paddingBottom, float paddingLeft,
            Rect[] out
    ) {
        float availableMain = containerHeight - paddingTop - paddingBottom;
        float availableCross = containerWidth - paddingLeft - paddingRight;
        if (availableCross < 0) availableCross = 0;

        float totalWeight = 0f;
        float fixedMain = 0f;
        float gapTotal = 0f;
        boolean first = true;

        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            if (!first) gapTotal += gap;
            first = false;

            float marginV = c.marginTop + c.marginBottom;
            if (c.weight > 0) {
                totalWeight += c.weight;
                fixedMain += marginV;
            } else {
                fixedMain += c.height + marginV;
            }
        }

        float remainingMain = availableMain - fixedMain - gapTotal;
        if (remainingMain < 0) remainingMain = 0;

        float curMain = paddingTop;
        if (totalWeight <= 0 && remainingMain > 0) {
            if (mainAlignment == Alignment.CENTER) {
                curMain += remainingMain / 2f;
            } else if (mainAlignment == Alignment.END) {
                curMain += remainingMain;
            }
        }

        first = true;
        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            if (!first) curMain += gap;
            first = false;

            float mainDim;
            if (c.weight > 0 && totalWeight > 0) {
                mainDim = (c.weight / totalWeight) * remainingMain;
            } else {
                mainDim = c.height;
            }

            if (c.minHeight > 0 && mainDim < c.minHeight) mainDim = c.minHeight;
            if (c.maxHeight > 0 && mainDim > c.maxHeight) mainDim = c.maxHeight;

            float availableForCross = availableCross - (c.marginLeft + c.marginRight);
            if (availableForCross < 0) availableForCross = 0;

            Alignment align = (c.crossAlignment != null) ? c.crossAlignment : this.crossAlignment;
            float crossDim = c.width;
            float crossPos;

            if (align == Alignment.STRETCH) {
                crossDim = availableForCross;
                crossPos = paddingLeft + c.marginLeft;
            } else if (align == Alignment.CENTER) {
                crossPos = paddingLeft + c.marginLeft + (availableForCross - crossDim) / 2f;
            } else if (align == Alignment.END) {
                crossPos = paddingLeft + availableCross - c.marginRight - crossDim;
            } else {
                crossPos = paddingLeft + c.marginLeft;
            }

            if (c.minWidth > 0 && crossDim < c.minWidth) crossDim = c.minWidth;
            if (c.maxWidth > 0 && crossDim > c.maxWidth) crossDim = c.maxWidth;

            float x = crossPos;
            float y = curMain + c.marginTop;
            float w = crossDim;
            float h = mainDim;

            if (out != null && i < out.length && out[i] != null) {
                out[i].set(x, y, w, h);
            }

            c.outer.set(paddingLeft, curMain, availableCross, mainDim + c.marginTop + c.marginBottom);
            c.inner.set(x, y, w, h);

            curMain += mainDim + c.marginTop + c.marginBottom;
        }
    }

    // --- Monomorphic Horizontal (Cell List -> Flat Buffer) ---
    private void arrangeHorizontalBuffer(
            List<Cell> cells, int n,
            float containerWidth, float containerHeight,
            float gap,
            float paddingTop, float paddingRight, float paddingBottom, float paddingLeft,
            float[] outBounds, int offset
    ) {
        float availableMain = containerWidth - paddingLeft - paddingRight;
        float availableCross = containerHeight - paddingTop - paddingBottom;
        if (availableCross < 0) availableCross = 0;

        float totalWeight = 0f;
        float fixedMain = 0f;
        float gapTotal = 0f;
        boolean first = true;

        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            if (!first) gapTotal += gap;
            first = false;

            float marginH = c.marginLeft + c.marginRight;
            if (c.weight > 0) {
                totalWeight += c.weight;
                fixedMain += marginH;
            } else {
                fixedMain += c.width + marginH;
            }
        }

        float remainingMain = availableMain - fixedMain - gapTotal;
        if (remainingMain < 0) remainingMain = 0;

        float curMain = paddingLeft;
        if (totalWeight <= 0 && remainingMain > 0) {
            if (mainAlignment == Alignment.CENTER) {
                curMain += remainingMain / 2f;
            } else if (mainAlignment == Alignment.END) {
                curMain += remainingMain;
            }
        }

        first = true;
        int dest = offset;
        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            if (!first) curMain += gap;
            first = false;

            float mainDim;
            if (c.weight > 0 && totalWeight > 0) {
                mainDim = (c.weight / totalWeight) * remainingMain;
            } else {
                mainDim = c.width;
            }

            if (c.minWidth > 0 && mainDim < c.minWidth) mainDim = c.minWidth;
            if (c.maxWidth > 0 && mainDim > c.maxWidth) mainDim = c.maxWidth;

            float availableForCross = availableCross - (c.marginTop + c.marginBottom);
            if (availableForCross < 0) availableForCross = 0;

            Alignment align = (c.crossAlignment != null) ? c.crossAlignment : this.crossAlignment;
            float crossDim = c.height;
            float crossPos;

            if (align == Alignment.STRETCH) {
                crossDim = availableForCross;
                crossPos = paddingTop + c.marginTop;
            } else if (align == Alignment.CENTER) {
                crossPos = paddingTop + c.marginTop + (availableForCross - crossDim) / 2f;
            } else if (align == Alignment.END) {
                crossPos = paddingTop + availableCross - c.marginBottom - crossDim;
            } else {
                crossPos = paddingTop + c.marginTop;
            }

            if (c.minHeight > 0 && crossDim < c.minHeight) crossDim = c.minHeight;
            if (c.maxHeight > 0 && crossDim > c.maxHeight) crossDim = c.maxHeight;

            float x = curMain + c.marginLeft;
            float y = crossPos;
            float w = mainDim;
            float h = crossDim;

            if (outBounds != null && dest + 3 < outBounds.length) {
                outBounds[dest]     = x;
                outBounds[dest + 1] = y;
                outBounds[dest + 2] = w;
                outBounds[dest + 3] = h;
            }
            dest += 4;

            c.outer.set(curMain, paddingTop, mainDim + c.marginLeft + c.marginRight, availableCross);
            c.inner.set(x, y, w, h);

            curMain += mainDim + c.marginLeft + c.marginRight;
        }
    }

    // --- Monomorphic Vertical (Cell List -> Flat Buffer) ---
    private void arrangeVerticalBuffer(
            List<Cell> cells, int n,
            float containerWidth, float containerHeight,
            float gap,
            float paddingTop, float paddingRight, float paddingBottom, float paddingLeft,
            float[] outBounds, int offset
    ) {
        float availableMain = containerHeight - paddingTop - paddingBottom;
        float availableCross = containerWidth - paddingLeft - paddingRight;
        if (availableCross < 0) availableCross = 0;

        float totalWeight = 0f;
        float fixedMain = 0f;
        float gapTotal = 0f;
        boolean first = true;

        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            if (!first) gapTotal += gap;
            first = false;

            float marginV = c.marginTop + c.marginBottom;
            if (c.weight > 0) {
                totalWeight += c.weight;
                fixedMain += marginV;
            } else {
                fixedMain += c.height + marginV;
            }
        }

        float remainingMain = availableMain - fixedMain - gapTotal;
        if (remainingMain < 0) remainingMain = 0;

        float curMain = paddingTop;
        if (totalWeight <= 0 && remainingMain > 0) {
            if (mainAlignment == Alignment.CENTER) {
                curMain += remainingMain / 2f;
            } else if (mainAlignment == Alignment.END) {
                curMain += remainingMain;
            }
        }

        first = true;
        int dest = offset;
        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            if (!first) curMain += gap;
            first = false;

            float mainDim;
            if (c.weight > 0 && totalWeight > 0) {
                mainDim = (c.weight / totalWeight) * remainingMain;
            } else {
                mainDim = c.height;
            }

            if (c.minHeight > 0 && mainDim < c.minHeight) mainDim = c.minHeight;
            if (c.maxHeight > 0 && mainDim > c.maxHeight) mainDim = c.maxHeight;

            float availableForCross = availableCross - (c.marginLeft + c.marginRight);
            if (availableForCross < 0) availableForCross = 0;

            Alignment align = (c.crossAlignment != null) ? c.crossAlignment : this.crossAlignment;
            float crossDim = c.width;
            float crossPos;

            if (align == Alignment.STRETCH) {
                crossDim = availableForCross;
                crossPos = paddingLeft + c.marginLeft;
            } else if (align == Alignment.CENTER) {
                crossPos = paddingLeft + c.marginLeft + (availableForCross - crossDim) / 2f;
            } else if (align == Alignment.END) {
                crossPos = paddingLeft + availableCross - c.marginRight - crossDim;
            } else {
                crossPos = paddingLeft + c.marginLeft;
            }

            if (c.minWidth > 0 && crossDim < c.minWidth) crossDim = c.minWidth;
            if (c.maxWidth > 0 && crossDim > c.maxWidth) crossDim = c.maxWidth;

            float x = crossPos;
            float y = curMain + c.marginTop;
            float w = crossDim;
            float h = mainDim;

            if (outBounds != null && dest + 3 < outBounds.length) {
                outBounds[dest]     = x;
                outBounds[dest + 1] = y;
                outBounds[dest + 2] = w;
                outBounds[dest + 3] = h;
            }
            dest += 4;

            c.outer.set(paddingLeft, curMain, availableCross, mainDim + c.marginTop + c.marginBottom);
            c.inner.set(x, y, w, h);

            curMain += mainDim + c.marginTop + c.marginBottom;
        }
    }

    // --- Monomorphic Horizontal (Arena SoA -> Flat Buffer Kernel) ---
    private void arrangeHorizontalArena(
            LayoutArena a, int offset, int count,
            float containerWidth, float containerHeight,
            float gap,
            float paddingTop, float paddingRight, float paddingBottom, float paddingLeft,
            float[] outBounds, int outOffset
    ) {
        float availableMain = containerWidth - paddingLeft - paddingRight;
        float availableCross = containerHeight - paddingTop - paddingBottom;
        if (availableCross < 0) availableCross = 0;

        float totalWeight = 0f;
        float fixedMain = 0f;
        float gapTotal = 0f;
        boolean first = true;
        int end = offset + count;

        for (int i = offset; i < end; i++) {
            if (!first) gapTotal += gap;
            first = false;

            float marginH = a.marginLeft[i] + a.marginRight[i];
            float w = a.weight[i];
            if (w > 0) {
                totalWeight += w;
                fixedMain += marginH;
            } else {
                fixedMain += a.width[i] + marginH;
            }
        }

        float remainingMain = availableMain - fixedMain - gapTotal;
        if (remainingMain < 0) remainingMain = 0;

        float curMain = paddingLeft;
        if (totalWeight <= 0 && remainingMain > 0) {
            if (mainAlignment == Alignment.CENTER) {
                curMain += remainingMain / 2f;
            } else if (mainAlignment == Alignment.END) {
                curMain += remainingMain;
            }
        }

        first = true;
        int dest = outOffset;
        Alignment defaultAlign = this.crossAlignment;

        for (int i = offset; i < end; i++) {
            if (!first) curMain += gap;
            first = false;

            float cellWeight = a.weight[i];
            float mainDim;
            if (cellWeight > 0 && totalWeight > 0) {
                mainDim = (cellWeight / totalWeight) * remainingMain;
            } else {
                mainDim = a.width[i];
            }

            float minW = a.minWidth[i];
            float maxW = a.maxWidth[i];
            if (minW > 0 && mainDim < minW) mainDim = minW;
            if (maxW > 0 && mainDim > maxW) mainDim = maxW;

            float marginTop = a.marginTop[i];
            float marginBottom = a.marginBottom[i];
            float availableForCross = availableCross - (marginTop + marginBottom);
            if (availableForCross < 0) availableForCross = 0;

            byte alignOrd = a.crossAlignment[i];
            Alignment align = (alignOrd >= 0) ? Alignment.values()[alignOrd] : defaultAlign;

            float crossDim = a.height[i];
            float crossPos;

            if (align == Alignment.STRETCH) {
                crossDim = availableForCross;
                crossPos = paddingTop + marginTop;
            } else if (align == Alignment.CENTER) {
                crossPos = paddingTop + marginTop + (availableForCross - crossDim) / 2f;
            } else if (align == Alignment.END) {
                crossPos = paddingTop + availableCross - marginBottom - crossDim;
            } else {
                crossPos = paddingTop + marginTop;
            }

            float minH = a.minHeight[i];
            float maxH = a.maxHeight[i];
            if (minH > 0 && crossDim < minH) crossDim = minH;
            if (maxH > 0 && crossDim > maxH) crossDim = maxH;

            float marginLeft = a.marginLeft[i];
            float x = curMain + marginLeft;
            float y = crossPos;
            float w = mainDim;
            float h = crossDim;

            if (outBounds != null && dest + 3 < outBounds.length) {
                outBounds[dest]     = x;
                outBounds[dest + 1] = y;
                outBounds[dest + 2] = w;
                outBounds[dest + 3] = h;
            }
            dest += 4;

            curMain += mainDim + marginLeft + a.marginRight[i];
        }
    }

    // --- Monomorphic Vertical (Arena SoA -> Flat Buffer Kernel) ---
    private void arrangeVerticalArena(
            LayoutArena a, int offset, int count,
            float containerWidth, float containerHeight,
            float gap,
            float paddingTop, float paddingRight, float paddingBottom, float paddingLeft,
            float[] outBounds, int outOffset
    ) {
        float availableMain = containerHeight - paddingTop - paddingBottom;
        float availableCross = containerWidth - paddingLeft - paddingRight;
        if (availableCross < 0) availableCross = 0;

        float totalWeight = 0f;
        float fixedMain = 0f;
        float gapTotal = 0f;
        boolean first = true;
        int end = offset + count;

        for (int i = offset; i < end; i++) {
            if (!first) gapTotal += gap;
            first = false;

            float marginV = a.marginTop[i] + a.marginBottom[i];
            float w = a.weight[i];
            if (w > 0) {
                totalWeight += w;
                fixedMain += marginV;
            } else {
                fixedMain += a.height[i] + marginV;
            }
        }

        float remainingMain = availableMain - fixedMain - gapTotal;
        if (remainingMain < 0) remainingMain = 0;

        float curMain = paddingTop;
        if (totalWeight <= 0 && remainingMain > 0) {
            if (mainAlignment == Alignment.CENTER) {
                curMain += remainingMain / 2f;
            } else if (mainAlignment == Alignment.END) {
                curMain += remainingMain;
            }
        }

        first = true;
        int dest = outOffset;
        Alignment defaultAlign = this.crossAlignment;

        for (int i = offset; i < end; i++) {
            if (!first) curMain += gap;
            first = false;

            float cellWeight = a.weight[i];
            float mainDim;
            if (cellWeight > 0 && totalWeight > 0) {
                mainDim = (cellWeight / totalWeight) * remainingMain;
            } else {
                mainDim = a.height[i];
            }

            float minH = a.minHeight[i];
            float maxH = a.maxHeight[i];
            if (minH > 0 && mainDim < minH) mainDim = minH;
            if (maxH > 0 && mainDim > maxH) mainDim = maxH;

            float marginLeft = a.marginLeft[i];
            float marginRight = a.marginRight[i];
            float availableForCross = availableCross - (marginLeft + marginRight);
            if (availableForCross < 0) availableForCross = 0;

            byte alignOrd = a.crossAlignment[i];
            Alignment align = (alignOrd >= 0) ? Alignment.values()[alignOrd] : defaultAlign;

            float crossDim = a.width[i];
            float crossPos;

            if (align == Alignment.STRETCH) {
                crossDim = availableForCross;
                crossPos = paddingLeft + marginLeft;
            } else if (align == Alignment.CENTER) {
                crossPos = paddingLeft + marginLeft + (availableForCross - crossDim) / 2f;
            } else if (align == Alignment.END) {
                crossPos = paddingLeft + availableCross - marginRight - crossDim;
            } else {
                crossPos = paddingLeft + marginLeft;
            }

            float minW = a.minWidth[i];
            float maxW = a.maxWidth[i];
            if (minW > 0 && crossDim < minW) crossDim = minW;
            if (maxW > 0 && crossDim > maxW) crossDim = maxW;

            float marginTop = a.marginTop[i];
            float x = crossPos;
            float y = curMain + marginTop;
            float w = crossDim;
            float h = mainDim;

            if (outBounds != null && dest + 3 < outBounds.length) {
                outBounds[dest]     = x;
                outBounds[dest + 1] = y;
                outBounds[dest + 2] = w;
                outBounds[dest + 3] = h;
            }
            dest += 4;

            curMain += mainDim + marginTop + a.marginBottom[i];
        }
    }
}
