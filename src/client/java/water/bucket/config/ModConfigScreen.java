package water.bucket.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModConfigScreen {

    public static Screen create(Screen parent) {

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Water Bucket Extinguish Settings"));

        builder.setSavingRunnable(() -> {
            // For now nothing, later we save to file
            ModConfig.INSTANCE.save();
        });

        ConfigCategory general = builder.getOrCreateCategory(Component.literal("Water Bucket Extinguish"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        var behaviorSub = entryBuilder.startSubCategory(
                Component.literal("Behavior")
        );

        behaviorSub.setExpanded(false);
        behaviorSub.setTooltip(Component.literal("Core activation behavior settings."));

        behaviorSub.add(
                entryBuilder.startIntSlider(
                                Component.literal("Pitch Threshold"),
                                ModConfig.INSTANCE.pitchThreshold,
                                0,
                                90
                        )
                        .setDefaultValue(67)
                        .setTooltip(
                                Component.literal("Minimum downward angle required, cobwebs bypass this.")
                        )
                        .setSaveConsumer(value -> ModConfig.INSTANCE.pitchThreshold = value)
                        .build()
        );

        general.addEntry(behaviorSub.build());

        var feedbackSub = entryBuilder.startSubCategory(
                Component.literal("Feedback")
        );

        feedbackSub.setExpanded(false);
        feedbackSub.setTooltip(Component.literal("Visual and audio feedback options."));

        feedbackSub.add(
                entryBuilder.startBooleanToggle(
                                Component.literal("Enable Sound"),
                                ModConfig.INSTANCE.enableSound
                        )
                        .setDefaultValue(true)
                        .setTooltip(Component.literal("Play sound when the mod activates double press."))
                        .setSaveConsumer(value -> ModConfig.INSTANCE.enableSound = value)
                        .build()
        );

        feedbackSub.add(
                entryBuilder.startBooleanToggle(
                                Component.literal("Enable Action Bar"),
                                ModConfig.INSTANCE.enableActionBar
                        )
                        .setDefaultValue(true)
                        .setTooltip(Component.literal("Display a 'Poof' in the action bar when extinguishing fire."))
                        .setSaveConsumer(value -> ModConfig.INSTANCE.enableActionBar = value)
                        .build()
        );

        general.addEntry(feedbackSub.build());

        var environmentSub = entryBuilder.startSubCategory(
                Component.literal("Environment")
        );

        environmentSub.setExpanded(false);
        environmentSub.setTooltip(
                Component.literal("Dimension-based restrictions.")
        );

        environmentSub.add(
                entryBuilder.startBooleanToggle(
                                Component.literal("Disable in Nether"),
                                ModConfig.INSTANCE.disableInNether
                        )
                        .setDefaultValue(true)
                        .setTooltip(
                                Component.literal("Prevents activation in the Nether.")
                        )
                        .setSaveConsumer(value ->
                                ModConfig.INSTANCE.disableInNether = value
                        )
                        .build()
        );

        general.addEntry(environmentSub.build());

        var hudSub = entryBuilder.startSubCategory(
                Component.literal("HUD")
        );

        hudSub.setExpanded(false);
        hudSub.setTooltip(Component.literal("Heads-up display settings."));

// Enable HUD toggle
        hudSub.add(
                entryBuilder.startBooleanToggle(
                                Component.literal("Enable HUD"),
                                ModConfig.INSTANCE.enableHud
                        )
                        .setDefaultValue(true)
                        .setTooltip(Component.literal("Show the bucket status and action feedback on screen."))
                        .setSaveConsumer(value ->
                                ModConfig.INSTANCE.enableHud = value
                        )
                        .build()
        );

// HUD Position selector
        hudSub.add(
                entryBuilder.startEnumSelector(
                                Component.literal("HUD Position"),
                                ModConfig.HudPosition.class,
                                ModConfig.INSTANCE.hudPosition
                        )
                        .setDefaultValue(ModConfig.HudPosition.TOP_LEFT)
                        .setTooltip(Component.literal("Change where the HUD appears on screen."))
                        .setSaveConsumer(value ->
                                ModConfig.INSTANCE.hudPosition = value
                        )
                        .build()
        );

        general.addEntry(hudSub.build());

        return builder.build();
    }
}