package dev.znova.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Base class for every zNova module. */
public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final Category category;
    private final String description;
    private boolean enabled;

    protected Module(String name, Category category, String description) {
        this(name, category, description, false);
    }

    protected Module(String name, Category category, String description, boolean enabledByDefault) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.enabled = enabledByDefault;
    }

    public String getName() {
        return this.name;
    }

    public Category getCategory() {
        return this.category;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void toggle() {
        setEnabled(!this.enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    /** Called when the module is switched on. */
    public void onEnable() {
    }

    /** Called when the module is switched off. Must undo anything the module started. */
    public void onDisable() {
    }

    /** Called once per client tick while the module is enabled. */
    public void onTick() {
    }

    /** Called every frame from the HUD element registered with HudElementRegistry while enabled. */
    public void onRenderHud(DrawContext context) {
    }

    /** Called when the client disconnects from a world, so nothing leaks between sessions. */
    public void onDisconnect() {
    }
}
