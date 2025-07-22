package net.sanfonic.hivemind.entity.custom.role;

import net.sanfonic.hivemind.entity.custom.role.*;
import java.util.HashMap;
import java.util.Map;

public class RoleRegistry {
    private static Map<DroneRole, DroneRoleBehavior> ROLE_BEHAVIORS = new HashMap<>();

    static {
        // Register all role implementations
        register(new IdleRole());
        register(new ScoutRole());
        register(new SoldierRole());
        // Add more roles here as you create them:
        // register(new WorkerRole());
        // register(new GuardRole());
    }

    private static void register(DroneRoleBehavior behavior) {
        ROLE_BEHAVIORS.put(behavior.getRole(), behavior);
    }

    // Get the behavior implementation for a specific role
    public static DroneRoleBehavior getBehavior(DroneRole role) {
        DroneRoleBehavior behavior = ROLE_BEHAVIORS.get(role);
        if (behavior == null) {
            // Fallback to idle if role not found
            behavior = ROLE_BEHAVIORS.get(DroneRole.IDLE);
        }
        return behavior;
    }

    // Check if a role is registered
    public static boolean hasRole(DroneRole role) {
        return ROLE_BEHAVIORS.containsKey(role);
    }

    // Get all registered roles
    public static DroneRole[] getAvailalbleRoles() {
        return ROLE_BEHAVIORS.keySet().toArray(new DroneRole[0]);
    }
}
