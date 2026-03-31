package net.mythic.jjkmod.character;

public enum JJKGrade {
    GRADE_4("grade_4", "Grade 4", 1, 500, 20),
    SEMI_GRADE_3("semi_grade_3", "Semi-Grade 3", 2, 1000, 25),
    GRADE_3("grade_3", "Grade 3", 3, 1500, 28),
    SEMI_GRADE_2("semi_grade_2", "Semi-Grade 2", 4, 2000, 30),
    GRADE_2("grade_2", "Grade 2", 5, 3000, 35),
    SEMI_GRADE_1("semi_grade_1", "Semi-Grade 1", 6, 5000, 40),
    GRADE_1("grade_1", "Grade 1", 7, 7500, 50),
    SPECIAL_GRADE("special_grade", "Special Grade", 8, 10000, 60);

    private final String id;
    private final String displayName;
    private final int level;
    private final int maxCE;
    private final int maxHP;

    JJKGrade(String id, String displayName, int level, int maxCE, int maxHP) {
        this.id = id;
        this.displayName = displayName;
        this.level = level;
        this.maxCE = maxCE;
        this.maxHP = maxHP;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getLevel() { return level; }
    public int getMaxCE() { return maxCE; }
    public int getMaxHP() { return maxHP; }

    /**
     * Returns the next higher grade, or {@code null} if already Special Grade.
     */
    public JJKGrade getNext() {
        JJKGrade[] grades = values();
        int nextIndex = this.ordinal() + 1;
        if (nextIndex >= grades.length) {
            return null;
        }
        return grades[nextIndex];
    }

    public static JJKGrade fromId(String id) {
        for (JJKGrade grade : values()) {
            if (grade.id.equals(id)) return grade;
        }
        return null;
    }
}
