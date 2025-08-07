package net.sanfonic.hivemind.control;

import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.player.PlayerAbilities;

public class PlayerState {
    private final Vec3d position;
    private final float xRot;
    private final float yRot;
    private final PlayerAbilities abilities;

    public PlayerState(Vec3d position, float xRot, float yRot, PlayerAbilities abilities) {
        this.position = position;
        this.xRot = xRot;
        this.yRot = yRot;
        this.abilities = abilities;
    }

    public Vec3d getPosition() {
        return position;
    }

    public float getxRot() {
        return xRot;
    }

    public float getyRot() {
        return yRot;
    }

    public PlayerAbilities getAbilities() {
        return abilities;
    }
}
