package net.mythic.jjkmod.client.animation;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;

/**
 * Duck interface injected into {@link net.minecraft.client.network.AbstractClientPlayerEntity}
 * via {@link net.mythic.jjkmod.mixin.ClientPlayerAnimMixin}.
 *
 * Provides access to the JJK mod's animation layer so abilities like
 * domain expansion can trigger custom player animations.
 */
public interface IJJKAnimatedPlayer {

    /**
     * Returns the mod's animation container for this player.
     * Use {@link ModifierLayer#setAnimation} to play an animation.
     */
    ModifierLayer<IAnimation> jjkmod_getAnimationLayer();
}
