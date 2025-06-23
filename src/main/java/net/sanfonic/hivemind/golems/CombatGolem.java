package net.sanfonic.hivemind.golems;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
//import net.minecraft.world.LocalDifficulty;
//import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class CombatGolem extends PathAwareEntity {
    private static final TrackedData<Boolean> SOME_FLAG =
            DataTracker.registerData(CombatGolem.class, TrackedDataHandlerRegistry.BOOLEAN);

    public CombatGolem(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    //FIXED: Method name matches what's called in ModEntites
    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0); // Added for better AI
    }

    @Override
    protected void initGoals() {
        // Improved: Better basic AI
        this.goalSelector.add(1, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(3, new LookAroundGoal(this));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SOME_FLAG, false);
        // No additional data to track
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return super.damage(source, amount);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!this.getWorld().isClient) {
            player.sendMessage(Text.of("You interacted with the Combat Golem! Health: " + this.getHealth()), false);
        }
        return ActionResult.SUCCESS;
    }

        @Override
        protected void updatePostDeath() {
            super.updatePostDeath();
            // Add any special death behavior here
    }
}