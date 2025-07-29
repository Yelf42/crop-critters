package yelf42.cropcritters.config;

public class CropCrittersConfig {
    public int thistle_chance = 3;
    public int thornweed_chance = 5;
    public int waftgrass_chance = 2;
    public int spiteweed_chance = 10;
    public int monoculture_dampener = 32;
    public boolean monoculture_penalize = true;
    public int lost_soul_drop_chance = 2;
    public int critter_spawn_chance = 7;

    public static CropCrittersConfig getDefaults() {
        return new CropCrittersConfig();
    }

}
