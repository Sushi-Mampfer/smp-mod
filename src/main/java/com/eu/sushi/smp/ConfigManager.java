package com.eu.sushi.smp;

import net.fabricmc.loader.api.FabricLoader;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.inspector.TagInspector;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static Yaml yaml;

    static {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setIndent(4);
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        LoaderOptions loaderOptions = new LoaderOptions();
        TagInspector tagInspector =
                tag -> tag.getClassName().equals(ModConfig.class.getName());
        loaderOptions.setTagInspector(tagInspector);

        Constructor constructor = new Constructor(ModConfig.class, loaderOptions);

        Representer representer = new Representer(dumperOptions);
        representer.addClassTag(ModConfig.class, Tag.MAP);
        representer.addClassTag(ModConfig.SpawnElytra.class, Tag.MAP);
        representer.addClassTag(ModConfig.Whitelist.class, Tag.MAP);
        representer.getPropertyUtils().setSkipMissingProperties(true);

        yaml = new Yaml(constructor, representer, dumperOptions, loaderOptions);
    }


    public static void saveConfig(ModConfig config) {
        Path configFile = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("smp.yml");

        try {
            Files.createDirectories(configFile.getParent());

            try (FileWriter writer = new FileWriter(configFile.toFile())) {
                yaml.dump(config, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static ModConfig loadConfig() {
        Path configFile = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("smp.yml");

        if (Files.exists(configFile)) {
            try (FileReader reader = new FileReader(configFile.toFile())) {
                return yaml.loadAs(reader, ModConfig.class);
            } catch (IOException e) {
                System.err.println("Failed to load config: " + e.getMessage());
                e.printStackTrace();
            }
        }

        ModConfig defaultConfig = new ModConfig();
        saveConfig(defaultConfig);
        return defaultConfig;
    }
}