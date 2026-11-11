package dev.znova.mixin;

import dev.znova.ZNovaClient;
import dev.znova.util.ColorUtils;
import dev.znova.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the zNova startup screen over the vanilla splash overlay.
 * <p>
 * The vanilla overlay still runs its own update logic (resource reload handling,
 * completion timing and fade-out), so the transition into the game stays intact. We
 * simply paint an opaque futuristic screen on top of it every frame.
 */
@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {
    @Shadow
    private float progress;

    @Shadow
    private long reloadCompleteTime;

    @Inject(method = "render", at = @At("TAIL"))
    private void znova$renderFuturisticSplash(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        // 1.21.11 submits GUI draws into a GuiRenderState; starting a new root layer puts
        // our startup screen above everything the vanilla overlay already submitted.
        context.createNewRootLayer();

        float fade = 1.0F;
        if (this.reloadCompleteTime > -1L) {
            float elapsed = (Util.getMeasuringTimeMs() - this.reloadCompleteTime) / 1000.0F;
            fade = 1.0F - MathHelper.clamp(elapsed, 0.0F, 1.0F);
        }
        if (fade <= 0.004F) {
            return;
        }

        long time = Util.getMeasuringTimeMs();
        float pulse = 0.55F + 0.45F * (float) Math.sin(time / 380.0D);

        // Deep dark backdrop.
        context.fill(0, 0, screenWidth, screenHeight, ColorUtils.scaleAlpha(ColorUtils.rgb(ColorUtils.BACKGROUND), fade));

        // Faint grid for depth.
        int gridColor = ColorUtils.scaleAlpha(ColorUtils.rgba(0xFFFFFF, 10), fade);
        for (int gridX = 0; gridX < screenWidth; gridX += 48) {
            context.fill(gridX, 0, gridX + 1, screenHeight, gridColor);
        }
        for (int gridY = 0; gridY < screenHeight; gridY += 48) {
            context.fill(0, gridY, screenWidth, gridY + 1, gridColor);
        }

        // Ambient glow behind the wordmark.
        RenderUtils.glow(context, screenWidth / 2.0F - 150.0F, screenHeight / 2.0F - 96.0F, 300.0F, 72.0F, 32.0F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, Math.round(30.0F + 20.0F * pulse)), fade), 9, 24.0F);

        // Wordmark.
        float wordmarkY = screenHeight / 2.0F - 86.0F;
        RenderUtils.glowTextScaled(context, textRenderer, "zNova", screenWidth / 2.0F, wordmarkY, 4.0F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, 24), fade), ColorUtils.scaleAlpha(0xFFF4FDFF, fade));

        // Thin violet-to-cyan accent rule under the wordmark.
        RenderUtils.horizontalGradient(context, screenWidth / 2.0F - 40.0F, wordmarkY + 44.0F, 40.0F, 1.0F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.VIOLET, 0), fade),
                ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, 220), fade));
        RenderUtils.horizontalGradient(context, screenWidth / 2.0F, wordmarkY + 44.0F, 40.0F, 1.0F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, 220), fade),
                ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.VIOLET, 0), fade));

        String tagline = "FUTURISTIC UTILITY CLIENT";
        RenderUtils.spacedText(context, textRenderer, tagline,
                screenWidth / 2.0F - RenderUtils.spacedWidth(textRenderer, tagline, 3) / 2.0F, wordmarkY + 52.0F, 3,
                ColorUtils.scaleAlpha(ColorUtils.rgba(0xA9B6C6, 200), fade), false);

        // Progress bar.
        int barWidth = Math.min(420, Math.max(180, screenWidth - 140));
        int barHeight = 4;
        int barX = screenWidth / 2 - barWidth / 2;
        int barY = screenHeight / 2 + 6;

        RenderUtils.roundedRect(context, barX - 1.0F, barY - 1.0F, barWidth + 2.0F, barHeight + 2.0F, (barHeight + 2) / 2.0F,
                ColorUtils.scaleAlpha(0x24FFFFFF, fade));
        RenderUtils.roundedRect(context, barX, barY, barWidth, barHeight, barHeight / 2.0F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(0x101018, 255), fade));

        float progressValue = MathHelper.clamp(this.progress, 0.0F, 1.0F);
        int fillWidth = Math.round((barWidth - 2) * progressValue);
        if (fillWidth > 0) {
            int fillX = barX + 1;

            RenderUtils.horizontalGradient(context, fillX, barY + 1.0F, fillWidth, barHeight - 2,
                    ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, 255), fade),
                    ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.VIOLET, 255), fade));

            RenderUtils.glow(context, fillX, barY + 1.0F, fillWidth, barHeight - 2.0F, 2.0F,
                    ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, Math.round(70.0F * pulse)), fade), 4, 3.0F);

            // Travelling highlight so the bar always feels alive.
            float phase = (time % 1600L) / 1600.0F;
            int shimmerX = fillX + Math.round(fillWidth * phase) - 16;
            int shimmerStart = Math.max(fillX, shimmerX);
            int shimmerEnd = Math.min(fillX + fillWidth, shimmerX + 32);
            if (shimmerEnd > shimmerStart) {
                RenderUtils.roundedRect(context, shimmerStart, barY + 1.0F, shimmerEnd - shimmerStart, barHeight - 2.0F, 1.0F,
                        ColorUtils.scaleAlpha(0x55FFFFFF, fade));
            }

            // Bright head of the bar.
            RenderUtils.glow(context, fillX + fillWidth - 3.0F, barY - 2.0F, 6.0F, barHeight + 4.0F, 3.0F,
                    ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, Math.round(140.0F * pulse)), fade), 3, 4.0F);
            RenderUtils.roundedRect(context, fillX + fillWidth - 2.0F, barY - 1.0F, 2.0F, barHeight + 2.0F, 1.0F,
                    ColorUtils.scaleAlpha(0xFFFFFFFF, fade));
        }

        RenderUtils.textCentered(context, textRenderer, Math.round(progressValue * 100.0F) + "%",
                screenWidth / 2.0F, barY + 12.0F, ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, 190), fade), false);

        // Corner captions.
        RenderUtils.text(context, textRenderer, "zNova Client  v" + ZNovaClient.VERSION, 10.0F, screenHeight - 18.0F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(0x8994A3, 190), fade), false);

        String minecraft = "Minecraft " + client.getGameVersion();
        RenderUtils.text(context, textRenderer, minecraft,
                screenWidth - 10.0F - textRenderer.getWidth(minecraft), screenHeight - 18.0F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(0x8994A3, 190), fade), false);
    }
}
