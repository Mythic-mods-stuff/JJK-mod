package net.mythic.jjkmod.client.animation;

import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.mythic.jjkmod.client.ClientCharacterData;
import net.mythic.jjkmod.client.combat.CombatModeManager;

/**
 * Handles domain expansion activation for the current character.
 *
 * <p>When the player presses the domain expansion key (9) while in combat
 * mode, this handler plays the corresponding animation via PlayerAnimator.
 *
 * <p>Currently supports:
 * <ul>
 *   <li><b>Gojo</b> — "Unlimited Void" domain expansion pose</li>
 * </ul>
 *
 * Animations are loaded from {@code assets/jjk-mod/player_animation/} by
 * the PlayerAnimator resource loader.
 */
public class DomainExpansionHandler {

    private static final Identifier DOMAIN_ANIM_ID =
            Identifier.of("jjk-mod", "gojo_domain_expansion_opening");

    /**
     * Attempts to trigger the domain expansion animation.
     * Only works when combat mode is active and the player is Gojo.
     *
     * @return true if the animation was triggered
     */
    public static boolean trigger() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;

        // Only trigger in combat mode
        if (!CombatModeManager.isActive()) return false;

        // Only Gojo has domain expansion for now
        if (!ClientCharacterData.isGojo()) return false;

        // Get the player's animation layer
        var animContainer = ((IJJKAnimatedPlayer) client.player).jjkmod_getAnimationLayer();

        // Load the animation from resources
        KeyframeAnimation anim = PlayerAnimationRegistry.getAnimation(DOMAIN_ANIM_ID);
        if (anim == null) return false;

        // Play the domain expansion animation
        animContainer.setAnimation(new KeyframeAnimationPlayer(anim));

        return true;
    }
}
