package dev.znova.module.modules;

import dev.znova.module.Category;
import dev.znova.module.Module;
import dev.znova.module.setting.SliderSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * Fullbright.
 * <p>
 * Since 1.21.9 the lightmap lives entirely on the GPU, so blending lightmap pixels is no
 * longer possible. The module keeps a client-side Night Vision effect alive (no
 * particles, no HUD icon) and exposes a real-time brightness slider that drives the
 * vanilla gamma option, which is the only brightness value the client still controls.
 * Vanilla clamps gamma to {@code 0.0 .. 1.0}, so the slider is expressed as a percentage.
 */
public final class FullbrightModule extends Module {
    private static final int DURATION_TICKS = 600;
    private static final int REFRESH_BELOW_TICKS = 400;

    private final SliderSetting brightness = addSetting(new SliderSetting("Brightness", 0.1F, 1.0F, 1.0F, true, 0.05F));

    private boolean applied;
    private double previousGamma = -1.0D;

    public FullbrightModule() {
        super("Fullbright", Category.RENDER, "Keeps the world fully lit with client-side Night Vision and a gamma slider.");
    }

    @Override
    public void onEnable() {
        this.applied = false;
        if (this.previousGamma < 0.0D) {
            this.previousGamma = mc.options.getGamma().getValue();
        }
    }

    @Override
    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        if (this.previousGamma < 0.0D) {
            this.previousGamma = client.options.getGamma().getValue();
        }
        client.options.getGamma().setValue((double) this.brightness.getValue());

        StatusEffectInstance existing = client.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (existing == null) {
            if (client.player.addStatusEffect(newStatusEffect())) {
                this.applied = true;
            }
            return;
        }

        // Keep our own effect topped up; never refresh an effect the player got elsewhere.
        if (this.applied && existing.getDuration() < REFRESH_BELOW_TICKS) {
            client.player.addStatusEffect(newStatusEffect());
        }
    }

    @Override
    public void onDisable() {
        restore();
    }

    @Override
    public void onDisconnect() {
        restore();
    }

    private static StatusEffectInstance newStatusEffect() {
        return new StatusEffectInstance(StatusEffects.NIGHT_VISION, DURATION_TICKS, 0, false, false, false);
    }

    private void restore() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (this.applied && client.player != null) {
            client.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
        this.applied = false;

        if (this.previousGamma >= 0.0D) {
            client.options.getGamma().setValue(this.previousGamma);
            this.previousGamma = -1.0D;
        }
    }
}
