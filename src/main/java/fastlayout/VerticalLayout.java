package fastlayout;

import java.util.List;

/**
 * 1D Vertical Layout (Column / Popup Menu / Settings List).
 *
 * <p>Arranges cells vertically from top to bottom along the Y-axis.
 * Supports auto-shrink fit-to-content (where container width/height matches cell content exactly),
 * flex weights for proportional stretching, margins, and horizontal cross-alignment
 * (LEFT/START, CENTER, RIGHT/END, STRETCH).</p>
 */
public final class VerticalLayout implements LayoutAlgorithm {

    private final Alignment crossAlignment;
    private final Alignment mainAlignment;

    public VerticalLayout() {
        this(Alignment.STRETCH, Alignment.START);
    }

    public VerticalLayout(Alignment crossAlignment) {
        this(crossAlignment, Alignment.START);
    }

    public VerticalLayout(Alignment crossAlignment, Alignment mainAlignment) {
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
        float maxW = 0f;
        float contentH = 0f;
        boolean first = true;

        for (int i = 0; i < cells.size(); i++) {
            Cell c = cells.get(i);
            if (!first) {
                contentH += gap;
            }
            first = false;

            float cellW = c.width + c.totalMarginHorizontal();
            float cellH = c.height + c.totalMarginVertical();

            contentH += cellH;
            if (cellW > maxW) {
                maxW = cellW;
            }
        }

        float totalW = maxW + paddingLeft + paddingRight;
        float totalH = contentH + paddingTop + paddingBottom;

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
        float fixedHeight = 0f;
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
                fixedHeight += c.totalMarginVertical();
            } else {
                fixedHeight += c.height + c.totalMarginVertical();
            }
        }

        float availableW = containerWidth - paddingLeft - paddingRight;
        float availableH = containerHeight - paddingTop - paddingBottom;
        if (availableW < 0) availableW = 0;

        float remainingSpace = availableH - fixedHeight - gapTotal;
        if (remainingSpace < 0) remainingSpace = 0;

        // Main-axis alignment offset if there is extra space and no weighted cells
        float startY = paddingTop;
        if (totalWeight <= 0 && remainingSpace > 0) {
            if (mainAlignment == Alignment.CENTER) {
                startY += remainingSpace / 2f;
            } else if (mainAlignment == Alignment.END) {
                startY += remainingSpace;
            }
        }

        // 2. Position cells
        float curY = startY;
        first = true;

        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            if (!first) {
                curY += gap;
            }
            first = false;

            float h;
            if (c.weight > 0 && totalWeight > 0) {
                h = (c.weight / totalWeight) * remainingSpace;
            } else {
                h = c.height;
            }

            if (c.minHeight > 0 && h < c.minHeight) h = c.minHeight;
            if (c.maxHeight > 0 && h > c.maxHeight) h = c.maxHeight;

            float itemY = curY + c.marginTop;
            float availableForCellW = availableW - c.totalMarginHorizontal();
            if (availableForCellW < 0) availableForCellW = 0;

            Alignment align = (c.crossAlignment != null) ? c.crossAlignment : this.crossAlignment;

            float w = c.width;
            float itemX;

            if (align == Alignment.STRETCH) {
                w = availableForCellW;
                itemX = paddingLeft + c.marginLeft;
            } else if (align == Alignment.CENTER) {
                itemX = paddingLeft + c.marginLeft + (availableForCellW - w) / 2f;
            } else if (align == Alignment.END) {
                itemX = paddingLeft + availableW - c.marginRight - w;
            } else { // START / LEFT
                itemX = paddingLeft + c.marginLeft;
            }

            if (c.minWidth > 0 && w < c.minWidth) w = c.minWidth;
            if (c.maxWidth > 0 && w > c.maxWidth) w = c.maxWidth;

            // Set output bounding box
            if (out != null && i < out.length && out[i] != null) {
                out[i].set(itemX, itemY, w, h);
            }

            // Also update cell cached bounds
            c.outer.set(paddingLeft, curY, availableW, h + c.totalMarginVertical());
            c.inner.set(itemX, itemY, w, h);

            curY += h + c.totalMarginVertical();
        }
    }
}
