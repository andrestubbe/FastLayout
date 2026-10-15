# FastLayout API Reference Manual

`FastLayout` provides a zero-allocation, decoupled primitive layout engine for the FastJava ecosystem. It computes geometry, bounds, padding, gaps, and alignments purely over lightweight cell models and writes directly into flat `Rect[]` buffers (`float x, y, w, h`), high-performance Struct-of-Arrays (`LayoutArena`), or flat contiguous `float[]` buffers (`stride: x, y, w, h`).

---

## 1. Class: `fastlayout.FastLayout`

Central facade providing convenience measurement and arrangement entry points.

### Methods

| Method / Signature | Return Type | Description |
|:---|:---|:---|
| `measure`<br>`(algo, cells, w, h, gap, pad)` | `LayoutMeasure` | Measures required bounding box with uniform padding into a new `LayoutMeasure`. |
| `measure`<br>`(algo, cells, w, h, gap, pad, out)` | `void` | **Zero-allocation:** Measures required bounding box into a pre-allocated/reusable `LayoutMeasure`. |
| `measure`<br>`(algo, cells, w, h, gap, padV, padH)` | `LayoutMeasure` | Measures required bounding box with separate vertical and horizontal padding. |
| `measure`<br>`(algo, cells, w, h, gap, padV, padH, out)` | `void` | **Zero-allocation:** Measures required bounding box with separate padding into reusable `LayoutMeasure`. |
| `arrange`<br>`(algo, cells, w, h, gap, pad, out)` | `void` | **Zero-allocation (AoS):** Arranges cells into pre-allocated `Rect[]` buffer. |
| `arrange`<br>`(algo, cells, w, h, gap, pad, outBounds, offset)` | `void` | **Zero-allocation (Flat Floats):** Arranges cells directly into contiguous `float[]` buffer (`x, y, w, h` stride). |
| `arrange`<br>`(algo, cells, w, h, gap, pad)` | `List<Rect>` | Convenience arrange returning a new `List<Rect>` (for non-hotpath setups). |

---

## 2. Class: `fastlayout.LinearLayout`

High-performance linear layout supporting both horizontal and vertical axes with fit-to-content, flex growth, and stretch capabilities.

### Factory Methods & Construction

| Method / Signature | Return Type | Description |
|:---|:---|:---|
| `LinearLayout.horizontal()` | `LinearLayout` | Creates horizontal layout (`Axis.HORIZONTAL`) with `crossAlignment = Alignment.CENTER`, `mainAlignment = Alignment.START`. |
| `LinearLayout.horizontal(crossAlign)` | `LinearLayout` | Creates horizontal layout with specified cross-axis alignment (`START`, `CENTER`, `END`, `STRETCH`). |
| `LinearLayout.vertical()` | `LinearLayout` | Creates vertical layout (`Axis.VERTICAL`) with `crossAlignment = Alignment.STRETCH`, `mainAlignment = Alignment.START`. |
| `LinearLayout.vertical(crossAlign)` | `LinearLayout` | Creates vertical layout with specified cross-axis alignment. |
| `new LinearLayout(axis, crossAlign, mainAlign)` | `LinearLayout` | Explicit constructor setting axis, cross-axis, and main-axis alignment. |

### Arrangement & Measurement Overloads

| Signature | Description |
|:---|:---|
| `measure(cells, w, h, gap, top, right, bottom, left, LayoutMeasure out)` | Zero-allocation measure from `List<Cell>` into reusable `out`. |
| `measure(arena, offset, count, w, h, gap, top, right, bottom, left, LayoutMeasure out)` | **Pure SoA:** Zero-indirection measure directly from contiguous primitive float arrays in `LayoutArena`. |
| `arrange(cells, w, h, gap, top, right, bottom, left, Rect[] out)` | **AoS Output:** Monomorphic arrangement into pre-allocated `Rect[]` objects. Also updates `cell.inner` and `cell.outer`. |
| `arrange(cells, w, h, gap, top, right, bottom, left, float[] outBounds, int offset)` | **Flat Float Stride Output:** Monomorphic arrangement writing `[x, y, w, h]` into primitive array. Also updates `cell.inner` and `cell.outer`. |
| `arrange(arena, slotOffset, count, w, h, gap, top, right, bottom, left, float[] outBounds, int outOffset)` | **Pure SoA Kernel:** Zero-indirection, zero-allocation kernel reading from `LayoutArena` and writing to `float[] outBounds`. Maximum cache locality. |

