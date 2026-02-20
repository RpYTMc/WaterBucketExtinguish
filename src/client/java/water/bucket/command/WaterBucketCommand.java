package water.bucket.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import water.bucket.config.ModConfig;

public class WaterBucketCommand {

    private static final MutableText PREFIX =
            Text.literal("[WaterBucket] ")
                    .formatted(Formatting.GOLD);

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {

        dispatcher.register(literal("waterbucket")

                // ===== HELP =====
                .executes(ctx -> {
                    send(ctx, Text.literal("Available commands:")
                            .formatted(Formatting.YELLOW));

                    sendHelp(ctx, "pitch [value|reset]", "View or change pitch threshold");
                    sendHelp(ctx, "sound [true|false|reset]", "Toggle activation sound");
                    sendHelp(ctx, "actionbar [true|false|reset]", "Toggle action bar message");
                    sendHelp(ctx, "nether [true|false|reset]", "Toggle Nether restriction");
                    sendHelp(ctx, "reload", "Reload config from file");
                    sendHelp(ctx, "reset", "Reset ALL settings to default");

                    return 1;
                })

                // ===== RELOAD =====
                .then(literal("reload")
                        .executes(ctx -> {
                            ModConfig.INSTANCE.reload();
                            send(ctx, Text.literal("Config reloaded.")
                                    .formatted(Formatting.GREEN));
                            return 1;
                        })
                )

                // ===== FULL RESET =====
                .then(literal("reset")
                        .executes(ctx -> {
                            ModConfig.INSTANCE.resetAll();
                            send(ctx, Text.literal("All settings reset to defaults.")
                                    .formatted(Formatting.YELLOW));
                            return 1;
                        })
                )

                // ===== PITCH =====
                .then(literal("pitch")
                        .executes(ctx -> {
                            send(ctx,
                                    Text.literal("Pitch threshold: ")
                                            .formatted(Formatting.GRAY)
                                            .append(Text.literal(String.valueOf(ModConfig.INSTANCE.pitchThreshold))
                                                    .formatted(Formatting.AQUA))
                            );
                            return 1;
                        })
                        .then(literal("reset")
                                .executes(ctx -> {
                                    if (ModConfig.INSTANCE.pitchThreshold == ModConfig.DEFAULT_PITCH) {
                                        send(ctx, Text.literal("Pitch already default.")
                                                .formatted(Formatting.DARK_GRAY));
                                        return 1;
                                    }

                                    ModConfig.INSTANCE.pitchThreshold = ModConfig.DEFAULT_PITCH;
                                    ModConfig.INSTANCE.save();

                                    send(ctx, Text.literal("Pitch reset to default.")
                                            .formatted(Formatting.YELLOW));
                                    return 1;
                                })
                        )
                        .then(argument("value", IntegerArgumentType.integer(0, 90))
                                .executes(ctx -> {
                                    int value = IntegerArgumentType.getInteger(ctx, "value");

                                    if (ModConfig.INSTANCE.pitchThreshold == value) {
                                        send(ctx,
                                                Text.literal("Pitch already ")
                                                        .formatted(Formatting.GRAY)
                                                        .append(Text.literal(String.valueOf(value))
                                                                .formatted(Formatting.AQUA))
                                                        .append(Text.literal(". Nothing changed.")
                                                                .formatted(Formatting.DARK_GRAY))
                                        );
                                        return 1;
                                    }

                                    ModConfig.INSTANCE.pitchThreshold = value;
                                    ModConfig.INSTANCE.save();

                                    send(ctx,
                                            Text.literal("Pitch set to ")
                                                    .formatted(Formatting.GRAY)
                                                    .append(Text.literal(String.valueOf(value))
                                                            .formatted(Formatting.AQUA))
                                    );

                                    return 1;
                                })
                        )
                )

                // ===== SOUND =====
                .then(literal("sound")
                        .executes(ctx -> {
                            sendBoolean(ctx, "Sound", ModConfig.INSTANCE.enableSound);
                            return 1;
                        })
                        .then(literal("reset")
                                .executes(ctx -> {
                                    if (ModConfig.INSTANCE.enableSound == ModConfig.DEFAULT_SOUND) {
                                        send(ctx, Text.literal("Sound already default.")
                                                .formatted(Formatting.DARK_GRAY));
                                        return 1;
                                    }

                                    ModConfig.INSTANCE.enableSound = ModConfig.DEFAULT_SOUND;
                                    ModConfig.INSTANCE.save();

                                    send(ctx, Text.literal("Sound reset.")
                                            .formatted(Formatting.YELLOW));
                                    return 1;
                                })
                        )
                        .then(argument("value", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean value = BoolArgumentType.getBool(ctx, "value");

                                    if (ModConfig.INSTANCE.enableSound == value) {
                                        send(ctx,
                                                Text.literal("Sound already ")
                                                        .formatted(Formatting.GRAY)
                                                        .append(Text.literal(String.valueOf(value))
                                                                .formatted(value ? Formatting.GREEN : Formatting.RED))
                                                        .append(Text.literal(". Nothing changed.")
                                                                .formatted(Formatting.DARK_GRAY))
                                        );
                                        return 1;
                                    }

                                    ModConfig.INSTANCE.enableSound = value;
                                    ModConfig.INSTANCE.save();

                                    sendBoolean(ctx, "Sound", value);
                                    return 1;
                                })
                        )
                )

                // ===== ACTIONBAR =====
                .then(literal("actionbar")
                        .executes(ctx -> {
                            sendBoolean(ctx, "ActionBar", ModConfig.INSTANCE.enableActionBar);
                            return 1;
                        })
                        .then(literal("reset")
                                .executes(ctx -> {
                                    if (ModConfig.INSTANCE.enableActionBar == ModConfig.DEFAULT_ACTIONBAR) {
                                        send(ctx, Text.literal("ActionBar already default.")
                                                .formatted(Formatting.DARK_GRAY));
                                        return 1;
                                    }

                                    ModConfig.INSTANCE.enableActionBar = ModConfig.DEFAULT_ACTIONBAR;
                                    ModConfig.INSTANCE.save();

                                    send(ctx, Text.literal("ActionBar reset.")
                                            .formatted(Formatting.YELLOW));
                                    return 1;
                                })
                        )
                        .then(argument("value", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean value = BoolArgumentType.getBool(ctx, "value");

                                    if (ModConfig.INSTANCE.enableActionBar == value) {
                                        send(ctx,
                                                Text.literal("ActionBar already ")
                                                        .formatted(Formatting.GRAY)
                                                        .append(Text.literal(String.valueOf(value))
                                                                .formatted(value ? Formatting.GREEN : Formatting.RED))
                                                        .append(Text.literal(". Nothing changed.")
                                                                .formatted(Formatting.DARK_GRAY))
                                        );
                                        return 1;
                                    }

                                    ModConfig.INSTANCE.enableActionBar = value;
                                    ModConfig.INSTANCE.save();

                                    sendBoolean(ctx, "ActionBar", value);
                                    return 1;
                                })
                        )
                )

