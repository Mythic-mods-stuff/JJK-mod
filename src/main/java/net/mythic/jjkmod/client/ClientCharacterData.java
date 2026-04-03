package net.mythic.jjkmod.client;

import net.mythic.jjkmod.character.JJKCharacter;

/**
 * Client-side storage for the locally selected character.
 * Updated when the player picks a character in the selection screen.
 */
public class ClientCharacterData {

    private static JJKCharacter selectedCharacter = JJKCharacter.NONE;

    public static void set(JJKCharacter character) {
        selectedCharacter = character;
    }

    public static JJKCharacter get() {
        return selectedCharacter;
    }

    public static boolean isGojo() {
        return selectedCharacter == JJKCharacter.GOJO;
    }

    public static boolean isSukuna() {
        return selectedCharacter == JJKCharacter.SUKUNA;
    }

    public static void reset() {
        selectedCharacter = JJKCharacter.NONE;
    }
}
