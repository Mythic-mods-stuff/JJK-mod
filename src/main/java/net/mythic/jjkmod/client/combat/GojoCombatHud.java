package net.mythic.jjkmod.client.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Gojo Satoru "Unlimited Void" themed combat hotbar HUD.
 *
 * Deep-blue infinity-themed bar with 6 glowing Six Eyes decorations
 * along the top ridge.  No horns — clean, ethereal design inspired
 * by Gojo's Infinity and the Six Eyes technique.
 */
public class GojoCombatHud {

    // ── Infinity palette ────────────────────────────────────────────────
    private static final int VOID_BODY       = 0xFF050A1A;  // deep dark navy body
    private static final int VOID_DARK       = 0xFF020510;  // darker shadow areas
    private static final int VOID_BORDER     = 0xFF1A4A8B;  // blue border
    private static final int VOID_ACCENT     = 0xFF2A6ABB;  // brighter blue accents
    private static final int VOID_HIGHLIGHT  = 0xFF33AAFF;  // bright electric blue highlights
    private static final int EYE_OUTER       = 0xFF0D3D8A;  // Six Eyes outer ring
    private static final int EYE_GLOW        = 0xFF1166CC;  // Six Eyes middle glow
    private static final int EYE_IRIS        = 0xFF33CCFF;  // Six Eyes bright iris
    private static final int EYE_PUPIL       = 0xFFFFFFFF;  // white pupil core
    private static final int SLOT_BG         = 0xFF030810;  // very dark slot background
    private static final int SLOT_BORDER     = 0xFF15406B;  // blue slot border
    private static final int SLOT_HIGHLIGHT  = 0xFF2060A0;  // slot hover/selected glow

    // ── Dimensions ──────────────────────────────────────────────────────
    private static final int SLOT_COUNT     = 9;
    private static final int SLOT_SIZE      = 20;
    private static final int SLOT_GAP       = 1;
    private static final int SLOTS_WIDTH    = SLOT_COUNT * SLOT_SIZE + (SLOT_COUNT - 1) * SLOT_GAP;
    private static final int BODY_HEIGHT    = 32;
    private static final int OVERHANG       = 18;
    private static final int ROOF_TAPER     = 8;
    private static final int EYE_COUNT      = 6;
    private static final int EYE_RADIUS     = 4;       // outer radius of each eye

    public static void render(DrawContext ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        // Bar sits at the bottom of the screen, centered
        int topWidth   = SLOTS_WIDTH + OVERHANG * 2;
        int botWidth   = SLOTS_WIDTH + OVERHANG * 2 - ROOF_TAPER * 2;

        int centerX    = screenW / 2;
        int bodyBottom = screenH - 1;
        int bodyTop    = bodyBottom - BODY_HEIGHT;

        // ── 1. Draw trapezoidal body (row by row) ──────────────────────
        for (int row = 0; row < BODY_HEIGHT; row++) {
            float t = (float) row / BODY_HEIGHT;
            int rowWidth = (int)(topWidth + (botWidth - topWidth) * t);
            int rx = centerX - rowWidth / 2;
            int ry = bodyTop + row;

            ctx.fill(rx, ry, rx + rowWidth, ry + 1, VOID_BODY);

            // Darker edge shading (3px on each side)
            int edgeShade = 3;
            if (rowWidth > edgeShade * 2) {
                ctx.fill(rx, ry, rx + edgeShade, ry + 1, VOID_DARK);
                ctx.fill(rx + rowWidth - edgeShade, ry, rx + rowWidth, ry + 1, VOID_DARK);
            }
        }

        // ── 2. Top border line (roofline accent) ──────────────────────
        int roofX = centerX - topWidth / 2;
        drawRect(ctx, roofX, bodyTop, topWidth, 2, VOID_BORDER);
        drawRect(ctx, roofX, bodyTop + 2, topWidth, 1, VOID_ACCENT);

        // ── 3. Bottom border line ─────────────────────────────────────
        int botX = centerX - botWidth / 2;
        drawRect(ctx, botX, bodyBottom - 2, botWidth, 2, VOID_BORDER);

        // ── 4. Side edges (angled lines via stacked pixels) ───────────
        for (int row = 0; row < BODY_HEIGHT; row++) {
            float t = (float) row / BODY_HEIGHT;
            int rowWidth = (int)(topWidth + (botWidth - topWidth) * t);
            int rx = centerX - rowWidth / 2;
            int ry = bodyTop + row;
            // Left edge
            ctx.fill(rx, ry, rx + 2, ry + 1, VOID_BORDER);
            // Right edge
            ctx.fill(rx + rowWidth - 2, ry, rx + rowWidth, ry + 1, VOID_BORDER);
        }

        // ── 5. Six Eyes (6 large glowing eyes above the bar) ─────────
        drawSixEyes(ctx, centerX, bodyTop, topWidth);

        // ── 6. Decorative inner ridge ─────────────────────────────────
        int ridgeY = bodyTop + 5;
        int ridgeW = topWidth - 6;
        int ridgeX = centerX - ridgeW / 2;
        drawRect(ctx, ridgeX, ridgeY, ridgeW, 1, VOID_ACCENT);

        // ── 7. Corner accents (infinity marks) ────────────────────────
        int tlx = centerX - topWidth / 2 + 3;
        int trx = centerX + topWidth / 2 - 7;
        drawRect(ctx, tlx, bodyTop + 3, 4, 1, VOID_HIGHLIGHT);
        drawRect(ctx, trx, bodyTop + 3, 4, 1, VOID_HIGHLIGHT);

        // ── 8. Nine ability slots ─────────────────────────────────────
        int slotsStartX = centerX - SLOTS_WIDTH / 2;
        int slotsY = bodyTop + (BODY_HEIGHT - SLOT_SIZE) / 2 + 2;

        for (int i = 0; i < SLOT_COUNT; i++) {
            int sx = slotsStartX + i * (SLOT_SIZE + SLOT_GAP);
            drawSlot(ctx, sx, slotsY, SLOT_SIZE);
        }

        // ── 9. Bottom glow strip (Infinity aura) ─────────────────────
        drawBottomGlow(ctx, centerX, bodyBottom, botWidth);
    }

