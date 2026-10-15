# FastLayout Technical Roadmap

This roadmap outlines planned layout algorithms, performance optimizations, and integrations for FastLayout.

---

## Milestone 1: Linear & Fit-To-Content Primitives (v0.1.0) — ✅ Complete
- [x] Decoupled `Cell` and primitive `Rect` model (`float x, y, w, h`).
- [x] Unified `LinearLayout` with runtime axis switching (`Axis.HORIZONTAL` & `Axis.VERTICAL`).
- [x] Cross-axis stretch alignment (`Alignment.STRETCH`) for menus and dropdowns.
- [x] Pre-allocated zero-allocation buffer arrangement (`Rect[] out`).
- [x] Comprehensive JUnit 5 unit test suite.

---

## Milestone 2: Advanced Grid & Flex Pipelines (v0.2.0) — 🚧 In Progress
- [ ] `GridLayout` with column/row template definitions and auto-wrapping.
- [ ] Multi-line `WrapLayout` for tag clouds, chips, and responsive toolbars.
- [ ] Multi-pass intrinsic size caching for deep component trees.

---

## Milestone 3: FastJava Ecosystem Integrations (v0.3.0) — 📋 Planned
- [ ] First-class layout provider for **FastGUI** retained-mode components.
- [ ] Spatial indexing & hit-testing integration with **FastWindow** and input listeners.
- [ ] OpenJDK JMH microbenchmark suite measuring throughput at 1,000,000 layouts/sec.
