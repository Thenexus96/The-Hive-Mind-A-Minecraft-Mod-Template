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
import java.util.ArrayList;
import java.util.List;

public class HiveMindDataManager extends PersistentState{
    private static final String DATA_NAME = "hivemind_data";

    // Store drone-owner relationships
    private final Map<UUID, UUID> droneOwnerMap = new HashMap<>(); // droneUUID -> ownerUUID
    private final Map<UUID, List<UUID>> ownerDroneMap = new HashMap<>(); // ownerUUID -> List<droneUUID>

    // Store drone positions (optional, for tracking) I am adding it
    private final Map<UUID, DroneData> droneDataMap = new HashMap<>();

    public static class DroneData {
        public UUID droneUUID;
        public UUID ownerUUID;
        public double x, y, z;
        public String dimensionKey;
        public double health;
        public double maxHealth;

        public DroneData(UUID droneUUID, UUID ownerUUID, double x, double y, double z,
                         String dimensionKey, double health, double maxHealth) {
            this.droneUUID = droneUUID;
            this.ownerUUID = ownerUUID;
            this.x = x;
            this.y = y;
            this.z = z;
            this.dimensionKey = dimensionKey;
            this.health = health;
            this.maxHealth = maxHealth;
        }
    }

    public static HiveMindDataManager getInstance(MinecraftServer server) {
        ServerWorld overworld = server.getWorld(World.OVERWORLD);
        PersistentStateManager persistentStateManager = overworld.getPersistentStateManager();

        HiveMindDataManager manager = persistentStateManager.getOrCreate(
                HiveMindDataManager::createFromNbt,
                HiveMindDataManager::new,
                DATA_NAME
        );

        return manager;
    }

    public static HiveMindDataManager createFromNbt(NbtCompound nbt) {
        HiveMindDataManager manager = new HiveMindDataManager();

        // Load drone-owner relationships
        NbtList droneOwnerList = nbt.getList("DroneOwners", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < droneOwnerList.size(); i++) {
            NbtCompound compound = droneOwnerList.getCompound(i);
            UUID droneUUID = compound.getUuid("DroneUUID");
            UUID ownerUUID = compound.getUuid("OwnerUUID");

            manager.droneOwnerMap.put(droneUUID, ownerUUID);
            manager.ownerDroneMap.computeIfAbsent(ownerUUID, k -> new ArrayList<>()).add(droneUUID);
        }

        // Load drone data
        NbtList droneDataList = nbt.getList("DroneData", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < droneDataList.size(); i++) {
            NbtCompound compound = droneDataList.getCompound(i);
            UUID droneUUID = compound.getUuid("DroneUUID");
            UUID ownerUUID = compound.getUuid("OwnerUUID");
            double x = compound.getDouble("X");
            double y = compound.getDouble("Y");
            double z = compound.getDouble("Z");
            String dimensionKey = compound.getString("Dimension");
            double health = compound.getDouble("Health");
            double maxHealth = compound.getDouble("MaxHealth");

            DroneData droneData = new DroneData(droneUUID, ownerUUID, x, y, z, dimensionKey, health,maxHealth);
            manager.droneDataMap.put(droneUUID, droneData);
        }

        return manager;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        // Save drone-owner relationships
        NbtList droneOwnerList = new NbtList();
        for (Map.Entry<UUID, UUID> entry : droneOwnerMap.entrySet()) {
            NbtCompound compound = new NbtCompound();
            compound.putUuid("DroneUUID", entry.getKey());
            compound.putUuid("OwnerUUID", entry.getValue());
            droneOwnerList.add(compound);
        }
        nbt.put("DroneOwners", droneOwnerList);

        // Save drone data
        NbtList droneDataList = new NbtList();
        for (DroneData droneData : droneDataMap.values()) {
            NbtCompound compound = new NbtCompound();
            compound.putUuid("DroneUUID", droneData.droneUUID);
            compound.putUuid("OwnerUUID", droneData.ownerUUID);
            compound.putDouble("X", droneData.x);
            compound.putDouble("Y", droneData.y);
            compound.putDouble("Z", droneData.z);
            compound.putString("Dimension", droneData.dimensionKey);
            compound.putDouble("Health", droneData.health);
            compound.putDouble("MaxHealth", droneData.maxHealth);
            droneDataList.add(compound);
        }
        nbt.put("DroneData", droneDataList);

        return nbt;
    }

    // Public methods for managing drone-owner relationships
    public void linkDroneToOwner(UUID droneUUID, UUID ownerUUID) {
        droneOwnerMap.put(droneUUID, ownerUUID);
        ownerDroneMap.computeIfAbsent(ownerUUID, k -> new ArrayList<>()).add(droneUUID);
        markDirty(); // Important: Mark as dirty to trigger save
    }

    public void unlinkDrone(UUID droneUUID) {
        UUID ownerUUID = droneOwnerMap.remove(droneUUID);
        if (ownerUUID != null) {
            List<UUID> ownerDrones = ownerDroneMap.get(ownerUUID);
            if (ownerDrones != null) {
                ownerDrones.remove(droneUUID);
                if (ownerDrones.isEmpty()) {
                    ownerDroneMap.remove(ownerUUID);
                }
            }
        }
        droneDataMap.remove(droneUUID);
        markDirty();
    }

    public UUID getDroneOwner(UUID droneUUID) {
        return droneOwnerMap.get(droneUUID);
    }

    public List<UUID> getOwnerDrones(UUID ownerUUID) {
        return ownerDroneMap.getOrDefault(ownerUUID, new ArrayList<>());
    }

    public boolean isDroneLinked(UUID droneUUID) {
        return droneOwnerMap.containsKey(droneUUID);
    }

    public void updateDroneData(UUID droneUUID, UUID ownerUUID,  double x, double y, double z, String dimensionKey, double health, double maxHealth) {
        DroneData droneData = new DroneData(droneUUID, ownerUUID, x, y, z, dimensionKey, health, maxHealth);
        droneDataMap.put(droneUUID, droneData);
        markDirty();
    }

    public DroneData getDroneData(UUID droneUUID) {
        return droneDataMap.get(droneUUID);
    }

    public Map<UUID, DroneData> getAllDroneData() {
        return new HashMap<>(droneDataMap);
    }

    // Cleanup method to remove data for drones that no longer exist
    public void cleanupNonExistentDrones(List<UUID> existingDroneUUIDs) {
        droneOwnerMap.keySet().removeIf(droneUUID -> !existingDroneUUIDs.contains(droneUUID));
        droneDataMap.keySet().removeIf(droneUUID -> !existingDroneUUIDs.contains(droneUUID));

        // Clean up owner drone lists
        for (List<UUID> droneList : ownerDroneMap.values()) {
            droneList.removeIf(droneUUID -> !existingDroneUUIDs.contains(droneUUID));
        }
        ownerDroneMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        markDirty();
    }
}
