package net.sanfonic.hivemind.entity.custom.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

import net.sanfonic.hivemind.entity.DroneEntity;

import java.util.EnumSet;

public class FollowHiveMindPlayerGoal extends Goal {
    private final DroneEntity drone;
    private PlayerEntity hiveMindPlayer;
    private final double speed;
    private final float followDistance;
    private final float stopDistance;

    public FollowHiveMindPlayerGoal(DroneEntity drone, double speed, float followDistance, float stopDistance) {
        this.drone = drone;
        this.speed = speed;
        this.followDistance = followDistance;
        this.stopDistance = stopDistance;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        hiveMindPlayer = drone.getHiveMindOwnerPlayer(); // You need to define this
        return hiveMindPlayer != null && drone.distanceTo(hiveMindPlayer) > followDistance;
    }

    @Override
    public boolean shouldContinue() {
        return hiveMindPlayer != null && !drone.getNavigation().isIdle() &&
                drone.distanceTo(hiveMindPlayer) > stopDistance;
    }

    @Override
    public void start() {
        drone.getNavigation().startMovingTo(hiveMindPlayer, speed);
    }

    @Override
    public void stop() {
        drone.getNavigation().stop();
        hiveMindPlayer = null;
    }

    @Override
    public void tick() {
        if (hiveMindPlayer != null) {
            drone.getLookControl().lookAt(hiveMindPlayer, 10.0F, (float)drone.getMaxLookPitchChange());
            if (drone.distanceTo(hiveMindPlayer) > followDistance) {
                drone.getNavigation().startMovingTo(hiveMindPlayer, speed);
            }
        }
    }
}

