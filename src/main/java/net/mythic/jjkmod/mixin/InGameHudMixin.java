package net.mythic.jjkmod.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.mythic.jjkmod.client.combat.CombatModeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides vanilla HUD elements when combat mode is active.
 *
 * <ul>
 *   <li>{@code renderHotbar} — replaced by the custom combat bar</li>
 *   <li>{@code renderExperienceBar} — the green XP progress bar</li>
 *   <li>{@code renderExperienceLevel} — the level number text</li>
 * </ul>
 *
 * The combat HUD itself renders on top of the remaining vanilla
 * elements (hearts, hunger, armor) via {@code RenderLayer.getGuiOverlay()}.
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void jjkmod$hideVanillaHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (CombatModeManager.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void jjkmod$hideExperienceBar(DrawContext context, int x, CallbackInfo ci) {
        if (CombatModeManager.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void jjkmod$hideExperienceLevel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (CombatModeManager.isActive()) {
            ci.cancel();
        }
    }
}
