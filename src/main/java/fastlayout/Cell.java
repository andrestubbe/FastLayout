package fastlayout;

/**
 * Primitive layout cell representing an element's spatial constraints and computed bounds.
 *
 * <p>A Cell is completely decoupled from any UI component, widget, or canvas.
 * It contains sizing constraints (fixed or aspect ratio), flex weights, margins,
 * and computed bounding boxes.</p>
 */
public class Cell {

    // Sizing properties
    public float width;
    public float height;
    public float minWidth;
    public float minHeight;
    public float maxWidth;
    public float maxHeight;

    // Flex weight for proportional expansion (0 = fixed / content size)
    public float weight;

    // Margins (outer spacing around this cell)
    public float marginTop;
    public float marginRight;
    public float marginBottom;
    public float marginLeft;

    // Alignment overrides (null = inherit from container layout)
    public Alignment crossAlignment;

    // Calculated bounds
    public final Rect outer = new Rect();
    public final Rect inner = new Rect();

    // Stable index / user object association
    public int index;
    public Object data;

    public Cell() {
    }

    public Cell(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public Cell(float width, float height, float weight) {
        this.width = width;
        this.height = height;
        this.weight = weight;
    }

    /** Creates an expandable spacer with the given flex weight (default 1.0). */
    public static Cell spacer(float weight) {
        Cell c = new Cell();
        c.weight = weight;
        return c;
    }

    /** Creates an expandable spacer with flex weight 1.0. */
    public static Cell spacer() {
        return spacer(1.0f);
    }

    public Cell size(float w, float h) {
        this.width = w;
        this.height = h;
        return this;
    }

    public Cell weight(float w) {
        this.weight = w;
        return this;
    }

    public Cell margin(float all) {
        this.marginTop = all;
        this.marginRight = all;
        this.marginBottom = all;
        this.marginLeft = all;
        return this;
    }

    public Cell margin(float topBottom, float leftRight) {
        this.marginTop = topBottom;
        this.marginBottom = topBottom;
        this.marginLeft = leftRight;
        this.marginRight = leftRight;
        return this;
    }

    public Cell margin(float top, float right, float bottom, float left) {
        this.marginTop = top;
        this.marginRight = right;
        this.marginBottom = bottom;
        this.marginLeft = left;
        return this;
    }

    public Cell align(Alignment align) {
        this.crossAlignment = align;
        return this;
    }

    public Cell data(Object data) {
        this.data = data;
        return this;
    }

    public float totalMarginHorizontal() {
        return marginLeft + marginRight;
    }

    public float totalMarginVertical() {
        return marginTop + marginBottom;
    }
}
