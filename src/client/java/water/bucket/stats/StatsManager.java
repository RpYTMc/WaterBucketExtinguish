package water.bucket.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatsManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE =
            FabricLoader.getInstance().getConfigDir().resolve("waterbucketstats.json");
    private static final Logger LOGGER =
            LoggerFactory.getLogger("WaterBucketStats");
    public static int getTotalActivations() {
        return data.totalActivations;
    }

    private static StatsData data = new StatsData();
    private static boolean dirty = false;

    // ===== INIT =====
    public static void load() {
        try {
            if (Files.exists(FILE)) {
                String json = Files.readString(FILE);
                StatsData parsed = GSON.fromJson(json, StatsData.class);
                data = (parsed != null) ? parsed : new StatsData();
            } else {
                save();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load stats file", e);
            data = new StatsData();
        }
    }

    // ===== SAVE =====
    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(data));
        } catch (IOException e) {
            LOGGER.error("Failed to save stats file", e);
        }
    }

    // ===== INCREMENT METHODS =====
    // These only mark the data dirty; saveIfDirty() flushes to disk periodically
    // and on game close, so we don't write a file on every single action.
    public static void incrementActivation() {
        data.totalActivations++;
        dirty = true;
    }

    public static void incrementFire() {
        data.fireExtinguishes++;
        dirty = true;
    }

    public static void incrementWeb() {
        data.webPlacements++;
        dirty = true;
    }

    public static void incrementPickup() {
        data.waterPickups++;
        dirty = true;
    }

    public static void saveIfDirty() {
        if (dirty) {
            save();
            dirty = false;
        }
    }

    // ===== GETTERS =====
    public static StatsData getData() {
        return data;
    }

    // ===== RESET =====
    public static void reset() {
        data = new StatsData();
        save();
        dirty = false;
    }
}