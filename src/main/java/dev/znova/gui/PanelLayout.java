package dev.znova.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.znova.ZNovaClient;
import dev.znova.module.Category;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Remembers where the player dragged each ClickGUI category panel.
 * <p>
 * Positions live in a static map so reopening the GUI (or resizing the window) never
 * resets them, and they are mirrored into {@code config/znova-panels.json} so they also
 * survive a game restart. Read and write failures are logged and otherwise ignored: a
 * broken config file must never keep the GUI from opening.
 */
public final class PanelLayout {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<Category, float[]> POSITIONS = new EnumMap<>(Category.class);

    private static boolean loaded;

    private PanelLayout() {
    }

    /** Reads the saved positions once per game session. */
    public static void load() {
        if (loaded) {
            return;
        }
        loaded = true;

        Path path = path();
        if (!Files.exists(path)) {
            return;
        }

        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            for (Category category : Category.values()) {
                JsonElement element = root.get(key(category));
                if (element == null || !element.isJsonObject()) {
                    continue;
                }
                JsonObject entry = element.getAsJsonObject();
                if (!entry.has("x") || !entry.has("y")) {
                    continue;
                }
                POSITIONS.put(category, new float[]{entry.get("x").getAsFloat(), entry.get("y").getAsFloat()});
            }
        } catch (Exception exception) {
            ZNovaClient.LOGGER.warn("Could not read the zNova panel layout, using defaults", exception);
        }
    }

    /** Writes the current positions to disk. */
    public static void save() {
        JsonObject root = new JsonObject();
        for (Map.Entry<Category, float[]> entry : POSITIONS.entrySet()) {
            JsonObject position = new JsonObject();
            position.addProperty("x", entry.getValue()[0]);
            position.addProperty("y", entry.getValue()[1]);
            root.add(key(entry.getKey()), position);
        }

        try {
            Path path = path();
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            ZNovaClient.LOGGER.warn("Could not save the zNova panel layout", exception);
        }
    }

    public static boolean has(Category category) {
        return POSITIONS.containsKey(category);
    }

    public static float getX(Category category) {
        float[] position = POSITIONS.get(category);
        return position == null ? 0.0F : position[0];
    }

    public static float getY(Category category) {
        float[] position = POSITIONS.get(category);
        return position == null ? 0.0F : position[1];
    }

    public static void setPosition(Category category, float x, float y) {
        POSITIONS.put(category, new float[]{x, y});
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("znova-panels.json");
    }

    private static String key(Category category) {
        return category.name().toLowerCase(Locale.ROOT);
    }
}
