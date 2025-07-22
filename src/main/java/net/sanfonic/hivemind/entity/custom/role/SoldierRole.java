package net.sanfonic.hivemind.entity.custom.role;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.sanfonic.hivemind.entity.DroneEntity;
import net.sanfonic.hivemind.entity.custom.role.DroneRole;
import net.sanfonic.hivemind.entity.custom.role.DroneRoleBehavior;

public class SoldierRole implements DroneRoleBehavior {
    // Constants for better code readability
    private static final boolean REGULAR_GOAL = false;
    private static final boolean TARGET_GOAL = true;

    private MeleeAttackGoal meleeAttackGoal;
    private RevengeGoal revengeGoal;

    @Override
    public DroneRole getRole() {
        return DroneRole.SOLDIER;
    }

    @Override
    public void onRoleAssigned(DroneEntity drone) {
        // Add combat goals for soldier role
        meleeAttackGoal = new MeleeAttackGoal(drone, 1.2, false);
        drone.addGoal(getGoalPriority(), meleeAttackGoal, REGULAR_GOAL);

        // Add target selection goals
        revengeGoal = new RevengeGoal(drone);
        drone.addGoal(1, revengeGoal, TARGET_GOAL);
    }

    @Override
    public void onRoleRemoved(DroneEntity drone) {
        // Remove soldier-specific goals when role changes
        if (meleeAttackGoal != null) {
            drone.removeGoal(meleeAttackGoal, REGULAR_GOAL);
            meleeAttackGoal = null;
        }
    }

    @Override
    public void tick(DroneEntity drone) {
        // Soldier-specific logic that runs every tick
        // This could include things like:
        // - Checking for nearby threats
        // - Adjusting combat behavior based on health
        // - Coordinating with other soldier drones
    }

    @Override
    public int getGoalPriority() {
        return 2; // High priority - combat is important
    }

    @Override
    public void applyGoals(DroneEntity drone) {
    }

    // Add any other required methods from DroneRoleBehavior interface
}
