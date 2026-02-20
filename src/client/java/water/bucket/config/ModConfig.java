package water.bucket.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("waterbucketconfig.json");

    public static ModConfig INSTANCE = load();

    public int pitchThreshold = 67;
    public boolean enableSound = true;
    public boolean enableActionBar = true;
    public boolean disableInNether = true;

    public static final int DEFAULT_PITCH = 67;
    public static final boolean DEFAULT_SOUND = true;
    public static final boolean DEFAULT_ACTIONBAR = true;
    public static final boolean DEFAULT_DISABLE_NETHER = true;

    public static ModConfig load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                return GSON.fromJson(Files.readString(CONFIG_PATH), ModConfig.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ModConfig();
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetAll() {
        pitchThreshold = DEFAULT_PITCH;
        enableSound = DEFAULT_SOUND;
        enableActionBar = DEFAULT_ACTIONBAR;
        disableInNether = DEFAULT_DISABLE_NETHER;
        save();
    }

    public void reload() {
        load(); // if you already have load()
    }
}
