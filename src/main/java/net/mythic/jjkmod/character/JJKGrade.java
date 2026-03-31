package net.mythic.jjkmod.character;

public enum JJKGrade {
    GRADE_4("grade_4", "Grade 4", 1),
    SEMI_GRADE_3("semi_grade_3", "Semi-Grade 3", 2),
    GRADE_3("grade_3", "Grade 3", 3),
    SEMI_GRADE_2("semi_grade_2", "Semi-Grade 2", 4),
    GRADE_2("grade_2", "Grade 2", 5),
    SEMI_GRADE_1("semi_grade_1", "Semi-Grade 1", 6),
    GRADE_1("grade_1", "Grade 1", 7),
    SPECIAL_GRADE("special_grade", "Special Grade", 8);

    private final String id;
    private final String displayName;
    private final int level;

    JJKGrade(String id, String displayName, int level) {
        this.id = id;
        this.displayName = displayName;
        this.level = level;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getLevel() { return level; }

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
