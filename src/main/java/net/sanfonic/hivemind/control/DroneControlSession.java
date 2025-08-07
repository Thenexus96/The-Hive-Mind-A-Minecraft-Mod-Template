package net.sanfonic.hivemind.control;

import net.minecraft.server.network.ServerPlayerEntity;
import net.sanfonic.hivemind.entity.DroneEntity;

public class DroneControlSession {
    private final ServerPlayerEntity player;
    private final DroneEntity drone;
    private final long startTime;

    public DroneControlSession(ServerPlayerEntity player, DroneEntity drone) {
        this.player = player;
        this.drone = drone;
        this.startTime = System.currentTimeMillis();
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public DroneEntity getDrone() {
        return drone;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDurationMs() {
        return System.currentTimeMillis() - startTime;
    }

    public boolean isValid() {
        return player != null && player.isAlive() &&
                drone != null && drone.isAlive();
    }
}
