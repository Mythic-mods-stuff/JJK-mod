package net.mythic.jjkmod.client.combat;

/**
 * Client-side combat mode state. Toggled by pressing R.
 * When active, the character-specific ability hotbar is shown.
 */
public class CombatModeManager {

    private static boolean active = false;

    public static void toggle() {
        active = !active;
    }

    public static boolean isActive() {
        return active;
    }

    public static void reset() {
        active = false;
    }
}
