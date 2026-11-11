package dev.znova.module.modules;

import dev.znova.module.Category;
import dev.znova.module.Module;
import dev.znova.module.setting.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.hit.BlockHitResult;

/**
 * Holds the attack key for you while a pickaxe is in the main hand.
 * <p>
 * Pickaxes are detected through the {@code #minecraft:pickaxes} item tag, because the
 * dedicated {@code PickaxeItem} class no longer exists in 1.21.11 (items are
 * component-driven now). The key is only ever released if this module was the one that
 * pressed it, so a manually held attack key never gets cancelled when you switch tools.
 */
public final class AutoMinerModule extends Module {
    private final BooleanSetting onlyWhenLookingAtBlock = addSetting(new BooleanSetting("Only When Looking At Block", false));

    private boolean holdingAttack;

    public AutoMinerModule() {
        super("AutoMiner", Category.QOL, "Holds attack while a pickaxe is in your main hand and no screen is open.");
    }

    @Override
    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            release(client);
            return;
        }

        boolean lookingAtBlock = !this.onlyWhenLookingAtBlock.getValue()
                || client.crosshairTarget instanceof BlockHitResult;

        boolean shouldMine = client.currentScreen == null
                && lookingAtBlock
                && client.player.getMainHandStack().isIn(ItemTags.PICKAXES);

        if (shouldMine) {
            if (!this.holdingAttack) {
                client.options.attackKey.setPressed(true);
                this.holdingAttack = true;
            }
        } else {
            release(client);
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
        if (!this.holdingAttack) {
            return;
        }
        client.options.attackKey.setPressed(false);
        this.holdingAttack = false;
    }
}
