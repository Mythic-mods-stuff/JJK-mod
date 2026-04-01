package net.mythic.jjkmod.client.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * Renders a Sukuna-themed ability hotbar when combat mode is active.
 * Dark red/black aesthetic inspired by Malevolent Shrine.
 */
public class CombatModeHud {

    // ── Colors ─────────────────────────────────────────────────────────
    private static final int BG_COLOR       = 0xDD0D0000;  // very dark red, mostly opaque
    private static final int PANEL_BORDER   = 0xFF8B0000;  // dark red border
    private static final int SLOT_BG        = 0xFF1A0505;  // near-black red
    private static final int SLOT_BORDER    = 0xFFAA0000;  // crimson border
    private static final int TEXT_COLOR     = 0xFFFF6666;  // salmon/light red
    private static final int TITLE_COLOR    = 0xFFFFD700;  // gold
    private static final int LABEL_COLOR    = 0xFFCC9999;  // muted pink
    private static final int KEY_COLOR      = 0xFF666666;  // gray for keybind hints

    // ── Layout ─────────────────────────────────────────────────────────
    private static final int SLOT_SIZE   = 22;
    private static final int SLOT_GAP    = 6;
    private static final int PADDING     = 10;
    private static final int TITLE_H     = 14;
    private static final int LABEL_H     = 12;

    private static final String[] SLOT_NAMES = {
            "Dismantle", "Cleave", "Domain", "Fire Arrow"
    };
    private static final String[] SLOT_KEYS = {
            "1", "2", "3", "4"
    };
    private static final String TITLE = "\u2726 SUKUNA \u2726";

    /**
     * Called every frame from the HUD render callback.
     */
    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!CombatModeManager.isActive()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        TextRenderer font = client.textRenderer;

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        int slotCount = SLOT_NAMES.length;
        int slotsWidth = slotCount * SLOT_SIZE + (slotCount - 1) * SLOT_GAP;
        int panelW = slotsWidth + PADDING * 2;
        int panelH = TITLE_H + 4 + SLOT_SIZE + 2 + LABEL_H + PADDING + 4;

        // Position: centered, above the vanilla hotbar (vanilla hotbar is ~22px from bottom)
        int panelX = (screenW - panelW) / 2;
        int panelY = screenH - 62 - panelH;

        // ── Background panel ───────────────────────────────────────────
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG_COLOR);

        // Border (draw 4 edges)
        ctx.fill(panelX, panelY, panelX + panelW, panelY + 1, PANEL_BORDER);           // top
        ctx.fill(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, PANEL_BORDER); // bottom
        ctx.fill(panelX, panelY, panelX + 1, panelY + panelH, PANEL_BORDER);           // left
        ctx.fill(panelX + panelW - 1, panelY, panelX + panelW, panelY + panelH, PANEL_BORDER); // right

        // ── Title ──────────────────────────────────────────────────────
        int titleW = font.getWidth(TITLE);
        int titleX = panelX + (panelW - titleW) / 2;
        int titleY = panelY + 5;
        ctx.drawText(font, TITLE, titleX, titleY, TITLE_COLOR, true);

        // Decorative line under title
        int lineY = titleY + 11;
        int lineMargin = 8;
        ctx.fill(panelX + lineMargin, lineY, panelX + panelW - lineMargin, lineY + 1, PANEL_BORDER);

        // ── Ability slots ──────────────────────────────────────────────
        int slotsStartX = panelX + PADDING;
        int slotsY = lineY + 4;

        for (int i = 0; i < slotCount; i++) {
            int slotX = slotsStartX + i * (SLOT_SIZE + SLOT_GAP);

            // Slot background
            ctx.fill(slotX, slotsY, slotX + SLOT_SIZE, slotsY + SLOT_SIZE, SLOT_BG);

            // Slot border
            ctx.fill(slotX, slotsY, slotX + SLOT_SIZE, slotsY + 1, SLOT_BORDER);
            ctx.fill(slotX, slotsY + SLOT_SIZE - 1, slotX + SLOT_SIZE, slotsY + SLOT_SIZE, SLOT_BORDER);
            ctx.fill(slotX, slotsY, slotX + 1, slotsY + SLOT_SIZE, SLOT_BORDER);
            ctx.fill(slotX + SLOT_SIZE - 1, slotsY, slotX + SLOT_SIZE, slotsY + SLOT_SIZE, SLOT_BORDER);

            // Key number (centered in slot)
            String key = SLOT_KEYS[i];
            int keyW = font.getWidth(key);
            ctx.drawText(font, key, slotX + (SLOT_SIZE - keyW) / 2,
                    slotsY + (SLOT_SIZE - 8) / 2, TEXT_COLOR, true);
        }

        // ── Slot labels (below slots) ──────────────────────────────────
        int labelY = slotsY + SLOT_SIZE + 3;
        for (int i = 0; i < slotCount; i++) {
            int slotX = slotsStartX + i * (SLOT_SIZE + SLOT_GAP);
            String label = SLOT_NAMES[i];
            int labelW = font.getWidth(label);
            // Center label under slot; use 0.5 scale for small text
            int labelX = slotX + (SLOT_SIZE - labelW) / 2;
            ctx.drawText(font, label, labelX, labelY, LABEL_COLOR, false);
        }

        // ── Combat mode indicator ──────────────────────────────────────
        String hint = "[R] Combat Mode";
        int hintW = font.getWidth(hint);
        ctx.drawText(font, hint, panelX + (panelW - hintW) / 2,
                panelY + panelH + 2, KEY_COLOR, false);
    }
}
