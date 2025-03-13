package io.github.eufranio.serverluckyblock.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class LuckyBlockConfig {

    @Setting
    public List<LuckyBlockConfiguration> availableLuckyBlocks = new ArrayList<>() {{
        add(new LuckyBlockConfiguration());
    }};

    @ConfigSerializable
    public static class LuckyBlockConfiguration {

        @Setting
        public String id = "basic";

        @Setting
        public int customModelData = 777777;

        @Setting
        public String itemTitle = "&eLucky Block";

        @Setting
        public List<String> itemLore = new ArrayList<>() {{
            add("&cLucky Block Lore");
        }};

        @Setting
        @Comment("If this is true, the weights in `commands` will be used when chosing the commands to execute. If false, " +
                "all commands from `commands` will be executed.")
        public boolean useWeights = true;

        @Setting
        @Comment("If useWeights = true, only an max of `weightedCommandsToExecute` commands will be executed")
        public int weightedCommandsToExecute = 5;

        @Setting
        public Map<String, Integer> commands = new HashMap<>() {{
            put("give %player% diamond 5", 1);
            put("give %player% diamond 2", 2);
            put("give %player% diamond 1", 5);
        }};

    }

}
