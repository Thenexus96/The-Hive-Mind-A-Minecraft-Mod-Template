package net.sanfonic.hivemind.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    private static KeyBinding openHiveMindGui;
    private static KeyBinding quickAccess;
    private static KeyBinding toggleMode;

    public static void registerKeyBindings() {
        openHiveMindGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hivemind.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.hivemind.general"
        ));

        quickAccess = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hivemind.quick_access",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.hivemind.general"
        ));

        toggleMode = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hivemind.toggle_mode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.hivemind.general"
        ));
    }

    public static KeyBinding getOpenHiveMindGui() {
        return openHiveMindGui;
    }

    public static KeyBinding getQuickAccess() {
        return quickAccess;
    }

    public static KeyBinding getToggleMode() {
        return toggleMode;
    }
}