---

## 3. Core Models

### `fastlayout.LayoutArena`
High-performance Struct-of-Arrays (SoA) memory arena for layout constraints:
- Stores all attributes in flat, contiguous primitive arrays (`float[] width, height, weight, minWidth, maxWidth, minHeight, maxHeight, marginTop, marginRight, marginBottom, marginLeft` and `byte[] crossAlignment`).
- **Methods**: `setSize(slot, w, h)`, `setWeight(slot, w)`, `setMargins(slot, t, r, b, l)`, `setMinMax(slot, ...)`, `setCrossAlignment(slot, align)`, `load(List<Cell> cells)`.
- Eliminates object dereferencing and pointer-chasing in massive loops.

### `fastlayout.Cell`
Lightweight cell descriptor specifying element constraints (OOP facade):
- `width`, `height`: Base fixed size.
- `weight`: Flex growth factor (`0.0f` for fixed size, `> 0.0f` for proportional flex expansion).
- `minWidth`, `maxWidth`, `minHeight`, `maxHeight`: Clamping constraints.
- `marginTop`, `marginBottom`, `marginLeft`, `marginRight`: Individual cell margins.
- `crossAlignment`: Alignment override (or `null` to inherit from layout).
- `inner`: `Rect` containing computed content bounding box (inside margins).
- `outer`: `Rect` containing computed cell outer bounding box (including margins).
- **Helper Methods**: `Cell.spacer()`, `Cell.spacer(weight)`, `margin(all)`, `margin(v, h)`, `margin(t, r, b, l)`.

### `fastlayout.Rect`
Flat primitive container for calculated bounds:
- Fields: `float x`, `float y`, `float w`, `float h`.
- Can be pre-allocated and reused across frames with **0 bytes GC overhead**.
- Methods: `set(x, y, w, h)`, `lerp(target, alpha)`, `contains(px, py)`.

### `fastlayout.LayoutMeasure`
Mutable and reusable container for computed layout dimensions:
- `float width`: Computed required width.
- `float height`: Computed required height.
- Methods: `width()`, `height()`, `set(w, h)`. Can be reused indefinitely to guarantee 0 bytes GC allocation.

### `fastlayout.Alignment`
Alignment enumerations:
- `START` — Aligns to top or left edge (aliases: `LEFT`, `TOP`).
- `CENTER` — Centers element along the axis.
- `END` — Aligns to bottom or right edge (aliases: `RIGHT`, `BOTTOM`).
- `STRETCH` — Expands element to fill the available cross-axis space.

### `fastlayout.Axis`
Orientation enumerations:
- `HORIZONTAL` — Elements placed left-to-right.
- `VERTICAL` — Elements placed top-to-bottom.

---

## 4. Performance & Memory Contract

- **Zero Allocations on Hot-Path**: When reusing `LayoutMeasure` and `Rect[]` or `float[]`, no objects are created on the heap (`gc.alloc.rate = 0 B/op`).
- **Monomorphic Kernels**: HotSpot C2 does not branch on layout axis within inner calculation loops.
- **Cache-Optimized Traversals**: Support for both AoS and pure SoA (`LayoutArena`) guarantees optimal L1/L2 data prefetching.
- **Render-Engine Agnostic**: Pure math engine with 0 toolkit dependencies. Feeds seamlessly into `FastGUI`, `FastVulkan`, `FastGraphics`, Swing, AWT, OpenGL, or native Win32 window chrome.

---

**Part of the FastJava Ecosystem** — *Making the JVM faster.*
