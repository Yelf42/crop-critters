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
                    case "regularWeedsGrowChance" -> CONFIG.regularWeedChance = Math.clamp(Integer.parseInt(value), 0, 100);
                    case "netherWeedsGrowChance" -> CONFIG.netherWeedChance = Math.clamp(Integer.parseInt(value), 0, 100);
                    case "spiteweedGrowChance" -> CONFIG.spiteweedChance = Math.clamp(Integer.parseInt(value), 0, 100);
                    case "lostSoulDropChance" -> CONFIG.lostSoulDropChance = Math.clamp(Integer.parseInt(value), 0, 100);
                    case "monoculturePenalize" -> CONFIG.monoculturePenalize = Boolean.parseBoolean(value);
                    case "critterSpawnChance" -> CONFIG.critterSpawnChance = Math.clamp(Integer.parseInt(value), 0, 100);
                    case "critterWorkSpeedMultiplier" -> CONFIG.critterWorkSpeedMultiplier = Math.clamp(Float.parseFloat(value), 0, 10);
                    case "deadCoralGeneration" -> CONFIG.deadCoralGeneration = Boolean.parseBoolean(value);
                    case "thornweedGeneration" -> CONFIG.thornweedGeneration = Boolean.parseBoolean(value);
                    case "waftgrassGeneration" -> CONFIG.waftgrassGeneration = Boolean.parseBoolean(value);
                    case "spiteweedGeneration" -> CONFIG.spiteweedGeneration = Boolean.parseBoolean(value);
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
            writer.write("regularWeedsGrowChance = " + CONFIG.regularWeedChance + "\n");
            writer.write("netherWeedsGrowChance = " + CONFIG.netherWeedChance + "\n");
            writer.write("spiteweedGrowChance = " + CONFIG.spiteweedChance + "\n");
            writer.write("# Should monocultures have increased weed chances:\n");
            writer.write("monoculturePenalize = " + CONFIG.monoculturePenalize + "\n");
            writer.write("# Strength monocultural crops have on weed chance:\n");
            writer.write("# \n");
            writer.write("# Lost soul mob drop chance:\n");
            writer.write("lostSoulDropChance = " + CONFIG.lostSoulDropChance + "\n");
            writer.write("# \n");
            writer.write("# Crop critter spawn chance on crop just matured\n");
            writer.write("# or on randomTick in SoulSandValley.\n");
            writer.write("# Chance doubled if on a 'Soul' block:\n");
            writer.write("critterSpawnChance = " + CONFIG.critterSpawnChance + "\n");
            writer.write("# \n");
            writer.write("# Multiplier on critter work speed:\n");
            writer.write("# (Between 0.01 and 10.0)\n");
            writer.write("critterWorkSpeedMultiplier = " + CONFIG.critterWorkSpeedMultiplier + "\n");
            writer.write("# \n");
            writer.write("# Biome generation toggles: \n");
            writer.write("deadCoralGeneration = " + CONFIG.deadCoralGeneration + "\n");
            writer.write("thornweedGeneration = " + CONFIG.thornweedGeneration + "\n");
            writer.write("waftgrassGeneration = " + CONFIG.waftgrassGeneration + "\n");
            writer.write("spiteweedGeneration = " + CONFIG.spiteweedGeneration + "\n");
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }


}
