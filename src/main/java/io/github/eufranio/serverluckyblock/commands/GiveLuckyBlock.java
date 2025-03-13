package io.github.eufranio.serverluckyblock.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.eufranio.serverluckyblock.ServerLuckyBlock;
import io.github.eufranio.serverluckyblock.commands.provider.LuckyBlockSuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;

public class GiveLuckyBlock {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var command = Commands.literal("giveluckyblock")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("player", EntityArgument.player())
                            .then(Commands.argument("luckyblock", StringArgumentType.string())
                                    .suggests(new LuckyBlockSuggestionProvider())
                                    .executes(src -> {
                                        String id = StringArgumentType.getString(src, "luckyblock");
                                        var configuration = ServerLuckyBlock.getConfig().get()
                                                .availableLuckyBlocks
                                                .stream()
                                                .filter(config -> config.id.equalsIgnoreCase(id))
                                                .findFirst()
                                                .orElse(null);
                                        if (configuration == null) {
                                            throw new SimpleCommandExceptionType(Component.literal("Unknown lucky block id: " + id)).create();
                                        }

                                        ItemStack stack = new ItemStack(Items.ITEM_FRAME);
                                        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(configuration.customModelData));

                                        ItemStack copy = stack.copy();
                                        copy.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(configuration.customModelData));

                                        stack.set(DataComponents.CUSTOM_NAME, Component.literal(replaceText(configuration.itemTitle)));

                                        stack.set(DataComponents.LORE, new ItemLore(configuration.itemLore.stream()
                                                .map(GiveLuckyBlock::replaceText)
                                                .map(text -> (Component) Component.literal(text))
                                                .toList()
                                        ));

                                        var copyTag = copy.has(DataComponents.CUSTOM_DATA) ?
                                                copy.get(DataComponents.CUSTOM_DATA).copyTag() :
                                                new CompoundTag();

                                        CustomData.update(DataComponents.ENTITY_DATA, stack, (tag) -> {
                                            tag.put("Item", copyTag);
                                            ListTag tags = tag.getList("Tags", Tag.TAG_STRING);
                                            tags.add(StringTag.valueOf("luckyblock"));
                                            tags.add(StringTag.valueOf("luckyblock_" + configuration.id));
                                            tag.put("Tags", tags);

                                            tag.putBoolean("Silent", true);
                                            tag.putBoolean("Invulnerable", true);
                                            tag.putBoolean("Invisible", true);
                                            tag.putBoolean("Fixed", true);
                                        });

                                        src.getSource().getPlayer().addItem(stack);
                                        src.getSource().sendSystemMessage(Component.literal("Given item stack"));
                                        return 1;
                                    })
                            )
                    );
            dispatcher.register(command);
        });
    }

    public static String replaceText(String msg) {
        return msg.replaceAll("&(?!\\s)", "§");
    }

}
