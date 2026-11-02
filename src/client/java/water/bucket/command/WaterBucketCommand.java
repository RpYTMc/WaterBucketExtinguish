package water.bucket.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import water.bucket.config.ModConfig;
import water.bucket.stats.StatsData;
import water.bucket.stats.StatsManager;
import water.bucket.util.HotbarUtils;

public class WaterBucketCommand {

    private static final MutableComponent PREFIX =
            Component.literal("[WaterBucket] ").withStyle(ChatFormatting.GOLD);

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
                                    panel(ctx, Component.literal("Statistics reset.")
                                            .withStyle(ChatFormatting.YELLOW));
                                    return 1;
                                }))
                )

                .then(literal("bucket")
                        .executes(WaterBucketCommand::showBucketStatus))

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

        panel(ctx, Component.literal("Water Bucket Extinguish v" + version)
                .withStyle(ChatFormatting.GOLD));
        panel(ctx, Component.literal("Semi PvP-oriented Fabric utility mod.")
                .withStyle(ChatFormatting.GRAY));
        panel(ctx, Component.literal("Use /waterbucket help for commands.")
                .withStyle(ChatFormatting.DARK_GRAY));

        return 1;
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> ctx) {

        String version = FabricLoader.getInstance()
                .getModContainer("waterbucket")
                .orElseThrow()
                .getMetadata()
                .getVersion()
                .getFriendlyString();

        panel(ctx, Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━")
                .withStyle(ChatFormatting.DARK_GRAY));
        panel(ctx, Component.literal(" Water Bucket Extinguish v" + version)
                .withStyle(ChatFormatting.GOLD));
        panel(ctx, Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━")
                .withStyle(ChatFormatting.DARK_GRAY));

        panel(ctx, Component.literal("➤ Information")
                .withStyle(ChatFormatting.YELLOW));
        helpLine(ctx, "help", "Show this help menu");
        helpLine(ctx, "bucket", "Check bucket readiness");
        helpLine(ctx, "stats", "View usage statistics");

        panel(ctx, Component.literal(""));

        panel(ctx, Component.literal("➤ Configuration")
                .withStyle(ChatFormatting.AQUA));
        helpLine(ctx, "config pitch", "Change pitch threshold");
        helpLine(ctx, "config sound", "Toggle activation sound");
        helpLine(ctx, "config actionbar", "Toggle action bar message");
        helpLine(ctx, "config nether", "Toggle Nether restriction");
        helpLine(ctx, "config hud", "Toggle HUD display");
        helpLine(ctx, "config hudpos", "Change HUD position");

        panel(ctx, Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━")
                .withStyle(ChatFormatting.DARK_GRAY));

        return 1;
    }

    private static int showStats(CommandContext<FabricClientCommandSource> ctx) {

        StatsData data = StatsManager.getData();

        panel(ctx, Component.literal("=== Statistics ===")
                .withStyle(ChatFormatting.YELLOW));

        feedback(ctx, Component.literal("Total Activations: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(data.totalActivations))
                        .withStyle(ChatFormatting.AQUA)));

        feedback(ctx, Component.literal("Fire Extinguishes: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(data.fireExtinguishes))
                        .withStyle(ChatFormatting.RED)));

        feedback(ctx, Component.literal("Web Placements: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(data.webPlacements))
                        .withStyle(ChatFormatting.GREEN)));

        feedback(ctx, Component.literal("Water Pickups: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(data.waterPickups))
                        .withStyle(ChatFormatting.BLUE)));

        return 1;
    }

    private static int showBucketStatus(CommandContext<FabricClientCommandSource> ctx) {

        var player = ctx.getSource().getPlayer();

        int hotbarSlot = HotbarUtils.findBucketSlot(player);

        boolean hasHotbar = hotbarSlot != -1;
        boolean hasInventory = false;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET)) {
                hasInventory = true;
                break;
            }
        }

        if (hasHotbar) {
            panel(ctx, Component.literal("✔ Bucket Ready")
                    .withStyle(ChatFormatting.GREEN));
        } else if (hasInventory) {
            panel(ctx, Component.literal("⚠ Bucket in Inventory")
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            panel(ctx, Component.literal("✖ No Bucket Found")
                    .withStyle(ChatFormatting.RED));
        }

        return 1;
    }

    private static int showConfigHelp(CommandContext<FabricClientCommandSource> ctx) {

        panel(ctx, Component.literal("Config commands:")
                .withStyle(ChatFormatting.YELLOW));

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
                    feedback(ctx, Component.literal(name + ": ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.valueOf(getter.get()))
                                    .withStyle(getter.get() ? ChatFormatting.GREEN : ChatFormatting.RED)));
                    return 1;
                })
                .then(literal("reset")
                        .executes(ctx -> {
                            setter.accept(defaultValue);
                            ModConfig.INSTANCE.save();
                            feedback(ctx, Component.literal(name + " reset.")
                                    .withStyle(ChatFormatting.YELLOW));
                            return 1;
                        }))
                .then(argument("value", BoolArgumentType.bool())
                        .executes(ctx -> {
                            boolean value = BoolArgumentType.getBool(ctx, "value");
                            setter.accept(value);
                            ModConfig.INSTANCE.save();
                            feedback(ctx, Component.literal(name + " set to ")
                                    .append(Component.literal(String.valueOf(value))
                                            .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED)));
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
                    feedback(ctx, Component.literal(name + ": ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.valueOf(getter.get()))
                                    .withStyle(ChatFormatting.AQUA)));
                    return 1;
                })
                .then(literal("reset")
                        .executes(ctx -> {
                            setter.accept(defaultValue);
                            ModConfig.INSTANCE.save();
                            feedback(ctx, Component.literal(name + " reset.")
                                    .withStyle(ChatFormatting.YELLOW));
                            return 1;
                        }))
                .then(argument("value", IntegerArgumentType.integer(min, max))
                        .executes(ctx -> {
                            int value = IntegerArgumentType.getInteger(ctx, "value");
                            setter.accept(value);
                            ModConfig.INSTANCE.save();
                            feedback(ctx, Component.literal(name + " set to ")
                                    .append(Component.literal(String.valueOf(value))
                                            .withStyle(ChatFormatting.AQUA)));
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> hudPosSetting() {

        return literal("hudpos")
                .executes(ctx -> {
                    feedback(ctx, Component.literal("HUD Position: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(ModConfig.INSTANCE.hudPosition.name())
                                    .withStyle(ChatFormatting.AQUA)));
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
                            ModConfig.HudPosition position;
                            try {
                                position = ModConfig.HudPosition.valueOf(input);
                            } catch (IllegalArgumentException e) {
                                feedback(ctx, Component.literal("Unknown position. Use top_left, top_right, bottom_left or bottom_right.")
                                        .withStyle(ChatFormatting.RED));
                                return 0;
                            }
                            ModConfig.INSTANCE.hudPosition = position;
                            ModConfig.INSTANCE.save();

                            feedback(ctx, Component.literal("HUD Position set to ")
                                    .append(Component.literal(input)
                                            .withStyle(ChatFormatting.AQUA)));
                            return 1;
                        }))
                .then(literal("reset")
                        .executes(ctx -> {
                            ModConfig.INSTANCE.hudPosition = ModConfig.HudPosition.TOP_LEFT;
                            ModConfig.INSTANCE.save();
                            feedback(ctx, Component.literal("HUD Position reset.")
                                    .withStyle(ChatFormatting.YELLOW));
                            return 1;
                        }));
    }

    // =========================================================
    // OUTPUT HELPERS
    // =========================================================

    private static void feedback(CommandContext<FabricClientCommandSource> ctx, Component message) {
        ctx.getSource().sendFeedback(PREFIX.copy().append(message));
    }

    private static void panel(CommandContext<FabricClientCommandSource> ctx, Component message) {
        ctx.getSource().sendFeedback(message);
    }

    private static void helpLine(CommandContext<FabricClientCommandSource> ctx,
                                 String command,
                                 String description) {

        panel(ctx,
                Component.literal("  /waterbucket ")
                        .withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(command).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" - " + description)
                                .withStyle(ChatFormatting.GRAY)));
    }
}