package dev.znova.module.modules;

import dev.znova.ZNovaClient;
import dev.znova.module.Category;
import dev.znova.module.Module;
import dev.znova.module.ModuleManager;
import dev.znova.module.setting.BooleanSetting;
import dev.znova.module.setting.ModeSetting;
import dev.znova.util.Animation;
import dev.znova.util.ColorUtils;
import dev.znova.util.KeyNames;
import dev.znova.util.RenderUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The classic client array list: every enabled module in the top-right (or bottom-right)
 * corner, each row with a dark translucent plate and a glowing cyan accent bar.
 * <p>
 * Rows are sorted by text width so the widest entry sits at the top, and every row slides
 * in from the right while fading when a module is enabled and slides out when it is
 * disabled.
 */
public final class ArrayListModule extends Module {
    private static final int ROW_HEIGHT = 14;
    private static final int ROW_GAP = 2;
    private static final int EDGE_INSET = 4;
    private static final int PADDING = 8;
    private static final int KEY_GAP = 5;

    private final BooleanSetting showKeybinds = addSetting(new BooleanSetting("Show Keybinds", false));
    private final ModeSetting position = addSetting(new ModeSetting("Position", 0, "Top-Right", "Bottom-Right"));

    private final Map<Module, Entry> entries = new LinkedHashMap<>();

    public ArrayListModule() {
        super("ArrayList", Category.RENDER, "Dynamic list of every enabled module, sorted by width.", false);
    }

    @Override
    public void onRenderHud(DrawContext context) {
        if (mc.player == null || mc.options.hudHidden) {
            return;
        }
        ModuleManager manager = ZNovaClient.getModuleManager();
        if (manager == null) {
            return;
        }

        for (Module module : manager.getModules()) {
            Entry entry = this.entries.get(module);
            if (module.isEnabled()) {
                if (entry == null) {
                    entry = new Entry(module);
                    this.entries.put(module, entry);
                }
                entry.active = true;
                entry.animation.setTarget(1.0F);
            } else if (entry != null) {
                entry.active = false;
                entry.animation.setTarget(0.0F);
            }
        }

        TextRenderer textRenderer = mc.textRenderer;
        List<Row> rows = new ArrayList<>();

        Iterator<Entry> iterator = this.entries.values().iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            float visibility = entry.animation.get();
            if (!entry.active && visibility < 0.02F) {
                iterator.remove();
                continue;
            }

            String name = entry.module.getName();
            String key = null;
            if (this.showKeybinds.getValue() && entry.module.getKeybind() != KeyNames.NONE) {
                key = "[" + entry.module.getKeybindName() + "]";
            }

            int textWidth = textRenderer.getWidth(name);
            if (key != null) {
                textWidth += KEY_GAP + textRenderer.getWidth(key);
            }
            rows.add(new Row(name, key, textWidth, visibility));
        }

        // Longest entry at the top, exactly like the classic array list layout.
        rows.sort((a, b) -> Integer.compare(b.textWidth, a.textWidth));

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int totalHeight = rows.size() * ROW_HEIGHT + Math.max(0, rows.size() - 1) * ROW_GAP;

        boolean topRight = this.position.getIndex() == 0;
        float y = topRight ? EDGE_INSET : screenHeight - EDGE_INSET - totalHeight;

        for (Row row : rows) {
            float visibility = row.visibility;
            int width = row.textWidth + PADDING * 2;
            float x = screenWidth - EDGE_INSET - width + (1.0F - visibility) * 12.0F;

            RenderUtils.roundedRect(context, x, y, width, ROW_HEIGHT, 3.0F,
                    ColorUtils.rgba(0x0A0A0F, Math.round(192.0F * visibility)));

            // Thin glowing cyan accent bar on the right edge of every entry.
            float barX = x + width - 2.0F;
            RenderUtils.glow(context, barX, y + 1.0F, 2.0F, ROW_HEIGHT - 2.0F, 1.0F,
                    ColorUtils.rgba(ColorUtils.CYAN, Math.round(95.0F * visibility)), 3, 2.0F);
            RenderUtils.roundedRect(context, barX, y + 1.0F, 2.0F, ROW_HEIGHT - 2.0F, 1.0F,
                    ColorUtils.rgba(ColorUtils.CYAN, Math.round(255.0F * visibility)));

            float textRight = x + width - 7.0F;
            int textY = Math.round(y + (ROW_HEIGHT - 8) / 2.0F);

            if (row.keyText != null) {
                int keyWidth = textRenderer.getWidth(row.keyText);
                RenderUtils.text(context, textRenderer, row.keyText, textRight - keyWidth, textY,
                        ColorUtils.rgba(ColorUtils.CYAN, Math.round(220.0F * visibility)), false);
                textRight -= keyWidth + KEY_GAP;
            }

            RenderUtils.text(context, textRenderer, row.name, textRight - textRenderer.getWidth(row.name), textY,
                    ColorUtils.rgba(0xF2FBFF, Math.round(235.0F * visibility)), false);

            y += ROW_HEIGHT + ROW_GAP;
        }
    }

    @Override
    public void onDisconnect() {
        this.entries.clear();
    }

    private static final class Entry {
        private final Module module;
        private final Animation animation = new Animation(16.0F, 0.0F);
        private boolean active;

        private Entry(Module module) {
            this.module = module;
        }
    }

    private record Row(String name, String keyText, int textWidth, float visibility) {
    }
}
