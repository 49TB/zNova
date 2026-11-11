package dev.znova.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * Drawing helpers built entirely on {@link DrawContext}, so they work in the HUD,
 * in screens and on top of the splash overlay.
 */
public final class RenderUtils {
    private RenderUtils() {
    }

    /* ------------------------------------------------------------------ text */

    public static void text(DrawContext context, TextRenderer textRenderer, String value, float x, float y, int color, boolean shadow) {
        context.drawText(textRenderer, Text.literal(value), Math.round(x), Math.round(y), color, shadow);
    }

    public static void textCentered(DrawContext context, TextRenderer textRenderer, String value, float centerX, float y, int color, boolean shadow) {
        text(context, textRenderer, value, centerX - textRenderer.getWidth(value) / 2.0F, y, color, shadow);
    }

    /** Draws text scaled around its top-left corner. */
    public static void scaledText(DrawContext context, TextRenderer textRenderer, String value, float x, float y, float scale, int color, boolean shadow) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(scale, scale);
        context.drawText(textRenderer, Text.literal(value), 0, 0, color, shadow);
        context.getMatrices().popMatrix();
    }

    public static void scaledTextCentered(DrawContext context, TextRenderer textRenderer, String value, float centerX, float y, float scale, int color, boolean shadow) {
        scaledText(context, textRenderer, value, centerX - textRenderer.getWidth(value) * scale / 2.0F, y, scale, color, shadow);
    }

    /**
     * Draws a large wordmark with a soft neon bloom around it. The bloom is faked with a
     * ring of low-alpha copies at small offsets, which is cheap and needs no shaders.
     */
    public static void glowTextScaled(DrawContext context, TextRenderer textRenderer, String value, float centerX, float y, float scale, int glowColor, int coreColor) {
        float x = centerX - textRenderer.getWidth(value) * scale / 2.0F;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(scale, scale);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                context.drawText(textRenderer, Text.literal(value), dx, dy, glowColor, false);
            }
        }
        context.drawText(textRenderer, Text.literal(value), 0, 0, coreColor, false);
        context.getMatrices().popMatrix();
    }

    /** Width of {@code value} when drawn with extra letter spacing. */
    public static int spacedWidth(TextRenderer textRenderer, String value, int spacing) {
        if (value.isEmpty()) {
            return 0;
        }
        int width = 0;
        for (int i = 0; i < value.length(); i++) {
            width += textRenderer.getWidth(String.valueOf(value.charAt(i))) + spacing;
        }
        return width - spacing;
    }

    /** Draws letter-spaced text, used for the small futuristic captions. */
    public static void spacedText(DrawContext context, TextRenderer textRenderer, String value, float x, float y, int spacing, int color, boolean shadow) {
        float cursor = x;
        for (int i = 0; i < value.length(); i++) {
            String character = String.valueOf(value.charAt(i));
            context.drawText(textRenderer, Text.literal(character), Math.round(cursor), Math.round(y), color, shadow);
            cursor += textRenderer.getWidth(character) + spacing;
        }
    }

    /* --------------------------------------------------------------- shapes */

    /** Fills a rectangle with rounded corners (radius in pixels, clamped to half the size). */
    public static void roundedRect(DrawContext context, float x, float y, float width, float height, float radius, int color) {
        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = Math.round(width);
        int ih = Math.round(height);
        if (iw <= 0 || ih <= 0) {
            return;
        }

        int ir = Math.round(MathHelper.clamp(radius, 0.0F, Math.min(iw, ih) / 2.0F));
        if (ir <= 0) {
            context.fill(ix, iy, ix + iw, iy + ih, color);
            return;
        }

        float radiusSquared = (float) ir * ir;
        for (int row = 0; row < ih; row++) {
            float inset = 0.0F;
            if (row < ir) {
                float dy = ir - row - 0.5F;
                inset = ir - (float) Math.sqrt(Math.max(0.0F, radiusSquared - dy * dy));
            } else if (row >= ih - ir) {
                float dy = row + 0.5F - (ih - ir);
                inset = ir - (float) Math.sqrt(Math.max(0.0F, radiusSquared - dy * dy));
            }
            int left = ix + Math.round(inset);
            int right = ix + iw - Math.round(inset);
            if (right > left) {
                context.fill(left, iy + row, right, iy + row + 1, color);
            }
        }
    }

    /** Draws a 1-2px outline that follows the rounded corner path of a rectangle. */
    public static void roundedBorder(DrawContext context, float x, float y, float width, float height, float radius, float thickness, int color) {
        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = Math.round(width);
        int ih = Math.round(height);
        int t = Math.max(1, Math.round(thickness));
        if (iw <= 0 || ih <= 0) {
            return;
        }

        int ir = Math.round(MathHelper.clamp(radius, 0.0F, Math.min(iw, ih) / 2.0F));
        if (ir <= 0) {
            context.fill(ix, iy, ix + iw, iy + t, color);
            context.fill(ix, iy + ih - t, ix + iw, iy + ih, color);
            context.fill(ix, iy, ix + t, iy + ih, color);
            context.fill(ix + iw - t, iy, ix + iw, iy + ih, color);
            return;
        }

        context.fill(ix + ir, iy, ix + iw - ir, iy + t, color);
        context.fill(ix + ir, iy + ih - t, ix + iw - ir, iy + ih, color);
        context.fill(ix, iy + ir, ix + t, iy + ih - ir, color);
        context.fill(ix + iw - t, iy + ir, ix + iw, iy + ih - ir, color);

        int steps = Math.max(10, ir * 4);
        for (int i = 0; i <= steps; i++) {
            double angle = Math.PI / 2.0 * i / steps;
            float offsetX = ir - ir * (float) Math.cos(angle);
            float offsetY = ir - ir * (float) Math.sin(angle);
            float cos = ir * (float) Math.cos(angle);
            float sin = ir * (float) Math.sin(angle);

            dot(context, ix + offsetX, iy + offsetY, t, color);
            dot(context, ix + iw - ir + cos, iy + offsetY, t, color);
            dot(context, ix + offsetX, iy + ih - ir + sin, t, color);
            dot(context, ix + iw - ir + cos, iy + ih - ir + sin, t, color);
        }
    }

    private static void dot(DrawContext context, float x, float y, int size, int color) {
        int ix = Math.round(x);
        int iy = Math.round(y);
        context.fill(ix, iy, ix + size, iy + size, color);
    }

    /**
     * Draws a soft neon bloom around a rectangle by stacking expanding outlines with
     * decreasing alpha. Each extra layer gets fainter, which reads as a glow.
     */
    public static void glow(DrawContext context, float x, float y, float width, float height, float radius, int color, int layers, float spread) {
        if (layers <= 0 || spread <= 0.0F) {
            return;
        }
        int baseAlpha = ColorUtils.alpha(color);
        if (baseAlpha <= 0) {
            return;
        }
        int rgb = color & 0xFFFFFF;
        for (int layer = layers; layer >= 1; layer--) {
            float expand = spread * layer / layers;
            float strength = 1.0F - (layer - 1) / (float) layers;
            int alpha = Math.round(baseAlpha * strength * 0.45F);
            if (alpha <= 1) {
                continue;
            }
            roundedBorder(context, x - expand, y - expand, width + expand * 2.0F, height + expand * 2.0F,
                    radius + expand, 1.5F, alpha << 24 | rgb);
        }
    }

    /** Horizontal ARGB gradient built from 1px columns (used by the loading bar). */
    public static void horizontalGradient(DrawContext context, float x, float y, float width, float height, int leftColor, int rightColor) {
        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = Math.round(width);
        int ih = Math.round(height);
        if (iw <= 0 || ih <= 0) {
            return;
        }
        for (int column = 0; column < iw; column++) {
            float delta = iw == 1 ? 1.0F : column / (float) (iw - 1);
            context.fill(ix + column, iy, ix + column + 1, iy + ih, ColorUtils.lerp(leftColor, rightColor, delta));
        }
    }
}
