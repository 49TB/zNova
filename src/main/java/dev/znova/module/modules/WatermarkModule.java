package dev.znova.module.modules;

import dev.znova.module.Category;
import dev.znova.module.Module;
import dev.znova.util.ColorUtils;
import dev.znova.util.RenderUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;

/** Top-left HUD badge: client name plus a live FPS readout. Enabled by default. */
public final class WatermarkModule extends Module {
    private static final int PADDING = 9;
    private static final int GAP = 12;
    private static final int HEIGHT = 18;

    public WatermarkModule() {
        super("Watermark", Category.RENDER, "Futuristic HUD badge with the client name and a live FPS counter.", true);
    }

    @Override
    public void onRenderHud(DrawContext context) {
        if (mc.player == null || mc.options.hudHidden) {
            return;
        }

        TextRenderer textRenderer = mc.textRenderer;
        String brand = "zNova";
        String fps = mc.getCurrentFps() + " FPS";

        int brandWidth = textRenderer.getWidth(brand);
        int fpsWidth = textRenderer.getWidth(fps);
        int width = PADDING + brandWidth + GAP + fpsWidth + PADDING + 3;
        int x = 6;
        int y = 6;

        long time = Util.getMeasuringTimeMs();
        float pulse = 0.55F + 0.45F * (float) Math.sin(time / 520.0D);

        RenderUtils.glow(context, x, y, width, HEIGHT, 3.0F,
                ColorUtils.rgba(ColorUtils.CYAN, Math.round(60.0F * pulse)), 3, 3.0F);
        RenderUtils.roundedRect(context, x, y, width, HEIGHT, 3.0F, ColorUtils.rgba(ColorUtils.PANEL, 217));

        RenderUtils.roundedRect(context, x + 2.0F, y + 3.0F, 2.0F, HEIGHT - 6.0F, 1.0F,
                ColorUtils.rgba(ColorUtils.CYAN, Math.round(170.0F + 85.0F * pulse)));

        RenderUtils.text(context, textRenderer, brand, x + PADDING, y + 5.0F, 0xFFF2FBFF, false);

        int dividerX = x + PADDING + brandWidth + GAP / 2;
        context.fill(dividerX, y + 5, dividerX + 1, y + HEIGHT - 5, ColorUtils.rgba(0xFFFFFF, 45));

        RenderUtils.text(context, textRenderer, fps, x + PADDING + brandWidth + GAP, y + 5.0F,
                ColorUtils.rgba(ColorUtils.CYAN, 215), false);
    }
}