                // ===== NETHER =====
                .then(literal("nether")
                        .executes(ctx -> {
                            sendBoolean(ctx, "Disable in Nether", ModConfig.INSTANCE.disableInNether);
                            return 1;
                        })
                        .then(literal("reset")
                                .executes(ctx -> {
                                    if (ModConfig.INSTANCE.disableInNether == ModConfig.DEFAULT_DISABLE_NETHER) {
                                        send(ctx, Text.literal("Nether setting already default.")
                                                .formatted(Formatting.DARK_GRAY));
                                        return 1;
                                    }

                                    ModConfig.INSTANCE.disableInNether = ModConfig.DEFAULT_DISABLE_NETHER;
                                    ModConfig.INSTANCE.save();

                                    send(ctx, Text.literal("Nether setting reset.")
                                            .formatted(Formatting.YELLOW));
                                    return 1;
                                })
                        )
                        .then(argument("value", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean value = BoolArgumentType.getBool(ctx, "value");

                                    if (ModConfig.INSTANCE.disableInNether == value) {
                                        send(ctx,
                                                Text.literal("Disable in Nether already ")
                                                        .formatted(Formatting.GRAY)
                                                        .append(Text.literal(String.valueOf(value))
                                                                .formatted(value ? Formatting.GREEN : Formatting.RED))
                                                        .append(Text.literal(". Nothing changed.")
                                                                .formatted(Formatting.DARK_GRAY))
                                        );
                                        return 1;
                                    }

                                    ModConfig.INSTANCE.disableInNether = value;
                                    ModConfig.INSTANCE.save();

                                    sendBoolean(ctx, "Disable in Nether", value);
                                    return 1;
                                })
                        )
                )
        );
    }

    private static void sendHelp(CommandContext<FabricClientCommandSource> ctx,
                                 String command,
                                 String description) {

        send(ctx,
                Text.literal("/waterbucket ")
                        .formatted(Formatting.DARK_GRAY)
                        .append(Text.literal(command).formatted(Formatting.AQUA))
                        .append(Text.literal(" - " + description)
                                .formatted(Formatting.GRAY))
        );
    }

    private static void send(CommandContext<FabricClientCommandSource> ctx,
                             Text message) {

        ctx.getSource().sendFeedback(
                PREFIX.copy().append(message)
        );
    }

    private static void sendBoolean(CommandContext<FabricClientCommandSource> ctx,
                                    String label,
                                    boolean value) {

        send(ctx,
                Text.literal(label + ": ")
                        .formatted(Formatting.GRAY)
                        .append(Text.literal(String.valueOf(value))
                                .formatted(value ? Formatting.GREEN : Formatting.RED))
        );
    }
}