package dev.znova.module;

import dev.znova.module.modules.AutoMinerModule;
import dev.znova.module.modules.FullbrightModule;
import dev.znova.module.modules.WatermarkModule;
import net.minecraft.client.gui.DrawContext;

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
        register(new FullbrightModule());
        register(new AutoMinerModule());
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
