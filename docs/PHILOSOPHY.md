# The Philosophy of FastLayout

> [!IMPORTANT]
> **"Pure arithmetic. No widget coupling. Zero allocations on hot paths."**

FastLayout is engineered for high-performance desktop interfaces, floating toolbars, and hardware-accelerated graphics where traditional layout managers introduce unacceptable garbage collection pauses and rigid widget hierarchies.

---

## Core Tenets

### 1. Decoupled Spatial Mathematics
Layout calculations are strictly mathematical transforms. FastLayout knows nothing about AWT, Swing, JavaFX, or Vulkan. It computes coordinates over lightweight `Cell` descriptors and writes directly to reusable output targets.

### 2. Zero Garbage Collection Overhead
In continuous interactive operations—such as live window resizing, animated docking, or 120 FPS game UI rendering—FastLayout creates **0 bytes of heap garbage**. Pre-allocated `Rect[]` arrays or flat `float[]` buffers are reused across frames.

### 3. Contiguous Buffer Optimization
By supporting flat `float[]` output buffers (`x, y, w, h` stride), FastLayout eliminates object indirection and enables immediate streaming to GPU vertex buffers or native rendering substrates.

### 4. Monomorphic Execution
Hot layout paths feature dedicated monomorphic loops for horizontal and vertical orientations, allowing HotSpot C2 to inline inner math routines without megamorphic dispatch penalties.

### 5. FastJava Blueprint Consistency
Adheres strictly to the standardized FastJava ecosystem:
- Microsecond execution latency.
- Comprehensive OpenJDK JMH microbenchmarks (`run-benchmark.bat`).
- Standardized documentation (`REFERENCE.md`, `ROADMAP.md`, `PHILOSOPHY.md`, `CHANGELOG.md`, `COMPILE.md`).

---

**⚡ FastLayout — Powering the next generation of JVM layout engines.**
