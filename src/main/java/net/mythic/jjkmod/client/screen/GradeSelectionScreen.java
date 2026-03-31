package net.mythic.jjkmod.client.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.mythic.jjkmod.character.JJKGrade;
import net.mythic.jjkmod.networking.GradeSelectedC2SPayload;

public class GradeSelectionScreen extends Screen {

    private static final int PANEL_WIDTH = 300;
    private static final int PANEL_HEIGHT = 290;

    // JJK themed colours
    private static final int BG_COLOR        = 0xE0101020;
    private static final int BORDER_OUTER    = 0xFF2D0A4E;
    private static final int BORDER_INNER    = 0xFF8B2FC9;
    private static final int TITLE_COLOR     = 0xFFFF4444;
    private static final int SUBTITLE_COLOR  = 0xFFBBBBBB;
    private static final int SEPARATOR_COLOR = 0xFF4A1A7A;

    // Colour indicators for each grade (weakest → strongest)
    private static final int[] GRADE_COLORS = {
            0xFF888888,  // Grade 4 — Gray
            0xFF6699CC,  // Semi-Grade 3 — Light blue
            0xFF55AA55,  // Grade 3 — Green
            0xFFCCCC33,  // Semi-Grade 2 — Yellow
            0xFFDD8833,  // Grade 2 — Orange
            0xFFCC5555,  // Semi-Grade 1 — Red
            0xFFDD3333,  // Grade 1 — Bright red
            0xFFAA44DD,  // Special Grade — Purple
    };

    private final String characterName;

    public GradeSelectionScreen(String characterName) {
        super(Text.literal("Select Grade"));
        this.characterName = characterName;
    }

    @Override
    protected void init() {
        super.init();

        int centerX   = this.width / 2;
        int centerY   = this.height / 2;
        int panelLeft = centerX - PANEL_WIDTH / 2;
        int panelTop  = centerY - PANEL_HEIGHT / 2;

        int buttonWidth = PANEL_WIDTH - 80;
        int buttonX = panelLeft + 40;
        int startY = panelTop + 58;
        int gap = 26;

        JJKGrade[] grades = JJKGrade.values();
        for (int i = 0; i < grades.length; i++) {
            final JJKGrade grade = grades[i];
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(grade.getDisplayName()),
                    button -> selectGrade(grade)
            ).dimensions(buttonX, startY + (i * gap), buttonWidth, 20).build());
        }
    }

    private void selectGrade(JJKGrade grade) {
        ClientPlayNetworking.send(new GradeSelectedC2SPayload(grade.getId()));
        this.close();
    }

    // Player must select a grade — ESC is disabled
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
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

        // Subtitle — shows which character the grade is for
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Choose grade for " + this.characterName),
                centerX, panelTop + 27, SUBTITLE_COLOR);

        // Separator
        context.fill(panelLeft + 20, panelTop + 44, panelRight - 20, panelTop + 45, SEPARATOR_COLOR);

        // Colour indicators next to each button
        int startY = panelTop + 58;
        int gap = 26;
        for (int i = 0; i < GRADE_COLORS.length; i++) {
            int y = startY + (i * gap) + 5;
            context.fill(panelLeft + 22, y, panelLeft + 34, y + 10, GRADE_COLORS[i]);
        }

        // Footer
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Your grade is bound to your character"),
                centerX, panelBottom - 18, 0xFF555555);

        // Render widgets (buttons)
        super.render(context, mouseX, mouseY, delta);
    }
}
