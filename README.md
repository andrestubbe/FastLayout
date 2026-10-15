# FastLayout 0.1.0 [ALPHA-2026-10-15] — Decoupled Zero-Allocation Layout Engine for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastLayout/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Cross%20Platform-lightgrey.svg)]()
[![JitPack](https://jitpack.io/v/andrestubbe/FastLayout.svg)](https://jitpack.io/#andrestubbe/FastLayout)

---

**⚡ Ultra-fast, zero-allocation primitive layout engine for the FastJava ecosystem. Powers floating toolbars, responsive sidebars, dropdown menus, and settings palettes.**

**FastLayout** completely decouples geometry computation and alignment from graphical widgets, window handles, and heavy toolkit hierarchies. By calculating layout bounds purely over lightweight `Cell` descriptors and writing directly into primitive-backed `Rect` structures (`float x, y, w, h`), FastLayout delivers microsecond execution and **0 bytes Garbage Collection churn**.

---

## Table of Contents

- [Why FastLayout?](#why-fastlayout)
- [Quick Start](#quick-start)
- [Key Features](#key-features)
- [Real-World Use Cases](#real-world-use-cases)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [Documentation](#documentation)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastLayout?

Standard Java layout managers (`FlowLayout`, `GridBagLayout`, `BoxLayout`, JavaFX layouts) are tightly coupled to their respective widget frameworks (`java.awt.Component`, `javafx.scene.Node`). They generate heavy object allocations on every layout pass, enforce rigid widget hierarchies, and cannot easily be reused in custom hardware-accelerated render loops (Vulkan, DirectX, OpenGL, or native overlays).

| Feature | AWT / Swing Layouts | JavaFX Layouts | FastLayout |
|:---|:---|:---|:---|
| **Toolkit Dependency** | ❌ Coupled to `java.awt.Container` | ❌ Coupled to `javafx.scene.Node` | **✅ 100% Decoupled & Pure Math** |
| **GC Allocations on Hot-Path** | ⚠️ High (Creates `Dimension`, `Insets`, `Rectangle`) | ⚠️ Moderate-to-High | **✅ 0 Bytes (Reusable `Rect[]` buffers)** |
| **Execution Latency** | Milliseconds | Milliseconds | **Sub-microsecond (< 1 µs)** |
| **Backend Agnostic** | Swing only | JavaFX only | **FastGUI, FastVulkan, FastWindow, Swing, CLI** |
| **Dynamic Axis Switching** | Requires swapping layout classes | Requires swapping nodes | **✅ Dynamic `layout.setAxis(...)` at runtime** |

---

## Quick Start

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
        LayoutMeasure measure = layout.measure(cells, 0, 0, 8, 10, 10, 10, 10);
        System.out.printf("Window size: %.0f × %.0f px%n", measure.width(), measure.height());
        // -> Window size: 172 × 52 px (Window sizes directly to this exact bounding box!)

        // 2. Arrange items with zero allocations into pre-allocated Rect buffer
        Rect[] bounds = new Rect[cells.size()];
        for (int i = 0; i < bounds.length; i++) bounds[i] = new Rect();
        layout.arrange(cells, measure.width(), measure.height(), 8, 10, 10, 10, 10, bounds);

        for (int i = 0; i < bounds.length; i++) {
            System.out.printf("Icon #%d bounds: %s%n", i, bounds[i]);
        }
    }
}
```

### 2. Vertical Popup Menu / Settings List

```java
import fastlayout.*;
import java.util.List;
import java.util.ArrayList;

public class MenuExample {
    public static void main(String[] args) {
        List<Cell> items = new ArrayList<>();
        items.add(new Cell(120, 24)); // Cut
        items.add(new Cell(160, 24)); // Copy (Widest)
        items.add(new Cell(90, 24));  // Paste

        // Vertical stretch aligns all items to the widest element
        LinearLayout layout = LinearLayout.vertical(Alignment.STRETCH);

        // Gap: 4, Padding: 6
        LayoutMeasure measure = layout.measure(items, 0, 0, 4, 6, 6, 6, 6);
        System.out.printf("Popup size: %.0f × %.0f px%n", measure.width(), measure.height());
        // -> Popup size: 172 × 92 px (matches widest item + vertical sum)
    }
}
```

---

## Key Features

- **🧩 Decoupled Architecture**: 100% agnostic of AWT, Swing, JavaFX, or Vulkan. Works with any rendering backend.
- **⚡ Zero GC Overhead**: Reads primitive constraints, writes directly into flat `Rect` arrays.
- **📏 Unified `LinearLayout`**:
  - `LinearLayout.horizontal()`: Fit-to-content icon bars, toolbars, breadcrumbs, flex-rows.
  - `LinearLayout.vertical()`: Dropdown menus, popup settings, command palettes, flex-columns.
  - Seamless axis switching (`Axis.HORIZONTAL` / `Axis.VERTICAL`) at runtime.
- **📐 Flexible Sizing**: Supports fixed sizes, flex weights (`weight > 0`), min/max bounds, margins, and padding.
- **🎯 Alignment Control**: Main-axis & cross-axis alignments (`START`, `CENTER`, `END`, `STRETCH`).

---

## Real-World Use Cases

- 🛠️ **Floating Toolbars & Docks**: Compute precise auto-sizing window dimensions (`LayoutMeasure`) for Mica/Acrylic floating docks and icon trays.
- 📋 **Context & Popup Menus**: Automatically align menu action items to the widest label with cross-axis `Alignment.STRETCH`.
- 🎮 **Hardware-Accelerated Canvas & Games**: Calculate HUD, minimap, and UI element coordinates without invoking AWT or triggering GC pauses.
- 🗂️ **Custom Window Chrome**: Position custom titlebar buttons (Minimize, Maximize, Close) and tab strips with microsecond precision.

---

## API Quick Reference

| Method / Signature | Return Type | Description | Docs |
|:---|:---|:---|:---|
| `LinearLayout.horizontal()` | `LinearLayout` | Creates horizontal layout with default top alignment. | [Reference](docs/REFERENCE.md#2-class-fastlayoutlinearlayout) |
| `LinearLayout.vertical()` | `LinearLayout` | Creates vertical layout with default left alignment. | [Reference](docs/REFERENCE.md#2-class-fastlayoutlinearlayout) |
| `layout.measure(cells, ...)` | `LayoutMeasure` | Measures required total bounding box dimensions (`width`, `height`). | [Reference](docs/REFERENCE.md#2-class-fastlayoutlinearlayout) |
| `layout.arrange(cells, ..., Rect[] out)` | `void` | Arranges cells with zero allocations into pre-allocated `Rect[]` array. | [Reference](docs/REFERENCE.md#2-class-fastlayoutlinearlayout) |
| `layout.setAxis(Axis.VERTICAL)` | `LinearLayout` | Switches layout orientation dynamically at runtime. | [Reference](docs/REFERENCE.md#2-class-fastlayoutlinearlayout) |
| `FastLayout.measure(...)` | `LayoutMeasure` | Convenience facade method with uniform padding. | [Reference](docs/REFERENCE.md#1-class-fastlayoutfastlayout) |

---

## Installation

### Maven (JitPack)

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

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastLayout:0.1.0'
}
```

---

## Documentation

- **[API Reference Manual](docs/REFERENCE.md)** — Exhaustive method reference and memory contracts.
- **[Technical Roadmap](docs/ROADMAP.md)** — Planned algorithms (`GridLayout`, `WrapLayout`) and milestones.

---

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## Related Projects

- **[FastJava Suite](https://github.com/andrestubbe)** — High-performance native JVM runtime libraries.
- **[FastGUI](https://github.com/andrestubbe/FastGUI)** — Retained-mode baking GUI framework for Java.
- **[FastWindow](https://github.com/andrestubbe/FastWindow)** — High-performance native Win32 window management.
- **[FastTheme](https://github.com/andrestubbe/FastTheme)** — Native Windows DWM theme and dark mode engine.
- **[FastDialog](https://github.com/andrestubbe/FastDialog)** — Native Windows 11 Shell dialogs & pickers.
