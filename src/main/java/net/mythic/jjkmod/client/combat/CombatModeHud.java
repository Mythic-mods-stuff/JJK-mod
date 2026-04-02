package net.mythic.jjkmod.client.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.mythic.jjkmod.client.ClientCharacterData;

/**
 * Combat-mode HUD dispatcher.
 *
 * Routes rendering to the character-specific HUD based on the
 * player's selected character:
 *   – Sukuna → Malevolent Shrine theme (crimson, horns, teeth)
 *   – Gojo   → Unlimited Void theme (blue, Six Eyes, no horns)
 *   – Default falls through to Sukuna style.
 */
public class CombatModeHud {

    // ── Shrine palette (Sukuna) ─────────────────────────────────────────
    private static final int SHRINE_BODY       = 0xFF1A0505;
    private static final int SHRINE_DARK       = 0xFF0D0202;
    private static final int SHRINE_BORDER     = 0xFF8B1A1A;
    private static final int SHRINE_ACCENT     = 0xFFA52A2A;
    private static final int SHRINE_HIGHLIGHT  = 0xFFCC3333;
    private static final int HORN_BASE         = 0xFF3D1111;
    private static final int HORN_TIP          = 0xFF1A0808;
    private static final int SLOT_BG           = 0xFF0F0303;
    private static final int SLOT_BORDER       = 0xFF6B1515;
    private static final int SLOT_HIGHLIGHT    = 0xFF8B2020;

    // ── Dimensions ──────────────────────────────────────────────────────
    private static final int SLOT_COUNT     = 9;
    private static final int SLOT_SIZE      = 20;
    private static final int SLOT_GAP       = 1;
    private static final int SLOTS_WIDTH    = SLOT_COUNT * SLOT_SIZE + (SLOT_COUNT - 1) * SLOT_GAP;
    private static final int BODY_HEIGHT    = 32;
    private static final int OVERHANG       = 18;
    private static final int ROOF_TAPER     = 8;
    private static final int HORN_HEIGHT    = 10;
    private static final int HORN_COUNT     = 14;

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!CombatModeManager.isActive()) return;

        // ── Dispatch to character-specific HUD ─────────────────────────
        if (ClientCharacterData.isGojo()) {
            GojoCombatHud.render(ctx);
            return;
        }

        // ── Default / Sukuna: Malevolent Shrine HUD ────────────────────
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

            ctx.fill(rx, ry, rx + rowWidth, ry + 1, SHRINE_BODY);

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
            ctx.fill(rx, ry, rx + 2, ry + 1, SHRINE_BORDER);
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
        int tlx = centerX - topWidth / 2 + 3;
        int trx = centerX + topWidth / 2 - 7;
        drawRect(ctx, tlx, bodyTop + 3, 4, 1, SHRINE_HIGHLIGHT);
        drawRect(ctx, trx, bodyTop + 3, 4, 1, SHRINE_HIGHLIGHT);

        // ── 8. Nine ability slots ─────────────────────────────────────
        int slotsStartX = centerX - SLOTS_WIDTH / 2;
        int slotsY = bodyTop + (BODY_HEIGHT - SLOT_SIZE) / 2 + 2;

        for (int i = 0; i < SLOT_COUNT; i++) {
            int sx = slotsStartX + i * (SLOT_SIZE + SLOT_GAP);
            drawSlot(ctx, sx, slotsY, SLOT_SIZE);
        }

        // ── 9. Bottom teeth/bone fragments ────────────────────────────
        drawBottomTeeth(ctx, centerX, bodyBottom, botWidth);
    }

    private static void drawHorns(DrawContext ctx, int centerX, int roofY, int roofWidth) {
        int roofX = centerX - roofWidth / 2;

        for (int h = 0; h < HORN_COUNT; h++) {
            float t = (float)(h + 0.5f) / HORN_COUNT;
            int hornCenterX = roofX + (int)(t * roofWidth);

            float lean = (t - 0.5f) * 1.8f;

            for (int row = 0; row < HORN_HEIGHT; row++) {
                float rowT = (float) row / HORN_HEIGHT;
                int width = 1 + (int)(rowT * 2.5f);
                int xOffset = (int)(lean * (HORN_HEIGHT - row) * 0.3f);

                int hx = hornCenterX - width / 2 + xOffset;
                int hy = roofY - HORN_HEIGHT + row;

                int color = rowT < 0.4f ? HORN_TIP : HORN_BASE;
                ctx.fill(hx, hy, hx + width, hy + 1, color);
            }
        }
    }

    private static void drawBottomTeeth(DrawContext ctx, int centerX, int bottomY, int width) {
        int baseX = centerX - width / 2;
        int teethCount = 8;

        for (int i = 0; i < teethCount; i++) {
            float t = (float)(i + 0.5f) / teethCount;
            int toothX = baseX + (int)(t * width);
            int toothHeight = 3 + (i % 2 == 0 ? 1 : 0);

            for (int row = 0; row < toothHeight; row++) {
                int w = toothHeight - row;
                ctx.fill(toothX - w / 2, bottomY + row, toothX + (w + 1) / 2, bottomY + row + 1, HORN_BASE);
            }
        }
    }

    private static void drawSlot(DrawContext ctx, int x, int y, int size) {
        ctx.fill(x, y, x + size, y + size, SLOT_BG);

        drawRect(ctx, x, y, size, 1, SLOT_BORDER);
        drawRect(ctx, x, y + size - 1, size, 1, SLOT_BORDER);
        drawRect(ctx, x, y, 1, size, SLOT_BORDER);
        drawRect(ctx, x + size - 1, y, 1, size, SLOT_BORDER);

        drawRect(ctx, x + 1, y + 1, size - 2, 1, SLOT_HIGHLIGHT);
        drawRect(ctx, x + 1, y + 1, 1, size - 2, SLOT_HIGHLIGHT);

        ctx.fill(x + 2, y + size - 2, x + size - 1, y + size - 1, SHRINE_DARK);
        ctx.fill(x + size - 2, y + 2, x + size - 1, y + size - 1, SHRINE_DARK);
    }

    private static void drawRect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + h, color);
    }
}
