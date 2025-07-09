package yelf42.cropcritters.config;

public class CropCrittersConfig {
    public int thistle_chance = 3;
    public int thornweed_chance = 5;
    public int monoculture_dampener = 16;
    public int lost_soul_drop_chance = 2;

    public static CropCrittersConfig getDefaults() {
        return new CropCrittersConfig();
    }

}
