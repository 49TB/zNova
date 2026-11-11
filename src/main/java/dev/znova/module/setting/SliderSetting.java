package dev.znova.module.setting;

import net.minecraft.util.math.MathHelper;

import java.util.Locale;

/**
 * A continuous value edited with a draggable horizontal slider.
 * <p>
 * Values are stored as floats and exposed either as a percentage ({@code 10% .. 100%})
 * or as a fixed-point number with two decimals.
 */
public final class SliderSetting extends Setting {
    private final float min;
    private final float max;
    private final float defaultValue;
    private final boolean percent;
    private final float step;
    private float value;

    public SliderSetting(String name, float min, float max, float defaultValue, boolean percent) {
        this(name, min, max, defaultValue, percent, 0.0F);
    }

    /**
     * @param step when greater than zero the value snaps to multiples of this step
     */
    public SliderSetting(String name, float min, float max, float defaultValue, boolean percent, float step) {
        super(name);
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
        this.defaultValue = MathHelper.clamp(defaultValue, this.min, this.max);
        this.percent = percent;
        this.step = Math.max(0.0F, step);
        this.value = this.defaultValue;
    }

    public float getValue() {
        return this.value;
    }

    public void setValue(float value) {
        float clamped = MathHelper.clamp(value, this.min, this.max);
        if (this.step > 0.0F) {
            clamped = this.min + Math.round((clamped - this.min) / this.step) * this.step;
            clamped = MathHelper.clamp(clamped, this.min, this.max);
        }
        this.value = clamped;
    }

    public float getMin() {
        return this.min;
    }

    public float getMax() {
        return this.max;
    }

    /** Position of the current value inside the range, from 0 to 1. */
    public float getRatio() {
        if (this.max - this.min <= 0.0F) {
            return 0.0F;
        }
        return MathHelper.clamp((this.value - this.min) / (this.max - this.min), 0.0F, 1.0F);
    }

    /** Sets the value from a 0..1 slider position. */
    public void setFromRatio(float ratio) {
        setValue(this.min + MathHelper.clamp(ratio, 0.0F, 1.0F) * (this.max - this.min));
    }

    public void reset() {
        this.value = this.defaultValue;
    }

    @Override
    public String getDisplayValue() {
        if (this.percent) {
            return Math.round(this.value * 100.0F) + "%";
        }
        return String.format(Locale.ROOT, "%.2f", this.value);
    }
}
