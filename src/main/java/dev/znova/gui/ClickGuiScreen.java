package dev.znova.gui;

import dev.znova.gui.widget.CategoryPanel;
import dev.znova.module.Category;
import dev.znova.module.Module;
import dev.znova.util.Animation;
import dev.znova.util.ColorUtils;
import dev.znova.util.RenderUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * zNova's ClickGUI: a blurred, dark glass overlay with three draggable category panels.
 * Opened and closed with Right Shift.
 * <p>
 * In 1.21.11 screen input is delivered through {@link Click} and {@link KeyInput} records
 * instead of raw doubles/key codes.
 */
public final class ClickGuiScreen extends Screen {
    private final List<CategoryPanel> panels = new ArrayList<>();
    private final Animation intro = new Animation(14.0F, 0.0F);

    private CategoryPanel draggingPanel;
    private double dragOffsetX;
    private double dragOffsetY;

    public ClickGuiScreen() {
        super(Text.literal("zNova ClickGUI"));
    }

    @Override
    protected void init() {
        if (this.panels.isEmpty()) {
            Category[] categories = Category.values();
            int totalWidth = categories.length * CategoryPanel.WIDTH + (categories.length - 1) * CategoryPanel.GAP;
            int startX = Math.max(10, (this.width - totalWidth) / 2);
            int startY = Math.max(56, (this.height - 220) / 2);

            for (int i = 0; i < categories.length; i++) {
                CategoryPanel panel = new CategoryPanel(categories[i],
                        startX + i * (CategoryPanel.WIDTH + CategoryPanel.GAP), startY);
                panel.clampTo(this.width, this.height);
                this.panels.add(panel);
            }
            this.intro.snapTo(0.0F);
        } else {
            for (CategoryPanel panel : this.panels) {
                panel.clampTo(this.width, this.height);
            }
        }
        this.intro.setTarget(1.0F);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        // Draws the blurred, darkened in-game background behind the glass panels.
        super.render(context, mouseX, mouseY, deltaTicks);

        TextRenderer textRenderer = this.textRenderer;
        float animation = this.intro.get();
        float eased = animation * animation * (3.0F - 2.0F * animation);

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(this.width / 2.0F, this.height / 2.0F);
        context.getMatrices().scale(0.97F + 0.03F * eased, 0.97F + 0.03F * eased);
        context.getMatrices().translate(-this.width / 2.0F, -this.height / 2.0F + (1.0F - eased) * 12.0F);

        RenderUtils.glowTextScaled(context, textRenderer, "zNova", this.width / 2.0F, 16.0F, 2.0F,
                ColorUtils.rgba(ColorUtils.CYAN, 30), 0xFFF2FBFF);

        String subtitle = "UTILITY CLIENT";
        RenderUtils.spacedText(context, textRenderer, subtitle,
                this.width / 2.0F - RenderUtils.spacedWidth(textRenderer, subtitle, 2) / 2.0F, 34.0F, 2,
                ColorUtils.rgba(ColorUtils.CYAN, 130), false);

        for (CategoryPanel panel : this.panels) {
            panel.render(context, textRenderer, mouseX, mouseY, eased);
        }

        Module hoveredModule = null;
        for (CategoryPanel panel : this.panels) {
            Module module = panel.getHoveredModule(mouseX, mouseY);
            if (module != null) {
                hoveredModule = module;
            }
        }

        String footer = hoveredModule != null
                ? hoveredModule.getName() + "  -  " + hoveredModule.getDescription()
                : "Right Shift closes the GUI  -  drag a panel header to move it";
        RenderUtils.textCentered(context, textRenderer, footer, this.width / 2.0F, this.height - 24.0F,
                ColorUtils.rgba(0xC3D2E0, hoveredModule != null ? 190 : 120), false);

        context.getMatrices().popMatrix();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            double mouseX = click.x();
            double mouseY = click.y();
            for (int i = this.panels.size() - 1; i >= 0; i--) {
                CategoryPanel panel = this.panels.get(i);
                if (!panel.isHovered(mouseX, mouseY)) {
                    continue;
                }
                if (panel.isOverHeader(mouseX, mouseY)) {
                    // Raise the clicked panel above the others and start dragging it.
                    this.panels.remove(panel);
                    this.panels.add(panel);
                    this.draggingPanel = panel;
                    this.dragOffsetX = mouseX - panel.getX();
                    this.dragOffsetY = mouseY - panel.getY();
                    panel.setDragging(true);
                    return true;
                }
                if (panel.mouseClicked(mouseX, mouseY, click.button())) {
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (this.draggingPanel != null) {
            this.draggingPanel.setDragging(false);
            this.draggingPanel = null;
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (this.draggingPanel != null && click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.draggingPanel.setPosition((float) (click.x() - this.dragOffsetX), (float) (click.y() - this.dragOffsetY));
            this.draggingPanel.clampTo(this.width, this.height);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.getKeycode();
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.client.setScreen(null);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
