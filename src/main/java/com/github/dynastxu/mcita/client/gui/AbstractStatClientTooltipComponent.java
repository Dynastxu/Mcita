package com.github.dynastxu.mcita.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;

/** Ported from NoitaCore; lays out label/value rows for wand and spell tooltips. */
public abstract class AbstractStatClientTooltipComponent implements ClientTooltipComponent {
    protected static final int LABEL_VALUE_GAP = 8;

    protected record StatLine(Component label, Component value) {
    }

    protected final List<StatLine> statLines;
    protected final int maxLabelWidth;
    protected final int maxValueWidth;

    protected AbstractStatClientTooltipComponent(@NonNull List<StatLine> statLines) {
        this.statLines = List.copyOf(statLines);
        Minecraft minecraft = Minecraft.getInstance();
        this.maxLabelWidth = statLines.stream()
                .mapToInt(line -> minecraft.font.width(line.label))
                .max()
                .orElse(0);
        this.maxValueWidth = statLines.stream()
                .mapToInt(line -> minecraft.font.width(line.value))
                .max()
                .orElse(0);
    }

    @Override
    public int getHeight(@NonNull Font font) {
        return statLines.size() * (font.lineHeight + 1);
    }

    @Override
    public int getWidth(@NonNull Font font) {
        return maxLabelWidth + LABEL_VALUE_GAP + maxValueWidth;
    }

    @Override
    public void extractText(@NonNull GuiGraphicsExtractor graphics, @NonNull Font font, int x, int y) {
        renderStatLines(graphics, font, x, y);
    }

    protected void renderStatLines(@NonNull GuiGraphicsExtractor graphics, @NonNull Font font, int x, int y) {
        int lineHeight = font.lineHeight + 1;
        int valueX = x + maxLabelWidth + LABEL_VALUE_GAP;
        for (StatLine line : statLines) {
            graphics.text(font, line.label, x, y, 0xFFFFFFFF);
            graphics.text(font, line.value, valueX, y, 0xFFFFFFFF);
            y += lineHeight;
        }
    }
}
