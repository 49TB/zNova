package dev.znova.util;

import net.minecraft.util.Util;

/**
 * A small, frame-rate independent animation value.
 * <p>
 * The current value eases towards {@link #setTarget(float) target} using exponential
 * smoothing, so transitions look identical at 60, 144 or 240 FPS.
 */
public final class Animation {
    private final float speed;
    private float value;
    private float target;
    private long lastUpdate;

    public Animation(float speed) {
        this(speed, 0.0F);
    }

    public Animation(float speed, float initialValue) {
        this.speed = speed;
        this.value = initialValue;
        this.target = initialValue;
        this.lastUpdate = Util.getMeasuringTimeMs();
    }

    public void setTarget(float target) {
        this.target = target;
    }

    /** Jumps to a value without animating (used for resets or click flashes). */
    public void snapTo(float value) {
        this.value = value;
        this.target = value;
        this.lastUpdate = Util.getMeasuringTimeMs();
    }

    public float getTarget() {
        return this.target;
    }

    /** Advances the animation to the current frame and returns the eased value. */
    public float get() {
        long now = Util.getMeasuringTimeMs();
        float deltaSeconds = Math.min((now - this.lastUpdate) / 1000.0F, 0.1F);
        this.lastUpdate = now;

        float factor = 1.0F - (float) Math.exp(-this.speed * deltaSeconds);
        this.value += (this.target - this.value) * factor;
        if (Math.abs(this.target - this.value) < 0.0005F) {
            this.value = this.target;
        }
        return this.value;
    }
}
