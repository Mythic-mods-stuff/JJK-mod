package net.mythic.jjkmod.client.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * Malevolent Shrine-themed combat hotbar HUD.
 *
 * Draws a trapezoidal shrine-roof shape with horn/spike decorations
 * along the top ridge, 9 ability slots centered inside, using a
 * dark crimson/maroon palette inspired by Sukuna's Domain Expansion.
 */
public class CombatModeHud {

    // ── Shrine palette ──────────────────────────────────────────────────
    private static final int SHRINE_BODY       = 0xFF1A0505;  // deep dark maroon body
    private static final int SHRINE_DARK       = 0xFF0D0202;  // darker shadow areas
    private static final int SHRINE_BORDER     = 0xFF8B1A1A;  // crimson red border
    private static final int SHRINE_ACCENT     = 0xFFA52A2A;  // brighter red accents
    private static final int SHRINE_HIGHLIGHT  = 0xFFCC3333;  // bright crimson highlights
    private static final int HORN_BASE         = 0xFF3D1111;  // dark horn color
    private static final int HORN_TIP          = 0xFF1A0808;  // near-black horn tips
    private static final int SLOT_BG           = 0xFF0F0303;  // very dark slot background
    private static final int SLOT_BORDER       = 0xFF6B1515;  // crimson slot border
    private static final int SLOT_HIGHLIGHT    = 0xFF8B2020;  // slot hover/selected color

    // ── Dimensions ──────────────────────────────────────────────────────
    private static final int SLOT_COUNT     = 9;
    private static final int SLOT_SIZE      = 20;
    private static final int SLOT_GAP       = 1;
    private static final int SLOTS_WIDTH    = SLOT_COUNT * SLOT_SIZE + (SLOT_COUNT - 1) * SLOT_GAP;
    private static final int BODY_HEIGHT    = 32;     // total shrine body height
    private static final int OVERHANG       = 18;     // how far the roof extends beyond slots on each side
    private static final int ROOF_TAPER     = 8;      // how many pixels narrower the bottom is vs top
    private static final int HORN_HEIGHT    = 10;     // spike height above the roofline
    private static final int HORN_COUNT     = 14;     // number of horns along the ridge

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!CombatModeManager.isActive()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        // Shrine sits at the bottom of the screen, centered
        int topWidth   = SLOTS_WIDTH + OVERHANG * 2;            // widest at top (roof)
        int botWidth   = SLOTS_WIDTH + OVERHANG * 2 - ROOF_TAPER * 2;  // narrower at bottom

        int centerX    = screenW / 2;
        int bodyBottom = screenH - 1;        // leave 1px at very bottom
        int bodyTop    = bodyBottom - BODY_HEIGHT;

        // ── 1. Draw trapezoidal body (row by row) ──────────────────────
        for (int row = 0; row < BODY_HEIGHT; row++) {
            float t = (float) row / BODY_HEIGHT;  // 0 = top, 1 = bottom
            int rowWidth = (int)(topWidth + (botWidth - topWidth) * t);
            int rx = centerX - rowWidth / 2;
            int ry = bodyTop + row;

            // Main body color (slightly darker toward edges)
            ctx.fill(rx, ry, rx + rowWidth, ry + 1, SHRINE_BODY);

            // Darker edge shading (3px on each side)
            int edgeShade = 3;
            if (rowWidth > edgeShade * 2) {
                ctx.fill(rx, ry, rx + edgeShade, ry + 1, SHRINE_DARK);
                ctx.fill(rx + rowWidth - edgeShade, ry, rx + rowWidth, ry + 1, SHRINE_DARK);
            }
        }

        // ── 2. Top border line (roofline accent) ──────────────────────
        int roofX = centerX - topWidth / 2;
        drawRect(ctx, roofX, bodyTop, topWidth, 2, SHRINE_BORDER);
        drawRect(ctx, roofX, bodyTop + 2, topWidth, 1, SHRINE_ACCENT);

        // ── 3. Bottom border line ─────────────────────────────────────
        int botX = centerX - botWidth / 2;
        drawRect(ctx, botX, bodyBottom - 2, botWidth, 2, SHRINE_BORDER);

        // ── 4. Side edges (angled lines via stacked pixels) ───────────
        for (int row = 0; row < BODY_HEIGHT; row++) {
            float t = (float) row / BODY_HEIGHT;
            int rowWidth = (int)(topWidth + (botWidth - topWidth) * t);
            int rx = centerX - rowWidth / 2;
            int ry = bodyTop + row;
            // Left edge
            ctx.fill(rx, ry, rx + 2, ry + 1, SHRINE_BORDER);
            // Right edge
            ctx.fill(rx + rowWidth - 2, ry, rx + rowWidth, ry + 1, SHRINE_BORDER);
        }

        // ── 5. Horn/spike decorations along top ridge ─────────────────
        drawHorns(ctx, centerX, bodyTop, topWidth);

