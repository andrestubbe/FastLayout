package fastlayout;

/**
 * Result or reusable container for layout measurement passes.
 *
 * <p>Can be instantiated as a value container, or reused across frames to achieve
 * strict 0 bytes / 0 GC pressure during layout passes.</p>
 */
public final class LayoutMeasure {

    public float width;
    public float height;

    public LayoutMeasure() {
    }

    public LayoutMeasure(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public LayoutMeasure set(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LayoutMeasure other)) return false;
        return Float.compare(other.width, width) == 0 && Float.compare(other.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * Float.hashCode(width) + Float.hashCode(height);
    }

    @Override
    public String toString() {
        return "LayoutMeasure[width=" + width + ", height=" + height + "]";
    }
}
