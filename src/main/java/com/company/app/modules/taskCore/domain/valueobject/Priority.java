package com.company.app.modules.taskCore.domain.valueobject;

public enum Priority {
    LOW(1, "Low"),
    MEDIUM(2, "Medium"),
    HIGH(3, "High"),
    URGENT(4, "Urgent");
    
    private final int level;
    private final String displayName;
    
    Priority(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isHigherThan(Priority other) {
        return this.level > other.level;
    }
}

