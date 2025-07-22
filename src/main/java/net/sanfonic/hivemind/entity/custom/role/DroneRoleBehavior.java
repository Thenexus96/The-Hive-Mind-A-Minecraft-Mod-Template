package net.sanfonic.hivemind.entity.custom.role;

import net.sanfonic.hivemind.entity.DroneEntity;

public interface DroneRoleBehavior {
    /**
     * Apply role specific goals to the drone
     * This is called when the role is first assigned or changed
     */
    void applyGoals(DroneEntity drone);

    /**
     * Called every tick for role -specifically behavior
     * Use sparingly - only implement if the role needs per-tick logic
     */
    default void tick(DroneEntity drone) {
        // Default: no per-tick behavior
    }

    void onRoleAssigned(DroneEntity drone);

    // Called when the role is removed or changed
    // Use for cleanup if needed
    default void onRoleRemoved(DroneEntity drone) {
        // Default: no cleanup needed
    }

    // Get the role this behavior represents

    DroneRole getRole();

    /**
     * Get priority for role-specific goals
     * Lower numbers = higher priority
     * Base drone goals use 0-1 and 8-10, so roles should use 2-7
     */
    default int getGoalPriority() {
        return 5; // Middle priority by default
    }
}
