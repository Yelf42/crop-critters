package yelf42.cropcritters.config;

public class CropCrittersConfig {
    public int thistle_chance = 55;
    public int thornweed_chance = 55;

    public static CropCrittersConfig getDefaults() {
        return new CropCrittersConfig();
    }

}
