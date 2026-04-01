package net.mythic.jjkmod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.mythic.jjkmod.client.combat.CombatModeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Consumes hotbar key presses (1-9) at the start of input handling
 * so vanilla never processes them while combat mode is active.
 */
@Mixin(MinecraftClient.class)
public class HotbarKeyMixin {

    @Shadow @Final public GameOptions options;

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void jjk$consumeHotbarKeysInCombat(CallbackInfo ci) {
        if (CombatModeManager.isActive()) {
            for (int i = 0; i < 9; i++) {
                while (this.options.hotbarKeys[i].wasPressed()) {
                    // Consume and discard — vanilla won't see these presses
                }
            }
        }
    }
}
