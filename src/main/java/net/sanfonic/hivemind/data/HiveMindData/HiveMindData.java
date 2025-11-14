package net.sanfonic.hivemind.data.HiveMindData;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import java.util.List;
import java.util.UUID;

/**
 * Manages player-specific HiveMind data using NBT persistence
 * Works alongside HiveMindDataManager for drone-player relationships
 */
public class HiveMindData {
    // NBT Keys for player data
    private static final String NBT_KEY_ACCESS = "HasHiveMindAccess";
    private static final String NBT_KEY_HIVE_ID = "HiveId";
    private static final String NBT_KEY_JOIN_TIME = "HiveMindJoinTime";

    // Custom NBT root key for our mod data
    private static final String NBT_ROOT = "hivemind";

    /**
     * Get or create HiveMind NBT compound for a player
     * In Fabric, we need to manually manage the NBT Infrastructure
     */
    private static NbtCompound getOrCreateNbt(PlayerEntity player) {
        // Get the main NBT compound (this works in Fabric)
        NbtCompound playerNbt = player.writeNbt(new NbtCompound());

        // Get or create our mod's sub-compound
        if (!playerNbt.contains(NBT_ROOT)) {
            playerNbt.put(NBT_ROOT, new NbtCompound());
        }

        return playerNbt.getCompound(NBT_ROOT);
    }

    /**
     * Save the NBT data back to the player
     */
    private static void saveNbt(PlayerEntity player, NbtCompound modNbt) {
        NbtCompound playerNbt = player.writeNbt(new NbtCompound());
        playerNbt.put(NBT_ROOT, modNbt);
        player.readNbt(playerNbt);
    }

    // ===== ACCESS MANAGEMENT =====

    /**
     * Check if player has HiveMind access
     * @param player The player to check
     * @return true if player has joined the HiveMind
     */
    public static boolean hasAccess(PlayerEntity player) {
        NbtCompound nbt = getOrCreateNbt(player);
        return nbt.getBoolean(NBT_KEY_ACCESS);
    }

    /**
     * Grant or revoke HiveMind access for a player
     * @param player The player
     * @param value true to grant access, false to revoke
     */
    public static void setHasAccess(PlayerEntity player, boolean value) {
        NbtCompound nbt = getOrCreateNbt(player);
        nbt.putBoolean(NBT_KEY_ACCESS, value);

        // Record join time when granting access
        if (value && !nbt.contains(NBT_KEY_JOIN_TIME)) {
            nbt.putLong(NBT_KEY_JOIN_TIME, System.currentTimeMillis());
        }

        // Auto-assign a Hive ID if player doesn't have one
        if (value && !nbt.contains(NBT_KEY_HIVE_ID)) {
            setHiveId(player, player.getUuid()); // Use player UUID as default Hive ID
        }
    }

    // ===== HIVE ID MANAGEMENT =====

    /**
     * Get the player's Hive ID
     * @param player The player
     * @return UUID of the player's hive, or null if not set
     */
    public static UUID getHiveId(PlayerEntity player) {
        NbtCompound nbt = getOrCreateNbt(player);
        if (nbt.contains(NBT_KEY_HIVE_ID)) {
            return nbt.getUuid(NBT_KEY_HIVE_ID);
        }
        return null;
    }

    /**
     * Set the player's Hive ID
     * @param player The player
     * @param hiveId UUID of the hive to assign
     */
    public static void setHiveId(PlayerEntity player, UUID hiveId) {
        NbtCompound nbt = getOrCreateNbt(player);
        nbt.putUuid(NBT_KEY_HIVE_ID, hiveId);
        saveNbt(player, nbt);
    }

    /**
     * Check if player belongs to a specific hive
     * @param player The player
     * @param hiveId The hive UUID to check
     * @return true if player belongs to this hive
     */
    public static boolean isInHive(PlayerEntity player, UUID hiveId) {
        UUID playerHiveId = getHiveId(player);
        return playerHiveId != null && playerHiveId.equals(hiveId);
    }

    // ===== STATISTICS =====

    /**
     * Get the timestamp when player joined the HiveMind
     * @param player The player
     * @return Timestamp in milliseconds, or 0 if never joined
     */
    public static long getJoinTime(PlayerEntity player) {
        NbtCompound nbt = getOrCreateNbt(player);
        return nbt.getLong(NBT_KEY_JOIN_TIME);
    }

    /**
     * Calculate how long player has been in the HiveMind (in milliseconds)
     * @param player The player
     * @return Duration in milliseconds, or 0 if never joined
     */
    public static long getMembershipDuration(PlayerEntity player) {
        long joinTime = getJoinTime(player);
        if (joinTime == 0) return 0;
        return System.currentTimeMillis() - joinTime;
    }

    // ===== DRONE MANAGEMENT (Integration with HiveMindDataManager) =====

    /**
     * Get all drones owned by this player
     * Requires server-side player entity
     * @param player Server player entity
     * @return List of drone UUIDs
     */
    public static List<UUID> getPlayerDrones(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return List.of();

        HiveMindDataManager dataManager = HiveMindDataManager.getInstance(server);
        return dataManager.getOwnerDrones(player.getUuid());
    }

    /**
     * Get the count of drones owned by this player
     * @param player Server player entity
     * @return Number of drones owned
     */
    public static int getDroneCount(ServerPlayerEntity player) {
        return getPlayerDrones(player).size();
    }

    /**
     * Check if player has reached their drone limit
     * @param player Server player entity
     * @param maxDrones Maximum allowed drones
     * @return true if player has reached the limit
     */
    public static boolean hasReachedDroneLimit(ServerPlayerEntity player, int maxDrones) {
        return getDroneCount(player) >= maxDrones;
    }

    // ===== UTILITY METHODS =====

    /**
     * Initialize a new HiveMind member with default values
     * @param player The player to initialize
     */
    public static void initializeNewMember(PlayerEntity player) {
        setHasAccess(player, true);
        // Join time and Hive ID are set automatically by setHasAccess
    }

    /**
     * Remove all HiveMind data from a player
     * Note: Does not remove drone ownership - use HiveMindDataManager for that
     * @param player The player to clear
     */
    public static void clearPlayerData(PlayerEntity player) {
        NbtCompound playerNbt = player.writeNbt(new NbtCompound());
        playerNbt.remove(NBT_ROOT);
        player.readNbt(playerNbt);
    }

    /**
     * Get a summary of player's HiveMind status
     * Useful for debugging or GUI display
     * @param player The player
     * @return Formatted string with player's HiveMind info
     */
    public static String getPlayerStatus(PlayerEntity player) {
        if (!hasAccess(player)) {
            return "Not a HiveMind member";
        }

        UUID hiveId = getHiveId(player);
        long duration = getMembershipDuration(player);
        long durationSeconds = duration / 1000;

        String droneInfo = "";
        if (player instanceof ServerPlayerEntity serverPlayer) {
            int droneCount = getDroneCount(serverPlayer);
            droneInfo = " | Drones: " + droneCount;
        }

        return String.format("HiveMind Member | Hive: %s | Member for: %ds%s",
                hiveId.toString().substring(0, 8),
                durationSeconds,
                droneInfo
        );
    }
}