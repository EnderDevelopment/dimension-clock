package com.enderdevelopment.dimensionclock;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public final class DimensionClockItem extends Item {
    public DimensionClockItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, net.minecraft.entity.player.Player user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.success(stack);
        if (!(user instanceof ServerPlayerEntity player)) return TypedActionResult.pass(stack);

        ServerWorld next = null;
        boolean passedCurrent = false;
        for (ServerWorld candidate : player.getServer().getWorlds()) {
            if (passedCurrent) {
                next = candidate;
                break;
            }
            if (candidate == player.getServerWorld()) passedCurrent = true;
        }
        if (next == null) next = player.getServer().getWorlds().iterator().next();
        if (next == player.getServerWorld()) {
            player.sendMessage(Text.literal("No other dimensions are available. Use /dimensionclock namespace:id for a registered dimension."), true);
            return TypedActionResult.success(stack);
        }

        DimensionClockMod.teleport(player, next);
        player.sendMessage(Text.literal("Travelled to " + next.getRegistryKey().getValue()), true);
        return TypedActionResult.success(stack);
    }
}
