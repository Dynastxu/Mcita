package com.github.dynastxu.mcita.core.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * A spell card, the port of one entry of {@code data/scripts/gun/gun_actions.lua}.
 * The Lua {@code action = function() ... end} body is replaced by {@link #ops}.
 */
public record Action(
        ActionType type,
        int manaDrain,
        int maxUses,
        int price,
        List<SpawnEntry> spawnTable,
        Optional<Identifier> sprite,
        Optional<Identifier> spriteUnidentified,
        UsesLogic usesLogic,
        List<ActionOp> ops
) {
    public static final int INFINITE_USES = -1;

    public Action {
        spawnTable = List.copyOf(spawnTable);
        ops = List.copyOf(ops);
    }

    public static final Codec<Action> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ActionType.CODEC.fieldOf("type").forGetter(Action::type),
            Codec.INT.optionalFieldOf("mana", 10).forGetter(Action::manaDrain),
            Codec.INT.optionalFieldOf("max_uses", INFINITE_USES).forGetter(Action::maxUses),
            Codec.INT.optionalFieldOf("price", 0).forGetter(Action::price),
            SpawnEntry.CODEC.listOf().optionalFieldOf("spawn", List.<SpawnEntry>of()).forGetter(Action::spawnTable),
            Identifier.CODEC.optionalFieldOf("sprite").forGetter(Action::sprite),
            Identifier.CODEC.optionalFieldOf("sprite_unidentified").forGetter(Action::spriteUnidentified),
            UsesLogic.CODEC.optionalFieldOf("uses_logic", UsesLogic.DEFAULT).forGetter(Action::usesLogic),
            ActionOp.CODEC.listOf().fieldOf("ops").forGetter(Action::ops)
    ).apply(instance, Action::new));

    /**
     * Client item model id for the card icon, derived from the sprite path:
     * {@code mcita:textures/item/spell_light_bullet} -> {@code mcita:spell_light_bullet}.
     */
    public java.util.Optional<Identifier> cardModel() {
        return sprite.map(id -> {
            String path = id.getPath();
            int slash = path.lastIndexOf('/');
            return Identifier.fromNamespaceAndPath(id.getNamespace(),
                    slash >= 0 ? path.substring(slash + 1) : path);
        });
    }
}
