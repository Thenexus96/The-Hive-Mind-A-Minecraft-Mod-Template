package net.sanfonic.hivemind.entity.custom.role;

public enum DroneRole {
    IDLE("idle", "Idle"),
    SCOUT("scout", "Scout"),
    WORKER("worker", "Worker"),
    SOLDIER("soldier", "Soldier"),
    GUARD("guard", "Guard");

    private final String id;
    private final String displayName;

    DroneRole(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Get role by ID for command parsing
    public static DroneRole fromId(String id) {
        for (DroneRole role : values()) {
            if (role.id.equalsIgnoreCase(id)) {
                return role;
            }
        }
        return IDLE; // Default fallback
    }

    @Override
    public String toString() {
        return displayName;
    }
}
