package net.mythic.jjkmod.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class CursedEnergyHudOverlay {

    private static final int BAR_WIDTH = 82;
    private static final int BAR_HEIGHT = 8;
    private static final int BAR_OFFSET_X = 4;
    private static final int BAR_OFFSET_Y = 4;

    private static final int BACKGROUND_COLOR = 0xAA000000;
    private static final int BORDER_COLOR = 0xFF3A0066;
    private static final int BAR_COLOR = 0xFF7B2FBE;
    private static final int BAR_FULL_COLOR = 0xFF9B4FDE;
    private static final int TEXT_COLOR = 0xFFE0C0FF;

    public static void render(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        int current = ClientCursedEnergyData.getCurrentEnergy();
        int max = ClientCursedEnergyData.getMaxEnergy();

        if (max <= 0) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;

        String label = "Cursed Energy";
        String valueText = current + " / " + max;

        int x = BAR_OFFSET_X;
        int y = BAR_OFFSET_Y;

        int labelWidth = textRenderer.getWidth(label);
        int valueWidth = textRenderer.getWidth(valueText);
        int totalWidth = Math.max(BAR_WIDTH, Math.max(labelWidth, valueWidth)) + 8;

        // Background panel
        drawContext.fill(x, y, x + totalWidth, y + 30, BACKGROUND_COLOR);

        // Label text
        drawContext.drawText(textRenderer, label, x + 4, y + 2, TEXT_COLOR, true);

        // Bar background
        int barX = x + 4;
        int barY = y + 12;
        drawContext.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, BORDER_COLOR);
        drawContext.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFF1A0033);

        // Filled portion of the bar
        float ratio = (float) current / max;
        int filledWidth = (int) (BAR_WIDTH * ratio);
        int barColor = ratio >= 1.0f ? BAR_FULL_COLOR : BAR_COLOR;
        if (filledWidth > 0) {
            drawContext.fill(barX, barY, barX + filledWidth, barY + BAR_HEIGHT, barColor);
        }

        // Value text
        drawContext.drawText(textRenderer, valueText, x + 4, y + 22, TEXT_COLOR, true);
    }
}
