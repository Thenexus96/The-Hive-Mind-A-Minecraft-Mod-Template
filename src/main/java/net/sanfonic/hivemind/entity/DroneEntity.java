package net.sanfonic.hivemind.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.SwimGoal;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
//import net.minecraft.world.LocalDifficulty;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import net.sanfonic.hivemind.entity.custom.goal.FollowHiveMindPlayerGoal;
import net.sanfonic.hivemind.config.ModConfig;

import java.util.Objects;
import java.util.UUID;

public class DroneEntity extends PathAwareEntity {
    private static final TrackedData<Boolean> SOME_FLAG =
            DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private UUID hiveMindOwnerUuid;

    // Add these fields to prevent console spam
    private UUID previousOwnerUuid = null;
    private long lastLogTime = 0;

    // Constructor
    public DroneEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        System.out.println("DroneEntity created");
    }

    //FIXED: Method name matches what's called in ModEntites
    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0); // Added for better AI
    }

    // Method to set the hivemind owner (called by the command)
    public void setHiveMindOwnerUuid(PlayerEntity player) {
        this.hiveMindOwnerUuid = player.getUuid();

        // Only log when the link status actually changes
        if (!Objects.equals(this.hiveMindOwnerUuid, player.getUuid())) {
            this.hiveMindOwnerUuid = player.getUuid();
            ModConfig.getInstance().droneLinkDebugLog("Drone linked to player: " + player.getName().getString());
        }
    }

    // Method to check if this drone has a hivemind owner
    public boolean hasHiveMindOwner() {
        return this.hiveMindOwnerUuid != null;
    }

    public PlayerEntity getHiveMindOwner() {
        if (this.hiveMindOwnerUuid == null || this.getWorld().isClient()) {
            return null;
        }

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            return serverWorld.getServer().getPlayerManager().getPlayer(this.hiveMindOwnerUuid);
        }

        return null;
    }

    // Method to get the actual player object (if they're online, Alternative method that takes a world parameter
        public PlayerEntity getHiveMindOwner (World world) {
            if (this.hiveMindOwnerUuid == null) return null;
            return world.getPlayerByUuid(this.hiveMindOwnerUuid);
        }

    @Override
    protected void initGoals() {
        // Improved: Better basic AI
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(3, new LookAroundGoal(this));
        //this.goalSelector.add(4, FollowHiveMindPlayerGoal(this)); // Custom Goal (optional)
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
            player.sendMessage(Text.of("You interacted with the Drone! Health: " + this.getHealth()), false);
        }
        return ActionResult.SUCCESS;
    }

        @Override
        protected void updatePostDeath() {
            super.updatePostDeath();
            // Add any special death behavior here
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.hiveMindOwnerUuid !=null) {
            nbt.putUuid("HiveMindOwner", this.hiveMindOwnerUuid);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.containsUuid("HiveMindOwner")) {
            this.hiveMindOwnerUuid = nbt.getUuid("HiveMindOwner");
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) return;

        // Fixed Only log periodically and when status changes, not every tick
        if (this.hiveMindOwnerUuid != null) {
            ModConfig config = ModConfig.getInstance();

               // Only log if drone linking debug is enabled AND (owner changed OR enough time has passed)
                if (config.isDroneLinkingDebugEnabled() &&
                        (!Objects.equals(this.previousOwnerUuid, this.hiveMindOwnerUuid) ||
                                (System.currentTimeMillis() - this.lastLogTime > config.getLogCooldownMillis()))) {

                    config.droneLinkDebugLog("Drone is linked to: " + this.hiveMindOwnerUuid);
                    this.previousOwnerUuid = this.hiveMindOwnerUuid;
                    this.lastLogTime = System.currentTimeMillis();
                }
            }
        }
        // Add your other tick logic here if needed
    }