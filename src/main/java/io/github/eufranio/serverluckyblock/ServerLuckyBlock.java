package io.github.eufranio.serverluckyblock;

import io.github.eufranio.config.Config;
import io.github.eufranio.serverluckyblock.commands.GiveLuckyBlock;
import io.github.eufranio.serverluckyblock.config.LuckyBlockConfig;
import io.github.eufranio.serverluckyblock.listeners.LuckyBlockListeners;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class ServerLuckyBlock implements ModInitializer {

    static ServerLuckyBlock instance;

    final Config<LuckyBlockConfig> config = new Config<>(
            LuckyBlockConfig.class,
            "LuckyBlocks.conf",
            FabricLoader.getInstance().getConfigDir()
                    .resolve("serverluckyblock")
                    .toFile()
    );

    public ServerLuckyBlock() {
        instance = this;
    }

    @Override
    public void onInitialize() {
        GiveLuckyBlock.register();
        LuckyBlockListeners.register();
    }

    public static Config<LuckyBlockConfig> getConfig() {
        return instance.config;
    }
}
