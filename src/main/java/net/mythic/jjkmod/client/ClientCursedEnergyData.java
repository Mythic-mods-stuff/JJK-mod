package net.mythic.jjkmod.client;

/**
 * Client-side cache for cursed energy AND character data.
 */
public class ClientCursedEnergyData {
    private static int currentEnergy = 0;
    private static int maxEnergy = 0;
    private static String selectedCharacter = \"\";
    private static boolean hasSelectedCharacter = false;

    // Energy methods
    public static void set(int current, int max) {
        currentEnergy = current;
        maxEnergy = max;
    }

    public static int getCurrentEnergy() {
        return currentEnergy;
    }

    public static int getMaxEnergy() {
        return maxEnergy;
    }

    // Character methods
    public static void setCharacter(String character, boolean hasSelected) {
        selectedCharacter = character;
        hasSelectedCharacter = hasSelected;
    }

    public static String getSelectedCharacter() {
        return selectedCharacter;
    }

    public static boolean hasSelectedCharacter() {
        return hasSelectedCharacter;
    }
}
