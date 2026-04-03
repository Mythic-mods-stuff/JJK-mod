package net.mythic.jjkmod.mixin;

import com.mojang.authlib.GameProfile;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.mythic.jjkmod.client.animation.IJJKAnimatedPlayer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Registers a {@link ModifierLayer} animation container on every client-side
 * player entity.  This layer is used by the JJK mod to play custom animations
 * (domain expansion, ability casts, etc.) via the PlayerAnimator library.
 *
 * Priority 1000 ensures JJK animations override lower-priority mod animations.
 */
@Mixin(AbstractClientPlayerEntity.class)
public class ClientPlayerAnimMixin implements IJJKAnimatedPlayer {

    @Unique
    private final ModifierLayer<IAnimation> jjkmod_animationContainer = new ModifierLayer<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void jjkmod$registerAnimLayer(ClientWorld world, GameProfile profile, CallbackInfo ci) {
        PlayerAnimationAccess.getPlayerAnimLayer((AbstractClientPlayerEntity) (Object) this)
                .addAnimLayer(1000, jjkmod_animationContainer);
    }

    @Override
    public ModifierLayer<IAnimation> jjkmod_getAnimationLayer() {
        return jjkmod_animationContainer;
    }
}
