package yelf42.cropcritters.config;

public class CropCrittersConfig {
    public int thistleChance = 3;
    public int thornweedChance = 5;
    public int waftgrassChance = 2;
    public int spiteweedChance = 10;
    public boolean monoculturePenalize = true;
    public int lostSoulDropChance = 6;
    public int critterSpawnChance = 7;
    public boolean deadCoralGeneration = true;
    public boolean thornweedGeneration = true;
    public boolean waftgrassGeneration = true;
    public boolean spiteweedGeneration = true;

    public static CropCrittersConfig getDefaults() {
        return new CropCrittersConfig();
    }

}
