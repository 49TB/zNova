package dev.znova.module;

import dev.znova.module.modules.ArrayListModule;
import dev.znova.module.modules.AutoJumpModule;
import dev.znova.module.modules.AutoMinerModule;
import dev.znova.module.modules.AutoWalkModule;
import dev.znova.module.modules.FullbrightModule;
import dev.znova.module.modules.WatermarkModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Holds every zNova module and dispatches lifecycle events to them. */
public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final Map<Category, List<Module>> byCategory = new EnumMap<>(Category.class);

    public ModuleManager() {
        for (Category category : Category.values()) {
            this.byCategory.put(category, new ArrayList<>());
        }
        register(new WatermarkModule());
        register(new ArrayListModule());
        register(new FullbrightModule());
        register(new AutoMinerModule());
        register(new AutoWalkModule());
        register(new AutoJumpModule());
    }

    private void register(Module module) {
        this.modules.add(module);
        this.byCategory.get(module.getCategory()).add(module);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(this.modules);
    }

    public List<Module> getModules(Category category) {
        return Collections.unmodifiableList(this.byCategory.get(category));
    }

    /** Returns the module of the given type, or {@code null} if it is not registered. */
    public <T extends Module> T get(Class<T> type) {
        for (Module module : this.modules) {
            if (type.isInstance(module)) {
                return type.cast(module);
            }
        }
        return null;
    }

    public void onTick() {
        for (Module module : this.modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }

    /**
     * Toggles modules whose universal keybind was just pressed.
     * <p>
     * Only the press edge counts, and only while no screen is open, so holding a key or
     * typing in chat never flips a module repeatedly.
     */
    public void handleKeybinds(MinecraftClient client) {
        boolean inGame = client.currentScreen == null;
        for (Module module : this.modules) {
            int key = module.getKeybind();
            boolean down = inGame
                    && key != GLFW.GLFW_KEY_UNKNOWN
                    && InputUtil.isKeyPressed(client.getWindow(), key);
            if (down && !module.wasBoundKeyDown()) {
                module.toggle();
            }
            module.setBoundKeyDown(down);
        }
    }

    /** Clears "Press a key..." listening mode, used when the ClickGUI is not open. */
    public void clearKeybindListening() {
        for (Module module : this.modules) {
            if (module.isListeningForBind()) {
                module.setListeningForBind(false);
            }
        }
    }

    public void onRenderHud(DrawContext context) {
        for (Module module : this.modules) {
            if (module.isEnabled()) {
                module.onRenderHud(context);
            }
        }
    }

    public void onDisconnect() {
        for (Module module : this.modules) {
            module.onDisconnect();
        }
    }
}
