# FastLayout 0.1.0 [ALPHA] — Decoupled Zero-Allocation Layout Engine for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastLayout/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Cross%20Platform-lightgrey.svg)]()
[![JitPack](https://jitpack.io/v/andrestubbe/FastLayout.svg)](https://jitpack.io/#andrestubbe/FastLayout)

---

**⚡ Zero-allocation, decoupled primitive layout engine for the FastJava ecosystem. High-performance layout pipelines for toolbars, floating bars, popup menus, and settings panels.**

**FastLayout** completely decouples geometry computation and alignment from graphical components, widgets, and window toolkits. By calculating layout bounds purely over lightweight `Cell` descriptors and writing directly into primitive-backed `Rect` structures (`float x, y, w, h`), FastLayout delivers microsecond execution and zero garbage collection overhead.

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

        HorizontalLayout layout = new HorizontalLayout(Alignment.CENTER);

        // 1. Measure exact bounding box required by the icons (8px gap, 10px padding)
        LayoutMeasure measure = layout.measure(cells, 0, 0, 8, 10, 10, 10, 10);
        System.out.printf("Window size: %.0f × %.0f px%n", measure.width(), measure.height());
        // -> Window size: 172 × 52 px (Mica/Acrylic window sizes to this exact size!)

        // 2. Arrange items with zero allocations
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

        VerticalLayout layout = new VerticalLayout(Alignment.STRETCH);

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
- **📏 Two Core Algorithms**:
  - `HorizontalLayout`: Fit-to-content icon bars, toolbars, breadcrumbs, flex-rows.
  - `VerticalLayout`: Dropdown menus, popup settings, command palettes, flex-columns.
- **📐 Flexible Sizing**: Supports fixed sizes, flex weights (`weight > 0`), min/max bounds, margins, and padding.
- **🎯 Alignment Control**: Main-axis & cross-axis alignments (`START`, `CENTER`, `END`, `STRETCH`).

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

---

## License

MIT License — Copyright (c) 2026 Andre Stubbe & FastJava Team.