        // ── 6. Decorative inner ridge (shrine roof line) ──────────────
        int ridgeY = bodyTop + 5;
        int ridgeW = topWidth - 6;
        int ridgeX = centerX - ridgeW / 2;
        drawRect(ctx, ridgeX, ridgeY, ridgeW, 1, SHRINE_ACCENT);

        // ── 7. Corner ornaments (small bone-like marks) ───────────────
        // Top-left and top-right small decorative marks
        int tlx = centerX - topWidth / 2 + 3;
        int trx = centerX + topWidth / 2 - 7;
        drawRect(ctx, tlx, bodyTop + 3, 4, 1, SHRINE_HIGHLIGHT);
        drawRect(ctx, trx, bodyTop + 3, 4, 1, SHRINE_HIGHLIGHT);

        // ── 8. Nine ability slots ─────────────────────────────────────
        int slotsStartX = centerX - SLOTS_WIDTH / 2;
        int slotsY = bodyTop + (BODY_HEIGHT - SLOT_SIZE) / 2 + 2;  // vertically centered, nudged down

        for (int i = 0; i < SLOT_COUNT; i++) {
            int sx = slotsStartX + i * (SLOT_SIZE + SLOT_GAP);
            drawSlot(ctx, sx, slotsY, SLOT_SIZE);
        }

        // ── 9. Bottom teeth/bone fragments ────────────────────────────
        drawBottomTeeth(ctx, centerX, bodyBottom, botWidth);
    }

    /**
     * Draws horn/spike shapes along the top roofline.
     * Each horn is a narrow tapered shape built from stacked 1px rows.
     */
    private static void drawHorns(DrawContext ctx, int centerX, int roofY, int roofWidth) {
        int roofX = centerX - roofWidth / 2;

        for (int h = 0; h < HORN_COUNT; h++) {
            // Distribute horns evenly along the roof
            float t = (float)(h + 0.5f) / HORN_COUNT;
            int hornCenterX = roofX + (int)(t * roofWidth);

            // Horns curve outward: ones near edges lean outward more
            float lean = (t - 0.5f) * 1.8f;  // negative = lean left, positive = lean right

            // Draw horn from tip (top) to base (roofline)
            for (int row = 0; row < HORN_HEIGHT; row++) {
                float rowT = (float) row / HORN_HEIGHT;  // 0 = tip, 1 = base
                int width = 1 + (int)(rowT * 2.5f);      // gets wider toward base
                int xOffset = (int)(lean * (HORN_HEIGHT - row) * 0.3f);

                int hx = hornCenterX - width / 2 + xOffset;
                int hy = roofY - HORN_HEIGHT + row;

                // Gradient from dark tip to lighter base
                int color = rowT < 0.4f ? HORN_TIP : HORN_BASE;
                ctx.fill(hx, hy, hx + width, hy + 1, color);
            }
        }
    }

    /**
     * Draws small tooth/bone fragments along the bottom edge.
     */
    private static void drawBottomTeeth(DrawContext ctx, int centerX, int bottomY, int width) {
        int baseX = centerX - width / 2;
        int teethCount = 8;

        for (int i = 0; i < teethCount; i++) {
            float t = (float)(i + 0.5f) / teethCount;
            int toothX = baseX + (int)(t * width);
            int toothHeight = 3 + (i % 2 == 0 ? 1 : 0);  // alternating heights

            // Small downward triangle
            for (int row = 0; row < toothHeight; row++) {
                int w = toothHeight - row;
                ctx.fill(toothX - w / 2, bottomY + row, toothX + (w + 1) / 2, bottomY + row + 1, HORN_BASE);
            }
        }
    }

    /**
     * Draws a single ability slot with border and inner shading.
     */
    private static void drawSlot(DrawContext ctx, int x, int y, int size) {
        // Slot background
        ctx.fill(x, y, x + size, y + size, SLOT_BG);

        // Outer border (1px)
        drawRect(ctx, x, y, size, 1, SLOT_BORDER);                     // top
        drawRect(ctx, x, y + size - 1, size, 1, SLOT_BORDER);          // bottom
        drawRect(ctx, x, y, 1, size, SLOT_BORDER);                     // left
        drawRect(ctx, x + size - 1, y, 1, size, SLOT_BORDER);          // right

        // Inner top-left highlight (1px)
        drawRect(ctx, x + 1, y + 1, size - 2, 1, SLOT_HIGHLIGHT);     // top glow
        drawRect(ctx, x + 1, y + 1, 1, size - 2, SLOT_HIGHLIGHT);     // left glow

        // Inner bottom-right shadow
        ctx.fill(x + 2, y + size - 2, x + size - 1, y + size - 1, SHRINE_DARK);  // bottom
        ctx.fill(x + size - 2, y + 2, x + size - 1, y + size - 1, SHRINE_DARK);  // right
    }

    private static void drawRect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + h, color);
    }
}
