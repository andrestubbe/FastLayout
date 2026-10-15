package fastlayout;

/**
 * Result of a layout measurement pass.
 * Contains the measured bounding size that the container strictly requires.
 */
public record LayoutMeasure(float width, float height) {}
