package fastlayout;

import java.util.List;

/**
 * 1D Horizontal Layout (Row / ToolBar / Icon Bar).
 *
 * <p>Arranges cells horizontally from left to right along the X-axis.
 * Supports auto-shrink fit-to-content (where container width/height matches cell content exactly),
 * flex weights for proportional stretching, margins, and vertical cross-alignment
 * (TOP/START, CENTER, BOTTOM/END, STRETCH).</p>
 */
public final class HorizontalLayout implements LayoutAlgorithm {

    private final Alignment crossAlignment;
    private final Alignment mainAlignment;

    public HorizontalLayout() {
        this(Alignment.CENTER, Alignment.START);
    }

    public HorizontalLayout(Alignment crossAlignment) {
        this(crossAlignment, Alignment.START);
    }

    public HorizontalLayout(Alignment crossAlignment, Alignment mainAlignment) {
        this.crossAlignment = crossAlignment;
        this.mainAlignment = mainAlignment;
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
        float contentW = 0f;
        float maxH = 0f;
        boolean first = true;

        for (int i = 0; i < cells.size(); i++) {
            Cell c = cells.get(i);
            if (!first) {
                contentW += gap;
            }
            first = false;

            float cellW = c.width + c.totalMarginHorizontal();
            float cellH = c.height + c.totalMarginVertical();

            contentW += cellW;
            if (cellH > maxH) {
                maxH = cellH;
            }
        }

        float totalW = contentW + paddingLeft + paddingRight;
        float totalH = maxH + paddingTop + paddingBottom;

        if (containerWidth > 0 && totalW < containerWidth) {
            totalW = containerWidth;
        }
        if (containerHeight > 0 && totalH < containerHeight) {
            totalH = containerHeight;
        }

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

        // 1. Calculate constant content size and total flex weight
        float totalWeight = 0f;
        float fixedWidth = 0f;
        float gapTotal = 0f;
        boolean first = true;

        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            if (!first) {
                gapTotal += gap;
            }
            first = false;

            if (c.weight > 0) {
                totalWeight += c.weight;
                fixedWidth += c.totalMarginHorizontal();
            } else {
                fixedWidth += c.width + c.totalMarginHorizontal();
            }
        }

        float availableW = containerWidth - paddingLeft - paddingRight;
        float availableH = containerHeight - paddingTop - paddingBottom;
        if (availableH < 0) availableH = 0;

        float remainingSpace = availableW - fixedWidth - gapTotal;
        if (remainingSpace < 0) remainingSpace = 0;

        // Main-axis alignment offset if there is extra space and no weighted cells
        float startX = paddingLeft;
        if (totalWeight <= 0 && remainingSpace > 0) {
            if (mainAlignment == Alignment.CENTER) {
                startX += remainingSpace / 2f;
            } else if (mainAlignment == Alignment.END) {
                startX += remainingSpace;
            }
        }

        // 2. Position cells
        float curX = startX;
        first = true;

        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            if (!first) {
                curX += gap;
            }
            first = false;

            float w;
            if (c.weight > 0 && totalWeight > 0) {
                w = (c.weight / totalWeight) * remainingSpace;
            } else {
                w = c.width;
            }

            if (c.minWidth > 0 && w < c.minWidth) w = c.minWidth;
            if (c.maxWidth > 0 && w > c.maxWidth) w = c.maxWidth;

            float itemX = curX + c.marginLeft;
            float availableForCellH = availableH - c.totalMarginVertical();
            if (availableForCellH < 0) availableForCellH = 0;

            Alignment align = (c.crossAlignment != null) ? c.crossAlignment : this.crossAlignment;

            float h = c.height;
            float itemY;

            if (align == Alignment.STRETCH) {
                h = availableForCellH;
                itemY = paddingTop + c.marginTop;
            } else if (align == Alignment.CENTER) {
                itemY = paddingTop + c.marginTop + (availableForCellH - h) / 2f;
            } else if (align == Alignment.END) {
                itemY = paddingTop + availableH - c.marginBottom - h;
            } else { // START / TOP
                itemY = paddingTop + c.marginTop;
            }

            if (c.minHeight > 0 && h < c.minHeight) h = c.minHeight;
            if (c.maxHeight > 0 && h > c.maxHeight) h = c.maxHeight;

            // Set output bounding box
            if (out != null && i < out.length && out[i] != null) {
                out[i].set(itemX, itemY, w, h);
            }

            // Also update cell cached bounds
            c.outer.set(curX, paddingTop, w + c.totalMarginHorizontal(), availableH);
            c.inner.set(itemX, itemY, w, h);

            curX += w + c.totalMarginHorizontal();
        }
    }
}
