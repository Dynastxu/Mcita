package com.github.dynastxu.mcita.datagen;

/**
 * The single place where Noita units become Minecraft units.
 * Nothing outside datagen should call these methods: runtime data is already
 * expressed in ticks / blocks / MC damage.
 *
 * <p>Noita runs at 60 fps, Minecraft at 20 tps, hence 3 frames per tick.
 * The pixel and damage scales are balance knobs and will be calibrated in M1.
 */
public final class NoitaValues {
    /** 60 Noita frames per second / 20 Minecraft ticks per second. */
    public static final float FRAMES_PER_TICK = 3f;
    /** One Noita pixel in blocks. Placeholder, needs in-game calibration. */
    public static final float PIXELS_PER_BLOCK = 16f / 1.8f;
    /** Noita damage number to Minecraft damage (half-hearts). Placeholder. */
    public static final float DAMAGE_SCALE = 0.2f;

    private NoitaValues() {
    }

    /** Noita frames -> Minecraft ticks. */
    public static float ticks(double frames) {
        return (float) (frames / FRAMES_PER_TICK);
    }

    /** Noita pixels -> Minecraft blocks. */
    public static float blocks(double pixels) {
        return (float) (pixels * PIXELS_PER_BLOCK);
    }

    /** Noita px/s -> Minecraft blocks per tick. */
    public static float speed(double pixelsPerSecond) {
        return (float) (pixelsPerSecond * PIXELS_PER_BLOCK / 20.0);
    }

    /** Noita px/s^2 -> Minecraft blocks per tick^2. */
    public static float gravity(double pixelsPerSecondSquared) {
        return (float) (pixelsPerSecondSquared * PIXELS_PER_BLOCK / 400.0);
    }

    /** Noita damage -> Minecraft damage. */
    public static float damage(double noitaDamage) {
        return (float) (noitaDamage * DAMAGE_SCALE);
    }
}
