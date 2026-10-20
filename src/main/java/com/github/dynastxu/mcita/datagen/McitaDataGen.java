package com.github.dynastxu.mcita.datagen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.HashMap;
import java.util.Map;

public final class McitaDataGen {
    private McitaDataGen() {
    }

    public static void gatherData(GatherDataEvent.Client event) {
        event.createDatapackRegistryObjects(ModDatapackEntries.build());
        validateNbtMapKeys();
    }

    /**
     * Wand components are persisted and synced as NBT, which only accepts string map
     * keys. An int-keyed map fails at encode time with "Not a string" (this is what
     * crashed the wand on the first right click), so the deck map uses string keys.
     * This check makes the constraint executable.
     */
    private static void validateNbtMapKeys() {
        Codec<Map<Integer, String>> intKeys = Codec.unboundedMap(Codec.INT, Codec.STRING);
        if (intKeys.encodeStart(NbtOps.INSTANCE, Map.of(0, "x")).result().isPresent()) {
            throw new IllegalStateException("Expected NBT to reject integer map keys");
        }

        Codec<Map<Integer, String>> stringKeys = Codec.unboundedMap(Codec.STRING, Codec.STRING).xmap(
                raw -> {
                    Map<Integer, String> map = new HashMap<>();
                    raw.forEach((key, value) -> map.put(Integer.parseInt(key), value));
                    return map;
                },
                map -> {
                    Map<String, String> raw = new HashMap<>();
                    map.forEach((key, value) -> raw.put(Integer.toString(key), value));
                    return raw;
                });

        DynamicOps<Tag> ops = NbtOps.INSTANCE;
        Tag encoded = stringKeys.encodeStart(ops, Map.of(0, "x"))
                .getOrThrow(error -> new IllegalStateException("String-keyed map encode failed: " + error));
        Map<Integer, String> decoded = stringKeys.parse(ops, encoded)
                .getOrThrow(error -> new IllegalStateException("String-keyed map decode failed: " + error));
        if (!"x".equals(decoded.get(0))) {
            throw new IllegalStateException("String-keyed map round trip failed: " + decoded);
        }
    }
}
