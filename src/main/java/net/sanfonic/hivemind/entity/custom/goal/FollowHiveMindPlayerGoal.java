package net.sanfonic.hivemind.entity.custom.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

import net.sanfonic.hivemind.entity.DroneEntity;

public class FollowHiveMindPlayerGoal extends Goal{
    private final DroneEntity drone;
    private PlayerEntity hiveMindPlayer;

    public FollowHiveMindPlayerGoal(DroneEntity drone) {
        this.drone = drone;
    }

    @Override
    public boolean canStart() {
        hiveMindPlayer = drone.getHiveMindOwner(); // You need to define this
        return hiveMindPlayer != null && drone.distanceTo(hiveMindPlayer) > 5.0f;
    }

    @Override
    public void start() {
        drone.getNavigation().startMovingTo(hiveMindPlayer, 1.0);
    }
}

