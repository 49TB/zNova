package dev.znova.module.setting;

/** An on/off switch rendered as a neon pill in the ClickGUI. */
public final class BooleanSetting extends Setting {
    private final boolean defaultValue;
    private boolean value;

    public BooleanSetting(String name, boolean defaultValue) {
        super(name);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public boolean getValue() {
        return this.value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public void toggle() {
        this.value = !this.value;
    }

    public void reset() {
        this.value = this.defaultValue;
    }

    @Override
    public String getDisplayValue() {
        return this.value ? "ON" : "OFF";
    }
}
