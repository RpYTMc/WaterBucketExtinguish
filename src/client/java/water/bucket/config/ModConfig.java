package water.bucket.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {

    public enum HudPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    public static final int DEFAULT_PITCH = 67;
    public static final boolean DEFAULT_SOUND = true;
    public static final boolean DEFAULT_ACTIONBAR = true;
    public static final boolean DEFAULT_DISABLE_NETHER = true;
    public static final boolean DEFAULT_HUD = true;
    public static final HudPosition DEFAULT_HUD_POSITION = HudPosition.TOP_LEFT;

    private static final Logger LOGGER = LoggerFactory.getLogger("WaterBucketConfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("waterbucketconfig.json");

    public int pitchThreshold = DEFAULT_PITCH;
    public boolean enableSound = DEFAULT_SOUND;
    public boolean enableActionBar = DEFAULT_ACTIONBAR;
    public boolean disableInNether = DEFAULT_DISABLE_NETHER;
    public boolean enableHud = DEFAULT_HUD;
    public HudPosition hudPosition = DEFAULT_HUD_POSITION;

    // Must stay below the static fields it depends on (static init order).
    public static ModConfig INSTANCE = load();

    public static ModConfig load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                ModConfig loaded = GSON.fromJson(Files.readString(CONFIG_PATH), ModConfig.class);
                if (loaded != null) {
                    // Gson leaves an unknown enum value as null; fall back instead of crashing later.
                    if (loaded.hudPosition == null) loaded.hudPosition = DEFAULT_HUD_POSITION;
                    return loaded;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Could not read {}, using defaults", CONFIG_PATH, e);
        }
        return new ModConfig();
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            LOGGER.error("Could not write {}", CONFIG_PATH, e);
        }
    }

    public void resetAll() {
        pitchThreshold = DEFAULT_PITCH;
        enableSound = DEFAULT_SOUND;
        enableActionBar = DEFAULT_ACTIONBAR;
        disableInNether = DEFAULT_DISABLE_NETHER;
        enableHud = DEFAULT_HUD;
        hudPosition = DEFAULT_HUD_POSITION;
        save();
    }
}
