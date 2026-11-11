package dev.znova.module.modules;

import dev.znova.module.Category;
import dev.znova.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * Fullbright.
 * <p>
 * Since 1.21.9 the lightmap lives entirely on the GPU, so the old "blend the 16x16
 * lightmap towards white" trick is no longer possible. This module instead keeps a
 * client-side Night Vision effect alive: it lights up caves and night exactly like
 * fullbright, without particles and without a HUD icon. The effect is only added by us,
 * and only removed again if we were the one who applied it.
 */
public final class FullbrightModule extends Module {
    private static final int DURATION_TICKS = 600;
    private static final int REFRESH_BELOW_TICKS = 400;

    private boolean applied;

    public FullbrightModule() {
        super("Fullbright", Category.RENDER, "Keeps the world fully lit with client-side Night Vision (no particles, no HUD icon).");
    }

    @Override
    public void onEnable() {
        this.applied = false;
    }

    @Override
    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        StatusEffectInstance existing = client.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (existing == null) {
            if (client.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, DURATION_TICKS, 0, false, false, false))) {
                this.applied = true;
            }
            return;
        }

        // Keep our own effect topped up; never refresh an effect the player got elsewhere.
        if (this.applied && existing.getDuration() < REFRESH_BELOW_TICKS) {
            client.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, DURATION_TICKS, 0, false, false, false));
        }
    }

    @Override
    public void onDisable() {
        removeEffect();
    }

    @Override
    public void onDisconnect() {
        removeEffect();
    }

    private void removeEffect() {
        if (!this.applied) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
        this.applied = false;
    }
}
