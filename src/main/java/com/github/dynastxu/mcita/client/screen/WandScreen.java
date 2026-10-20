package com.github.dynastxu.mcita.client.screen;

import com.github.dynastxu.mcita.Mcita;
import com.github.dynastxu.mcita.menu.WandContainer;
import com.github.dynastxu.mcita.menu.WandMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;

/** Ported verbatim from NoitaCore's WandScreen, with mcita textures. */
public class WandScreen extends AbstractContainerScreen<WandMenu> {
    private static final int WIDTH = 230;
    private static final int HEIGHT = 166;

    private static final Identifier TEXTURE = Mcita.id("textures/gui/wand_bg.png");

    private Button leftButton;
    private Button rightButton;

    public WandScreen(WandMenu menu, Inventory inventory, Component title) {
        this(menu, inventory, title, WIDTH, HEIGHT);
    }

    public WandScreen(WandMenu menu, Inventory inventory, Component title, int imageWidth, int imageHeight) {
        super(menu, inventory, title, imageWidth, imageHeight);
    }

    @Override
    protected void init() {
        super.init();

        if (menu.getTotalPages() > 1) {
            Player player = this.minecraft.player;
            if (player != null) {
                leftButton = Button.builder(Component.literal("<"), _ -> this.menu.clickMenuButton(player, 0))
                        .bounds(this.leftPos, this.topPos + 32, 9, 18)
                        .build();
                this.addRenderableWidget(leftButton);

                rightButton = Button.builder(Component.literal(">"), _ -> this.menu.clickMenuButton(player, 1))
                        .bounds(this.leftPos + this.imageWidth - 9, this.topPos + 32, 9, 18)
                        .build();
                this.addRenderableWidget(rightButton);
            }
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (leftButton != null) {
            leftButton.active = menu.getCurrentPage() > 0;
        }
        if (rightButton != null) {
            rightButton.active = menu.getCurrentPage() < menu.getTotalPages() - 1;
        }
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.leftPos, this.topPos,
                0, 0,
                this.imageWidth, this.imageHeight,
                WIDTH, HEIGHT
        );

        menu.slots.forEach(slot -> {
            if (!slot.isActive()) {
                return;
            }
            if (slot.container instanceof WandContainer) {
                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        Mcita.id("slot_spell"),
                        this.leftPos + slot.x, this.topPos + slot.y,
                        16, 16);
            } else if (slot.container instanceof Inventory) {
                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        Identifier.withDefaultNamespace("container/slot"),
                        this.leftPos + slot.x - 1, this.topPos + slot.y - 1,
                        18, 18);
            }
        });
    }
}
