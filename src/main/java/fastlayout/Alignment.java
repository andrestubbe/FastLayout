package fastlayout;

/**
 * Alignment options for main-axis and cross-axis layout positioning.
 *
 * <p>Provides intuitive direction names (LEFT, RIGHT, TOP, BOTTOM)
 * alongside universal flex-axis names (START, END, CENTER, STRETCH).</p>
 */
public enum Alignment {
    /** Beginning of axis: Left for horizontal, Top for vertical. */
    START,

    /** Center of axis. */
    CENTER,

    /** End of axis: Right for horizontal, Bottom for vertical. */
    END,

    /** Stretches across the entire available space. */
    STRETCH;

    // Intuitive directional aliases
    public static final Alignment LEFT = START;
    public static final Alignment TOP = START;
    public static final Alignment RIGHT = END;
    public static final Alignment BOTTOM = END;
}
