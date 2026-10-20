package com.github.dynastxu.mcita.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

public class SpellProjectileRenderState extends EntityRenderState {
    public Identifier texture = Identifier.fromNamespaceAndPath("mcita", "textures/entity/buckshot");
    public boolean additive;
    public float scale = 1f;
}
