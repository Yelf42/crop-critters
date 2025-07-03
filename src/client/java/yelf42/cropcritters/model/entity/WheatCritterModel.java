package yelf42.cropcritters.model.entity;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.entity.WheatCritterEntity;

public class WheatCritterModel extends DefaultedEntityGeoModel<WheatCritterEntity> {
    public WheatCritterModel(){
        super(Identifier.of(CropCritters.MOD_ID, "wheat_critter"), false);
    }
}
