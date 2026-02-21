package water.bucket.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import water.bucket.config.ModConfig;
import water.bucket.stats.StatsData;
import water.bucket.stats.StatsManager;
import water.bucket.util.HotbarUtils;

public class WaterBucketCommand {

    private static final MutableText PREFIX =
            Text.literal("[WaterBucket] ").formatted(Formatting.GOLD);

    // =========================================================
    // REGISTER
    // =========================================================

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {

        dispatcher.register(literal("waterbucket")

                .executes(WaterBucketCommand::showInfo)

                .then(literal("help")
                        .executes(WaterBucketCommand::showHelp))

                .then(literal("stats")
                        .executes(WaterBucketCommand::showStats)
                        .then(literal("reset")
                                .executes(ctx -> {
                                    StatsManager.reset();
                                    panel(ctx, Text.literal("Statistics reset.")
                                            .formatted(Formatting.YELLOW));
                                    return 1;
                                }))
                )

                .then(literal("bucket")
                        .executes(WaterBucketCommand::showBucketStatus))

                .then(literal("reload")
                        .executes(ctx -> {
                            ModConfig.INSTANCE.reload();
                            feedback(ctx, Text.literal("Config reloaded.")
                                    .formatted(Formatting.GREEN));
                            return 1;
                        }))

                .then(literal("config")
                        .executes(WaterBucketCommand::showConfigHelp)

                        .then(intSetting("pitch",
                                () -> ModConfig.INSTANCE.pitchThreshold,
                                v -> ModConfig.INSTANCE.pitchThreshold = v,
                                ModConfig.DEFAULT_PITCH,
                                0, 90))

                        .then(boolSetting("sound",
                                () -> ModConfig.INSTANCE.enableSound,
                                v -> ModConfig.INSTANCE.enableSound = v,
                                ModConfig.DEFAULT_SOUND))

                        .then(boolSetting("actionbar",
                                () -> ModConfig.INSTANCE.enableActionBar,
                                v -> ModConfig.INSTANCE.enableActionBar = v,
                                ModConfig.DEFAULT_ACTIONBAR))

                        .then(boolSetting("nether",
                                () -> ModConfig.INSTANCE.disableInNether,
                                v -> ModConfig.INSTANCE.disableInNether = v,
                                ModConfig.DEFAULT_DISABLE_NETHER))

                        .then(boolSetting("hud",
                                () -> ModConfig.INSTANCE.enableHud,
                                v -> ModConfig.INSTANCE.enableHud = v,
                                true))

                        .then(hudPosSetting())
                )
        );
    }

    // =========================================================
    // INFO / HELP / STATS
    // =========================================================

    private static int showInfo(CommandContext<FabricClientCommandSource> ctx) {

        String version = FabricLoader.getInstance()
                .getModContainer("waterbucket")
                .orElseThrow()
                .getMetadata()
                .getVersion()
                .getFriendlyString();

        panel(ctx, Text.literal("Water Bucket Extinguish v" + version)
                .formatted(Formatting.GOLD));
        panel(ctx, Text.literal("Semi PvP-oriented Fabric utility mod.")
                .formatted(Formatting.GRAY));
        panel(ctx, Text.literal("Use /waterbucket help for commands.")
                .formatted(Formatting.DARK_GRAY));

        return 1;
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> ctx) {

        String version = FabricLoader.getInstance()
                .getModContainer("waterbucket")
                .orElseThrow()
                .getMetadata()
                .getVersion()
                .getFriendlyString();

        panel(ctx, Text.literal("━━━━━━━━━━━━━━━━━━━━━━━━")
                .formatted(Formatting.DARK_GRAY));
        panel(ctx, Text.literal(" Water Bucket Extinguish v" + version)
                .formatted(Formatting.GOLD));
        panel(ctx, Text.literal("━━━━━━━━━━━━━━━━━━━━━━━━")
                .formatted(Formatting.DARK_GRAY));

        panel(ctx, Text.literal("➤ Information")
                .formatted(Formatting.YELLOW));
        helpLine(ctx, "help", "Show this help menu");
        helpLine(ctx, "bucket", "Check bucket readiness");
        helpLine(ctx, "stats", "View usage statistics");

        panel(ctx, Text.literal(""));

        panel(ctx, Text.literal("➤ Configuration")
                .formatted(Formatting.AQUA));
        helpLine(ctx, "config pitch", "Change pitch threshold");
        helpLine(ctx, "config sound", "Toggle activation sound");
        helpLine(ctx, "config actionbar", "Toggle action bar message");
        helpLine(ctx, "config nether", "Toggle Nether restriction");
        helpLine(ctx, "config hud", "Toggle HUD display");
        helpLine(ctx, "config hudpos", "Change HUD position");

        panel(ctx, Text.literal(""));

        panel(ctx, Text.literal("➤ Utility")
                .formatted(Formatting.GREEN));
        helpLine(ctx, "reload", "Reload configuration file");

        panel(ctx, Text.literal("━━━━━━━━━━━━━━━━━━━━━━━━")
                .formatted(Formatting.DARK_GRAY));

        return 1;
    }

