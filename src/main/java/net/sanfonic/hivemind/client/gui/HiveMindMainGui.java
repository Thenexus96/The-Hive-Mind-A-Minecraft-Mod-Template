package net.sanfonic.hivemind.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.entity.ai.brain.task.FindInteractionTargetTask;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HiveMindMainGui extends Screen {
    private final Screen parent;

    public HiveMindMainGui(Screen parent) {
        super(Text.translatable("gui.hivemind.main.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(1);

        // Title
        adder.add(new TextWidget(Text.translatable("gui.hivemind.main.title")
                .formatted(Formatting.BOLD, Formatting.GOLD), this.textRenderer));

        // Main content area - customize this for your mod's functionality
        adder.add(new TextWidget(Text.translatable("gui.hivemind.main.welcome"), this.textRenderer));

        // Settings button
        adder.add(ButtonWidget.builder(Text.translatable("gui.hivemind.main.settings"),
                        button -> this.client.setScreen(new HiveMindConfigScreen(this)))
                                .dimensions(0, 0, 200,20)
                                .tooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.translatable("gui.hivemind.main.settings")))
                                .build());

        // Close button
        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
                .dimensions(0, 0, 200, 20)
                .build());

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height / 6, this.width, this.height, 0.5F, 0.0F);
        gridWidget.forEachChild(this::addDrawableChild);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.parent != null) {
            this.client.setScreen(this.parent);
        } else {
            super.close();
        }
    }
}


