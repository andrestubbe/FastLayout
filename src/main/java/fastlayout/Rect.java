package fastlayout;

/**
 * High-performance 2D primitive bounding rectangle.
 * Completely eliminates java.awt.geom.Rectangle2D heap allocations in layout loops.
 */
public final class Rect {
    public float x;
    public float y;
    public float w;
    public float h;

    public Rect() {
    }

    public Rect(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void set(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void set(Rect other) {
        this.x = other.x;
        this.y = other.y;
        this.w = other.w;
        this.h = other.h;
    }

    public void setLerp(Rect a, Rect b, float t) {
        this.x = a.x + (b.x - a.x) * t;
        this.y = a.y + (b.y - a.y) * t;
        this.w = a.w + (b.w - a.w) * t;
        this.h = a.h + (b.h - a.h) * t;
    }

    public boolean contains(float px, float py) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    @Override
    public String toString() {
        return String.format("Rect[x=%.1f, y=%.1f, w=%.1f, h=%.1f]", x, y, w, h);
    }
}
