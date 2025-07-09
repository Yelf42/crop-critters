package yelf42.cropcritters.config;

import net.fabricmc.loader.api.FabricLoader;
import yelf42.cropcritters.CropCritters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("crop-critters-config.toml");
    public static CropCrittersConfig CONFIG = CropCrittersConfig.getDefaults();

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            CropCritters.LOGGER.info("Generating first time CropCritters config");
            save(); // Save defaults
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || !line.contains("=")) continue;

                String[] parts = line.split("=", 2);
                String key = parts[0].trim();
                String value = parts[1].trim();

                switch (key) {
                    case "thistleGrowChance" -> CONFIG.thistle_chance = Math.clamp(Integer.parseInt(value), 0, 100);
                    case "thornweedGrowChance" -> CONFIG.thornweed_chance = Math.clamp(Integer.parseInt(value), 0, 100);
                    case "lostSoulDropChance" -> CONFIG.lost_soul_drop_chance = Math.clamp(Integer.parseInt(value), 0, 100);
                    case "monocultureDampener" -> CONFIG.monoculture_dampener = Math.clamp(Integer.parseInt(value), 1, 1000);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
    }

    public static void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            writer.write("# CropCritters Configuration\n");
            writer.write("# Config changes only apply on restart\n");
            writer.write("# \n");
            writer.write("# Weed percentage grow chances:\n");
            writer.write("thistleGrowChance = " + CONFIG.thistle_chance + "\n");
            writer.write("thornweedGrowChance = " + CONFIG.thornweed_chance + "\n");
            writer.write("# Strength monocultural crops have on weed chance:\n");
            writer.write("# (Higher number means monoculture's have less impact)\n");
            writer.write("monocultureDampener = " + CONFIG.monoculture_dampener + "\n");
            writer.write("# \n");
            writer.write("# Lost soul mob drop chance:\n");
            writer.write("lostSoulDropChance = " + CONFIG.lost_soul_drop_chance + "\n");
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }


}
