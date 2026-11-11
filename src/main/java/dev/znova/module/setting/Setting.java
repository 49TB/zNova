package dev.znova.module.setting;

/** Base type for every configurable module option shown in the ClickGUI. */
public abstract class Setting {
    private final String name;

    protected Setting(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    /** Text shown on the right hand side of the setting row. */
    public abstract String getDisplayValue();
}
