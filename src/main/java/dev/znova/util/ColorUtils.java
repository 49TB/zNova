package dev.znova.util;

import net.minecraft.util.math.MathHelper;

/**
 * Palette and colour helpers for zNova.
 * <p>
 * Every colour in the mod is a packed ARGB integer (0xAARRGGBB), which is the format
 * {@link net.minecraft.client.gui.DrawContext} expects for fills and text.
 */
public final class ColorUtils {
    /** Deep, near-black background used by the splash screen. */
    public static final int BACKGROUND = 0x0A0A0F;
    /** Glass panel tint: rgb(15, 15, 25). */
    public static final int PANEL = 0x0F0F19;
    /** Neon cyan accent. */
    public static final int CYAN = 0x00F0FF;
    /** Neon violet accent. */
    public static final int VIOLET = 0x8B5CF6;

    private ColorUtils() {
    }

    /** Opaque ARGB from a plain 0xRRGGBB value. */
    public static int rgb(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    /** ARGB from a plain 0xRRGGBB value plus an explicit alpha. */
    public static int rgba(int rgb, int alpha) {
        return (alpha & 0xFF) << 24 | (rgb & 0xFFFFFF);
    }

    /** ARGB from separate channels. */
    public static int rgba(int red, int green, int blue, int alpha) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
    }

    public static int alpha(int color) {
        return color >>> 24 & 0xFF;
    }

    /** Multiplies the alpha channel of {@code color} by {@code factor} (clamped to 0..1). */
    public static int scaleAlpha(int color, float factor) {
        int newAlpha = Math.round((color >>> 24 & 0xFF) * MathHelper.clamp(factor, 0.0F, 1.0F));
        return (newAlpha & 0xFF) << 24 | color & 0xFFFFFF;
    }

    /** Linear interpolation between two ARGB colours in all four channels. */
    public static int lerp(int from, int to, float delta) {
        float t = MathHelper.clamp(delta, 0.0F, 1.0F);
        return lerpChannel(from, to, t, 24)
                | lerpChannel(from, to, t, 16)
                | lerpChannel(from, to, t, 8)
                | lerpChannel(from, to, t, 0);
    }

    private static int lerpChannel(int from, int to, float delta, int shift) {
        float start = from >>> shift & 0xFF;
        float end = to >>> shift & 0xFF;
        int value = Math.round(start + (end - start) * delta);
        return (value & 0xFF) << shift;
    }
}
