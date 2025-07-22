package net.sanfonic.hivemind.client;

import net.minecraft.text.Text;
import net.sanfonic.hivemind.client.gui.HiveMindMainGui;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class ClientEventHandler {
    public static void registerEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client ->{
            while (KeyBindings.getOpenHiveMindGui().wasPressed()) {
                MinecraftClient.getInstance().setScreen(new HiveMindMainGui(null));
            }

            while (KeyBindings.getQuickAccess().wasPressed()) {
                // Implements quick access functionlity here
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("HiveMind Quick Access activated!"),false);
                }
            }

            while (KeyBindings.getToggleMode().wasPressed()) {
                // Implements mode toggle functionality here
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("HiveMind Mode Toggled!"), false);
                }
            }
        });
    }
}
