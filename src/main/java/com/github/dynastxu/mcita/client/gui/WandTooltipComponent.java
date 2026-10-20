package com.github.dynastxu.mcita.client.gui;

import com.github.dynastxu.mcita.core.wand.WandData;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record WandTooltipComponent(WandData wandData) implements TooltipComponent {
}
