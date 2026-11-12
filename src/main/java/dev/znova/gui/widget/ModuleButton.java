package dev.znova.gui.widget;

import dev.znova.module.Module;
import dev.znova.module.setting.BooleanSetting;
import dev.znova.module.setting.ModeSetting;
import dev.znova.module.setting.Setting;
import dev.znova.module.setting.SliderSetting;
import dev.znova.util.Animation;
import dev.znova.util.ColorUtils;
import dev.znova.util.KeyNames;
import dev.znova.util.RenderUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

/**
 * A single clickable module row plus its inline configuration panel.
 * <p>
 * Left click toggles the module, right click expands the settings card underneath it. The
 * card always starts with the universal keybind row, followed by the module's own settings.
 * Rows are kept compact so three fully expanded panels still fit on a 1080p screen at
 * GUI scale 3, and long setting names are trimmed with an ellipsis instead of overflowing.
 */
public final class ModuleButton {
    public static final int ROW_HEIGHT = 17;
    public static final int ROW_GAP = 1;

    private static final int CONFIG_TOP = 4;
    private static final int CONFIG_BOTTOM = 6;
    private static final int ROW_INSET = 7;
    private static final int CHIP_HEIGHT = 11;
    private static final int PILL_WIDTH = 16;
    private static final int PILL_HEIGHT = 8;

    private final Module module;
    private final Animation hover = new Animation(16.0F);
    private final Animation toggle = new Animation(14.0F);
    private final Animation press = new Animation(22.0F);

    private boolean expanded;
    private SliderSetting draggingSlider;
    private float sliderTrackX;
    private float sliderTrackWidth;

    public ModuleButton(Module module) {
        this.module = module;
        this.toggle.snapTo(module.isEnabled() ? 1.0F : 0.0F);
    }

