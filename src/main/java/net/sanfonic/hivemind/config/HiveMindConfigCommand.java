package net.sanfonic.hivemind.config;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import net.sanfonic.hivemind.config.ModConfig;

public class HiveMindConfigCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
            commandDispatcher.register(
                    CommandManager.literal("hivemind_config")
                            .requires(source -> source.hasPermissionLevel(2))
                            .then(CommandManager.literal("reload")
                                    .executes(HiveMindConfigCommand::reloadConfig))
                            .then(CommandManager.literal("save")
                                    .executes(HiveMindConfigCommand::saveConfig))
                            .then(CommandManager.literal("show")
                                    .executes(HiveMindConfigCommand::showConfig))
                            .then(CommandManager.literal("debug")
                                    .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                            .executes(HiveMindConfigCommand::setDebugLogging)))
                            .then(CommandManager.literal("drone_debug")
                                    .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                            .executes(HiveMindConfigCommand::setDroneLinkingDebug)))
                            .then(CommandManager.literal("command_debug")
                                    .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                            .executes(HiveMindConfigCommand::setCommandDebug)))
                            .then(CommandManager.literal("cooldown")
                                    .then(CommandManager.argument("seconds", IntegerArgumentType.integer(1, 60))
                                            .executes(HiveMindConfigCommand::setLogCooldown)))
                            .then(CommandManager.literal("drone_health")
                                    .then(CommandManager.argument("health", DoubleArgumentType.doubleArg(1.0, 100.0))
                                            .executes(HiveMindConfigCommand::setDroneHealth)))
                            .then(CommandManager.literal("drone_speed")
                                    .then(CommandManager.argument("speed", DoubleArgumentType.doubleArg(0.1, 2.0))
                                            .executes(HiveMindConfigCommand::setDroneSpeed)))
            );
        }));
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        ModConfig.getInstance().load();
        context.getSource().sendFeedback(() -> Text.literal("§aHiveMind config reloaded!"), false);
        return 1;
    }

    private static int saveConfig(CommandContext<ServerCommandSource> context) {
        ModConfig.getInstance().save();
        context.getSource().sendFeedback(() -> Text.literal("§HiveMind config saved!"), false);
        return 1;
    }

    private static int showConfig(CommandContext<ServerCommandSource> context) {
        ModConfig config = ModConfig.getInstance();

        context.getSource().sendFeedback(() -> Text.literal("§6=== HiveMind Config ==="), false);
        context.getSource().sendFeedback(() -> Text.literal("§7Debug Logging: §" + (config.enableDebugLogging ? "a" : "c") + config.enableDebugLogging), false);
        context.getSource().sendFeedback(() -> Text.literal("§7Drone Link Debug: §" + (config.enableDroneLinkingDebug ? "a" : "c") + config.enableDroneLinkingDebug), false);
        context.getSource().sendFeedback(() -> Text.literal("§7Command Debug: §" + (config.enableCommandDebug ? "a" : "c") + config.enableCommandDebug), false);
        context.getSource().sendFeedback(() -> Text.literal("§7Log Cooldown: §b" + config.logCooldownSeconds + "s"), false);
        context.getSource().sendFeedback(() -> Text.literal("§7Drone Health: §b" + config.droneHealth), false);
        context.getSource().sendFeedback(() -> Text.literal("§7Drone Speed: §b" + config.droneSpeed), false);
        context.getSource().sendFeedback(() -> Text.literal("§7Drone Follow Range: §b" + config.droneFollowRange), false);

        return 1;
    }

    private static int setDebugLogging(CommandContext<ServerCommandSource> context) {
        boolean enabled = BoolArgumentType.getBool(context, "enabled");
        ModConfig.getInstance().enableDebugLogging = enabled;
        ModConfig.getInstance().save();

        context.getSource().sendFeedback(() -> Text.literal("§aDebug logging " + (enabled ? "enabled" : "disabled")), false);
        return 1;
    }

    private static int setDroneLinkingDebug(CommandContext<ServerCommandSource> context) {
        boolean enabled = BoolArgumentType.getBool(context, "enabled");
        ModConfig.getInstance().enableDroneLinkingDebug = enabled;
        ModConfig.getInstance().save();

        context.getSource().sendFeedback(() -> Text.literal("§aDrone linking debug " + (enabled ? "enabled" : "disabled")), false);
        return 1;
    }

    private static int setCommandDebug(CommandContext<ServerCommandSource> context) {
        boolean enabled = BoolArgumentType.getBool(context, "enabled");
        ModConfig.getInstance().enableCommandDebug = enabled;
        ModConfig.getInstance().save();

        context.getSource().sendFeedback(() -> Text.literal("§aCommand debug " + (enabled ? "enabled" : "disabled")), false);
        return 1;
    }

    private static int setLogCooldown(CommandContext<ServerCommandSource> context) {
        int seconds = IntegerArgumentType.getInteger(context, "seconds");
        ModConfig.getInstance().logCooldownSeconds = seconds;
        ModConfig.getInstance().save();

        context.getSource().sendFeedback(() -> Text.literal("§aLog cooldown set to " + seconds + " seconds"), false);
        return 1;
    }

    private static int setDroneHealth(CommandContext<ServerCommandSource> context) {
        double health = DoubleArgumentType.getDouble(context, "health");
        ModConfig.getInstance().droneHealth = health;
        ModConfig.getInstance().save();

        context.getSource().sendFeedback(() -> Text.literal("§aDrone health set to " + health + " (requires restart for existing drones)"), false);
        return 1;
    }

    private static int setDroneSpeed(CommandContext<ServerCommandSource> context) {
        double speed = DoubleArgumentType.getDouble(context, "speed");
        ModConfig.getInstance().droneSpeed = speed;
        ModConfig.getInstance().save();

        context.getSource().sendFeedback(() -> Text.literal("§aDrone speed set to " + speed + " (requires restart for existing drones)"), false);
        return 1;
    }
}