    private static int showStats(CommandContext<FabricClientCommandSource> ctx) {

        StatsData data = StatsManager.getData();

        panel(ctx, Text.literal("=== Statistics ===")
                .formatted(Formatting.YELLOW));

        feedback(ctx, Text.literal("Total Activations: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal(String.valueOf(data.totalActivations))
                        .formatted(Formatting.AQUA)));

        feedback(ctx, Text.literal("Fire Extinguishes: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal(String.valueOf(data.fireExtinguishes))
                        .formatted(Formatting.RED)));

        feedback(ctx, Text.literal("Web Placements: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal(String.valueOf(data.webPlacements))
                        .formatted(Formatting.GREEN)));

        feedback(ctx, Text.literal("Water Pickups: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal(String.valueOf(data.waterPickups))
                        .formatted(Formatting.BLUE)));

        return 1;
    }

    private static int showBucketStatus(CommandContext<FabricClientCommandSource> ctx) {

        var player = ctx.getSource().getPlayer();

        int hotbarSlot = HotbarUtils.findBucketSlot(player);

        boolean hasHotbar = hotbarSlot != -1;
        boolean hasInventory = false;

        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isOf(Items.WATER_BUCKET)) {
                hasInventory = true;
                break;
            }
        }

        if (hasHotbar) {
            panel(ctx, Text.literal("✔ Bucket Ready")
                    .formatted(Formatting.GREEN));
        } else if (hasInventory) {
            panel(ctx, Text.literal("⚠ Bucket in Inventory")
                    .formatted(Formatting.YELLOW));
        } else {
            panel(ctx, Text.literal("✖ No Bucket Found")
                    .formatted(Formatting.RED));
        }

        return 1;
    }

    private static int showConfigHelp(CommandContext<FabricClientCommandSource> ctx) {

        panel(ctx, Text.literal("Config commands:")
                .formatted(Formatting.YELLOW));

        helpLine(ctx, "config pitch", "Change pitch threshold");
        helpLine(ctx, "config sound", "Toggle sound");
        helpLine(ctx, "config actionbar", "Toggle action bar");
        helpLine(ctx, "config nether", "Toggle Nether restriction");
        helpLine(ctx, "config hud", "Toggle HUD");
        helpLine(ctx, "config hudpos", "Change HUD position");

        return 1;
    }

    // =========================================================
    // SETTING BUILDERS
    // =========================================================

    private static LiteralArgumentBuilder<FabricClientCommandSource> boolSetting(
            String name,
            Supplier<Boolean> getter,
            Consumer<Boolean> setter,
            boolean defaultValue) {

        return literal(name)
                .executes(ctx -> {
                    feedback(ctx, Text.literal(name + ": ")
                            .formatted(Formatting.GRAY)
                            .append(Text.literal(String.valueOf(getter.get()))
                                    .formatted(getter.get() ? Formatting.GREEN : Formatting.RED)));
                    return 1;
                })
                .then(literal("reset")
                        .executes(ctx -> {
                            setter.accept(defaultValue);
                            ModConfig.INSTANCE.save();
                            feedback(ctx, Text.literal(name + " reset.")
                                    .formatted(Formatting.YELLOW));
                            return 1;
                        }))
                .then(argument("value", BoolArgumentType.bool())
                        .executes(ctx -> {
                            boolean value = BoolArgumentType.getBool(ctx, "value");
                            setter.accept(value);
                            ModConfig.INSTANCE.save();
                            feedback(ctx, Text.literal(name + " set to ")
                                    .append(Text.literal(String.valueOf(value))
                                            .formatted(value ? Formatting.GREEN : Formatting.RED)));
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> intSetting(
            String name,
            Supplier<Integer> getter,
            Consumer<Integer> setter,
            int defaultValue,
            int min, int max) {

        return literal(name)
                .executes(ctx -> {
                    feedback(ctx, Text.literal(name + ": ")
                            .formatted(Formatting.GRAY)
                            .append(Text.literal(String.valueOf(getter.get()))
                                    .formatted(Formatting.AQUA)));
                    return 1;
                })
                .then(literal("reset")
                        .executes(ctx -> {
                            setter.accept(defaultValue);
                            ModConfig.INSTANCE.save();
                            feedback(ctx, Text.literal(name + " reset.")
                                    .formatted(Formatting.YELLOW));
                            return 1;
                        }))
                .then(argument("value", IntegerArgumentType.integer(min, max))
                        .executes(ctx -> {
                            int value = IntegerArgumentType.getInteger(ctx, "value");
                            setter.accept(value);
                            ModConfig.INSTANCE.save();
                            feedback(ctx, Text.literal(name + " set to ")
                                    .append(Text.literal(String.valueOf(value))
                                            .formatted(Formatting.AQUA)));
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> hudPosSetting() {

        return literal("hudpos")
                .executes(ctx -> {
                    feedback(ctx, Text.literal("HUD Position: ")
                            .formatted(Formatting.GRAY)
                            .append(Text.literal(ModConfig.INSTANCE.hudPosition.name())
                                    .formatted(Formatting.AQUA)));
                    return 1;
                })
                .then(argument("value", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("top_left");
                            builder.suggest("top_right");
                            builder.suggest("bottom_left");
                            builder.suggest("bottom_right");
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String input = StringArgumentType.getString(ctx, "value").toUpperCase();
                            ModConfig.INSTANCE.hudPosition =
                                    ModConfig.HudPosition.valueOf(input);
                            ModConfig.INSTANCE.save();

                            feedback(ctx, Text.literal("HUD Position set to ")
                                    .append(Text.literal(input)
                                            .formatted(Formatting.AQUA)));
                            return 1;
                        }))
                .then(literal("reset")
                        .executes(ctx -> {
                            ModConfig.INSTANCE.hudPosition = ModConfig.HudPosition.TOP_LEFT;
                            ModConfig.INSTANCE.save();
                            feedback(ctx, Text.literal("HUD Position reset.")
                                    .formatted(Formatting.YELLOW));
                            return 1;
                        }));
    }

    // =========================================================
    // OUTPUT HELPERS
    // =========================================================

    private static void feedback(CommandContext<FabricClientCommandSource> ctx, Text message) {
        ctx.getSource().sendFeedback(PREFIX.copy().append(message));
    }

    private static void panel(CommandContext<FabricClientCommandSource> ctx, Text message) {
        ctx.getSource().sendFeedback(message);
    }

    private static void helpLine(CommandContext<FabricClientCommandSource> ctx,
                                 String command,
                                 String description) {

        panel(ctx,
                Text.literal("  /waterbucket ")
                        .formatted(Formatting.DARK_GRAY)
                        .append(Text.literal(command).formatted(Formatting.AQUA))
                        .append(Text.literal(" - " + description)
                                .formatted(Formatting.GRAY)));
    }
}