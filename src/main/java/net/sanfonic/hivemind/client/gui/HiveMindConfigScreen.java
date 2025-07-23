package net.sanfonic.hivemind.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.sanfonic.hivemind.client.KeyBindings;

public class HiveMindConfigScreen extends Screen {
    private final Screen parent;
    private KeyBinding selectedKeyBinding;
    private boolean waitingForKey = false;

    public HiveMindConfigScreen(Screen parent) {
        super(Text.translatable("gui.hivemind.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(2);

        // Title
        adder.add(new TextWidget(Text.translatable("gui_hivemind.config.title")
                .formatted(Formatting.BOLD), this.textRenderer), 2);

        //Keybind Settings
        adder.add(new TextWidget(Text.translatable("gui.hivemind.config.keybinds")
                .formatted(Formatting.UNDERLINE), this.textRenderer), 2);

        // Open GUI Keybind
        adder.add(new TextWidget(Text.translatable("key.hivemind.open_gui"), this.textRenderer));
        adder.add(ButtonWidget.builder(getKeyText(KeyBindings.getOpenHiveMindGui()),
                button -> selectKeyBinding(KeyBindings.getOpenHiveMindGui(), button))
                .dimensions(0,0,150,20)
                .tooltip(net.minecraft.client.gui.tooltip.Tooltip.of(getKeyText(KeyBindings.getOpenHiveMindGui())))
                .build());

        // Quick Access Keybind
        adder.add(new TextWidget(Text.translatable("key.hivemind.quick_access"), this.textRenderer));
        adder.add(ButtonWidget.builder(getKeyText(KeyBindings.getQuickAccess()),
                        button -> selectKeyBinding(KeyBindings.getOpenHiveMindGui(), button))
                .dimensions(0,0,150,20)
                .tooltip(net.minecraft.client.gui.tooltip.Tooltip.of(getKeyText(KeyBindings.getOpenHiveMindGui())))
                .build());

        // Toggle Mode Keybind
        adder.add(new TextWidget(Text.translatable("key.hivemind.toggle_mode"), this.textRenderer));
        adder.add(ButtonWidget.builder(getKeyText(KeyBindings.getToggleMode()),
                button -> selectKeyBinding(KeyBindings.getToggleMode(), button))
                .dimensions(0, 0, 150, 20)
                .tooltip(net.minecraft.client.gui.tooltip.Tooltip.of(getKeyText(KeyBindings.getToggleMode())))
                .build());

        // Reset to defaults button
        adder.add(ButtonWidget.builder(Text.translatable("gui.hivemind.config.reset_defaults"),
                button -> resetToDefault())
                .dimensions(0, 0,150, 20)
                .tooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.translatable("gui.hivemind.config.reset_defaults")))
                .build());

        // Done button
        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
                        .dimensions(0, 0, 150, 20)
                                .tooltip(net.minecraft.client.gui.tooltip.Tooltip.of(ScreenTexts.DONE))
                                        .build(), 2);

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height / 6 - 12, this.width, this.height, 0.5F, 0.0F);
        gridWidget.forEachChild(this::addDrawableChild);
    }

    private Text getKeyText(KeyBinding keyBinding) {
        if (waitingForKey && selectedKeyBinding == keyBinding) {
            return Text.translatable("gui.hivemind.config.press_key").formatted(Formatting.YELLOW);
        }
        return keyBinding.getBoundKeyLocalizedText();
    }

    private void selectKeyBinding(KeyBinding keyBinding, ButtonWidget button) {
        if (waitingForKey) {
            waitingForKey = false;
            selectedKeyBinding = null;
            this.clearAndInit(); // Refresh the entire screen to update button text
            return;
        }

        selectedKeyBinding = keyBinding;
        waitingForKey = true;
        this.clearAndInit(); // Refresh the entire screen to update button text
    }

    private void resetToDefault() {
        KeyBindings.getOpenHiveMindGui().setBoundKey(KeyBindings.getOpenHiveMindGui().getDefaultKey());
        KeyBindings.getQuickAccess().setBoundKey(KeyBindings.getQuickAccess().getDefaultKey());
        KeyBindings.getToggleMode().setBoundKey(KeyBindings.getToggleMode().getDefaultKey());

        KeyBinding.updateKeysByCode();
        this.clearAndInit();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingForKey && selectedKeyBinding != null) {
            if (keyCode == 256) { // ESC key
                selectedKeyBinding.setBoundKey(selectedKeyBinding.getDefaultKey());
            } else {
                selectedKeyBinding.setBoundKey(net.minecraft.client.util.InputUtil.fromKeyCode(keyCode, scanCode));
            }

            KeyBinding.updateKeysByCode();
            waitingForKey = false;
            selectedKeyBinding = null;
            this.clearAndInit(); // Refresh the entire screen to update button text
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        if (waitingForKey && selectedKeyBinding != null) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.hivemindconfig.press_key_instruction"),
                    this.width / 2, this.height - 40, 0xFFFFFF);
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
