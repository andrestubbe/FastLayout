package fastlayout;

import java.util.List;

/**
 * 1D Linear Layout (Row / Column / ToolBar / Popup Menu / Settings).
 *
 * <p>Unifies horizontal and vertical layouts into a single mathematical engine.
 * The primary axis determines accumulation (sum of sizes + gaps + flex weights),
 * while the cross axis determines alignment (START, CENTER, END, STRETCH).</p>
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
        this.crossAlignment = crossAlignment;
        this.mainAlignment = mainAlignment;
    }

    // Factory methods
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

    @Override
    public LayoutMeasure measure(
            List<Cell> cells,
            float containerWidth,
            float containerHeight,
            float gap,
            float paddingTop,
            float paddingRight,
            float paddingBottom,
            float paddingLeft
    ) {
        boolean isH = (axis == Axis.HORIZONTAL);
        float mainAccum = 0f;
        float crossMax = 0f;
        boolean first = true;

        for (int i = 0; i < cells.size(); i++) {
            Cell c = cells.get(i);
            if (!first) {
                mainAccum += gap;
            }
            first = false;

            float mainSize = isH ? (c.width + c.totalMarginHorizontal()) : (c.height + c.totalMarginVertical());
            float crossSize = isH ? (c.height + c.totalMarginVertical()) : (c.width + c.totalMarginHorizontal());

            mainAccum += mainSize;
            if (crossSize > crossMax) {
                crossMax = crossSize;
            }
        }

        float totalW, totalH;
        if (isH) {
            totalW = mainAccum + paddingLeft + paddingRight;
            totalH = crossMax + paddingTop + paddingBottom;
        } else {
            totalW = crossMax + paddingLeft + paddingRight;
            totalH = mainAccum + paddingTop + paddingBottom;
        }

        if (containerWidth > 0 && totalW < containerWidth) totalW = containerWidth;
        if (containerHeight > 0 && totalH < containerHeight) totalH = containerHeight;

        return new LayoutMeasure(totalW, totalH);
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

        float paddingStart = isH ? paddingLeft : paddingTop;
        float paddingCross = isH ? paddingTop : paddingLeft;

        float availableMain = isH ? (containerWidth - paddingLeft - paddingRight)
                                  : (containerHeight - paddingTop - paddingBottom);
        float availableCross = isH ? (containerHeight - paddingTop - paddingBottom)
                                   : (containerWidth - paddingLeft - paddingRight);
        if (availableCross < 0) availableCross = 0;

        // 1. Calculate constant content size along main axis and total flex weight
        float totalWeight = 0f;
        float fixedMain = 0f;
        float gapTotal = 0f;
        boolean first = true;

        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            if (!first) {
                gapTotal += gap;
            }
            first = false;

            float cellMarginMain = isH ? c.totalMarginHorizontal() : c.totalMarginVertical();
            float cellMain = isH ? c.width : c.height;

            if (c.weight > 0) {
                totalWeight += c.weight;
                fixedMain += cellMarginMain;
            } else {
                fixedMain += cellMain + cellMarginMain;
            }
        }

        float remainingMain = availableMain - fixedMain - gapTotal;
        if (remainingMain < 0) remainingMain = 0;

        // Main-axis alignment offset if space remains and no flex weights
        float curMain = paddingStart;
        if (totalWeight <= 0 && remainingMain > 0) {
            if (mainAlignment == Alignment.CENTER) {
                curMain += remainingMain / 2f;
            } else if (mainAlignment == Alignment.END) {
                curMain += remainingMain;
            }
        }

        // 2. Position cells
        first = true;
        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            if (!first) {
                curMain += gap;
            }
            first = false;

            float marginLeadMain = isH ? c.marginLeft : c.marginTop;
            float marginTrailMain = isH ? c.marginRight : c.marginBottom;
            float marginLeadCross = isH ? c.marginTop : c.marginLeft;
            float marginTrailCross = isH ? c.marginBottom : c.marginRight;
            float marginTotalCross = marginLeadCross + marginTrailCross;

            // Compute main dimension
            float mainDim;
            if (c.weight > 0 && totalWeight > 0) {
                mainDim = (c.weight / totalWeight) * remainingMain;
            } else {
                mainDim = isH ? c.width : c.height;
            }

            float minMain = isH ? c.minWidth : c.minHeight;
            float maxMain = isH ? c.maxWidth : c.maxHeight;
            if (minMain > 0 && mainDim < minMain) mainDim = minMain;
            if (maxMain > 0 && mainDim > maxMain) mainDim = maxMain;

            // Compute cross dimension and position
            float availableForCross = availableCross - marginTotalCross;
            if (availableForCross < 0) availableForCross = 0;

            Alignment align = (c.crossAlignment != null) ? c.crossAlignment : this.crossAlignment;
            float crossDim = isH ? c.height : c.width;
            float crossPos;

            if (align == Alignment.STRETCH) {
                crossDim = availableForCross;
                crossPos = paddingCross + marginLeadCross;
            } else if (align == Alignment.CENTER) {
                crossPos = paddingCross + marginLeadCross + (availableForCross - crossDim) / 2f;
            } else if (align == Alignment.END) {
                crossPos = paddingCross + availableCross - marginTrailCross - crossDim;
            } else { // START
                crossPos = paddingCross + marginLeadCross;
            }

            float minCross = isH ? c.minHeight : c.minWidth;
            float maxCross = isH ? c.maxHeight : c.maxWidth;
            if (minCross > 0 && crossDim < minCross) crossDim = minCross;
            if (maxCross > 0 && crossDim > maxCross) crossDim = maxCross;

            float mainPos = curMain + marginLeadMain;

            // Assign coordinates
            float x = isH ? mainPos : crossPos;
            float y = isH ? crossPos : mainPos;
            float w = isH ? mainDim : crossDim;
            float h = isH ? crossDim : mainDim;

            if (out != null && i < out.length && out[i] != null) {
                out[i].set(x, y, w, h);
            }

            c.outer.set(
                    isH ? curMain : paddingCross,
                    isH ? paddingCross : curMain,
                    isH ? (mainDim + marginLeadMain + marginTrailMain) : availableCross,
                    isH ? availableCross : (mainDim + marginLeadMain + marginTrailMain)
            );
            c.inner.set(x, y, w, h);

            curMain += mainDim + marginLeadMain + marginTrailMain;
        }
    }
}
