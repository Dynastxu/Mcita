package com.github.dynastxu.mcita.client.gui;

import com.github.dynastxu.mcita.core.action.Action;
import com.github.dynastxu.mcita.core.action.ShotStat;
import com.github.dynastxu.mcita.core.wand.ActionCard;
import com.github.dynastxu.mcita.core.wand.WandStats;
import com.github.dynastxu.mcita.item.ActionCardItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/** Ported from NoitaCore's WandClientTooltipComponent; stats now come from WandStats. */
public class WandClientTooltipComponent extends AbstractStatClientTooltipComponent {
    private static final int ICON_SIZE = 16;
    private static final int ICON_GAP = 2;
    private static final int MAX_PER_ROW = 8;
    private static final int GAP = 2;

    private final Component label;
    private final int labelWidth;
    private final List<ItemStack> alwaysCasts;
    private final int gridWidth;
    private final int gridHeight;

    public WandClientTooltipComponent(@NonNull WandTooltipComponent component) {
        super(buildStatLines(component.wandData().stats().value()));

        WandStats stats = component.wandData().stats().value();
        this.alwaysCasts = new ArrayList<>();
        for (Holder<Action> holder : stats.alwaysCast()) {
            this.alwaysCasts.add(ActionCardItem.stackFor(new ActionCard(holder, Action.INFINITE_USES)));
        }

        Minecraft minecraft = Minecraft.getInstance();
        this.label = Component.translatable("tooltip.mcita.always_casts");
        this.labelWidth = alwaysCasts.isEmpty() ? 0 : minecraft.font.width(label);

        int cols = Math.min(alwaysCasts.size(), MAX_PER_ROW);
        this.gridWidth = alwaysCasts.isEmpty() ? 0 : cols * ICON_SIZE + (cols - 1) * ICON_GAP;

        int rows = (alwaysCasts.size() + MAX_PER_ROW - 1) / MAX_PER_ROW;
        this.gridHeight = alwaysCasts.isEmpty() ? 0 : rows * ICON_SIZE + (rows - 1) * ICON_GAP;
    }

    private static @NonNull List<StatLine> buildStatLines(@NonNull WandStats stats) {
        List<StatLine> lines = new ArrayList<>();

        lines.add(new StatLine(
                Component.translatable("tooltip.mcita.shuffle"),
                yesNo(stats.shuffleWhenEmpty())));
        lines.add(new StatLine(
                Component.translatable("tooltip.mcita.spells_per_cast"),
                Component.literal(String.valueOf(stats.actionsPerRound()))));
        lines.add(new StatLine(
                Component.translatable("tooltip.mcita.cast_delay"),
                seconds(stats.shotDefaults().getOrDefault(ShotStat.FIRE_RATE_WAIT, 0f))));
        lines.add(new StatLine(
                Component.translatable("tooltip.mcita.recharge_time"),
                seconds(stats.reloadTicks())));
        lines.add(new StatLine(
                Component.translatable("tooltip.mcita.mana_max"),
                Component.literal(String.valueOf((int) stats.manaMax()))));
        lines.add(new StatLine(
                Component.translatable("tooltip.mcita.mana_charge_speed"),
                Component.literal(String.valueOf((int) stats.manaChargeSpeed()))));
        lines.add(new StatLine(
                Component.translatable("tooltip.mcita.capacity"),
                Component.literal(String.valueOf(stats.deckCapacity()))));
        lines.add(new StatLine(
                Component.translatable("tooltip.mcita.spread"),
                Component.literal(String.format("%.2f°",
                        stats.shotDefaults().getOrDefault(ShotStat.SPREAD_DEGREES, 0f)))));
        lines.add(new StatLine(
                Component.translatable("tooltip.mcita.speed"),
                Component.literal(String.format("%.2f×",
                        stats.shotDefaults().getOrDefault(ShotStat.SPEED_MULTIPLIER, 1f)))));

        return lines;
    }

    private static Component yesNo(boolean value) {
        return Component.translatable(value ? "gui.yes" : "gui.no");
    }

    private static Component seconds(float ticks) {
        return Component.literal(String.format("%.2fs", ticks / 20f));
    }

    @Override
    public int getHeight(@NonNull Font font) {
        int textHeight = super.getHeight(font);
        if (alwaysCasts.isEmpty()) {
            return textHeight;
        }
        return (font.lineHeight + 1) + gridHeight + GAP + textHeight;
    }

    @Override
    public int getWidth(@NonNull Font font) {
        int statsWidth = super.getWidth(font);
        return Math.max(Math.max(labelWidth, gridWidth), statsWidth);
    }

    @Override
    public void extractText(@NonNull GuiGraphicsExtractor graphics, @NonNull Font font, int x, int y) {
        if (!alwaysCasts.isEmpty()) {
            graphics.text(font, label, x, y, 0xFFFFFFFF);
            y += font.lineHeight + 1 + gridHeight + GAP;
        }
        renderStatLines(graphics, font, x, y);
    }

    @Override
    public void extractImage(@NonNull Font font, int x, int y, int width, int height,
                             @NonNull GuiGraphicsExtractor graphics) {
        if (alwaysCasts.isEmpty()) {
            return;
        }

        int gridY = y + font.lineHeight + 1;
        for (int i = 0; i < alwaysCasts.size(); i++) {
            int col = i % MAX_PER_ROW;
            int row = i / MAX_PER_ROW;
            int iconX = x + col * (ICON_SIZE + ICON_GAP);
            int iconY = gridY + row * (ICON_SIZE + ICON_GAP);
            graphics.fakeItem(alwaysCasts.get(i), iconX, iconY);
            graphics.itemDecorations(font, alwaysCasts.get(i), iconX, iconY);
        }
    }
}
