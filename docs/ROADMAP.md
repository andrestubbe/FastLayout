# FastLayout Technical Roadmap

This roadmap outlines layout algorithms, performance optimizations, and integrations for FastLayout across the FastJava ecosystem.

---

## Milestone 1: Linear & Fit-To-Content Primitives (v0.1.0) — ✅ Complete
- [x] Decoupled `Cell` and primitive `Rect` model (`float x, y, w, h`).
- [x] Unified `LinearLayout` with monomorphic horizontal and vertical C2-inlinable loops.
- [x] Cross-axis stretch alignment (`Alignment.STRETCH`) for menus and dropdowns.
- [x] Main-axis flex distribution (`weight > 0.0f`) with clamping (`min/maxWidth`, `min/maxHeight`).
- [x] Zero-allocation reusable `LayoutMeasure` (`layout.measure(..., out)`).
- [x] Dual-output format: AoS (`Rect[]`) and flat contiguous `float[]` buffers (`stride: x, y, w, h`).
- [x] Pure Struct-of-Arrays (SoA) `LayoutArena` for pointer-free cache prefetching.
- [x] OpenJDK JMH microbenchmark suite (`FastLayoutBenchmark.java` and `run-benchmark.bat`).
- [x] Comprehensive JUnit 5 unit test suite (9 tests covering 1D linear, SoA, multi-line wrap, and grid).

---

## Milestone 2: Advanced Grid & Wrap Pipelines (v0.2.0) — ✅ Complete
- [x] `GridLayout` with fixed/dynamic columns, uniform cell proportions, and zero-allocation SoA/float[] execution.
- [x] Multi-line `WrapLayout` (FlexWrap) for tag clouds, chips, and responsive toolbar breaks.
- [x] Dirty layout tracking in `LayoutArena` (`isStructureDirty`, `isBoundsDirty`, `checkResize`) to avoid redundant array copies on container resize.

---

## Milestone 3: FastGUI & Subtree Pipelines (v0.3.0) — 📋 Planned
- [ ] Subtree dirty-propagation and reflow isolation for hierarchical component trees.
- [ ] Multi-pass intrinsic size caching for complex nested components (buttons, text labels, input fields).
- [ ] First-class layout provider for **FastGUI** retained-mode components.
- [ ] Spatial indexing & hit-testing integration with **FastWindow** and native Win32/Mica floating chrome.
- [ ] Preparation for Java Project Valhalla (`value class Rect`).
