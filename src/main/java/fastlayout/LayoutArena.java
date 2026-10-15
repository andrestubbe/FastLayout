package fastlayout;

import java.util.Arrays;
import java.util.List;

/**
 * High-performance Struct-of-Arrays (SoA) memory arena for layout constraints.
 *
 * <p>Stores all input dimensions, weights, margins, and clamping boundaries
 * in flat, contiguous primitive float arrays. This eliminates pointer-chasing,
 * optimizes CPU L1/L2 cache prefetching, and guarantees zero GC overhead.</p>
 */
public final class LayoutArena {

    public int capacity;

    // Sizing arrays (SoA)
    public float[] width;
    public float[] height;
    public float[] weight;
    public float[] minWidth;
    public float[] maxWidth;
    public float[] minHeight;
    public float[] maxHeight;

    // Margin arrays (SoA)
    public float[] marginTop;
    public float[] marginRight;
    public float[] marginBottom;
    public float[] marginLeft;

    // Per-slot alignment overrides (packed ordinal: -1 = inherit, or Alignment.ordinal())
    public byte[] crossAlignment;

    public LayoutArena(int initialCapacity) {
        ensureCapacity(initialCapacity);
    }

    public void ensureCapacity(int minCapacity) {
        if (minCapacity <= capacity) return;
        int newCap = Math.max(minCapacity, Math.max(16, capacity * 2));

        width = (width == null) ? new float[newCap] : Arrays.copyOf(width, newCap);
        height = (height == null) ? new float[newCap] : Arrays.copyOf(height, newCap);
        weight = (weight == null) ? new float[newCap] : Arrays.copyOf(weight, newCap);
        minWidth = (minWidth == null) ? new float[newCap] : Arrays.copyOf(minWidth, newCap);
        maxWidth = (maxWidth == null) ? new float[newCap] : Arrays.copyOf(maxWidth, newCap);
        minHeight = (minHeight == null) ? new float[newCap] : Arrays.copyOf(minHeight, newCap);
        maxHeight = (maxHeight == null) ? new float[newCap] : Arrays.copyOf(maxHeight, newCap);

        marginTop = (marginTop == null) ? new float[newCap] : Arrays.copyOf(marginTop, newCap);
        marginRight = (marginRight == null) ? new float[newCap] : Arrays.copyOf(marginRight, newCap);
        marginBottom = (marginBottom == null) ? new float[newCap] : Arrays.copyOf(marginBottom, newCap);
        marginLeft = (marginLeft == null) ? new float[newCap] : Arrays.copyOf(marginLeft, newCap);

        crossAlignment = (crossAlignment == null) ? new byte[newCap] : Arrays.copyOf(crossAlignment, newCap);
        if (capacity == 0) {
            Arrays.fill(crossAlignment, (byte) -1);
        } else {
            Arrays.fill(crossAlignment, capacity, newCap, (byte) -1);
        }

        capacity = newCap;
    }

    /**
     * Resets a slot with default values.
     */
    public void reset(int slot) {
        width[slot] = 0f;
        height[slot] = 0f;
        weight[slot] = 0f;
        minWidth[slot] = 0f;
        maxWidth[slot] = 0f;
        minHeight[slot] = 0f;
        maxHeight[slot] = 0f;
        marginTop[slot] = 0f;
        marginRight[slot] = 0f;
        marginBottom[slot] = 0f;
        marginLeft[slot] = 0f;
        crossAlignment[slot] = -1;
    }

    /**
     * Sets sizing and weight for a slot.
     */
    public void setSize(int slot, float w, float h) {
        width[slot] = w;
        height[slot] = h;
    }

    public void setWeight(int slot, float w) {
        weight[slot] = w;
    }

    public void setMargins(int slot, float top, float right, float bottom, float left) {
        marginTop[slot] = top;
        marginRight[slot] = right;
        marginBottom[slot] = bottom;
        marginLeft[slot] = left;
    }

    public void setMinMax(int slot, float minW, float maxW, float minH, float maxH) {
        minWidth[slot] = minW;
        maxWidth[slot] = maxW;
        minHeight[slot] = minH;
        maxHeight[slot] = maxH;
    }

    public void setCrossAlignment(int slot, Alignment align) {
        crossAlignment[slot] = (align != null) ? (byte) align.ordinal() : (byte) -1;
    }

    /**
     * Bulk loads from an existing Cell list into this arena starting at offset 0.
     */
    public void load(List<Cell> cells) {
        int n = cells.size();
        ensureCapacity(n);
        for (int i = 0; i < n; i++) {
            Cell c = cells.get(i);
            width[i] = c.width;
            height[i] = c.height;
            weight[i] = c.weight;
            minWidth[i] = c.minWidth;
            maxWidth[i] = c.maxWidth;
            minHeight[i] = c.minHeight;
            maxHeight[i] = c.maxHeight;
            marginTop[i] = c.marginTop;
            marginRight[i] = c.marginRight;
            marginBottom[i] = c.marginBottom;
            marginLeft[i] = c.marginLeft;
            crossAlignment[i] = (c.crossAlignment != null) ? (byte) c.crossAlignment.ordinal() : (byte) -1;
        }
    }
}
