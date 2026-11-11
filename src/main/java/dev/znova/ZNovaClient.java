package dev.znova;

import dev.znova.gui.ClickGuiScreen;
import dev.znova.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Client entrypoint for zNova. */
public final class ZNovaClient implements ClientModInitializer {
    public static final String MOD_ID = "znova";
    public static final String NAME = "zNova";
    public static final String VERSION = "1.1.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    private static ZNovaClient instance;

    private ModuleManager moduleManager;
    private KeyBinding clickGuiKey;

    @Override
    public void onInitializeClient() {
        instance = this;
        this.moduleManager = new ModuleManager();

        this.clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.znova.clickgui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.create(Identifier.of(MOD_ID, "main"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (this.clickGuiKey.wasPressed()) {
                toggleClickGui(client);
            }
            if (client.currentScreen == null) {
                // Leaving the GUI cancels any "Press a key..." prompt.
                this.moduleManager.clearKeybindListening();
            }
            this.moduleManager.handleKeybinds(client);
            this.moduleManager.onTick();
        });

        // Rendered just before chat, inheriting vanilla's hide-gui render condition.
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.of(MOD_ID, "watermark"),
                (context, tickCounter) -> this.moduleManager.onRenderHud(context));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> this.moduleManager.onDisconnect());

        LOGGER.info("{} v{} initialised with {} modules", NAME, VERSION, this.moduleManager.getModules().size());
    }

    private static void toggleClickGui(MinecraftClient client) {
        if (client.currentScreen instanceof ClickGuiScreen) {
            client.setScreen(null);
            return;
        }
        if (client.currentScreen == null) {
            client.setScreen(new ClickGuiScreen());
        }
    }

    public static ZNovaClient getInstance() {
        return instance;
    }

    /** @return the module registry, or {@code null} before client initialisation finishes. */
    public static ModuleManager getModuleManager() {
        return instance == null ? null : instance.moduleManager;
    }
}
