package com.github.dynastxu.mcita.client;

import com.github.dynastxu.mcita.core.projectile.ProjectileProfile;
import com.github.dynastxu.mcita.entity.SpellProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

/**
 * Ported from NoitaCore's {@code TextureEntityRenderer}: a camera-facing quad using
 * the texture declared by the projectile profile.
 */
public class SpellProjectileRenderer extends EntityRenderer<SpellProjectile, SpellProjectileRenderState> {
    private static final Identifier FALLBACK = Identifier.fromNamespaceAndPath("mcita", "textures/entity/buckshot");

    public SpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NonNull SpellProjectileRenderState createRenderState() {
        return new SpellProjectileRenderState();
    }

    @Override
    public void extractRenderState(@NonNull SpellProjectile entity, @NonNull SpellProjectileRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        ProjectileProfile profile = entity.getProfile();
        if (profile != null && profile.render().isPresent()) {
            ProjectileProfile.Render render = profile.render().get();
            state.texture = render.texture();
            state.additive = render.additive();
            state.scale = render.scale();
        } else {
            state.texture = FALLBACK;
            state.additive = false;
            state.scale = 1f;
        }
    }

    @Override
    public void submit(SpellProjectileRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        RenderType renderType = RenderTypes.entityCutout(state.texture);

        stack.pushPose();
        stack.scale(state.scale, state.scale, state.scale);
        stack.mulPose(camera.orientation);
        collector.submitCustomGeometry(stack, renderType, (pose, buffer) -> quad(state, pose, buffer));
        stack.popPose();
    }

    private static void quad(SpellProjectileRenderState state, PoseStack.Pose pose, VertexConsumer buffer) {
        vertex(buffer, pose, state.lightCoords, 0.0F, 0, 0, 1);
        vertex(buffer, pose, state.lightCoords, 1.0F, 0, 1, 1);
        vertex(buffer, pose, state.lightCoords, 1.0F, 1, 1, 0);
        vertex(buffer, pose, state.lightCoords, 0.0F, 1, 0, 0);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, int lightCoords,
                               float x, int y, int u, int v) {
        buffer.addVertex(pose, x - 0.5F, y - 0.25F, 0.0F)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
