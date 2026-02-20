package water.bucket.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;


import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModConfigScreen {

    public static Screen create(Screen parent) {

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Water Bucket Extinguish Settings"));

        builder.setSavingRunnable(() -> {
            // For now nothing, later we save to file
            ModConfig.INSTANCE.save();
        });

        ConfigCategory general = builder.getOrCreateCategory(Text.literal("Water Bucket Extinguish"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        var behaviorSub = entryBuilder.startSubCategory(
                Text.literal("Behavior")
        );

        behaviorSub.setExpanded(false);
        behaviorSub.setTooltip(Text.literal("Core activation behavior settings."));

        behaviorSub.add(
                entryBuilder.startIntSlider(
                                Text.literal("Pitch Threshold"),
                                ModConfig.INSTANCE.pitchThreshold,
                                0,
                                90
                        )
                        .setDefaultValue(67)
                        .setTooltip(
                                Text.literal("Minimum downward angle required, cobwebs bypass this.")
                        )
                        .setSaveConsumer(value -> ModConfig.INSTANCE.pitchThreshold = value)
                        .build()
        );

        general.addEntry(behaviorSub.build());

        var feedbackSub = entryBuilder.startSubCategory(
                Text.literal("Feedback")
        );

        feedbackSub.setExpanded(false);
        feedbackSub.setTooltip(Text.literal("Visual and audio feedback options."));

        feedbackSub.add(
                entryBuilder.startBooleanToggle(
                                Text.literal("Enable Sound"),
                                ModConfig.INSTANCE.enableSound
                        )
                        .setDefaultValue(true)
                        .setTooltip(Text.literal("Play sound when the mod activates double press."))
                        .setSaveConsumer(value -> ModConfig.INSTANCE.enableSound = value)
                        .build()
        );

        feedbackSub.add(
                entryBuilder.startBooleanToggle(
                                Text.literal("Enable Action Bar"),
                                ModConfig.INSTANCE.enableActionBar
                        )
                        .setDefaultValue(true)
                        .setTooltip(Text.literal("Display a 'Poof' in the action bar when extinguishing fire."))
                        .setSaveConsumer(value -> ModConfig.INSTANCE.enableActionBar = value)
                        .build()
        );

        general.addEntry(feedbackSub.build());

        var environmentSub = entryBuilder.startSubCategory(
                Text.literal("Environment")
        );

        environmentSub.setExpanded(false);
        environmentSub.setTooltip(
                Text.literal("Dimension-based restrictions.")
        );

        environmentSub.add(
                entryBuilder.startBooleanToggle(
                                Text.literal("Disable in Nether"),
                                ModConfig.INSTANCE.disableInNether
                        )
                        .setDefaultValue(true)
                        .setTooltip(
                                Text.literal("Prevents activation in the Nether.")
                        )
                        .setSaveConsumer(value ->
                                ModConfig.INSTANCE.disableInNether = value
                        )
                        .build()
        );

        general.addEntry(environmentSub.build());

        return builder.build();
    }
}