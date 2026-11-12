package dev.znova.gui;

import dev.znova.ZNovaClient;
import dev.znova.gui.widget.CategoryPanel;
import dev.znova.module.Category;
import dev.znova.module.Module;
import dev.znova.util.Animation;
import dev.znova.util.ColorUtils;
import dev.znova.util.KeyNames;
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
 * Panel positions are restored from {@link PanelLayout} every time the screen opens and
 * written back when a drag finishes or the GUI closes, so a dragged layout survives both
 * reopening the GUI and restarting the game. Panel width adapts to the current window so
 * all three columns fit at any GUI scale.
 */
public final class ClickGuiScreen extends Screen {
    private static final int PANEL_GAP = 10;
    private static final int PANEL_MIN_WIDTH = 120;
    private static final int PANEL_MAX_WIDTH = CategoryPanel.DEFAULT_WIDTH;
    private static final int SCREEN_INSET = 8;

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
        PanelLayout.load();

        int panelWidth = computePanelWidth();
        if (this.panels.isEmpty()) {
            Category[] categories = Category.values();
            int totalWidth = categories.length * panelWidth + (categories.length - 1) * PANEL_GAP;
            int startX = Math.max(SCREEN_INSET, (this.width - totalWidth) / 2);
            int startY = Math.max(44, (this.height - 200) / 2);

            for (int i = 0; i < categories.length; i++) {
                Category category = categories[i];
                float panelX = PanelLayout.has(category) ? PanelLayout.getX(category) : startX + i * (panelWidth + PANEL_GAP);
                float panelY = PanelLayout.has(category) ? PanelLayout.getY(category) : startY;
                CategoryPanel panel = new CategoryPanel(category, panelWidth, panelX, panelY);
                panel.clampTo(this.width, this.height);
                this.panels.add(panel);
            }
            this.intro.snapTo(0.0F);
        } else {
            for (CategoryPanel panel : this.panels) {
                panel.setWidth(panelWidth);
                panel.clampTo(this.width, this.height);
            }
        }
        this.intro.setTarget(1.0F);
    }

    /** Width that keeps all three panels on screen at the current GUI scale. */
    private int computePanelWidth() {
        int count = Category.values().length;
        int available = this.width - SCREEN_INSET * 2 - (count - 1) * PANEL_GAP;
        int perPanel = available / count;
        return Math.max(PANEL_MIN_WIDTH, Math.min(PANEL_MAX_WIDTH, perPanel));
    }

    private void persistPanels() {
        for (CategoryPanel panel : this.panels) {
            PanelLayout.setPosition(panel.getCategory(), panel.getX(), panel.getY());
        }
        PanelLayout.save();
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
        context.getMatrices().translate(-this.width / 2.0F, -this.height / 2.0F + (1.0F - eased) * 10.0F);

        RenderUtils.glowTextScaled(context, textRenderer, "zNova", this.width / 2.0F, 8.0F, 1.5F,
                ColorUtils.rgba(ColorUtils.CYAN, 30), 0xFFF2FBFF);

        String subtitle = "UTILITY CLIENT";
        RenderUtils.spacedText(context, textRenderer, subtitle,
                this.width / 2.0F - RenderUtils.spacedWidth(textRenderer, subtitle, 2) / 2.0F, 24.0F, 2,
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
            persistPanels();
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
                listening.setKeybind(KeyNames.NONE);
            } else {
                listening.setKeybind(keyCode);
            }
            listening.setListeningForBind(false);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            persistPanels();
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
