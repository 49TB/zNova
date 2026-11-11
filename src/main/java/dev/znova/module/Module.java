package dev.znova.module;

import dev.znova.module.setting.Setting;
import dev.znova.util.KeyNames;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for every zNova module.
 * <p>
 * Every module automatically owns a universal keybind (bound through the ClickGUI) and
 * may declare any number of {@link Setting settings} that are rendered in its inline
 * configuration panel.
 */
public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final Category category;
    private final String description;
    private final List<Setting> settings = new ArrayList<>();

    private boolean enabled;
    private int keybind = KeyNames.NONE;
    private boolean listeningForBind;
    private boolean boundKeyDown;

    protected Module(String name, Category category, String description) {
        this(name, category, description, false);
    }

    protected Module(String name, Category category, String description, boolean enabledByDefault) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.enabled = enabledByDefault;
    }

    /** Registers a setting so the ClickGUI can render and edit it. */
    protected <T extends Setting> T addSetting(T setting) {
        this.settings.add(setting);
        return setting;
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

    public List<Setting> getSettings() {
        return Collections.unmodifiableList(this.settings);
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

    /* --------------------------------------------------------------- keybind */

    public int getKeybind() {
        return this.keybind;
    }

    public void setKeybind(int keybind) {
        this.keybind = keybind;
    }

    public String getKeybindName() {
        return KeyNames.getName(this.keybind);
    }

    public boolean isListeningForBind() {
        return this.listeningForBind;
    }

    public void setListeningForBind(boolean listening) {
        this.listeningForBind = listening;
    }

    /** Previous tick's physical state of the bound key, used for press-edge detection. */
    public boolean wasBoundKeyDown() {
        return this.boundKeyDown;
    }

    public void setBoundKeyDown(boolean down) {
        this.boundKeyDown = down;
    }

    /** @return the key code used by GLFW for the bound key, or {@code GLFW_KEY_UNKNOWN} */
    public int getRawKeybind() {
        return this.keybind == KeyNames.NONE ? GLFW.GLFW_KEY_UNKNOWN : this.keybind;
    }

    /* -------------------------------------------------------------- lifecycle */

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
