package dev.znova.gui;

import dev.znova.ZNovaClient;
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
 * Left click toggles a module, right click expands its settings card. While a module is
 * waiting for a keybind the screen swallows the next key press: ESC or DELETE unbinds.
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
            int startY = Math.max(56, (this.height - 230) / 2);

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

        RenderUtils.glowTextScaled(context, textRenderer, "zNova", this.width / 2.0F, 12.0F, 2.0F,
                ColorUtils.rgba(ColorUtils.CYAN, 30), 0xFFF2FBFF);

        String subtitle = "UTILITY CLIENT";
        RenderUtils.spacedText(context, textRenderer, subtitle,
                this.width / 2.0F - RenderUtils.spacedWidth(textRenderer, subtitle, 2) / 2.0F, 30.0F, 2,
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
                : "Left click toggles  -  Right click opens settings  -  Right Shift closes";
        RenderUtils.textCentered(context, textRenderer, footer, this.width / 2.0F, this.height - 30.0F,
                ColorUtils.rgba(0xC3D2E0, hoveredModule != null ? 190 : 120), false);

        // Non-intrusive disclaimer pinned to the bottom-left corner.
        RenderUtils.text(context, textRenderer,
                "zNova QoL & Visual Utility Mod - for singleplayer & quality-of-life use only.",
                12.0F, this.height - 20.0F, ColorUtils.rgba(0x8994A3, 130), false);
        RenderUtils.text(context, textRenderer,
                "Not a cheat client: no movement, reach or combat automation.",
                12.0F, this.height - 10.0F, ColorUtils.rgba(0x8994A3, 100), false);

        context.getMatrices().popMatrix();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int button = click.button();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            double mouseX = click.x();
            double mouseY = click.y();
            for (int i = this.panels.size() - 1; i >= 0; i--) {
                CategoryPanel panel = this.panels.get(i);
                if (!panel.isHovered(mouseX, mouseY)) {
                    continue;
                }
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && panel.isOverHeader(mouseX, mouseY)) {
                    // Raise the clicked panel above the others and start dragging it.
                    this.panels.remove(panel);
                    this.panels.add(panel);
                    this.draggingPanel = panel;
                    this.dragOffsetX = mouseX - panel.getX();
                    this.dragOffsetY = mouseY - panel.getY();
                    panel.setDragging(true);
                    return true;
                }
                if (panel.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        boolean handled = false;
        if (this.draggingPanel != null) {
            this.draggingPanel.setDragging(false);
            this.draggingPanel = null;
            handled = true;
        }
        for (CategoryPanel panel : this.panels) {
            panel.mouseReleased();
        }
        return handled || super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (this.draggingPanel != null && click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.draggingPanel.setPosition((float) (click.x() - this.dragOffsetX), (float) (click.y() - this.dragOffsetY));
            this.draggingPanel.clampTo(this.width, this.height);
            return true;
        }
        for (CategoryPanel panel : this.panels) {
            if (panel.mouseDragged(click.x(), click.y(), click.button())) {
                return true;
            }
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.getKeycode();

        Module listening = getListeningModule();
        if (listening != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                listening.setKeybind(dev.znova.util.KeyNames.NONE);
            } else {
                listening.setKeybind(keyCode);
            }
            listening.setListeningForBind(false);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.client.setScreen(null);
            return true;
        }
        return super.keyPressed(input);
    }

    private Module getListeningModule() {
        if (ZNovaClient.getModuleManager() == null) {
            return null;
        }
        for (Module module : ZNovaClient.getModuleManager().getModules()) {
            if (module.isListeningForBind()) {
                return module;
            }
        }
        return null;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
