package net.mythic.jjkmod.character;

public enum JJKCharacter {
    NONE("none", "None", "No character selected"),
    SUKUNA("sukuna", "Ryomen Sukuna", "The King of Curses"),
    GOJO("gojo", "Satoru Gojo", "The Strongest Sorcerer");

    private final String id;
    private final String displayName;
    private final String description;

    JJKCharacter(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static JJKCharacter fromId(String id) {
        for (JJKCharacter character : values()) {
            if (character.id.equals(id)) {
                return character;
            }
        }
        return NONE;
    }
}
