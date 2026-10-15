# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.1.0] - 2026-10-15

### Added
- Initial release of FastLayout zero-allocation layout engine.
- Decoupled `Cell` constraints and flat primitive `Rect` model (`float x, y, w, h`).
- Unified `LinearLayout` with monomorphic loops for horizontal and vertical arrangements.
- Support for both AoS (`Rect[]`) and flat contiguous primitive float buffers (`float[]` stride: `x, y, w, h`).
- Reusable `LayoutMeasure` container for 0 bytes GC measurement passes.
- Automatic tracking of `cell.inner` (content box) and `cell.outer` (inclusive of margins).
- Comprehensive JUnit 5 unit test suite (`FastLayoutTest`).
- OpenJDK JMH microbenchmark suite (`FastLayoutBenchmark`) with launcher `run-benchmark.bat`.
- Complete documentation suite (`REFERENCE.md`, `PHILOSOPHY.md`, `ROADMAP.md`, `COMPILE.md`, `CHANGELOG.md`).
