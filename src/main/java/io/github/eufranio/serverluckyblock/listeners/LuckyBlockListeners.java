package io.github.eufranio.serverluckyblock.listeners;

import io.github.eufranio.serverluckyblock.ServerLuckyBlock;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class LuckyBlockListeners {

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
             if (entity instanceof ItemFrame && entity.getTags().contains("luckyblock")) {
                 boolean placed = entity.getTags().contains("placed");
                 if (!placed) {
                     entity.addTag("placed");
                     world.setBlock(entity.blockPosition(), Blocks.BARRIER.defaultBlockState(), 3);
                 }
             }
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            var entity = world.getEntities(
                    EntityTypeTest.forClass(ItemFrame.class),
                    new AABB(pos),
                    e -> e.getTags().contains("luckyblock")
            ).stream().findFirst().orElse(null);
            if (entity == null) return InteractionResult.PASS;

            String id = entity.getTags()
                    .stream()
                    .filter(cmd -> cmd.startsWith("luckyblock_"))
                    .map(cmd -> cmd.replace("luckyblock_", ""))
                    .findFirst().orElse(null);
            if (id == null) return InteractionResult.PASS;

            var configuration = ServerLuckyBlock.getConfig().get()
                    .availableLuckyBlocks
                    .stream()
                    .filter(cfg -> cfg.id.equalsIgnoreCase(id))
                    .findFirst()
                    .orElse(null);
            if (configuration == null) return InteractionResult.PASS;

            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            entity.setItem(ItemStack.EMPTY);
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
                var string = cmd.replace("%player%", player.getName().getString());
                var stack = player.getServer().createCommandSourceStack();
                player.getServer().getCommands().performPrefixedCommand(stack, string);
            });

            return InteractionResult.SUCCESS;
        });
    }

}
