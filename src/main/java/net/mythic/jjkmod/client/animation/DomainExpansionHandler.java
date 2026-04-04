package net.mythic.jjkmod.client.animation;

import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.mythic.jjkmod.client.ClientCharacterData;
import net.mythic.jjkmod.client.combat.CombatModeManager;
import net.mythic.jjkmod.networking.DomainExpansionC2SPayload;

/**
 * Handles domain expansion activation for the current character.
 *
 * <p>Called from {@link net.mythic.jjkmod.mixin.HotbarKeyMixin} when
 * the player presses hotbar key 9 while combat mode is active.
 *
 * <p>Currently supports:
 * <ul>
 *   <li><b>Gojo</b> — "Unlimited Void" domain expansion pose</li>
 * </ul>
 *
 * <p>Player animation (arms) is handled client-side by Player Animator.
 * The domain itself (black-concrete sphere, floor, player freeze) is
 * created server-side by {@code DomainExpansionManager}.
 */
public class DomainExpansionHandler {

    private static final Identifier DOMAIN_ANIM_ID =
            Identifier.of("jjk-mod", "gojo_domain_expansion_opening");

    /** Fade-in duration in ticks (5 ticks = 0.25 s). */
    private static final int FADE_IN_TICKS = 5;

    /**
     * Attempts to trigger the domain expansion animation + server-side domain.
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
        if (!ClientCharacterData.isGojo()) {
            client.player.sendMessage(
                    Text.literal("\u00A7c[JJK] Domain expansion requires Gojo (current: "
                            + ClientCharacterData.get().getDisplayName() + ")"),
                    true
            );
            return false;
        }

        // Get the player's animation layer
        var animContainer = ((IJJKAnimatedPlayer) client.player).jjkmod_getAnimationLayer();

        // Load the animation from resources
        KeyframeAnimation anim = PlayerAnimationRegistry.getAnimation(DOMAIN_ANIM_ID);
        if (anim == null) {
            client.player.sendMessage(
                    Text.literal("\u00A7c[JJK] Animation not found: " + DOMAIN_ANIM_ID
                            + " — make sure Player Animator is installed"),
                    true
            );
            return false;
        }

        // ── Player arm animation (Player Animator) ─────────────────
        KeyframeAnimationPlayer player = new KeyframeAnimationPlayer(anim);
        player.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
        player.setFirstPersonConfiguration(
                new FirstPersonConfiguration()
                        .setShowRightArm(true)
                        .setShowLeftArm(true)
                        .setShowRightItem(false)
                        .setShowLeftItem(false)
        );

        // Smooth fade-in from idle pose
        animContainer.replaceAnimationWithFade(
                AbstractFadeModifier.standardFadeIn(FADE_IN_TICKS, Ease.INOUTSINE),
                player
        );

        // ── Domain expansion (server-side block sphere) ────────────
        ClientPlayNetworking.send(new DomainExpansionC2SPayload());

        return true;
    }
}
