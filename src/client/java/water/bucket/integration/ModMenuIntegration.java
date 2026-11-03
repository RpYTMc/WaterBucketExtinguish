package water.bucket.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import water.bucket.config.ModConfigScreen;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // The settings screen needs Cloth Config, which is optional. Without it Mod Menu
        // gets its default factory (no screen) and shows no settings button. The lambda
        // keeps ModConfigScreen from being loaded unless Cloth Config is there.
        if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
            return ModMenuApi.super.getModConfigScreenFactory();
        }
        return parent -> ModConfigScreen.create(parent);
    }
}