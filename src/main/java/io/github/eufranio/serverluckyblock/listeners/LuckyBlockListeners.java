package io.github.eufranio.serverluckyblock.listeners;

import io.github.eufranio.serverluckyblock.ServerLuckyBlock;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class LuckyBlockListeners {

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
             if (entity instanceof ItemFrameEntity && entity.getCommandTags().contains("luckyblock")) {
                 boolean placed = entity.getCommandTags().contains("placed");
                 if (!placed) {
                     entity.getCommandTags().add("placed");
                     world.setBlockState(entity.getBlockPos(), Blocks.BARRIER.getDefaultState());
                 }
             }
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            var entity = world.getEntitiesByType(
                    TypeFilter.instanceOf(ItemFrameEntity.class),
                    new Box(pos),
                    e -> e.getCommandTags().contains("luckyblock")
            ).stream().findFirst().orElse(null);
            if (entity == null) return ActionResult.PASS;

            String id = entity.getCommandTags()
                    .stream()
                    .filter(cmd -> cmd.startsWith("luckyblock_"))
                    .map(cmd -> cmd.replace("luckyblock_", ""))
                    .findFirst().orElse(null);
            if (id == null) return ActionResult.PASS;

            var configuration = ServerLuckyBlock.getConfig().get()
                    .availableLuckyBlocks
                    .stream()
                    .filter(cfg -> cfg.id.equalsIgnoreCase(id))
                    .findFirst()
                    .orElse(null);
            if (configuration == null) return ActionResult.PASS;

            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            entity.setHeldItemStack(ItemStack.EMPTY, false);
            entity.kill();

            final List<String> commandsToExecute = new ArrayList<>();

            if (configuration.useWeights) {
                List<String> weightedList = configuration.commands.entrySet()
                        .stream()
                        .flatMap(e -> IntStream.range(0, e.getValue())
                                .mapToObj(i -> e.getKey()))
                        .toList();
                IntStream.range(0, Math.min(weightedList.size(), configuration.weightedCommandsToExecute))
                        .forEach(i -> commandsToExecute.add(weightedList.get((int) (Math.random() * weightedList.size()))));
            } else {
                commandsToExecute.addAll(configuration.commands.keySet());
            }

            commandsToExecute.forEach(cmd -> {
                var string = cmd.replace("%player%", player.getEntityName());
                player.getServer().getCommandManager().executeWithPrefix(
                        player.getServer().getCommandSource(),
                        string
                );
            });

            return ActionResult.SUCCESS;
        });
    }

}
