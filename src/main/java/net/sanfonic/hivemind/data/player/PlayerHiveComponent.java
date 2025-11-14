package net.sanfonic.hivemind.data.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side storage for player HiveMind data with FILE-BASED persistence
 * This is the ONLY way to reliably save player data in Fabric
 */
public class PlayerHiveComponent {
    // In-memory cache for active players
    private static final Map<UUID, PlayerHiveData> PLAYER_DATA = new HashMap<>();

    // NBT keys
    private static final String NBT_ACCESS = "hasAccess";
    private static final String NBT_HIVE_ID = "hiveId";
    private static final String NBT_JOIN_TIME = "joinTime";

    public static class PlayerHiveData {
        public boolean hasAccess = false;
        public UUID hiveId = null;
        public long joinTime = 0;

        public NbtCompound toNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean(NBT_ACCESS, hasAccess);
            if (hiveId != null) {
                nbt.putUuid(NBT_HIVE_ID, hiveId);
            }
            nbt.putLong(NBT_JOIN_TIME, joinTime);
            return nbt;
        }

        public static PlayerHiveData fromNbt(NbtCompound nbt) {
            PlayerHiveData data = new PlayerHiveData();
            data.hasAccess = nbt.getBoolean(NBT_ACCESS);
            if (nbt.contains(NBT_HIVE_ID)) {
                data.hiveId = nbt.getUuid(NBT_HIVE_ID);
            }
            data.joinTime = nbt.getLong(NBT_JOIN_TIME);
            return data;
        }
    }

    /**
     * Get the save file for a player
     */
    private static File getPlayerDataFile(ServerPlayerEntity player) {
        World world = player.getWorld();
        File worldDir = world.getServer().getRunDirectory();
        File savesDir = new File(worldDir, "saves");
        File currentWorldDir = new File(savesDir, world.getServer()
                .getSaveProperties().getLevelName());
        File hivemindDir = new File(worldDir, "hivemind");

        if (!hivemindDir.exists()) {
            hivemindDir.mkdirs();
        }

        return new File(hivemindDir, player.getUuidAsString() + ".dat");
    }

    /**
     * Load data from file
     */
    private static PlayerHiveData loadFromFile(ServerPlayerEntity player) {
        File dataFile = getPlayerDataFile(player);

        if (dataFile.exists()) {
            try {
                NbtCompound nbt = NbtIo.read(dataFile);
                System.out.println("[HiveMind] Loaded existing data from file for: " + player.getName().getString());
                return PlayerHiveData.fromNbt(nbt);
            } catch (IOException e) {
                System.err.println("[HiveMind] Failed to load data for " + player
                        .getName().getString() + ": " + e.getMessage());
            }
        }

        System.out.println("[HiveMind] No existing data file, creating new for: " + player.getName().getString());
        return new PlayerHiveData();
    }

    /**
     * Save data to file
     */
    private static void saveToFile(ServerPlayerEntity player, PlayerHiveData data) {
        File dataFile = getPlayerDataFile(player);

        try {
            NbtIo.write(data.toNbt(), dataFile);
            System.out.println("[HiveMind] Saved data to file for: " + player.getName().getString());
        } catch (IOException e) {
            System.err.println("[HiveMind] Failed to save data for " + player
                    .getName().getString() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get or create player data
     */
    public static PlayerHiveData getData(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return new PlayerHiveData(); // Client-side, return empty
        }

        UUID uuid = player.getUuid();

        // Check cache first
        if (!PLAYER_DATA.containsKey(uuid)) {
            // Load from file
            PlayerHiveData data = loadFromFile(serverPlayer);
            PLAYER_DATA.put(uuid, data);
        }

        return PLAYER_DATA.get(uuid);
    }

    /**
     * Check if player has HiveMind access
     */
    public static boolean hasAccess(PlayerEntity player) {
        boolean access = getData(player).hasAccess;
        System.out.println("[HiveMind] Checking access for " + player.getName().getString() + ": " + access);
        return access;
    }

    /**
     * Set HiveMind access for player
     */
    public static void setAccess(PlayerEntity player, boolean access) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        PlayerHiveData data = getData(player);
        data.hasAccess = access;

        if (access && data.hiveId == null) {
            data.hiveId = player.getUuid();
            data.joinTime = System.currentTimeMillis();
        }

        // Save immediately to file
        saveToFile(serverPlayer, data);

        System.out.println("[HiveMind] Set access for " + player.getName().getString() + " to: " + access);
    }

    /**
     * Get player's Hive ID
     */
    public static UUID getHiveId(PlayerEntity player) {
        return getData(player).hiveId;
    }

    /**
     * Get join time
     */
    public static long getJoinTime(PlayerEntity player) {
        return getData(player).joinTime;
    }

    /**
     * Calculate membership duration
     */
    public static long getMembershipDuration(PlayerEntity player) {
        long joinTime = getJoinTime(player);
        if (joinTime == 0) return 0;
        return System.currentTimeMillis() - joinTime;
    }

    /**
     * Initialize new member
     */
    public static void initializeNewMember(PlayerEntity player) {
        System.out.println("[HiveMind] Initializing new member: " + player.getName().getString());
        setAccess(player, true);
    }

    /**
     * Get player status string
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
            int droneCount = net.sanfonic.hivemind.data.HiveMindData.HiveMindData.getDroneCount(serverPlayer);
            droneInfo = " | Drones: " + droneCount;
        }

        return String.format("HiveMind Member | Hive: %s | Member for: %ds%s",
                hiveId.toString().substring(0, 8),
                durationSeconds,
                droneInfo
        );
    }

    /**
     * Called when player joins - load their data
     */
    public static void onPlayerJoin(ServerPlayerEntity player) {
        System.out.println("[HiveMind] Player joined: " + player.getName().getString());
        // Force reload from file
        PLAYER_DATA.remove(player.getUuid());
        getData(player); // This will trigger loadFromFile
    }

    /**
     * Called when player leaves - save and cleanup
     */
    public static void onPlayerLeave(ServerPlayerEntity player) {
        System.out.println("[HiveMind] Player leaving: " + player.getName().getString());
        PlayerHiveData data = PLAYER_DATA.get(player.getUuid());
        if (data != null) {
            saveToFile(player, data);
        }
        // Keep in cache for potential quick rejoin
    }
}