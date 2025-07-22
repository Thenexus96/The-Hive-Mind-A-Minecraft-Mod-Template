package net.sanfonic.hivemind.entity.custom.role;

import net.sanfonic.hivemind.entity.DroneEntity;
import net.sanfonic.hivemind.entity.custom.role.DroneRole;
import net.sanfonic.hivemind.entity.custom.role.DroneRoleBehavior;


public class IdleRole implements DroneRoleBehavior {

    @Override
    public DroneRole getRole() {
        return DroneRole.IDLE;
    }

    @Override
    public void onRoleAssigned(DroneEntity drone) {
        // Idle role might not need any specific goals
        // Or you could add basic goals like wandering, looking around, etc.
        // Example: drone.addGoal(7, new WanderAroundGoal(drone, 1.0), false);
    }

    @Override
    public void onRoleRemoved(DroneEntity drone) {
        // Clean up any idle-specific goals if you added any
        // Since idle might not have specific goals, this could be empty
    }

    @Override
    public void tick(DroneEntity drone) {
        // Any idle-specific logic that runs every tick
        // This could be empty for idle role, or include basic behaviors
    }

    @Override
    public int getGoalPriority() {
        return 10; // Lower priorities than active roles
    }

    @Override
    public void applyGoals(DroneEntity drone) {
        // Apply idle-specific goals here
        // This might be where you add basic goals like wandering
        // Example: drone.addGoal(8, new WanderAroundGoal(drone, 1.0), false);
    }
}