    /**
     * Draws 6 large glowing Six Eyes evenly spaced above the roofline.
     *
     * Each eye is built as concentric diamond/oval layers:
     *   – outer ring   (dark blue glow)
     *   – middle ring  (medium blue)
     *   – iris         (bright cyan)
     *   – pupil centre (white)
     */
    private static void drawSixEyes(DrawContext ctx, int centerX, int roofY, int roofWidth) {
        int startX = centerX - roofWidth / 2 + 16;
        int endX   = centerX + roofWidth / 2 - 16;
        int span   = endX - startX;
        int eyeCenterY = roofY - EYE_RADIUS - 2;  // above the roofline

        for (int i = 0; i < EYE_COUNT; i++) {
            float t = (float)(i + 0.5f) / EYE_COUNT;
            int ex = startX + (int)(t * span);

            // Layer 1: Outer glow (radius 4) — dark blue diamond
            for (int dy = -EYE_RADIUS; dy <= EYE_RADIUS; dy++) {
                int halfW = EYE_RADIUS - Math.abs(dy);
                // Stretch horizontally for an eye/oval shape (1.5× wider)
                int hStretch = halfW + halfW / 2;
                ctx.fill(ex - hStretch, eyeCenterY + dy,
                         ex + hStretch + 1, eyeCenterY + dy + 1, EYE_OUTER);
            }

            // Layer 2: Middle glow (radius 3) — medium blue
            int midR = 3;
            for (int dy = -midR; dy <= midR; dy++) {
                int halfW = midR - Math.abs(dy);
                int hStretch = halfW + halfW / 2;
                ctx.fill(ex - hStretch, eyeCenterY + dy,
                         ex + hStretch + 1, eyeCenterY + dy + 1, EYE_GLOW);
            }

            // Layer 3: Iris (radius 2) — bright cyan
            int irisR = 2;
            for (int dy = -irisR; dy <= irisR; dy++) {
                int halfW = irisR - Math.abs(dy);
                int hStretch = halfW + halfW / 2;
                ctx.fill(ex - hStretch, eyeCenterY + dy,
                         ex + hStretch + 1, eyeCenterY + dy + 1, EYE_IRIS);
            }

            // Layer 4: Pupil centre (1×3 vertical slit)
            ctx.fill(ex, eyeCenterY - 1, ex + 1, eyeCenterY + 2, EYE_PUPIL);
        }
    }

    /**
     * Draws a subtle blue glow strip along the bottom edge
     * (Infinity barrier effect instead of Sukuna's teeth).
     */
    private static void drawBottomGlow(DrawContext ctx, int centerX, int bottomY, int width) {
        int baseX = centerX - width / 2;
        int glowCount = 12;

        for (int i = 0; i < glowCount; i++) {
            float t = (float)(i + 0.5f) / glowCount;
            int gx = baseX + (int)(t * width);

            // Small downward glow dot
            ctx.fill(gx - 1, bottomY,     gx + 2, bottomY + 1, VOID_BORDER);
            ctx.fill(gx,     bottomY + 1,  gx + 1, bottomY + 2, VOID_ACCENT);
        }
    }

    /**
     * Draws a single ability slot with border and inner shading.
     */
    private static void drawSlot(DrawContext ctx, int x, int y, int size) {
        // Slot background
        ctx.fill(x, y, x + size, y + size, SLOT_BG);

        // Outer border (1px)
        drawRect(ctx, x, y, size, 1, SLOT_BORDER);
        drawRect(ctx, x, y + size - 1, size, 1, SLOT_BORDER);
        drawRect(ctx, x, y, 1, size, SLOT_BORDER);
        drawRect(ctx, x + size - 1, y, 1, size, SLOT_BORDER);

        // Inner top-left highlight (1px)
        drawRect(ctx, x + 1, y + 1, size - 2, 1, SLOT_HIGHLIGHT);
        drawRect(ctx, x + 1, y + 1, 1, size - 2, SLOT_HIGHLIGHT);

        // Inner bottom-right shadow
        ctx.fill(x + 2, y + size - 2, x + size - 1, y + size - 1, VOID_DARK);
        ctx.fill(x + size - 2, y + 2, x + size - 1, y + size - 1, VOID_DARK);
    }

    private static void drawRect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + h, color);
    }
}
