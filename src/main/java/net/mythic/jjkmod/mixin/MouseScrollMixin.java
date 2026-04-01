package net.mythic.jjkmod.mixin;

import net.minecraft.client.Mouse;
import net.mythic.jjkmod.client.combat.CombatModeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents the scroll wheel from changing the vanilla hotbar slot
 * while combat mode is active.
 */
@Mixin(Mouse.class)
public class MouseScrollMixin {

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void jjk$blockScrollInCombat(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (CombatModeManager.isActive()) {
            ci.cancel();
        }
    }
}
