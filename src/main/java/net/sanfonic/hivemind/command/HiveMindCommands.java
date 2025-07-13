package net.sanfonic.hivemind.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.server.network.ServerPlayerEntity;

import net.sanfonic.hivemind.entity.DroneEntity;

public class HiveMindCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) ->
                commandDispatcher.register(
                        CommandManager.literal("hivemind_link")
                                .requires(source -> source.hasPermissionLevel(2)) // or 0 for all players
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    ServerPlayerEntity player = source.getPlayer();

                                    //Check if player is null (command executed from console)
                                    if (player == null) {
                                        source.sendFeedback(() -> Text.literal("This command can only be executed by a player!"), false);
                                        return 1;
                                    }

                                    System.out.println("[DEBUG] Player executing command: " + player.getName().getString());

                                    //Check what the player is looking at
                                    HitResult hitResult = player.raycast(10.0, 1.0f, false);
                                    if (hitResult instanceof EntityHitResult entityHit) {
                                        Entity target = entityHit.getEntity();
                                        System.out.println("Entity Class: " + target.getClass().getName());
                                        if (target instanceof DroneEntity drone) {
                                            System.out.println("[DEBUG] Found DroneEntity, linking...");
                                            drone.setHiveMindOwnerUuid(player);
                                            source.sendFeedback(() -> Text.literal("Linked Drone to HiveMind!"), false);
                                        } else {
                                            source.sendFeedback(() -> Text.literal("That entity is not a Drone! It's: " + target.getClass().getSimpleName()), false);
                                        }
                                    } else {
                                        source.sendFeedback(() -> Text.literal("No entity in sight! Your looking at: " + hitResult.getType()), false);
                                    }

                                    return 1;
                                })
                )));
    }
}
