package com.enderdevelopment.dimensionclock;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class DimensionClockMod implements ModInitializer {
    public static final String MOD_ID = "dimensionclock";
    public static final Item DIMENSION_CLOCK = new DimensionClockItem(new Item.Settings().maxCount(1));

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "dimension_clock"), DIMENSION_CLOCK);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(DIMENSION_CLOCK));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("dimensionclock")
                .then(argument("dimension", StringArgumentType.word())
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                        String value = StringArgumentType.getString(context, "dimension");
                        Identifier id = Identifier.tryParse(value);
                        if (id == null) {
                            context.getSource().sendError(Text.literal("Invalid dimension ID. Use namespace:id."));
                            return 0;
                        }
                        ServerWorld target = player.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, id));
                        if (target == null) {
                            context.getSource().sendError(Text.literal("Dimension is not available: " + id));
                            return 0;
                        }
                        teleport(player, target);
                        return 1;
                    }))));
    }

    static void teleport(ServerPlayerEntity player, ServerWorld target) {
        double x = player.getX();
        double z = player.getZ();
        int y = Math.max(target.getBottomY() + 1, Math.min((int) player.getY(), target.getTopY() - 2));
        if (!target.isSpaceEmpty(player, player.getBoundingBox().offset(x - player.getX(), y - player.getY(), z - player.getZ()))) {
            BlockPos spawn = target.getSpawnPos();
            x = spawn.getX() + 0.5;
            y = spawn.getY();
            z = spawn.getZ() + 0.5;
        }
        player.teleport(target, x, y, z, player.getYaw(), player.getPitch());
    }
}
