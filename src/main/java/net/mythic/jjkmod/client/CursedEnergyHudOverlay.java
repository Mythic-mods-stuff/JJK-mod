package net.mythic.jjkmod.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class CursedEnergyHudOverlay {

    private static final int BAR_WIDTH = 70;
    private static final int BAR_HEIGHT = 6;
    private static final int MARGIN = 8;

    // Blue color scheme with shading
    private static final int BAR_COLOR_DARK = 0xFF0A5A9E;       // Dark blue (bottom shade)
    private static final int BAR_COLOR_MID = 0xFF1E90FF;        // Dodger Blue (main)
    private static final int BAR_COLOR_LIGHT = 0xFF5AB0FF;      // Light blue (top highlight)
    private static final int BAR_COLOR_FULL = 0xFF00BFFF;       // Deep Sky Blue when full

    private static final int BAR_BACKGROUND = 0xFF1A1A1A;       // Dark background
    private static final int BORDER_COLOR = 0xFF0D0D0D;         // Near black border

    private static final int TEXT_COLOR = 0xFFFFFFFF;           // White text

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
        int screenWidth = client.getWindow().getScaledWidth();

        String valueText = current + " / " + max;
        int valueWidth = textRenderer.getWidth(valueText);

        // Position in top-right corner
        int barX = screenWidth - BAR_WIDTH - MARGIN;
        int barY = MARGIN;

        // Draw outer border (thin black outline)
        drawContext.fill(barX - 2, barY - 2, barX + BAR_WIDTH + 2, barY + BAR_HEIGHT + 2, BORDER_COLOR);

        // Draw bar background
        drawContext.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, BAR_BACKGROUND);

        // Calculate filled portion
        float ratio = (float) current / max;
        int filledWidth = (int) (BAR_WIDTH * ratio);

        if (filledWidth > 0) {
            // Draw shaded blue bar with gradient effect (3 layers)
            int baseColor = ratio >= 1.0f ? BAR_COLOR_FULL : BAR_COLOR_MID;
            int darkColor = ratio >= 1.0f ? 0xFF0099CC : BAR_COLOR_DARK;
            int lightColor = ratio >= 1.0f ? 0xFF66D9FF : BAR_COLOR_LIGHT;

            // Main bar fill
            drawContext.fill(barX, barY, barX + filledWidth, barY + BAR_HEIGHT, baseColor);

            // Top highlight (lighter, 1px)
            drawContext.fill(barX, barY, barX + filledWidth, barY + 1, lightColor);

            // Bottom shade (darker, 2px)
            drawContext.fill(barX, barY + BAR_HEIGHT - 2, barX + filledWidth, barY + BAR_HEIGHT, darkColor);

            // Left edge highlight for depth
            if (filledWidth > 1) {
                drawContext.fill(barX, barY, barX + 1, barY + BAR_HEIGHT, lightColor);
            }
        }

        // Draw value text centered below the bar
        int textX = barX + (BAR_WIDTH - valueWidth) / 2;
        int textY = barY + BAR_HEIGHT + 3;
        drawContext.drawText(textRenderer, valueText, textX, textY, TEXT_COLOR, true);
    }
}

