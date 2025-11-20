package net.sanfonic.hivemind.data.HiveMindData;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages HiveCode assignment for drones
 * HiveCodes are short, readable identifiers like "D-001", "D-042"
 * Format: D-XXX where XXX is a zero-padded number
 */
public class HiveCodeManager extends PersistentState {
    private static final String DATA_NAME = "hivemind_hivecodes";
    private static final String HIVECODE_PREFIX = "D-";

    // Counter for next available drone number
    private final AtomicInteger nextDroneNumber = new AtomicInteger(1);

    // Maps: droneUUID <-> HiveCode
    private final Map<UUID, String> droneToCode = new HashMap<>();
    private final Map<String, UUID> codeToDrone = new HashMap<>();

    // Maps: HiveCode -> ownerUUID (for quick ownership lookup)
    private final Map<String, UUID> codeToOwner = new HashMap<>();

    /**
     * Get or create the HiveCodeManager instance
     */
    public static HiveCodeManager getInstance(MinecraftServer server) {
        ServerWorld overworld = server.getWorld(World.OVERWORLD);
        PersistentStateManager stateManager = overworld.getPersistentStateManager();

        return stateManager.getOrCreate(
                HiveCodeManager::createFromNbt,
                HiveCodeManager::new,
                DATA_NAME
        );
    }

    /**
     * Create manager from saved NBT data
     */
    public static HiveCodeManager createFromNbt(NbtCompound nbt) {
        HiveCodeManager manager = new HiveCodeManager();

        // Load next drone number
        if (nbt.contains("NextDroneNumber")) {
            manager.nextDroneNumber.set(nbt.getInt("NextDroneNumber"));
        }

        // Load drone code mappings
        NbtList mappingsList = nbt.getList("CodeMappings", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < mappingsList.size(); i++) {
            NbtCompound mapping = mappingsList.getCompound(i);
            UUID droneUUID = mapping.getUuid("DroneUUID");
            String hiveCode = mapping.getString("HiveCode");
            UUID ownerUUID = mapping.getUuid("OwnerUUID");

            manager.droneToCode.put(droneUUID, hiveCode);
            manager.codeToDrone.put(hiveCode, droneUUID);
            manager.codeToOwner.put(hiveCode, ownerUUID);
        }
        return manager;
    }

    /**
     * Format a drone number into a HiveCode
     *
     * @param number The drone number (1, 2, 3, etc.)
     * @return Formatted HiveCode (D-001, D-002, etc.)
     */
    public static String formatHiveCode(int number) {
        return HIVECODE_PREFIX + String.format("%03d", number);
    }

    /**
     * Parse a HiveCode to get the drone number
     *
     * @param hiveCode The HiveCode string
     * @return The drone number, or -1 if invalid
     */
    public static int parseHiveCodeNumber(String hiveCode) {
        if (hiveCode == null || !hiveCode.startsWith(HIVECODE_PREFIX)) {
            return -1;
        }

        try {
            String numberPart = hiveCode.substring(HIVECODE_PREFIX.length());
            return Integer.parseInt(numberPart);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        // Save next drone number
        nbt.putInt("NextDroneNumber", nextDroneNumber.get());
        // Save drone code mappings
        NbtList mappingsList = new NbtList();
        for (Map.Entry<UUID, String> entry : droneToCode.entrySet()) {
            NbtCompound mapping = new NbtCompound();
            UUID droneUUID = entry.getKey();
            String hiveCode = entry.getValue();

            mapping.putUuid("DroneUUID", droneUUID);
            mapping.putString("HiveCode", hiveCode);
            mapping.putUuid("OwnerUUID", codeToOwner.get(hiveCode));
            mappingsList.add(mapping);
        }
        nbt.put("CodeMappings", mappingsList);
        return nbt;
    }

    /**
     * Generate a new HiveCode for a drone
     *
     * @param droneUUID The Drone's UUID
     * @param ownerUUID The Owner's UUID
     * @return The generated HiveCode (e.g., "D-01")
     */
    public String generateHiveCode(UUID droneUUID, UUID ownerUUID) {
        // Check if drone already has a code
        if (droneToCode.containsKey(droneUUID)) {
            return droneToCode.get(droneUUID);
        }

        // Generate new code
        int droneNumber = nextDroneNumber.getAndIncrement();
        String hiveCode = HIVECODE_PREFIX + String.format("%03d", droneNumber);
        // Store Mappings
        droneToCode.put(droneUUID, hiveCode);
        codeToDrone.put(hiveCode, droneUUID);
        codeToOwner.put(hiveCode, ownerUUID);

        markDirty();
        return hiveCode;
    }

    /**
     * Get the HiveCode for a Drone
     *
     * @param droneUUID The drone's UUID
     * @return The HiveCode, or null if not assigned
     */
    public String getHiveCode(UUID droneUUID) {
        return droneToCode.get(droneUUID);
    }

    /**
     * Get the drone UUID from a HiveCode
     *
     * @param hiveCode The HiveCode (e.g., "D-001")
     * @return The drone's UUID or null if not assigned
     */
    public UUID getDroneFromCode(String hiveCode) {
        return codeToDrone.get(hiveCode);
    }

    /**
     * Get the owner of a dron by HiveCode
     *
     * @param hiveCode The HiveCode
     * @return The owner's UUID, or null if not assigned
     */
    public UUID getOwnerFromCode(String hiveCode) {
        return codeToOwner.get(hiveCode);
    }

    /**
     * Check if a HiveCode is already in use
     *
     * @param hiveCode The code to check
     * @return true if the code exists
     */
    public boolean isCodeInUse(String hiveCode) {
        return codeToDrone.containsKey(hiveCode);
    }

    /**
     * Remove a drone's HiveCode (called when drone dies or is removed)
     *
     * @param droneUUID The drone's UUID
     */
    public void removeHiveCode(UUID droneUUID) {
        String hiveCode = droneToCode.remove(droneUUID);
        if (hiveCode != null) {
            codeToDrone.remove(hiveCode);
            codeToOwner.remove(hiveCode);
            markDirty();
        }
    }

    /**
     * Get all HiveCodes owned by a player
     *
     * @param ownerUUID The owner's UUID
     * @return Map of HiveCode -> DroneUUID
     */
    public Map<String, UUID> getOwnerDroneCodes(UUID ownerUUID) {
        Map<String, UUID> result = new HashMap<>();
        for (Map.Entry<String, UUID> entry : codeToOwner.entrySet()) {
            if (entry.getValue().equals(ownerUUID)) {
                String code = entry.getKey();
                result.put(code, codeToDrone.get(code));
            }
        }
        return result;
    }

    /**
     * Cleanup codes for non-existent drones
     *
     * @param existingDroneUUIDs List of currently existing drone UUIDs
     */
    public void cleanupInvalidCodes(java.util.List<UUID> existingDroneUUIDs) {
        droneToCode.keySet().removeIf(droneUUID -> !existingDroneUUIDs.contains(droneUUID));

        // Rebuild reverse mappings
        codeToDrone.clear();
        codeToOwner.clear();
        for (Map.Entry<UUID, String> entry : droneToCode.entrySet()) {
            codeToDrone.put(entry.getValue(), entry.getKey());
            // Note: owner mapping will need to be rebuilt from HiveMindDataManager
        }

        markDirty();
    }
}
