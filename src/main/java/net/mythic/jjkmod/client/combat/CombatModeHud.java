package net.mythic.jjkmod.client.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * Renders a Sukuna-themed 9-slot hotbar directly over the vanilla one
 * when combat mode is active. Dark red/black recolor with thicker borders.
 */
public class CombatModeHud {

    // ── Colors ─────────────────────────────────────────────────────────
    private static final int PANEL_BG       = 0xFF0A0000;  // near-black red (opaque)
    private static final int PANEL_BORDER   = 0xFF8B0000;  // dark red outer border
    private static final int SLOT_BG        = 0xFF150303;  // very dark slot fill
    private static final int SLOT_BORDER    = 0xFF6B0000;  // crimson slot border
    private static final int INNER_GLOW     = 0xFF2A0505;  // subtle inner edge

    // ── Vanilla hotbar dimensions ──────────────────────────────────────
    private static final int VANILLA_W  = 182;
    private static final int VANILLA_H  = 22;
    private static final int SLOT_COUNT = 9;
    private static final int SLOT_SIZE  = 20;
    private static final int PAD        = 3;   // extra pixels beyond vanilla = "thicker"

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!CombatModeManager.isActive()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        // Vanilla hotbar position
        int hotbarX = (screenW - VANILLA_W) / 2;
        int hotbarY = screenH - VANILLA_H;

        // Our panel extends beyond vanilla for thicker look
        int panelX = hotbarX - PAD;
        int panelY = hotbarY - PAD;
        int panelW = VANILLA_W + PAD * 2;
        int panelH = VANILLA_H + PAD * 2;

        // ── Opaque background (fully covers vanilla hotbar) ────────────
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL_BG);

        // ── Thick outer border (2px) ───────────────────────────────────
        drawRect(ctx, panelX, panelY, panelW, 2, PANEL_BORDER);                        // top
        drawRect(ctx, panelX, panelY + panelH - 2, panelW, 2, PANEL_BORDER);           // bottom
        drawRect(ctx, panelX, panelY, 2, panelH, PANEL_BORDER);                        // left
        drawRect(ctx, panelX + panelW - 2, panelY, 2, panelH, PANEL_BORDER);           // right

        // ── Inner glow line (1px inside border) ────────────────────────
        drawRect(ctx, panelX + 2, panelY + 2, panelW - 4, 1, INNER_GLOW);             // top
        drawRect(ctx, panelX + 2, panelY + panelH - 3, panelW - 4, 1, INNER_GLOW);    // bottom
        drawRect(ctx, panelX + 2, panelY + 2, 1, panelH - 4, INNER_GLOW);             // left
        drawRect(ctx, panelX + panelW - 3, panelY + 2, 1, panelH - 4, INNER_GLOW);    // right

        // ── 9 ability slots ────────────────────────────────────────────
        int slotsStartX = hotbarX + 1;
        int slotsY = hotbarY + 1;

        for (int i = 0; i < SLOT_COUNT; i++) {
            int sx = slotsStartX + i * SLOT_SIZE;

            // Slot background
            ctx.fill(sx, slotsY, sx + SLOT_SIZE, slotsY + SLOT_SIZE, SLOT_BG);

            // Slot border (1px)
            drawRect(ctx, sx, slotsY, SLOT_SIZE, 1, SLOT_BORDER);                      // top
            drawRect(ctx, sx, slotsY + SLOT_SIZE - 1, SLOT_SIZE, 1, SLOT_BORDER);      // bottom
            drawRect(ctx, sx, slotsY, 1, SLOT_SIZE, SLOT_BORDER);                      // left
            drawRect(ctx, sx + SLOT_SIZE - 1, slotsY, 1, SLOT_SIZE, SLOT_BORDER);      // right
        }
    }

    private static void drawRect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + h, color);
    }
}