    public Module getModule() {
        return this.module;
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    /** Height of the whole settings card, including its inner padding. */
    public int getConfigHeight() {
        int rows = 1 + this.module.getSettings().size();
        return CONFIG_TOP + rows * ROW_HEIGHT + (rows - 1) * ROW_GAP + CONFIG_BOTTOM;
    }

    public int getHeight() {
        return CategoryPanel.BUTTON_HEIGHT + (this.expanded ? getConfigHeight() : 0);
    }

    /* ---------------------------------------------------------------- render */

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
        RenderUtils.roundedRect(context, x, y, width, height, 5.0F, ColorUtils.scaleAlpha(background, alpha));

        if (hoverAmount > 0.01F) {
            RenderUtils.roundedBorder(context, x, y, width, height, 5.0F, 1.0F,
                    ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, Math.round(70.0F * hoverAmount)), alpha));
        }

        if (toggleAmount > 0.05F) {
            RenderUtils.glow(context, x + 1.0F, y + 2.0F, width - 2.0F, height - 4.0F, 4.0F,
                    ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, Math.round(26.0F * toggleAmount)), alpha), 2, 2.0F);
        }

        float textY = y + (height - 8.0F) / 2.0F;
        float accentHeight = (height - 8.0F) * (0.3F + 0.7F * toggleAmount);
        RenderUtils.roundedRect(context, x + 2.0F, y + height / 2.0F - accentHeight / 2.0F, 2.0F, accentHeight, 1.0F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, Math.round(110.0F + 145.0F * toggleAmount)), alpha));

        int textColor = ColorUtils.lerp(0xFF8B95A6, 0xFFF2FBFF, toggleAmount);
        RenderUtils.text(context, textRenderer, this.module.getName(), x + 8.0F, textY,
                ColorUtils.scaleAlpha(textColor, alpha), false);

        // Small chevron hinting that a right click opens the settings card.
        String arrow = this.expanded ? "v" : ">";
        RenderUtils.text(context, textRenderer, arrow, x + width - 38.0F, textY,
                ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, this.expanded ? 220 : 120), alpha), false);

        float pillX = x + width - PILL_WIDTH - 6.0F;
        float pillY = y + (height - PILL_HEIGHT) / 2.0F;
        int pillColor = ColorUtils.lerp(ColorUtils.rgba(0xFFFFFF, 30), ColorUtils.rgba(ColorUtils.CYAN, 140), toggleAmount);
        RenderUtils.roundedRect(context, pillX, pillY, PILL_WIDTH, PILL_HEIGHT, PILL_HEIGHT / 2.0F,
                ColorUtils.scaleAlpha(pillColor, alpha));

        float knobSize = 5.0F;
        float knobX = pillX + 1.5F + (PILL_WIDTH - knobSize - 3.0F) * toggleAmount;
        RenderUtils.roundedRect(context, knobX, pillY + 1.5F, knobSize, knobSize, knobSize / 2.0F,
                ColorUtils.scaleAlpha(ColorUtils.lerp(0xFF9AA5B5, 0xFF0A0A0F, toggleAmount), alpha));

        if (this.expanded) {
            renderConfig(context, textRenderer, x, y + CategoryPanel.BUTTON_HEIGHT, width, mouseX, mouseY, alpha);
        }
    }

    private void renderConfig(DrawContext context, TextRenderer textRenderer, float x, float y, float width,
                              int mouseX, int mouseY, float alpha) {
        float configHeight = getConfigHeight();
        RenderUtils.roundedRect(context, x, y + 1.0F, width, configHeight - 2.0F, 5.0F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(0x0A0A0F, 175), alpha));
        RenderUtils.roundedBorder(context, x, y + 1.0F, width, configHeight - 2.0F, 5.0F, 1.0F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, 38), alpha));

        renderKeybindRow(context, textRenderer, x, y + CONFIG_TOP, width, mouseX, mouseY, alpha);

        int index = 1;
        for (Setting setting : this.module.getSettings()) {
            float rowY = y + CONFIG_TOP + index * (ROW_HEIGHT + ROW_GAP);
            if (setting instanceof BooleanSetting booleanSetting) {
                renderBooleanRow(context, textRenderer, booleanSetting, x, rowY, width, mouseX, mouseY, alpha);
            } else if (setting instanceof SliderSetting sliderSetting) {
                renderSliderRow(context, textRenderer, sliderSetting, x, rowY, width, mouseX, mouseY, alpha);
            } else if (setting instanceof ModeSetting modeSetting) {
                renderModeRow(context, textRenderer, modeSetting, x, rowY, width, mouseX, mouseY, alpha);
            }
            index++;
        }
    }

    private void renderRowHover(DrawContext context, float x, float y, float width, int mouseX, int mouseY, float alpha) {
        if (isInside(mouseX, mouseY, x, y, width, ROW_HEIGHT)) {
            RenderUtils.roundedRect(context, x + 3.0F, y, width - 6.0F, ROW_HEIGHT, 3.0F,
                    ColorUtils.scaleAlpha(ColorUtils.rgba(0xFFFFFF, 12), alpha));
        }
    }

    private void renderKeybindRow(DrawContext context, TextRenderer textRenderer, float x, float y, float width,
                                  int mouseX, int mouseY, float alpha) {
        renderRowHover(context, x, y, width, mouseX, mouseY, alpha);

        boolean listening = this.module.isListeningForBind();
        boolean bound = this.module.getKeybind() != KeyNames.NONE;
        String value = listening ? "Press key..." : this.module.getKeybindName();
        float chipWidth = textRenderer.getWidth(value) + 8.0F;
        float chipX = x + width - ROW_INSET - chipWidth;

        RenderUtils.text(context, textRenderer,
                fit(textRenderer, "Keybind", chipX - (x + ROW_INSET) - 4.0F),
                x + ROW_INSET, y + (ROW_HEIGHT - 8.0F) / 2.0F,
                ColorUtils.scaleAlpha(0xFF8B95A6, alpha), false);

        RenderUtils.roundedRect(context, chipX, y + (ROW_HEIGHT - CHIP_HEIGHT) / 2.0F, chipWidth, CHIP_HEIGHT, 3.0F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, listening ? 48 : bound ? 30 : 16), alpha));
        RenderUtils.text(context, textRenderer, value, chipX + 4.0F, y + (ROW_HEIGHT - 8.0F) / 2.0F,
                ColorUtils.scaleAlpha(listening || bound ? 0xFF00F0FF : 0xFF7C8797, alpha), false);
    }

    private void renderBooleanRow(DrawContext context, TextRenderer textRenderer, BooleanSetting setting,
                                  float x, float y, float width, int mouseX, int mouseY, float alpha) {
        renderRowHover(context, x, y, width, mouseX, mouseY, alpha);

        float labelMaxWidth = width - ROW_INSET * 2.0F - PILL_WIDTH - 5.0F;
        RenderUtils.text(context, textRenderer, fit(textRenderer, setting.getName(), labelMaxWidth),
                x + ROW_INSET, y + (ROW_HEIGHT - 8.0F) / 2.0F,
                ColorUtils.scaleAlpha(0xFFC3D2E0, alpha), false);

        float pillX = x + width - ROW_INSET - PILL_WIDTH;
        float pillY = y + (ROW_HEIGHT - PILL_HEIGHT) / 2.0F;
        float amount = setting.getValue() ? 1.0F : 0.0F;

        RenderUtils.roundedRect(context, pillX, pillY, PILL_WIDTH, PILL_HEIGHT, PILL_HEIGHT / 2.0F,
                ColorUtils.scaleAlpha(ColorUtils.lerp(ColorUtils.rgba(0xFFFFFF, 28), ColorUtils.rgba(ColorUtils.CYAN, 150), amount), alpha));

        float knobSize = 5.0F;
        float knobX = pillX + 1.5F + (PILL_WIDTH - knobSize - 3.0F) * amount;
        RenderUtils.roundedRect(context, knobX, pillY + 1.5F, knobSize, knobSize, knobSize / 2.0F,
                ColorUtils.scaleAlpha(ColorUtils.lerp(0xFF9AA5B5, 0xFF0A0A0F, amount), alpha));
    }

    private void renderModeRow(DrawContext context, TextRenderer textRenderer, ModeSetting setting,
                               float x, float y, float width, int mouseX, int mouseY, float alpha) {
        renderRowHover(context, x, y, width, mouseX, mouseY, alpha);

        String value = setting.getValue();
        float chipWidth = textRenderer.getWidth(value) + 8.0F;
        float chipX = x + width - ROW_INSET - chipWidth;

        RenderUtils.text(context, textRenderer,
                fit(textRenderer, setting.getName(), chipX - (x + ROW_INSET) - 4.0F),
                x + ROW_INSET, y + (ROW_HEIGHT - 8.0F) / 2.0F,
                ColorUtils.scaleAlpha(0xFFC3D2E0, alpha), false);

        RenderUtils.roundedRect(context, chipX, y + (ROW_HEIGHT - CHIP_HEIGHT) / 2.0F, chipWidth, CHIP_HEIGHT, 3.0F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, 26), alpha));
        RenderUtils.text(context, textRenderer, value, chipX + 4.0F, y + (ROW_HEIGHT - 8.0F) / 2.0F,
                ColorUtils.scaleAlpha(0xFF00F0FF, alpha), false);
    }

    private void renderSliderRow(DrawContext context, TextRenderer textRenderer, SliderSetting setting,
                                 float x, float y, float width, int mouseX, int mouseY, float alpha) {
        renderRowHover(context, x, y, width, mouseX, mouseY, alpha);

        String value = setting.getDisplayValue();
        float valueWidth = textRenderer.getWidth(value);
        RenderUtils.text(context, textRenderer,
                fit(textRenderer, setting.getName(), width - ROW_INSET * 2.0F - valueWidth - 6.0F),
                x + ROW_INSET, y + 1.0F, ColorUtils.scaleAlpha(0xFFC3D2E0, alpha), false);
        RenderUtils.text(context, textRenderer, value, x + width - ROW_INSET - valueWidth, y + 1.0F,
                ColorUtils.scaleAlpha(0xFF00F0FF, alpha), false);

        float trackX = x + ROW_INSET;
        float trackWidth = width - ROW_INSET * 2.0F;
        float trackY = y + ROW_HEIGHT - 5.0F;
        float ratio = MathHelper.clamp(setting.getRatio(), 0.0F, 1.0F);

        RenderUtils.roundedRect(context, trackX, trackY, trackWidth, 2.5F, 1.25F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(0xFFFFFF, 22), alpha));

        if (ratio > 0.0F) {
            float fillWidth = Math.max(2.5F, trackWidth * ratio);
            RenderUtils.horizontalGradient(context, trackX, trackY, fillWidth, 2.5F,
                    ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, 235), alpha),
                    ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.VIOLET, 235), alpha));
            RenderUtils.glow(context, trackX, trackY, fillWidth, 2.5F, 1.25F,
                    ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.CYAN, 80), alpha), 2, 2.0F);
        }

        float knobX = trackX + trackWidth * ratio;
        RenderUtils.roundedRect(context, knobX - 2.0F, trackY - 1.75F, 4.0F, 6.0F, 2.0F,
                ColorUtils.scaleAlpha(0xFF00F0FF, alpha));
    }

    /* ---------------------------------------------------------------- input */

    /**
     * @return {@code true} when the click was consumed by this button
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button, float x, float y, float width) {
        boolean overHeader = isInside(mouseX, mouseY, x, y, width, CategoryPanel.BUTTON_HEIGHT);

        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (overHeader) {
                this.expanded = !this.expanded;
                return true;
            }
            return isInside(mouseX, mouseY, x, y, width, getHeight());
        }

        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return isInside(mouseX, mouseY, x, y, width, getHeight());
        }

        if (overHeader) {
            this.module.toggle();
            this.press.snapTo(1.0F);
            return true;
        }

        if (!this.expanded) {
            return false;
        }

        float configY = y + CategoryPanel.BUTTON_HEIGHT;
        if (isInside(mouseX, mouseY, x, configY + CONFIG_TOP, width, ROW_HEIGHT)) {
            this.module.setListeningForBind(!this.module.isListeningForBind());
            return true;
        }

        int index = 1;
        for (Setting setting : this.module.getSettings()) {
            float settingY = configY + CONFIG_TOP + index * (ROW_HEIGHT + ROW_GAP);
            if (isInside(mouseX, mouseY, x, settingY, width, ROW_HEIGHT)) {
                if (setting instanceof BooleanSetting booleanSetting) {
                    booleanSetting.toggle();
                } else if (setting instanceof ModeSetting modeSetting) {
                    modeSetting.cycle();
                } else if (setting instanceof SliderSetting sliderSetting) {
                    this.draggingSlider = sliderSetting;
                    this.sliderTrackX = x + ROW_INSET;
                    this.sliderTrackWidth = width - ROW_INSET * 2.0F;
                    applySlider(mouseX);
                }
                return true;
            }
            index++;
        }

        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (this.draggingSlider != null && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            applySlider(mouseX);
            return true;
        }
        return false;
    }

    public void mouseReleased() {
        this.draggingSlider = null;
    }

    private void applySlider(double mouseX) {
        if (this.draggingSlider == null || this.sliderTrackWidth <= 0.0F) {
            return;
        }
        float ratio = (float) (mouseX - this.sliderTrackX) / this.sliderTrackWidth;
        this.draggingSlider.setFromRatio(MathHelper.clamp(ratio, 0.0F, 1.0F));
    }

    private static boolean isInside(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /** Trims a label with an ellipsis so a long setting name never collides with its control. */
    private static String fit(TextRenderer textRenderer, String text, float maxWidth) {
        if (maxWidth <= 0.0F) {
            return "";
        }
        if (textRenderer.getWidth(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "..";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            String candidate = builder.toString() + text.charAt(i) + ellipsis;
            if (textRenderer.getWidth(candidate) > maxWidth) {
                break;
            }
            builder.append(text.charAt(i));
        }
        return builder.toString() + ellipsis;
    }
}
