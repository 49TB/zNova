package dev.znova.module.modules;

import dev.znova.module.Category;
import dev.znova.module.Module;
import net.minecraft.client.MinecraftClient;

/**
 * Holds the jump key for you while the module is enabled and no screen is open.
 * The key is released the moment the module turns off, a menu opens or the player
 * disconnects, and only if this module was the one holding it.
 */
public final class AutoJumpModule extends Module {
    private boolean holdingJump;

    public AutoJumpModule() {
        super("AutoJump", Category.QOL, "Holds the jump key for you while no screen is open.");
    }

    @Override
    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.currentScreen != null) {
            release(client);
            return;
        }

        if (!this.holdingJump) {
            client.options.jumpKey.setPressed(true);
            this.holdingJump = true;
        }
    }

    @Override
    public void onDisable() {
        release(MinecraftClient.getInstance());
    }

    @Override
    public void onDisconnect() {
        release(MinecraftClient.getInstance());
    }

    private void release(MinecraftClient client) {
        if (!this.holdingJump) {
            return;
        }
        client.options.jumpKey.setPressed(false);
        this.holdingJump = false;
    }
}
