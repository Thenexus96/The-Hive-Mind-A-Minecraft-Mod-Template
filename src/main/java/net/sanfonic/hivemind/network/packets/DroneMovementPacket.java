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
        if (DroneControlManager.isPlayerControllingDrone(player)) {
            DroneEntity drone = DroneControlManager.getControlledDrone(player);
            if (drone != null && drone.isAlive()) {
                // Apply movement to drone
                Vec3d movement = calculateMovement();
                drone.setVelocity(movement);

                // Update drone rotation
                drone.setYaw(yaw);
                drone.setPitch(pitch);

                // Handle special actions
                if (jumping) {
                    drone.triggerPrimaryAbility();
                }
                if (crouching) {
                    drone.triggerSecondaryAbility();
                }
            }
        }
    }

    private Vec3d calculateMovement() {
        // Calculate movement vectors based on player's look direction
        Vec3d lookAngle = Vec3d.fromPolar(pitch, yaw);
        Vec3d rightVector = lookAngle.crossProduct(new Vec3d(0, 1, 0)).normalize();
        Vec3d upVector = new Vec3d(0, 1, 0);

        // Combine movement inputs
        Vec3d movement = lookAngle.multiply(forward)
                .add(rightVector.multiply(strafe))
                .add(upVector.multiply(up));

        // Scale by drone's fly speed (default speed if drone access not available)
        return movement.multiply(0.2f);
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
