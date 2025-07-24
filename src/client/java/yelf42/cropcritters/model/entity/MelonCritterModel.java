package yelf42.cropcritters.model.entity;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.entity.MelonCritterEntity;
import yelf42.cropcritters.entity.WheatCritterEntity;

public class MelonCritterModel extends DefaultedEntityGeoModel<MelonCritterEntity> {
    public MelonCritterModel(){
        super(Identifier.of(CropCritters.MOD_ID, "melon_critter"), false);
    }
}
