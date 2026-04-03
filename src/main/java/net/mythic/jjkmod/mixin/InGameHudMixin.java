package net.mythic.jjkmod.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.mythic.jjkmod.client.combat.CombatModeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the vanilla hotbar when combat mode is active and shifts
 * status bars (hearts, hunger, armor) upward so they don't overlap
 * the combat HUD.
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {

    /**
     * Pixels to shift status bars upward when combat mode is active.
     * The combat bar body is 32 px tall + decorations above it, so we
     * nudge the status bars up to keep them clear of the bar.
     */
    @Unique
    private static final int JJKMOD_STATUS_BAR_OFFSET = 20;

    // ── Hide vanilla hotbar in combat mode ─────────────────────────────
    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void jjkmod$hideVanillaHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (CombatModeManager.isActive()) {
            ci.cancel();
        }
    }

    // ── Shift status bars up before rendering ──────────────────────────
    @Inject(method = "renderStatusBars", at = @At("HEAD"))
    private void jjkmod$shiftStatusBarsUp(DrawContext context, CallbackInfo ci) {
        if (CombatModeManager.isActive()) {
            context.getMatrices().push();
            context.getMatrices().translate(0, -JJKMOD_STATUS_BAR_OFFSET, 0);
        }
    }

    // ── Restore position after rendering ───────────────────────────────
    @Inject(method = "renderStatusBars", at = @At("RETURN"))
    private void jjkmod$restoreStatusBars(DrawContext context, CallbackInfo ci) {
        if (CombatModeManager.isActive()) {
            context.getMatrices().pop();
        }
    }
}
