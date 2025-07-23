package net.sanfonic.hivemind.entity.custom.role;

import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.sanfonic.hivemind.entity.DroneEntity;
import net.sanfonic.hivemind.entity.custom.role.DroneRole;
import net.sanfonic.hivemind.entity.custom.role.DroneRoleBehavior;

public class ScoutRole implements DroneRoleBehavior {
    // Constants for better code readability
    private static final boolean REGULAR_GOAL = false;
    private static final boolean TARGET_GOAL = true;

    private LookAtEntityGoal lookAtPlayerGoal;

    @Override
    public DroneRole getRole() {
        return DroneRole.SCOUT;
    }

    @Override
    public void onRoleAssigned(DroneEntity drone) {
        // Add Scout-Specific goals
        lookAtPlayerGoal = new LookAtEntityGoal(drone, PlayerEntity.class, 16.0f);
        drone.addGoal(getGoalPriority() + 1, lookAtPlayerGoal, REGULAR_GOAL);
        // Add other scout goals like exploration, detection, etc.
    }

    @Override
    public void onRoleRemoved(DroneEntity drone) {
        // Remove Scout-Specific goals when role changes
        if (lookAtPlayerGoal != null) {
            drone.removeGoal(lookAtPlayerGoal, REGULAR_GOAL);
            lookAtPlayerGoal = null;
        }
    }

    @Override
    public void tick(DroneEntity drone) {
        // Scout-specific logic that runs every tick
        // For example: check for new areas, reporting discoveries, etc.
        // Could also include things like:
        // - Scanning for enemies or items
        // - Reporting back to hive mind
        // - Path finding and exploration
    }

    @Override
    public int getGoalPriority() {
        return 3; // Higher priority than idle wandering
    }

    @Override
    public void applyGoals(DroneEntity drone) {
        // Scouts move faster and wander more extensively. Exploring far and wide using the public addGoal method
        drone.addGoal(getGoalPriority(), new WanderAroundFarGoal(drone, 1.2, 0.001f), REGULAR_GOAL);
        drone.addGoal(getGoalPriority(), new LookAtEntityGoal(drone, PlayerEntity.class, 16.0f), REGULAR_GOAL);
        // You could add more scout-specific goals here:
        // - Explore unexplored area's
        // - Report back to owner periodically
        // - Avoid combat when possible
        onRoleAssigned(drone);
    }
}
