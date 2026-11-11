package dev.znova.gui.widget;

import dev.znova.ZNovaClient;
import dev.znova.module.Category;
import dev.znova.module.Module;
import dev.znova.util.Animation;
import dev.znova.util.ColorUtils;
import dev.znova.util.RenderUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

/** A draggable glass panel that lists every module of one {@link Category}. */
public final class CategoryPanel {
    public static final int WIDTH = 196;
    public static final int GAP = 16;
    public static final int HEADER_HEIGHT = 32;
    public static final int PADDING = 8;
    public static final int BUTTON_HEIGHT = 24;
    public static final int BUTTON_GAP = 4;

    private final Category category;
    private final List<ModuleButton> buttons = new ArrayList<>();
    private final Animation hover = new Animation(14.0F);
    private final Animation lift = new Animation(16.0F);

    private float x;
    private float y;
    private boolean dragging;

    public CategoryPanel(Category category, float x, float y) {
        this.category = category;
        this.x = x;
        this.y = y;
        if (ZNovaClient.getModuleManager() != null) {
            for (Module module : ZNovaClient.getModuleManager().getModules(category)) {
                this.buttons.add(new ModuleButton(module));
            }
        }
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public Category getCategory() {
        return this.category;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public float getHeight() {
        int contentHeight = this.buttons.isEmpty()
                ? 18
                : this.buttons.size() * BUTTON_HEIGHT + (this.buttons.size() - 1) * BUTTON_GAP;
        return HEADER_HEIGHT + PADDING + contentHeight + PADDING;
    }

    public void clampTo(float screenWidth, float screenHeight) {
        this.x = MathHelper.clamp(this.x, 4.0F, Math.max(4.0F, screenWidth - WIDTH - 4.0F));
        this.y = MathHelper.clamp(this.y, 4.0F, Math.max(4.0F, screenHeight - getHeight() - 4.0F));
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX < this.x + WIDTH
                && mouseY >= this.y && mouseY < this.y + getHeight();
    }

    public boolean isOverHeader(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX < this.x + WIDTH
                && mouseY >= this.y && mouseY < this.y + HEADER_HEIGHT;
    }

    /** @return the module whose row is under the cursor, or {@code null}. */
    public Module getHoveredModule(int mouseX, int mouseY) {
        for (int i = 0; i < this.buttons.size(); i++) {
            float buttonY = getContentY() + i * (BUTTON_HEIGHT + BUTTON_GAP);
            if (mouseX >= this.x + 6 && mouseX < this.x + WIDTH - 6
                    && mouseY >= buttonY && mouseY < buttonY + BUTTON_HEIGHT) {
                return this.buttons.get(i).getModule();
            }
        }
        return null;
    }

    /**
     * Handles a click inside the panel.
     *
     * @return {@code true} when the click was consumed by the panel
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) {
            return false;
        }
        for (int i = 0; i < this.buttons.size(); i++) {
            float buttonY = getContentY() + i * (BUTTON_HEIGHT + BUTTON_GAP);
            if (mouseX >= this.x + 6 && mouseX < this.x + WIDTH - 6
                    && mouseY >= buttonY && mouseY < buttonY + BUTTON_HEIGHT) {
                this.buttons.get(i).click();
                return true;
            }
        }
        return true;
    }

    private float getContentY() {
        return this.y + HEADER_HEIGHT + PADDING;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float alpha) {
        this.hover.setTarget(isHovered(mouseX, mouseY) ? 1.0F : 0.0F);
        this.lift.setTarget(this.dragging ? 1.0F : 0.0F);

        float hoverAmount = this.hover.get();
        float liftAmount = this.lift.get();

        float panelX = this.x;
        float panelY = this.y - liftAmount * 2.0F;
        float panelHeight = getHeight();
        int accent = this.category.getAccentColor();

        RenderUtils.glow(context, panelX, panelY, WIDTH, panelHeight, 12.0F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(accent, Math.round(22.0F + 34.0F * hoverAmount)), alpha), 5, 4.0F);

        RenderUtils.roundedRect(context, panelX, panelY, WIDTH, panelHeight, 12.0F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(ColorUtils.PANEL, 217), alpha));
        RenderUtils.roundedRect(context, panelX + 1.0F, panelY + 1.0F, WIDTH - 2.0F, HEADER_HEIGHT - 8.0F, 11.0F,
                ColorUtils.scaleAlpha(0x12FFFFFF, alpha));

        context.fill(Math.round(panelX) + 10, Math.round(panelY) + HEADER_HEIGHT - 1,
                Math.round(panelX) + WIDTH - 10, Math.round(panelY) + HEADER_HEIGHT,
                ColorUtils.scaleAlpha(ColorUtils.rgba(accent, 90), alpha));

        RenderUtils.roundedBorder(context, panelX, panelY, WIDTH, panelHeight, 12.0F, 1.0F,
                ColorUtils.scaleAlpha(ColorUtils.lerp(ColorUtils.rgba(accent, 55), ColorUtils.rgba(accent, 150), hoverAmount), alpha));

        RenderUtils.text(context, textRenderer, this.category.getDisplayName(), panelX + 12.0F, panelY + 10.0F,
                ColorUtils.scaleAlpha(0xFFF2FBFF, alpha), false);
        RenderUtils.roundedRect(context, panelX + 12.0F, panelY + 22.0F, 16.0F, 1.5F, 0.75F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(accent, 200), alpha));

        int enabledCount = 0;
        for (ModuleButton button : this.buttons) {
            if (button.getModule().isEnabled()) {
                enabledCount++;
            }
        }
        String counter = enabledCount + "/" + this.buttons.size();
        RenderUtils.text(context, textRenderer, counter,
                panelX + WIDTH - 12.0F - textRenderer.getWidth(counter), panelY + 10.0F,
                ColorUtils.scaleAlpha(ColorUtils.rgba(accent, 170), alpha), false);

        if (this.buttons.isEmpty()) {
            RenderUtils.text(context, textRenderer, "No modules yet", panelX + 12.0F, panelY + HEADER_HEIGHT + PADDING + 4.0F,
                    ColorUtils.scaleAlpha(0xFF6C7787, alpha), false);
            return;
        }

        float contentY = panelY + HEADER_HEIGHT + PADDING;
        for (int i = 0; i < this.buttons.size(); i++) {
            this.buttons.get(i).render(context, textRenderer, panelX + 6.0F,
                    contentY + i * (BUTTON_HEIGHT + BUTTON_GAP), WIDTH - 12.0F, BUTTON_HEIGHT,
                    mouseX, mouseY, alpha);
        }
    }
}
