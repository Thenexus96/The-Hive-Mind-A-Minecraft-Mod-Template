package net.sanfonic.hivemind.network.packets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.sanfonic.hivemind.control.DroneControlManager;
import net.sanfonic.hivemind.entity.DroneEntity;

public class DroneMovementPacket {
    private final float forward;
    private final float strafe;
    private final float up;
    private final boolean jumping;
    private final boolean crouching;
    private final float yaw;
    private final float pitch;

    public DroneMovementPacket(float forward, float strafe, float up, boolean jumping,
                               boolean crouching, float yaw, float pitch) {
        this.forward = forward;
        this.strafe = strafe;
        this.up = up;
        this.jumping = jumping;
        this.crouching = crouching;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static DroneMovementPacket read(PacketByteBuf buf) {
        float forward = buf.readFloat();
        float strafe = buf.readFloat();
        float up = buf.readFloat();
        boolean jumping = buf.readBoolean();
        boolean crouching = buf.readBoolean();
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();

        return new DroneMovementPacket(forward, strafe, up, jumping, crouching, yaw, pitch);
    }

    public void write(PacketByteBuf buf) {
        buf.writeFloat(forward);
        buf.writeFloat(strafe);
        buf.writeFloat(up);
        buf.writeBoolean(jumping);
        buf.writeBoolean(crouching);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
    }

    public void handle(ServerPlayerEntity player) {
        if (!DroneControlManager.isPlayerControllingDrone(player)) {
            return;
        }

        DroneEntity drone = DroneControlManager.getControlledDrone(player);
        if (drone == null || !drone.isAlive()) {
            return;
        }

        // Apply rotation with proper synchronization
        drone.updateRotationFromInput(yaw, pitch);

        // Apply movement if there's any input
        if (forward != 0f || strafe != 0f || up != 0f) {
            Vec3d movement = calculateMovement(drone);
            drone.setVelocity(movement);
            drone.velocityDirty = true;
        } else {
            // Stop the drone if no input - but keep some momentum for smooth feel
            Vec3d currentVel = drone.getVelocity();
            drone.setVelocity(currentVel.multiply(0.8, 0.9, 0.8)); // Gradual stop
            drone.velocityDirty = true;
        }

        // Handle special actions
        if (jumping) {
            drone.triggerPrimaryAbility();
        }
        if (crouching) {
            drone.triggerSecondaryAbility();
        }
    }

            private Vec3d calculateMovement (DroneEntity drone) {
                // Get drone's current orientation in radians
                float yawRad = (float) Math.toRadians(drone.getYaw());
                float pitchRad = (float) Math.toRadians(drone.getPitch());

                // Calculate forward direction vector
                Vec3d forwardDir = new Vec3d(
                        -Math.sin(yawRad) * Math.cos(pitchRad),
                        -Math.sin(pitchRad),
                        Math.cos(yawRad) * Math.cos(pitchRad)
                );

                // Calculate right direction vector (perpendicular to forward, on horizontal plane)
                Vec3d rightDir = new Vec3d(
                        Math.cos(yawRad),
                        0,
                        Math.sin(yawRad)
                );

                // Calculate up direction vector (always world up)
                Vec3d upDir = new Vec3d(0, 1, 0);

                // Base movement speed
                float baseSpeed = drone.getFlySpeed() * 2.0f; // Increase speed for better control feel

                // Combine movement vectors
                Vec3d movement = forwardDir.multiply(forward * baseSpeed)
                        .add(rightDir.multiply(strafe * baseSpeed))
                        .add(upDir.multiply(up * baseSpeed));
                return movement;
            }

            // Getters for accessing packet data
            public float getForward() { return forward; }
            public float getStrafe() { return strafe; }
            public float getUp() { return up; }
            public boolean isJumping() { return jumping; }
            public boolean isCrouching() { return crouching; }
            public float getYaw() { return yaw; }
            public float getPitch() { return pitch; }
}