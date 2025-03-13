package io.github.eufranio.serverluckyblock.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.eufranio.serverluckyblock.ServerLuckyBlock;
import io.github.eufranio.serverluckyblock.commands.provider.LuckyBlockSuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GiveLuckyBlock {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var command = CommandManager.literal("giveluckyblock")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.argument("luckyblock", StringArgumentType.string())
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
                                            throw new CommandException(Text.of("Unknown lucky block id: " + id));
                                        }

                                        ItemStack stack = new ItemStack(Items.ITEM_FRAME);
                                        stack.setSubNbt("CustomModelData", NbtInt.of(configuration.customModelData));

                                        ItemStack copy = stack.copy();
                                        copy.setSubNbt("CustomModelData", NbtInt.of(configuration.customModelData));

                                        stack.setCustomName(Text.of(replaceText(configuration.itemTitle)));

                                        NbtCompound display = stack.getOrCreateSubNbt("display");
                                        NbtList lore = display.getList("Lore", NbtList.STRING_TYPE);
                                        configuration.itemLore.forEach(item -> lore.add(NbtString.of("{\"text\":\"" + replaceText(item) + "\"}")));
                                        display.put("Lore", lore);
                                        stack.setSubNbt("display", display);

                                        NbtCompound entityTag = new NbtCompound();
                                        entityTag.put("Item", copy.writeNbt(new NbtCompound()));

                                        NbtList tags = entityTag.getList("Tags", NbtList.STRING_TYPE);
                                        tags.add(NbtString.of("luckyblock"));
                                        tags.add(NbtString.of("luckyblock_" + configuration.id));
                                        entityTag.put("Tags", tags);

                                        entityTag.putBoolean("Silent", true);
                                        entityTag.putBoolean("Invulnerable", true);
                                        entityTag.putBoolean("Invisible", true);
                                        entityTag.putBoolean("Fixed", true);

                                        stack.setSubNbt("EntityTag", entityTag);

                                        src.getSource().getPlayer().giveItemStack(stack);
                                        src.getSource().sendMessage(Text.of("Given item stack"));
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
