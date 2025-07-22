package net.sanfonic.hivemind.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
//import net.minecraft.world.LocalDifficulty;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import net.sanfonic.hivemind.entity.custom.goal.FollowHiveMindPlayerGoal;
import net.sanfonic.hivemind.config.ModConfig;
import net.sanfonic.hivemind.data.HiveMindData.HiveMindDataManager;
import net.sanfonic.hivemind.entity.custom.role.DroneRole;
import net.sanfonic.hivemind.entity.custom.role.DroneRoleBehavior;
import net.sanfonic.hivemind.entity.custom.role.RoleRegistry;

import java.util.Objects;
import java.util.UUID;

public class DroneEntity extends PathAwareEntity {
    // Tracked data for syncing linked status to client
    private static final TrackedData<Boolean> IS_LINKED =
            DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<String> OWNER_NAME =
            DataTracker.registerData(DroneEntity.class, TrackedDataHandlerRegistry.STRING);
    private DroneRole currentRole = DroneRole.IDLE;
    private DroneRoleBehavior roleBehavior = RoleRegistry.getBehavior(DroneRole.IDLE);

    private UUID hiveMindOwnerUuid;

    // Add these fields to prevent console spam
    private UUID previousOwnerUuid = null;
    private long lastLogTime = 0;

    // Visual effect timing
    private int particleTimer = 0;

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
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        UUID playerUuid = player.getUuid();
        if (playerUuid == null) {
            throw new IllegalArgumentException("Player UUID cannot be null");
        }

