package dev.znova.module.modules;

import dev.znova.module.Category;
import dev.znova.module.Module;
import dev.znova.module.setting.BooleanSetting;
import dev.znova.util.ColorUtils;
import dev.znova.util.RenderUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/** Top-left HUD badge: client name plus optional live FPS and coordinate readouts. */
public final class WatermarkModule extends Module {
    private static final int PADDING = 9;
    private static final int GAP = 10;
    private static final int HEIGHT = 18;

    private final BooleanSetting showFps = addSetting(new BooleanSetting("Show FPS", true));
    private final BooleanSetting showCoordinates = addSetting(new BooleanSetting("Show Coordinates", false));

    public WatermarkModule() {
        super("Watermark", Category.RENDER, "HUD badge with the client name, live FPS and your coordinates.", true);
    }

    @Override
    public void onRenderHud(DrawContext context) {
        if (mc.player == null || mc.options.hudHidden) {
            return;
        }

        TextRenderer textRenderer = mc.textRenderer;
        String brand = "zNova";

        List<String> segments = new ArrayList<>();
        if (this.showFps.getValue()) {
            segments.add(mc.getCurrentFps() + " FPS");
        }
        if (this.showCoordinates.getValue()) {
            BlockPos pos = mc.player.getBlockPos();
            segments.add(pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        }

        int textWidth = textRenderer.getWidth(brand);
        for (String segment : segments) {
            textWidth += GAP + textRenderer.getWidth(segment);
        }

        int x = 6;
        int y = 6;
        int width = PADDING + textWidth + PADDING + 3;

        long time = Util.getMeasuringTimeMs();
        float pulse = 0.55F + 0.45F * (float) Math.sin(time / 520.0D);

        RenderUtils.glow(context, x, y, width, HEIGHT, 3.0F,
                ColorUtils.rgba(ColorUtils.CYAN, Math.round(60.0F * pulse)), 3, 3.0F);
        RenderUtils.roundedRect(context, x, y, width, HEIGHT, 3.0F, ColorUtils.rgba(ColorUtils.PANEL, 217));

        RenderUtils.roundedRect(context, x + 2.0F, y + 3.0F, 2.0F, HEIGHT - 6.0F, 1.0F,
                ColorUtils.rgba(ColorUtils.CYAN, Math.round(170.0F + 85.0F * pulse)));

        RenderUtils.text(context, textRenderer, brand, x + PADDING, y + 5.0F, 0xFFF2FBFF, false);

        float cursor = x + PADDING + textRenderer.getWidth(brand);
        if (!segments.isEmpty()) {
            int dividerX = Math.round(cursor + GAP / 2.0F);
            context.fill(dividerX, y + 5, dividerX + 1, y + HEIGHT - 5, ColorUtils.rgba(0xFFFFFF, 45));
            cursor += GAP;
        }

        for (String segment : segments) {
            RenderUtils.text(context, textRenderer, segment, cursor, y + 5.0F,
                    ColorUtils.rgba(ColorUtils.CYAN, 215), false);
            cursor += textRenderer.getWidth(segment) + GAP;
        }
    }
}
