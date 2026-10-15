package fastlayout;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FastLayoutTest {

    @Test
    public void testHorizontalToolbarFitContent() {
        // 4 Icons each 32x32, 8px gap, 10px padding
        List<Cell> cells = new ArrayList<>();
        cells.add(new Cell(32, 32));
        cells.add(new Cell(32, 32));
        cells.add(new Cell(32, 32));
        cells.add(new Cell(32, 32));

        LinearLayout layout = LinearLayout.horizontal();

        // Measure: 4 * 32 = 128 contentW + 3 * 8 (gaps = 24) + 20 (padding = 10+10) = 172
        // Height: 32 + 20 (padding = 10+10) = 52
        LayoutMeasure measure = layout.measure(cells, 0, 0, 8, 10, 10, 10, 10);
        assertEquals(172f, measure.width(), 0.001f);
        assertEquals(52f, measure.height(), 0.001f);

        // Arrange into measured size
        Rect[] out = new Rect[4];
        for (int i = 0; i < 4; i++) out[i] = new Rect();
        layout.arrange(cells, measure.width(), measure.height(), 8, 10, 10, 10, 10, out);

        // Cell 0: x = 10, y = 10, w = 32, h = 32
        assertEquals(10f, out[0].x, 0.001f);
        assertEquals(10f, out[0].y, 0.001f);
        assertEquals(32f, out[0].w, 0.001f);
        assertEquals(32f, out[0].h, 0.001f);

        // Cell 1: x = 10 + 32 + 8 = 50
        assertEquals(50f, out[1].x, 0.001f);
        // Cell 2: x = 50 + 32 + 8 = 90
        assertEquals(90f, out[2].x, 0.001f);
        // Cell 3: x = 90 + 32 + 8 = 130
        assertEquals(130f, out[3].x, 0.001f);
    }

    @Test
    public void testVerticalMenuFitContent() {
        // 3 Menu entries: item0: 120x24, item1: 160x24 (widest), item2: 90x24
        List<Cell> cells = new ArrayList<>();
        cells.add(new Cell(120, 24));
        cells.add(new Cell(160, 24));
        cells.add(new Cell(90, 24));

        LinearLayout layout = LinearLayout.vertical(Alignment.STRETCH);

        // Gap: 4, Padding: 6
        // Width: max(120, 160, 90) = 160 + 12 (padding) = 172
        // Height: 3 * 24 = 72 + 2 * 4 (gaps = 8) + 12 (padding) = 92
        LayoutMeasure measure = layout.measure(cells, 0, 0, 4, 6, 6, 6, 6);
        assertEquals(172f, measure.width(), 0.001f);
        assertEquals(92f, measure.height(), 0.001f);

        Rect[] out = new Rect[3];
        for (int i = 0; i < 3; i++) out[i] = new Rect();
        layout.arrange(cells, measure.width(), measure.height(), 4, 6, 6, 6, 6, out);

        // Since Alignment.STRETCH is active, each item width becomes 160
        assertEquals(160f, out[0].w, 0.001f);
        assertEquals(160f, out[1].w, 0.001f);
        assertEquals(160f, out[2].w, 0.001f);

        assertEquals(6f, out[0].y, 0.001f);
        assertEquals(6f + 24f + 4f, out[1].y, 0.001f);
        assertEquals(6f + 24f + 4f + 24f + 4f, out[2].y, 0.001f);
    }

    @Test
    public void testDynamicAxisSwitch() {
        List<Cell> cells = new ArrayList<>();
        cells.add(new Cell(20, 20));
        cells.add(new Cell(20, 20));

        // Horizontal
        LinearLayout hLayout = LinearLayout.horizontal();
        LayoutMeasure mH = hLayout.measure(cells, 0, 0, 5, 0, 0, 0, 0);
        assertEquals(45f, mH.width(), 0.001f); // 20 + 5 + 20
        assertEquals(20f, mH.height(), 0.001f);

        // Vertical
        LinearLayout vLayout = LinearLayout.vertical();
        LayoutMeasure mV = vLayout.measure(cells, 0, 0, 5, 0, 0, 0, 0);
        assertEquals(20f, mV.width(), 0.001f);
        assertEquals(45f, mV.height(), 0.001f); // 20 + 5 + 20
    }
}
