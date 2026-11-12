package dev.znova.module.modules;

import dev.znova.module.Category;
import dev.znova.module.Module;
import dev.znova.module.setting.BooleanSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Holds the forward key for you, optionally sprinting while it does.
 * <p>
 * The keys are only released if this module is the one that pressed them, so a manually
 * held W or sprint key is never cancelled when the module turns off. Both keys are released
 * whenever a screen opens, which keeps the player from sliding around inside menus.
 */
public final class AutoWalkModule extends Module {
    private final BooleanSetting sprint = addSetting(new BooleanSetting("Sprint", false));

    private boolean holdingForward;
    private boolean holdingSprint;

    public AutoWalkModule() {
        super("AutoWalk", Category.QOL, "Holds the forward key for you while no screen is open.");
    }

    @Override
    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.currentScreen != null) {
            release(client);
            return;
        }

        if (!this.holdingForward) {
            client.options.forwardKey.setPressed(true);
            this.holdingForward = true;
        }

        if (this.sprint.getValue()) {
            if (!this.holdingSprint) {
                client.options.sprintKey.setPressed(true);
                this.holdingSprint = true;
            }
        } else {
            releaseSprint(client);
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
        if (this.holdingForward) {
            client.options.forwardKey.setPressed(false);
            this.holdingForward = false;
        }
        releaseSprint(client);
    }

    private void releaseSprint(MinecraftClient client) {
        if (this.holdingSprint) {
            client.options.sprintKey.setPressed(false);
            this.holdingSprint = false;
        }
    }
}
