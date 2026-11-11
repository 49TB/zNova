package dev.znova.module;

import dev.znova.util.ColorUtils;

/** ClickGUI categories, in panel order. */
public enum Category {
    MISC("Misc", ColorUtils.VIOLET),
    RENDER("Render", ColorUtils.CYAN),
    QOL("QoL", 0x7CFFB2);

    private final String displayName;
    private final int accentColor;

    Category(String displayName, int accentColor) {
        this.displayName = displayName;
        this.accentColor = accentColor;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    /** Accent colour as 0xRRGGBB (no alpha). */
    public int getAccentColor() {
        return this.accentColor;
    }
}
