package net.mythic.jjkmod.client.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;

/**
 * Gojo Satoru "Unlimited Void" themed combat hotbar HUD.
 *
 * Deep-blue infinity-themed bar with 6 glowing Six Eyes decorations
 * along the top ridge.  No horns — clean, ethereal design inspired
 * by Gojo's Infinity and the Six Eyes technique.
 *
 * All fill operations use {@link RenderLayer#getGuiOverlay()} so the
 * combat bar always renders on top of vanilla HUD elements.
 */
public class GojoCombatHud {

    // ── Render layer (no depth test → always on top) ────────────────────
    private static final RenderLayer OVERLAY = RenderLayer.getGuiOverlay();

    // ── Infinity palette ────────────────────────────────────────────────
    private static final int VOID_BODY       = 0xFF050A1A;
    private static final int VOID_DARK       = 0xFF020510;
    private static final int VOID_BORDER     = 0xFF1A4A8B;
    private static final int VOID_ACCENT     = 0xFF2A6ABB;
    private static final int VOID_HIGHLIGHT  = 0xFF33AAFF;
    private static final int EYE_OUTER       = 0xFF0D3D8A;
    private static final int EYE_GLOW        = 0xFF1166CC;
    private static final int EYE_IRIS        = 0xFF33CCFF;
    private static final int EYE_PUPIL       = 0xFFFFFFFF;
    private static final int SLOT_BG         = 0xFF030810;
    private static final int SLOT_BORDER     = 0xFF15406B;
    private static final int SLOT_HIGHLIGHT  = 0xFF2060A0;

    // ── Dimensions ──────────────────────────────────────────────────────
    private static final int SLOT_COUNT     = 9;
    private static final int SLOT_SIZE      = 20;
    private static final int SLOT_GAP       = 1;
    private static final int SLOTS_WIDTH    = SLOT_COUNT * SLOT_SIZE + (SLOT_COUNT - 1) * SLOT_GAP;
    private static final int BODY_HEIGHT    = 32;
    private static final int OVERHANG       = 18;
    private static final int ROOF_TAPER     = 8;
    private static final int EYE_COUNT      = 6;
    private static final int EYE_RADIUS     = 4;

    public static void render(DrawContext ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

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

            fill(ctx, rx, ry, rx + rowWidth, ry + 1, VOID_BODY);

            int edgeShade = 3;
            if (rowWidth > edgeShade * 2) {
                fill(ctx, rx, ry, rx + edgeShade, ry + 1, VOID_DARK);
                fill(ctx, rx + rowWidth - edgeShade, ry, rx + rowWidth, ry + 1, VOID_DARK);
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
            fill(ctx, rx, ry, rx + 2, ry + 1, VOID_BORDER);
            fill(ctx, rx + rowWidth - 2, ry, rx + rowWidth, ry + 1, VOID_BORDER);
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

    private static void drawSixEyes(DrawContext ctx, int centerX, int roofY, int roofWidth) {
        int startX = centerX - roofWidth / 2 + 16;
        int endX   = centerX + roofWidth / 2 - 16;
        int span   = endX - startX;
        int eyeCenterY = roofY - EYE_RADIUS - 2;

        for (int i = 0; i < EYE_COUNT; i++) {
            float t = (float)(i + 0.5f) / EYE_COUNT;
            int ex = startX + (int)(t * span);

            for (int dy = -EYE_RADIUS; dy <= EYE_RADIUS; dy++) {
                int halfW = EYE_RADIUS - Math.abs(dy);
                int hStretch = halfW + halfW / 2;
                fill(ctx, ex - hStretch, eyeCenterY + dy,
                         ex + hStretch + 1, eyeCenterY + dy + 1, EYE_OUTER);
            }

            int midR = 3;
            for (int dy = -midR; dy <= midR; dy++) {
                int halfW = midR - Math.abs(dy);
                int hStretch = halfW + halfW / 2;
                fill(ctx, ex - hStretch, eyeCenterY + dy,
                         ex + hStretch + 1, eyeCenterY + dy + 1, EYE_GLOW);
            }

            int irisR = 2;
            for (int dy = -irisR; dy <= irisR; dy++) {
                int halfW = irisR - Math.abs(dy);
                int hStretch = halfW + halfW / 2;
                fill(ctx, ex - hStretch, eyeCenterY + dy,
                         ex + hStretch + 1, eyeCenterY + dy + 1, EYE_IRIS);
            }

            fill(ctx, ex, eyeCenterY - 1, ex + 1, eyeCenterY + 2, EYE_PUPIL);
        }
    }

    private static void drawBottomGlow(DrawContext ctx, int centerX, int bottomY, int width) {
        int baseX = centerX - width / 2;
        int glowCount = 12;

        for (int i = 0; i < glowCount; i++) {
            float t = (float)(i + 0.5f) / glowCount;
            int gx = baseX + (int)(t * width);

            fill(ctx, gx - 1, bottomY,     gx + 2, bottomY + 1, VOID_BORDER);
            fill(ctx, gx,     bottomY + 1,  gx + 1, bottomY + 2, VOID_ACCENT);
        }
    }

    private static void drawSlot(DrawContext ctx, int x, int y, int size) {
        fill(ctx, x, y, x + size, y + size, SLOT_BG);

        drawRect(ctx, x, y, size, 1, SLOT_BORDER);
        drawRect(ctx, x, y + size - 1, size, 1, SLOT_BORDER);
        drawRect(ctx, x, y, 1, size, SLOT_BORDER);
        drawRect(ctx, x + size - 1, y, 1, size, SLOT_BORDER);

        drawRect(ctx, x + 1, y + 1, size - 2, 1, SLOT_HIGHLIGHT);
        drawRect(ctx, x + 1, y + 1, 1, size - 2, SLOT_HIGHLIGHT);

        fill(ctx, x + 2, y + size - 2, x + size - 1, y + size - 1, VOID_DARK);
        fill(ctx, x + size - 2, y + 2, x + size - 1, y + size - 1, VOID_DARK);
    }

    /** Overlay fill — always draws on top of vanilla HUD elements. */
    private static void fill(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(OVERLAY, x1, y1, x2, y2, color);
    }

    private static void drawRect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(OVERLAY, x, y, x + w, y + h, color);
    }
}
