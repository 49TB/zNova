package dev.znova.module.setting;

import java.util.List;

/** A settings entry that cycles through a fixed list of named modes. */
public final class ModeSetting extends Setting {
    private final List<String> modes;
    private final int defaultValue;
    private int index;

    public ModeSetting(String name, int defaultIndex, String... modes) {
        super(name);
        if (modes.length == 0) {
            throw new IllegalArgumentException("A mode setting needs at least one mode");
        }
        this.modes = List.of(modes);
        this.defaultValue = Math.floorMod(defaultIndex, modes.length);
        this.index = this.defaultValue;
    }

    public String getValue() {
        return this.modes.get(this.index);
    }

    public int getIndex() {
        return this.index;
    }

    public List<String> getModes() {
        return this.modes;
    }

    public void cycle() {
        this.index = (this.index + 1) % this.modes.size();
    }

    public void cycleBack() {
        this.index = Math.floorMod(this.index - 1, this.modes.size());
    }

    public void setIndex(int index) {
        this.index = Math.floorMod(index, this.modes.size());
    }

    public void reset() {
        this.index = this.defaultValue;
    }

    @Override
    public String getDisplayValue() {
        return getValue();
    }
}
