package water.bucket.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StatsManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE =
            FabricLoader.getInstance().getConfigDir().resolve("waterbucketstats.json");

    private static StatsData data = new StatsData();

    // ===== INIT =====
    public static void load() {
        try {
            if (Files.exists(FILE)) {
                String json = Files.readString(FILE);
                data = GSON.fromJson(json, StatsData.class);
            } else {
                save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===== SAVE =====
    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===== INCREMENT METHODS =====
    public static void incrementActivation() {
        data.totalActivations++;
        save();
    }

    public static void incrementFire() {
        data.fireExtinguishes++;
        save();
    }

    public static void incrementWeb() {
        data.webPlacements++;
        save();
    }

    public static void incrementPickup() {
        data.waterPickups++;
        save();
    }

    // ===== GETTERS =====
    public static StatsData getData() {
        return data;
    }

    // ===== RESET =====
    public static void reset() {
        data = new StatsData();
        save();
    }
}