package com.github.dynastxu.mcita.core.action;

import com.github.dynastxu.mcita.core.action.op.AddRecoilOp;
import com.github.dynastxu.mcita.core.action.op.AddReloadOp;
import com.github.dynastxu.mcita.core.action.op.CustomOp;
import com.github.dynastxu.mcita.core.action.op.DrawActionsOp;
import com.github.dynastxu.mcita.core.action.op.PatchShotOp;
import com.github.dynastxu.mcita.core.action.op.SpawnProjectileOp;
import com.mojang.serialization.MapCodec;

import java.util.Map;

/** Registry of the {@code "type"} discriminator used by {@link ActionOp#CODEC}. */
public final class ActionOps {
    private static final Map<String, MapCodec<? extends ActionOp>> CODECS = Map.of(
            "spawn_projectile", SpawnProjectileOp.MAP_CODEC,
            "draw_actions", DrawActionsOp.MAP_CODEC,
            "patch_shot", PatchShotOp.MAP_CODEC,
            "add_reload", AddReloadOp.MAP_CODEC,
            "add_recoil", AddRecoilOp.MAP_CODEC,
            "custom", CustomOp.MAP_CODEC);

    private ActionOps() {
    }

    public static MapCodec<? extends ActionOp> codec(String type) {
        return CODECS.get(type);
    }
}
