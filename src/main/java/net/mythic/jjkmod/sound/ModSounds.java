package net.mythic.jjkmod.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.mythic.jjkmod.JJKMod;

/**
 * Registers all custom sound events for the JJK mod.
 */
public class ModSounds {

    public static final Identifier DOMAIN_EXPANSION_THEME_ID =
            Identifier.of(JJKMod.MOD_ID, "domain_expansion_theme");

    /**
     * Gojo's domain expansion theme — plays when the domain activates.
     * Audio file: assets/jjk-mod/sounds/domain_expansion_theme.ogg
     */
    public static final SoundEvent DOMAIN_EXPANSION_THEME =
            SoundEvent.of(DOMAIN_EXPANSION_THEME_ID);

    /**
     * Register all mod sound events. Call from {@link JJKMod#onInitialize()}.
     */
    public static void register() {
        Registry.register(Registries.SOUND_EVENT, DOMAIN_EXPANSION_THEME_ID, DOMAIN_EXPANSION_THEME);
        JJKMod.LOGGER.info("Registered JJK mod sounds");
    }
}
