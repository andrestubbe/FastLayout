# FastLayout 0.1.0 [ALPHA-2026-10-15] — Decoupled Zero-Allocation Layout Engine for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastLayout/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey.svg)]()
[![JitPack](https://jitpack.io/v/andrestubbe/FastLayout.svg)](https://jitpack.io/#andrestubbe/FastLayout)

---

**⚡ Ultra-fast, zero-allocation primitive layout engine for the FastJava ecosystem. Powers floating toolbars, responsive sidebars, dropdown menus, and settings palettes.**

**FastLayout** completely decouples geometry computation and alignment from graphical widgets, window handles, and heavy toolkit hierarchies. By calculating layout bounds purely over lightweight `Cell` descriptors or high-performance `LayoutArena` (SoA) and writing directly into primitive-backed `Rect` structures or flat contiguous `float[]` buffers (`stride: x, y, w, h`), FastLayout delivers microsecond execution and **0 bytes Garbage Collection churn**.

---

## Quick Start — Example

### 1. Horizontal Toolbar (Fit-to-Content for Floating Bars)

```java
import fastlayout.*;
import java.util.List;
import java.util.ArrayList;

public class ToolbarExample {
    public static void main(String[] args) {
        // Prepare 4 icon buttons (each 32×32)
        List<Cell> cells = new ArrayList<>();
        cells.add(new Cell(32, 32));
        cells.add(new Cell(32, 32));
        cells.add(new Cell(32, 32));
        cells.add(new Cell(32, 32));

        LinearLayout layout = LinearLayout.horizontal();

        // 1. Measure exact bounding box required by the icons (8px gap, 10px padding)
        // Zero-allocation: pass a reusable LayoutMeasure instance
        LayoutMeasure measure = new LayoutMeasure();
        layout.measure(cells, 0, 0, 8, 10, 10, 10, 10, measure);
        System.out.printf("Window size: %.0f × %.0f px%n", measure.width, measure.height);
        // -> Window size: 172 × 52 px (Window sizes directly to this exact bounding box!)

        // 2. Arrange items with zero allocations into pre-allocated Rect buffer
        Rect[] bounds = new Rect[cells.size()];
        for (int i = 0; i < bounds.length; i++) bounds[i] = new Rect();
        layout.arrange(cells, measure.width, measure.height, 8, 10, 10, 10, 10, bounds);

        for (int i = 0; i < bounds.length; i++) {
            System.out.printf("Icon #%d bounds: %s (outer: %s)%n", i, bounds[i], cells.get(i).outer);
        }
    }
}
```

### 2. High-Performance Contiguous Float Buffer (Zero-GC Hot-Path)

For game loops, Vulkan instanced quads, and direct memory streaming:

```java
// Contiguous flat float buffer: [x, y, w, h, x, y, w, h, ...]
float[] outBuffer = new float[cells.size() * 4];

// Arranges directly into buffer at offset 0 — 0 bytes heap allocation
layout.arrange(cells, containerW, containerH, 8, 10, 10, 10, 10, outBuffer, 0);
```

### 3. Pure Struct-of-Arrays (SoA) Kernel via `LayoutArena`

Eliminates pointer-chasing and object headers completely by reading directly from contiguous primitive arrays:

```java
LayoutArena arena = new LayoutArena(3);
arena.setSize(0, 30, 20);      // Item A: 30x20
arena.setWeight(1, 1.0f);      // Item B: Flexible spacer
arena.setSize(2, 30, 20);      // Item C: 30x20

float[] outBounds = new float[3 * 4];
layout.arrange(arena, 0, 3, 300, 20, 0, 0, 0, 0, 0, outBounds, 0);
// Item A: [0, 0, 30, 20]
// Item B: [30, 0, 240, 20]
// Item C: [270, 0, 30, 20]
```

---

## Table of Contents

- [Why FastLayout?](#why-fastlayout)
- [Key Features](#key-features)
- [Architecture & Progressive Roadmap](#architecture--progressive-roadmap)
- [Real-World Use Cases](#real-world-use-cases)
- [Performance Benchmarks](#performance-benchmarks)
- [API Quick Reference](#api-quick-reference)
- [Technical Demos & Benchmarks](#technical-demos--benchmarks)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastLayout?

Standard Java layout managers (`FlowLayout`, `GridBagLayout`, `BoxLayout`, JavaFX layouts) are tightly coupled to their respective widget frameworks (`java.awt.Component`, `javafx.scene.Node`). They generate heavy object allocations on every layout pass, enforce rigid widget hierarchies, and cannot easily be reused in custom hardware-accelerated render loops (Vulkan, DirectX, OpenGL, or native overlays).

| Feature | AWT / Swing Layouts | JavaFX Layouts | FastLayout |
|:---|:---|:---|:---|
| **Toolkit Dependency** | ❌ Coupled to `java.awt.Container` | ❌ Coupled to `javafx.scene.Node` | **✅ 100% Decoupled & Pure Math** |
| **GC Allocations on Hot-Path** | ⚠️ High (Creates `Dimension`, `Insets`, `Rectangle`) | ⚠️ Moderate-to-High | **✅ 0 Bytes (Reusable `LayoutMeasure` & buffers)** |
| **Data Output Layouts** | Only widget coordinates | Only scene graph bounds | **AoS (`Rect[]`), SoA, & Flat `float[]` Buffers** |
| **Inner & Outer Margin Bounds** | ❌ Missing (or complex borders) | ⚠️ CSS margin box calculation | **✅ Automatic `cell.inner` & `cell.outer` tracking** |
| **Execution Latency** | Milliseconds | Milliseconds | **Sub-microsecond (< 1 µs for ≤ 100 cells, ~7 µs for 1,000)** |
| **Backend Agnostic** | Swing only | JavaFX only | **FastGUI, FastVulkan, FastWindow, Swing, CLI** |

---

## Key Features

- **🧩 Decoupled Architecture**: 100% agnostic of AWT, Swing, JavaFX, or Vulkan. Pure mathematical geometry engine.
- **⚡ Zero GC Overhead**: Operates over primitive constraints, writes directly into flat `float[]` or reusable `Rect[]` buffers.
- **🚀 Dual Output Modes**:
  - **AoS (Array of Structures)**: `Rect[] out` for object-oriented component engines.
  - **Flat Contiguous Floats**: `float[] outBuffer` (`x, y, w, h` stride) for direct GPU / Vulkan vertex buffer uploads.
- **📏 Comprehensive Algorithm Suite**:
  - **`LinearLayout`**: Monomorphic horizontal & vertical loops (alignment, flex growth, min/max clamping).
  - **`WrapLayout`**: Multi-line / multi-column responsive wrapping (flex-wrap for tags and toolbars).
  - **`GridLayout`**: 2D uniform grid layout with fixed/dynamic rows & columns.
- **⚡ Dirty Layout Tracking (`LayoutArena`)**: Distinguishes structural changes from window resizes/drags. Zero copying on resize.
- **📦 Dual Box Geometry**: Calculates both `cell.outer` (inclusive of margins) and `cell.inner` (content box).
- **📦 FastJava Ecosystem Ready**: Interoperates seamlessly with **FastGrid**, **FastGUI**, **FastVulkan**, and **FastWindow**.

---

## Architecture & Progressive Roadmap

1. ✅ **Decoupled Primitive Kernel**: Pure arithmetic `Cell`, `Rect`, and `LayoutMeasure` abstraction.
2. ✅ **Monomorphic `LinearLayout`**: High-speed C2 inlinable horizontal and vertical loops.
3. ✅ **Contiguous Float Buffering**: Direct flat `float[]` array streaming for GPU vertex and quad batching.
4. ✅ **Struct-of-Arrays (SoA) `LayoutArena`**: Flat contiguous memory arena optimizing CPU L1/L2 prefetching and eliminating pointer chasing.
5. ✅ **Multi-line `WrapLayout` & 2D `GridLayout`**: Responsive wrapping and grid architectures with zero-allocation SoA kernels.
6. ✅ **Dirty Layout Tracking**: Subtree / geometry dirtiness avoiding redundant array synchronization.
7. 📋 **FastGUI & FastVulkan Integration**: First-class layout provider for high-FPS desktop UI pipelines.

---

## Real-World Use Cases

- 🛠️ **Floating Toolbars & Docks**: Compute precise auto-sizing window dimensions (`LayoutMeasure`) for Mica/Acrylic floating docks and icon trays.
- 📋 **Context & Popup Menus**: Automatically align menu action items to the widest label with cross-axis `Alignment.STRETCH`.
- 🎮 **Hardware-Accelerated Canvas & Vulkan ([FastVulkan](https://github.com/andrestubbe/FastVulkan) / [FastGraphics](https://github.com/andrestubbe/FastGraphics))**: Calculate HUD, minimap, and UI coordinates directly into flat vertex float buffers without GC pauses.
- 🗂️ **Custom Window Chrome**: Position custom titlebar buttons (Minimize, Maximize, Close) and tab strips with microsecond precision.

---

## Performance Benchmarks

FastLayout is rigorously profiled with **OpenJDK JMH (Java Microbenchmark Harness)** across 10, 100, and 1,000 element topologies:

| Benchmark Operation | 10 Elements (Toolbar) | 100 Elements (Menu / Panel) | 1,000 Elements (Large List) | Allocation Rate |
|:---|:---|:---|:---|:---|
| **Zero-Alloc Measurement (`measure`)** | **~41,000 ops/ms** *(41M/s)* | **~4,500 ops/ms** *(4.5M/s)* | **~403 ops/ms** *(403k/s)* | **0 bytes / op (Zero GC)** |
| **Pure SoA Measurement (`LayoutArena`)** | **~44,000 ops/ms** *(44M/s)* | **~4,440 ops/ms** *(4.4M/s)* | **~435 ops/ms** *(435k/s)* | **0 bytes / op (Zero GC)** |
| **AoS Arrange (`Rect[]`)** | **~9,300 - 13,800 ops/ms** | **~1,040 - 1,860 ops/ms** | **~56 - 127 ops/ms** | **0 bytes / op (Zero GC)** |
| **Flat Float Buffer Arrange (`float[]`)** | **~14,100 ops/ms** *(14.1M/s)* | **~1,480 ops/ms** *(1.48M/s)* | **~112 ops/ms** *(112k/s)* | **0 bytes / op (Zero GC)** |
| **Pure SoA Kernel (`Arena -> float[]`)** | **~13,700 ops/ms** *(13.7M/s)* | **~1,500 ops/ms** *(1.5M/s)* | **~134 ops/ms** *(134k/s)* | **0 bytes / op (Zero GC)** |

*Run the benchmarks locally:*
```powershell
.\run-benchmark.bat
```

---

## API Quick Reference

| Method / Signature | Return Type | Description | Docs |
|:---|:---|:---|:---|
| `LinearLayout.horizontal()` | `LinearLayout` | Creates horizontal layout with default center alignment. | [Reference](docs/REFERENCE.md#2-class-fastlayoutlinearlayout) |
| `LinearLayout.vertical()` | `LinearLayout` | Creates vertical layout with default stretch alignment. | [Reference](docs/REFERENCE.md#2-class-fastlayoutlinearlayout) |
| `layout.measure(cells, ..., measure)` | `void` | Zero-allocation measurement writing into reusable `LayoutMeasure`. | [Reference](docs/REFERENCE.md#2-class-fastlayoutlinearlayout) |
| `layout.measure(arena, offset, count, ..., measure)` | `void` | **Pure SoA:** Zero-indirection measurement directly from `LayoutArena`. | [Reference](docs/REFERENCE.md#2-class-fastlayoutlinearlayout) |
| `layout.arrange(cells, ..., Rect[] out)` | `void` | Zero-allocation arrangement writing into pre-allocated `Rect[]` array. | [Reference](docs/REFERENCE.md#2-class-fastlayoutlinearlayout) |
| `layout.arrange(cells, ..., float[] buf, int off)` | `void` | Zero-allocation arrangement writing into flat contiguous float buffer. | [Reference](docs/REFERENCE.md#2-class-fastlayoutlinearlayout) |
| `layout.arrange(arena, offset, count, ..., float[] buf, int off)` | `void` | **Pure SoA Kernel:** Zero-indirection arrangement from `LayoutArena` to `float[]`. | [Reference](docs/REFERENCE.md#2-class-fastlayoutlinearlayout) |
| `FastLayout.measure(...)` | `LayoutMeasure` | Convenience facade method with uniform padding. | [Reference](docs/REFERENCE.md#1-class-fastlayoutfastlayout) |

---

## Technical Demos & Benchmarks

| Case | Java Source | Launcher | Description |
|:---|:---|:---|:---|
| **Unit Verification Suite** | [FastLayoutTest.java](src/test/java/fastlayout/FastLayoutTest.java) | `mvn test` | Complete JUnit 5 suite covering horizontal/vertical fit-content, flex stretch, and SoA. |
| **JMH Microbenchmark Suite** | [FastLayoutBenchmark.java](src/test/java/fastlayout/benchmark/FastLayoutBenchmark.java) | `run-benchmark.bat` | Formal OpenJDK JMH throughput measurements across 10, 100, and 1,000 cells. |

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastLayout</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastLayout:0.1.0'
}
```

### Option 3: Direct Download (Pre-built JAR)

Download the latest pre-compiled JAR directly from GitHub Releases:

1. 📦 [**FastLayout-0.1.0.jar**](https://github.com/andrestubbe/FastLayout/releases/download/0.1.0/FastLayout-0.1.0.jar)

---

## Documentation

- **[REFERENCE.md](docs/REFERENCE.md)**: Full API contracts, primitive layout math, and memory layouts.
- **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Zero-allocation and decoupled architectural principles.
- **[ROADMAP.md](docs/ROADMAP.md)**: Future development milestones and planned algorithms.
- **[CHANGELOG.md](docs/CHANGELOG.md)**: Version history, release notes, and migration guides.
- **[COMPILE.md](docs/COMPILE.md)**: Compilation guide for Java sources and JMH benchmarks.

---

## Platform Support

| Platform | Architecture | Status | Engine Subsystem |
|:---|:---:|:---:|:---|
| **Windows 10 / 11** | x64 / ARM64 | ✅ Fully Supported | Pure JVM primitive math |
| **Linux** | x64 / AArch64 | ✅ Fully Supported | Pure JVM primitive math |
| **macOS** | Apple Silicon / x64 | ✅ Fully Supported | Pure JVM primitive math |

---

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## Related Projects

- **[FastJava Suite](https://github.com/andrestubbe)** — High-performance native JVM runtime libraries.
- **[FastGrid](https://github.com/andrestubbe/FastGrid)** — Multi-item zero-allocation grid, masonry, and gallery layout engine.
- **[FastGUI](https://github.com/andrestubbe/FastGUI)** — Retained-mode baking GUI framework for Java.
- **[FastVulkan](https://github.com/andrestubbe/FastVulkan)** — High-performance native Vulkan 2D rendering engine.
- **[FastWindow](https://github.com/andrestubbe/FastWindow)** — High-performance native Win32 window management.
- **[FastTheme](https://github.com/andrestubbe/FastTheme)** — Native Windows DWM theme and dark mode engine.

---

Part of the FastJava Ecosystem — Making the JVM faster. Small package. Maximum speed. Zero bloat. ⚡
