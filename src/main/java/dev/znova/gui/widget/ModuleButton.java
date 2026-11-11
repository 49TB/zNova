package dev.znova.gui.widget;

import dev.znova.module.Module;
import dev.znova.util.Animation;
import dev.znova.util.ColorUtils;
import dev.znova.util.RenderUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** A single clickable module row inside a {@link CategoryPanel}. */
public final class ModuleButton {
    private final Module module;
    private final Animation hover = new Animation(16.0F);
    private final Animation toggle = new Animation(14.0F);
    private final Animation press = new Animation(22.0F);

    public ModuleButton(Module module) {
        this.module = module;
        this.toggle.snapTo(module.isEnabled() ? 1.0F : 0.0F);
    }

    public Module getModule() {
        return this.module;
    }

    public void click() {
        this.module.toggle();
        this.press.snapTo(1.0F);
    }

    public void render(DrawContext context, TextRenderer textRenderer, float x, float y, float width, float height,
                       int mouseX, int mouseY, float alpha) {
        boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        this.hover.setTarget(hovered ? 1.0F : 0.0F);
        this.toggle.setTarget(this.module.isEnabled() ? 1.0F : 0.0F);
        this.press.setTarget(0.0F);

        float hoverAmount = this.hover.get();
        float toggleAmount = this.toggle.get();
        float pressAmount = this.press.get();

        int background = ColorUtils.lerp(ColorUtils.rgba(0xFFFFFF, 8), ColorUtils.rgba(0xFFFFFF, 24), hoverAmount);
        background = ColorUtils.lerp(background, ColorUtils.rgba(ColorUtils.CYAN, 40), toggleAmount * 0.85F);
        background = ColorUtils.lerp(background, ColorUtils.rgba(0xFFFFFF, 80), pressAmount * 0.35F);
        RenderUtils.roundedRect(context, x, y, width, height, 6.0F, ColorUtils.scaleAlpha(background, alpha));

        if (hoverAmount > 0.01F) {
            RenderUtils.roundedBorder(context, x, y, width, height, 6.0F, 1.0F,
                    ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, Math.round(70.0F * hoverAmount)), alpha));
        }

        if (toggleAmount > 0.05F) {
            RenderUtils.glow(context, x + 1.0F, y + 3.0F, width - 2.0F, height - 6.0F, 5.0F,
                    ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, Math.round(26.0F * toggleAmount)), alpha), 2, 2.0F);
        }

        float accentHeight = (height - 10.0F) * (0.3F + 0.7F * toggleAmount);
        RenderUtils.roundedRect(context, x + 3.0F, y + height / 2.0F - accentHeight / 2.0F, 2.0F, accentHeight, 1.0F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, Math.round(110.0F + 145.0F * toggleAmount)), alpha));

        int textColor = ColorUtils.lerp(0xFF8B95A6, 0xFFF2FBFF, toggleAmount);
        RenderUtils.text(context, textRenderer, this.module.getName(), x + 11.0F, y + (height - 8.0F) / 2.0F,
                ColorUtils.scaleAlpha(textColor, alpha), false);

        float pillWidth = 20.0F;
        float pillHeight = 9.0F;
        float pillX = x + width - pillWidth - 8.0F;
        float pillY = y + (height - pillHeight) / 2.0F;
        int pillColor = ColorUtils.lerp(ColorUtils.rgba(0xFFFFFF, 30), ColorUtils.rgba(ColorUtils.CYAN, 140), toggleAmount);
        RenderUtils.roundedRect(context, pillX, pillY, pillWidth, pillHeight, pillHeight / 2.0F,
                ColorUtils.scaleAlpha(pillColor, alpha));

        float knobSize = 6.0F;
        float knobX = pillX + 1.5F + (pillWidth - knobSize - 3.0F) * toggleAmount;
        RenderUtils.roundedRect(context, knobX, pillY + 1.5F, knobSize, knobSize, knobSize / 2.0F,
                ColorUtils.scaleAlpha(ColorUtils.lerp(0xFF9AA5B5, 0xFF0A0A0F, toggleAmount), alpha));
    }
}
