package dev.znova.util;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/**
 * Human readable names for GLFW key codes.
 * <p>
 * The names are built from the GLFW constants directly so the display never depends on
 * the user's language or on input API internals, and a keybind always shows something
 * meaningful in the ClickGUI and in the array list.
 */
public final class KeyNames {
    public static final int NONE = GLFW.GLFW_KEY_UNKNOWN;

    private static final Map<Integer, String> NAMES = new HashMap<>();

    static {
        for (int i = 0; i < 26; i++) {
            NAMES.put(GLFW.GLFW_KEY_A + i, String.valueOf((char) ('A' + i)));
        }
        for (int i = 0; i < 10; i++) {
            NAMES.put(GLFW.GLFW_KEY_0 + i, String.valueOf((char) ('0' + i)));
        }
        for (int i = 0; i < 25; i++) {
            NAMES.put(GLFW.GLFW_KEY_F1 + i, "F" + (i + 1));
        }
        for (int i = 0; i < 10; i++) {
            NAMES.put(GLFW.GLFW_KEY_KP_0 + i, "NUM" + i);
        }
        for (int i = 0; i < 8; i++) {
            NAMES.put(GLFW.GLFW_KEY_KP_1 + i, "NUM" + (i + 1));
        }

        NAMES.put(GLFW.GLFW_KEY_SPACE, "SPACE");
        NAMES.put(GLFW.GLFW_KEY_TAB, "TAB");
        NAMES.put(GLFW.GLFW_KEY_ENTER, "ENTER");
        NAMES.put(GLFW.GLFW_KEY_BACKSPACE, "BACKSPACE");
        NAMES.put(GLFW.GLFW_KEY_INSERT, "INSERT");
        NAMES.put(GLFW.GLFW_KEY_DELETE, "DELETE");
        NAMES.put(GLFW.GLFW_KEY_HOME, "HOME");
        NAMES.put(GLFW.GLFW_KEY_END, "END");
        NAMES.put(GLFW.GLFW_KEY_PAGE_UP, "PGUP");
        NAMES.put(GLFW.GLFW_KEY_PAGE_DOWN, "PGDN");
        NAMES.put(GLFW.GLFW_KEY_UP, "UP");
        NAMES.put(GLFW.GLFW_KEY_DOWN, "DOWN");
        NAMES.put(GLFW.GLFW_KEY_LEFT, "LEFT");
        NAMES.put(GLFW.GLFW_KEY_RIGHT, "RIGHT");
        NAMES.put(GLFW.GLFW_KEY_LEFT_SHIFT, "LSHIFT");
        NAMES.put(GLFW.GLFW_KEY_RIGHT_SHIFT, "RSHIFT");
        NAMES.put(GLFW.GLFW_KEY_LEFT_CONTROL, "LCTRL");
        NAMES.put(GLFW.GLFW_KEY_RIGHT_CONTROL, "RCTRL");
        NAMES.put(GLFW.GLFW_KEY_LEFT_ALT, "LALT");
        NAMES.put(GLFW.GLFW_KEY_RIGHT_ALT, "RALT");
        NAMES.put(GLFW.GLFW_KEY_LEFT_SUPER, "LWIN");
        NAMES.put(GLFW.GLFW_KEY_RIGHT_SUPER, "RWIN");
        NAMES.put(GLFW.GLFW_KEY_MENU, "MENU");
        NAMES.put(GLFW.GLFW_KEY_CAPS_LOCK, "CAPS");
        NAMES.put(GLFW.GLFW_KEY_NUM_LOCK, "NUMLOCK");
        NAMES.put(GLFW.GLFW_KEY_SCROLL_LOCK, "SCROLL");
        NAMES.put(GLFW.GLFW_KEY_PAUSE, "PAUSE");
        NAMES.put(GLFW.GLFW_KEY_PRINT_SCREEN, "PRTSC");
        NAMES.put(GLFW.GLFW_KEY_MINUS, "-");
        NAMES.put(GLFW.GLFW_KEY_EQUAL, "=");
        NAMES.put(GLFW.GLFW_KEY_LEFT_BRACKET, "[");
        NAMES.put(GLFW.GLFW_KEY_RIGHT_BRACKET, "]");
        NAMES.put(GLFW.GLFW_KEY_BACKSLASH, "\\");
        NAMES.put(GLFW.GLFW_KEY_SEMICOLON, ";");
        NAMES.put(GLFW.GLFW_KEY_APOSTROPHE, "'");
        NAMES.put(GLFW.GLFW_KEY_GRAVE_ACCENT, "`");
        NAMES.put(GLFW.GLFW_KEY_COMMA, ",");
        NAMES.put(GLFW.GLFW_KEY_PERIOD, ".");
        NAMES.put(GLFW.GLFW_KEY_SLASH, "/");
        NAMES.put(GLFW.GLFW_KEY_KP_ENTER, "NUMENTER");
        NAMES.put(GLFW.GLFW_KEY_KP_ADD, "NUM+");
        NAMES.put(GLFW.GLFW_KEY_KP_SUBTRACT, "NUM-");
        NAMES.put(GLFW.GLFW_KEY_KP_MULTIPLY, "NUM*");
        NAMES.put(GLFW.GLFW_KEY_KP_DIVIDE, "NUM/");
        NAMES.put(GLFW.GLFW_KEY_KP_DECIMAL, "NUM.");
        NAMES.put(GLFW.GLFW_KEY_KP_EQUAL, "NUM=");
    }

    private KeyNames() {
    }

    /** @return a short display name such as {@code "F"}, or {@code "NONE"} when unbound */
    public static String getName(int keyCode) {
        if (keyCode == NONE || keyCode < 0) {
            return "NONE";
        }
        String name = NAMES.get(keyCode);
        if (name != null) {
            return name;
        }
        return "KEY " + keyCode;
    }
}
