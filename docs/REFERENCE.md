# FastLayout API Reference Manual

`FastLayout` provides a zero-allocation, decoupled primitive layout engine for the FastJava ecosystem. It computes geometry, bounds, padding, gaps, and alignments purely over lightweight cell models and writes directly into flat `Rect` buffers (`float x, y, w, h`).

---

## 1. Class: `fastlayout.FastLayout`

Central facade providing convenience measurement and arrangement entry points.

### Methods

| Method / Signature | Return Type | Description |
|:---|:---|:---|
| `measure`<br>`(algo, cells, w, h, gap, pad)` | `LayoutMeasure` | Measures required bounding box with uniform padding. |
| `measure`<br>`(algo, cells, w, h, gap, padV, padH)` | `LayoutMeasure` | Measures required bounding box with separate vertical and horizontal padding. |
| `arrange`<br>`(algo, cells, w, h, gap, pad, out)` | `void` | Arranges cells with zero allocations into pre-allocated `Rect[]` buffer. |
| `arrange`<br>`(algo, cells, w, h, gap, pad)` | `List<Rect>` | Convenience arrange returning a new `List<Rect>` (for non-hotpath setups). |

---

## 2. Class: `fastlayout.LinearLayout`

High-performance linear layout supporting both horizontal and vertical axes with fit-to-content and stretch capabilities.

### Factory Methods & Construction

| Method / Signature | Return Type | Description |
|:---|:---|:---|
| `LinearLayout.horizontal()` | `LinearLayout` | Creates horizontal layout (`Axis.HORIZONTAL`) with `Alignment.START`. |
| `LinearLayout.horizontal(crossAlign)` | `LinearLayout` | Creates horizontal layout with specified cross-axis alignment (`START`, `CENTER`, `END`, `STRETCH`). |
| `LinearLayout.vertical()` | `LinearLayout` | Creates vertical layout (`Axis.VERTICAL`) with `Alignment.START`. |
| `LinearLayout.vertical(crossAlign)` | `LinearLayout` | Creates vertical layout with specified cross-axis alignment. |
| `new LinearLayout(axis, crossAlign)` | `LinearLayout` | Explicit constructor setting axis and cross-axis alignment. |

### Configuration Setters (Fluent)

| Method / Signature | Return Type | Description |
|:---|:---|:---|
| `setAxis(Axis axis)` | `LinearLayout` | Switches layout orientation dynamically (`Axis.HORIZONTAL` or `Axis.VERTICAL`). |
| `setCrossAxis(Alignment align)` | `LinearLayout` | Sets cross-axis alignment (`Alignment.START`, `CENTER`, `END`, `STRETCH`). |
| `setMainAxis(Alignment align)` | `LinearLayout` | Sets main-axis alignment (`Alignment.START`, `CENTER`, `END`). |

---

## 3. Core Models

### `fastlayout.Cell`
Lightweight cell descriptor specifying element constraints:
- `width`, `height`: Base fixed size.
- `weight`: Flex growth factor (`0.0f` for fixed size, `> 0.0f` for proportional flex expansion).
- `minWidth`, `maxWidth`, `minHeight`, `maxHeight`: Clamping constraints.
- `marginTop`, `marginBottom`, `marginLeft`, `marginRight`: Individual cell margins.

### `fastlayout.Rect`
Flat primitive container for calculated bounds:
- Fields: `float x`, `float y`, `float w`, `float h`.
- Can be pre-allocated and reused across frames with **0 bytes GC overhead**.

### `fastlayout.LayoutMeasure`
Record containing calculated content dimensions:
- `float width()`
- `float height()`

### `fastlayout.Alignment`
Alignment enumerations:
- `START` — Aligns to top or left edge.
- `CENTER` — Centers element along the axis.
- `END` — Aligns to bottom or right edge.
- `STRETCH` — Expands element to fill the available cross-axis space.

### `fastlayout.Axis`
Orientation enumerations:
- `HORIZONTAL` — Elements placed left-to-right.
- `VERTICAL` — Elements placed top-to-bottom.

---

## 4. Performance & Memory Contract

- **Zero Allocations on Hot-Path**: When using `arrange(..., Rect[] out)`, no objects are created on the heap.
- **Pure Arithmetic**: All calculations run purely on float primitives.
- **Render-Engine Agnostic**: Works seamlessly with `FastGUI`, `FastVulkan`, `FastGraphics`, Swing, AWT, OpenGL, or custom hardware render loops.

---

**Part of the FastJava Ecosystem** — *Making the JVM faster.*
