package net.sanfonic.hivemind.golems;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CombatGolem extends PathAwareEntity {

    public CombatGolem(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    // Basic AI Behavior tick

    @Override
    protected void initGoals() {
        // No AI goals yet - idle for MVR
    }

    @Override
    protected void initDataTracker() {
        // No additional data to track
    }

    @Override
    protected void updatePostDeath() {
        this.remove(RemovalReason.KILLED);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return super.damage(source, amount);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!this.getWorld().isClient) {
            player.sendMessage(Text.of("You interacted with the Combat Golem!"), false);
        }
        return ActionResult.SUCCESS;
    }
    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return EntityDimensions.fixed(0.75f, 1.75f); // Use default dimensions from type
    }
}