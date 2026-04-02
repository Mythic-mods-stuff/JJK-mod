package net.mythic.jjkmod.client.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.mythic.jjkmod.character.JJKCharacter;
import net.mythic.jjkmod.client.ClientCharacterData;
import net.mythic.jjkmod.networking.CharacterSelectedC2SPayload;

public class CharacterSelectionScreen extends Screen {

    private static final int PANEL_WIDTH = 300;
    private static final int PANEL_HEIGHT = 255;

    // JJK themed colours
    private static final int BG_COLOR        = 0xE0101020;   // Dark navy, semi-transparent
    private static final int BORDER_OUTER    = 0xFF2D0A4E;   // Dark purple
    private static final int BORDER_INNER    = 0xFF8B2FC9;   // Bright purple accent
    private static final int TITLE_COLOR     = 0xFFFF4444;   // Cursed-energy red
    private static final int SUBTITLE_COLOR  = 0xFFBBBBBB;   // Light gray
    private static final int SEPARATOR_COLOR = 0xFF4A1A7A;   // Purple
    private static final int CHAR_NAME_COLOR = 0xFFFFCC00;   // Gold
    private static final int CHAR_DESC_COLOR = 0xFF999999;   // Gray

    public CharacterSelectionScreen() {
        super(Text.literal("Choose Your Cursed Technique"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX   = this.width / 2;
        int centerY   = this.height / 2;
        int panelLeft = centerX - PANEL_WIDTH / 2;
        int panelTop  = centerY - PANEL_HEIGHT / 2;

        int buttonWidth = PANEL_WIDTH - 80;

        // Sukuna selection button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("\u2620 Ryomen Sukuna \u2620"),
                button -> selectCharacter(JJKCharacter.SUKUNA)
        ).dimensions(panelLeft + 40, panelTop + 100, buttonWidth, 20).build());

        // Gojo selection button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("\u2728 Satoru Gojo \u2728"),
                button -> selectCharacter(JJKCharacter.GOJO)
        ).dimensions(panelLeft + 40, panelTop + 190, buttonWidth, 20).build());
    }

    private void selectCharacter(JJKCharacter character) {
        // Store selection on the client for HUD rendering
        ClientCharacterData.set(character);
        ClientPlayNetworking.send(new CharacterSelectedC2SPayload(character.getId()));
        this.close();
    }

    // Player must select a character — ESC is disabled
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    // Pause the game in singleplayer while the menu is open
    @Override
    public boolean shouldPause() {
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Darken the background
        this.renderBackground(context, mouseX, mouseY, delta);

        int centerX     = this.width / 2;
        int centerY     = this.height / 2;
        int panelLeft   = centerX - PANEL_WIDTH / 2;
        int panelTop    = centerY - PANEL_HEIGHT / 2;
        int panelRight  = panelLeft + PANEL_WIDTH;
        int panelBottom = panelTop + PANEL_HEIGHT;

        // Panel background
        context.fill(panelLeft, panelTop, panelRight, panelBottom, BG_COLOR);

        // Outer border
        context.fill(panelLeft - 2, panelTop - 2, panelRight + 2, panelTop, BORDER_OUTER);
        context.fill(panelLeft - 2, panelBottom, panelRight + 2, panelBottom + 2, BORDER_OUTER);
        context.fill(panelLeft - 2, panelTop, panelLeft, panelBottom, BORDER_OUTER);
        context.fill(panelRight, panelTop, panelRight + 2, panelBottom, BORDER_OUTER);

        // Inner border accent
        context.fill(panelLeft, panelTop, panelRight, panelTop + 1, BORDER_INNER);
        context.fill(panelLeft, panelBottom - 1, panelRight, panelBottom, BORDER_INNER);
        context.fill(panelLeft, panelTop, panelLeft + 1, panelBottom, BORDER_INNER);
        context.fill(panelRight - 1, panelTop, panelRight, panelBottom, BORDER_INNER);

        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
                centerX, panelTop + 12, TITLE_COLOR);

        // Subtitle
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Select a Jujutsu sorcerer to begin"),
                centerX, panelTop + 27, SUBTITLE_COLOR);

        // Separator
        context.fill(panelLeft + 20, panelTop + 44, panelRight - 20, panelTop + 45, SEPARATOR_COLOR);

        // ── Sukuna section ──
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Ryomen Sukuna"),
                centerX, panelTop + 55, CHAR_NAME_COLOR);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("The King of Curses"),
                centerX, panelTop + 68, CHAR_DESC_COLOR);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Cleave \u2022 Dismantle \u2022 Malevolent Shrine"),
                centerX, panelTop + 81, CHAR_DESC_COLOR);

        // Separator between characters
        context.fill(panelLeft + 40, panelTop + 128, panelRight - 40, panelTop + 129, SEPARATOR_COLOR);

        // ── Gojo section ──
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Satoru Gojo"),
                centerX, panelTop + 140, CHAR_NAME_COLOR);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("The Strongest Sorcerer"),
                centerX, panelTop + 153, CHAR_DESC_COLOR);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Infinity \u2022 Blue \u2022 Red \u2022 Hollow Purple"),
                centerX, panelTop + 166, CHAR_DESC_COLOR);

        // Footer
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("More characters coming soon..."),
                centerX, panelTop + PANEL_HEIGHT - 18, 0xFF555555);

        // Render widgets (buttons)
        super.render(context, mouseX, mouseY, delta);
    }
}