        // Check if the owner is actually changing before setting
        if (!Objects.equals(this.hiveMindOwnerUuid, player.getUuid())) {
            this.hiveMindOwnerUuid = player.getUuid();
            updateTrackedData(player);
            ModConfig.getInstance().droneLinkDebugLog("Drone linked to player: " + player.getName().getString());
        }
    }

    // Method to check if this drone has a hivemind owner
    public boolean hasHiveMindOwner() {
        return this.hiveMindOwnerUuid != null;
    }

    // Get the PlayerEntity object of the owner
    public PlayerEntity getHiveMindOwnerPlayer() {
        if (this.hiveMindOwnerUuid == null || this.getWorld().isClient()) {
            return null;
        }

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            return serverWorld.getServer().getPlayerManager().getPlayer(this.hiveMindOwnerUuid);
        }

        return null;
    }

    // Method to get the actual player object (if they're online, Alternative method that takes a world parameter
    public PlayerEntity getHiveMindOwnerPlayer(World world) {
        if (this.hiveMindOwnerUuid == null) return null;
        return world.getPlayerByUuid(this.hiveMindOwnerUuid);
    }

    /**
     * Restores the HiveMind Connection from persistent data
     * Called when the server loads and needs to restore drone-owner relationships
     */

    public void restoreHiveMindConnection() {
        if (this.getWorld().isClient) return; // Only run on server

        MinecraftServer server = this.getWorld().getServer();
        if (server == null) return;

        HiveMindDataManager dataManager = HiveMindDataManager.getInstance(server);
        if (dataManager == null) return;

        UUID ownerUUID = dataManager.getDroneOwner(this.getUuid());

        if (ownerUUID != null) {
            this.hiveMindOwnerUuid = ownerUUID;

            // Update the drone's data in the manager with current position/health
            String dimensionKey = this.getWorld().getRegistryKey().getValue().toString();
            dataManager.updateDroneData(
                    this.getUuid(),
                    ownerUUID,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    dimensionKey,
                    this.getHealth(),
                    this.getMaxHealth()
            );

            // Optional: Add any additional restoration logic here
            // For example, setting AI goals, behaviors, etc.
        }
    }

    /**
     * Sets the HiveMind owner and saves to persistent data
     * Calls this when initially linking a drone to an owner
     */
    public void setHiveMindOwner(UUID ownerUUID) {
        if (ownerUUID == null) {
            throw new IllegalArgumentException("Owner UUID cannot be null");
        }

        this.hiveMindOwnerUuid = ownerUUID;

        if (!this.getWorld().isClient) {
            MinecraftServer server = this.getWorld().getServer();
            if (server != null) {
                HiveMindDataManager dataManager = HiveMindDataManager.getInstance(server);
                if (dataManager != null) {
                    dataManager.linkDroneToOwner(this.getUuid(), ownerUUID);

                    // Also update the drone's current data
                    String dimensionKey = this.getWorld().getRegistryKey().getValue().toString();
                    dataManager.updateDroneData(
                            this.getUuid(),
                            ownerUUID,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            dimensionKey,
                            this.getHealth(),
                            this.getMaxHealth()
                    );
                }
            }

            // Update tracked data for visual effect
            PlayerEntity owner = this.getWorld().getPlayerByUuid(ownerUUID);
            updateTrackedData(owner);
        }
    }

    /**
     * Gets the current HiveMind owner UUID
     */
    public UUID getHiveMindOwnerUuid() {
        return this.hiveMindOwnerUuid;
    }

    // Remove HiveMind connection
    public void removeHiveMindConnection() {
        if (!this.getWorld().isClient) {
            MinecraftServer server = this.getWorld().getServer();
            if (server != null) {
                HiveMindDataManager dataManager = HiveMindDataManager.getInstance(server);
                if (dataManager != null) {
                    dataManager.unlinkDrone(this.getUuid());
                }
            }
        }

        this.hiveMindOwnerUuid = null;
        updateTrackedData(null); // Clear visual indicators
    }

    // Validates a UUID to ensure it's not null and properly formatted
    private boolean isValidUUID(UUID uuid) {
        return uuid != null && !uuid.toString().equals("00000000-0000-0000-0000-000000000000");
    }

    // Updates tracked data for client-side visual effects
    private void updateTrackedData(PlayerEntity owner) {
        if (owner != null) {
            this.dataTracker.set(IS_LINKED, true);
            this.dataTracker.set(OWNER_NAME, owner.getName().getString());
        } else {
            this.dataTracker.set(IS_LINKED, false);
            this.dataTracker.set(OWNER_NAME, "");
        }
    }

    // Client-side method to check if drone is linked (for visual effects)
    public boolean isLinkedForVisuals() {
        return this.dataTracker.get(IS_LINKED);
    }

    // Client-side method to get owner name for display
    public String getOwnerNameForDisplay() {
        return this.dataTracker.get(OWNER_NAME);
    }

    // Set the drone's role and apply the corresponding behavior
    public void setRole(DroneRole newRole) {
        if (newRole == null) {
            newRole = DroneRole.IDLE;
        }

        // Don't Change if already this role
        if (this.currentRole == newRole) {
            return;
        }

        // Clean up old role
        if (this.roleBehavior != null) {
            this.roleBehavior.onRoleRemoved(this);
        }

        // Set new role
        DroneRole oldRole = this.currentRole;
        this.currentRole = newRole;
        this.roleBehavior = RoleRegistry.getBehavior(newRole);

        // Log Change
        ModConfig.getInstance().droneLinkDebugLog(
                "Drone role changed from " + oldRole.getDisplayName() + " to " + newRole.getDisplayName()
        );
    }

    // Get the current role behavior
    public DroneRole getRole() {
        return this.currentRole;
    }

    /**
     * Apply the current role's behavior to this drone
     * This clears role specific goals and re-applies them
     */
    public void applyRoleBehavior() {
        // Clear all target selector (combat roles manage these)
        this.targetSelector.clear(goal -> {
        return true;
    });

        //Re-add base goals
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new FollowHiveMindPlayerGoal(this, 1.2D, 6.0F, 2.0F));
        this.goalSelector.add(8, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(10, new LookAroundGoal(this));

        // Clear all target selector (combat roles manage this
        this.targetSelector.clear(goal -> true);

        // Apply new role behavior
        if (this.roleBehavior != null) {
            this.roleBehavior.applyGoals(this);
        }
    }

    // Helper method to clear target goals
    private void clearTargetGoals() {
        this.targetSelector.clear(goal -> true);
    }

    public void addGoal(int priority, Goal goal, boolean isTargetGoal) {
        if (isTargetGoal) {
            this.targetSelector.add(priority, goal);
        } else {
            this.goalSelector.add(priority, goal);
        }
    }

    public void removeGoal(Goal goal, boolean isTargetGoal) {
        if (isTargetGoal) {
        this.targetSelector.remove(goal);
        } else {
            this.goalSelector.remove(goal);
        }
    }

    @Override
    protected void initGoals() {
        // Basic survival goals (Highest priority)
        this.goalSelector.add(0, new SwimGoal(this));

        // HiveMind Specific behavior (High priority)
        this.goalSelector.add(1, new FollowHiveMindPlayerGoal(this, 1.2D, 6.0F, 2.0F));

        // Priorities 2-7 are reserved for role-specific goals

        // Default wandering behavior (lower priority - will be overridden by roles)
        this.goalSelector.add(8, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(10, new LookAroundGoal(this));

        // Apply initial role behavior
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(IS_LINKED, false);
        this.dataTracker.startTracking(OWNER_NAME, "");
        // No additional data to track
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return super.damage(source, amount);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!this.getWorld().isClient) {
            String ownerInfo;
            if (hasHiveMindOwner()) {
                PlayerEntity owner = getHiveMindOwnerPlayer();
                if (owner != null) {
                    ownerInfo = "Owner: " + owner.getName().getString();
                } else {
                    ownerInfo = "Owner: " + this.hiveMindOwnerUuid + " (offline)";
                }
            } else {
                ownerInfo = "No Owner";
            }

            Text message = Text.literal("Drone - Health: " + (int) this.getHealth() + "/" + (int) this.getMaxHealth())
                    .formatted(Formatting.AQUA)
                    .append(Text.literal(" | Role: " + this.currentRole.getDisplayName()).formatted(Formatting.YELLOW))
                    .append(Text.literal(" | " + ownerInfo).formatted(
                            hasHiveMindOwner() ? Formatting.GREEN : Formatting.GRAY));
            player.sendMessage(message, false);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void updatePostDeath() {
        super.updatePostDeath();
        // Remove the drone from the data manager when it dies
        if (!this.getWorld().isClient && hasHiveMindOwner()) {
            removeHiveMindConnection();
        }
    }

    // Don't forget to save/load the owner UUID to/from NBT
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.hiveMindOwnerUuid != null) {
            nbt.putUuid("HiveMindOwner", this.hiveMindOwnerUuid);
        }
        // Save Role
        nbt.putString("DroneRole", this.currentRole.getId());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.containsUuid("HiveMindOwner")) {
            UUID loadedUuid = nbt.getUuid("HiveMindOwner");
            if (isValidUUID(loadedUuid)) {
                // Updated tracked data after loading
                if (!this.getWorld().isClient) {
                    PlayerEntity owner = this.getWorld().getPlayerByUuid(loadedUuid);
                    updateTrackedData(owner);
                }
            }
        }

        // Load Role
        if (nbt.contains("DroneRole")) {
            String roleId = nbt.getString("DroneRole");
            DroneRole loadedRole = DroneRole.fromId(roleId);
            setRole(loadedRole);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) return;

        // Handle visual effects on client side
        if (this.getWorld().isClient) {
            handleClientVisualEffects();
            return;
        }

        // Server-side logic
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

        // Role Specific tick behavior
        if (this.roleBehavior != null) {
            this.roleBehavior.tick(this);
        }

        // Add your other tick logic here if needed

    }

    // Handles client-side visual effects for linked drones
    private void handleClientVisualEffects() {
        if (!isLinkedForVisuals()) {
            return;
        }

        // Increment particle timer
        particleTimer++;

        // Spawn particles every 10 ticks (0.5 seconds)
        if (particleTimer % 10 == 0) {
            spawnLinkParticles();
        }

        // Show floating name tag every 40 ticks (2 seconds)
        if (particleTimer % 40 == 0) {
            showOwnerNameTag();
        }
    }

    // Spawns particle effects to indicate the drone is linked
    private void spawnLinkParticles() {
        World world = this.getWorld();
        if (world == null) return;

        // Create a small circle of particles around the drone
        for (int i = 0; i < 3; i++) {
            double angle = (System.currentTimeMillis() / 1000.0 + i + 2.0943951) % (Math.PI * 2); // 120 degrees apart
            double radius = 0.8;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            world.addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + offsetX,
                    this.getY() + this.getHeight() + 0.2,
                    this.getZ() + offsetZ,
                    0.0, 0.02, 0.0
            );
        }

        // Add a subtle glow effect
        world.addParticle(
                ParticleTypes.END_ROD,
                this.getX(),
                this.getY() + this.getHeight() + 0.5,
                this.getZ(),
                0.0, 0.0, 0.0
        );
    }

    // Shows a floating name tag above the drone
    private void showOwnerNameTag() {
        String ownerName = getOwnerNameForDisplay();
        if (ownerName.isEmpty()) return;

        World world = this.getWorld();
        if (world == null) return;

        // Create enchantment glint particles to simulate text
        for (int i = 0; i < 2; i++) {
            world.addParticle(
                    ParticleTypes.ENCHANT,
                    this.getX() + (world.random.nextDouble() - 0.5) * 0.5,
                    this.getY() + this.getHeight() + 1.0,
                    this.getZ() + (world.random.nextDouble() - 0.5) * 0.5,
                    0.0, 0.1, 0.0
            );
        }
    }

    // Returns the custom name for display, showing owner if linked
    @Override
    public Text getDisplayName() {
        if (hasHiveMindOwner() && !getOwnerNameForDisplay().isEmpty()) {
            return Text.literal("Drone")
                    .formatted(Formatting.AQUA)
                    .append(Text.literal(" [" + getOwnerNameForDisplay() + "]")
                            .formatted(Formatting.GREEN));
        }
        return Text.literal("Drone").formatted(Formatting.GRAY);
    }
}