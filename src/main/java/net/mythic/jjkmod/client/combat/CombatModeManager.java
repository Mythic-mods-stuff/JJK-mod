package net.mythic.jjkmod.client.combat;

/**
 * Client-side combat mode state. Toggled by pressing R.
 * When active, the Sukuna-themed ability hotbar replaces the vanilla one
 * and vanilla hotbar interaction is blocked.
 */
public class CombatModeManager {

    private static boolean active = false;
    private static int savedVanillaSlot = 0;

    public static void toggle() {
        active = !active;
    }

    public static boolean isActive() {
        return active;
    }

    /** Saves the vanilla hotbar slot before entering combat mode. */
    public static void setSavedSlot(int slot) {
        savedVanillaSlot = slot;
    }

    /** Returns the slot to lock the vanilla hotbar to in combat mode. */
    public static int getSavedSlot() {
        return savedVanillaSlot;
    }

    public static void reset() {
        active = false;
        savedVanillaSlot = 0;
    }
}
