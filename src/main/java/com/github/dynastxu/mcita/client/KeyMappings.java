package com.github.dynastxu.mcita.client;

import com.github.dynastxu.mcita.Mcita;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

public final class KeyMappings {
    public static final Lazy<KeyMapping> OPEN_WAND_GUI = Lazy.of(() -> new KeyMapping(
            "key." + Mcita.MOD_ID + ".open_wand_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            KeyMapping.Category.MISC));

    private KeyMappings() {
    }
}
