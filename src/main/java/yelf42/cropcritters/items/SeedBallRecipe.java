package yelf42.cropcritters.items;

import net.minecraft.block.CropBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import yelf42.cropcritters.CropCritters;

import java.util.ArrayList;
import java.util.List;

public class SeedBallRecipe extends SpecialCraftingRecipe {

    public SeedBallRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (input.getStackCount() != 9 || input.getHeight() != 3 || input.getWidth() != 3) return false;
        if (!input.getStackInSlot(1,1).isOf(Items.MUD)) return false;

        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++) {
                if (i == 1 && j == 1) continue;
                ItemStack is = input.getStackInSlot(i,j);

                if (!is.isOf(Items.POISONOUS_POTATO) && validItem(is) == null) return false;
            }
        }

//        // 4 direction version
//        if (!input.getStackInSlot(0,1).isIn(CropCritters.SEED_BALL_CROPS) && !input.getStackInSlot(0,1).isOf(Items.POISONOUS_POTATO)) return false;
//        if (!input.getStackInSlot(1,0).isIn(CropCritters.SEED_BALL_CROPS) && !input.getStackInSlot(1,0).isOf(Items.POISONOUS_POTATO)) return false;
//        if (!input.getStackInSlot(1,2).isIn(CropCritters.SEED_BALL_CROPS) && !input.getStackInSlot(1,2).isOf(Items.POISONOUS_POTATO)) return false;
//        if (!input.getStackInSlot(2,1).isIn(CropCritters.SEED_BALL_CROPS) && !input.getStackInSlot(2,1).isOf(Items.POISONOUS_POTATO)) return false;
//

        return true;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        ItemStack result = new ItemStack(ModItems.SEED_BALL, 1);

        List<Identifier> usedSeeds = new ArrayList<>();
        int poisonousPotatoes = 0;

        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++) {
                ItemStack is = input.getStackInSlot(i,j);
                if (is.isOf(Items.POISONOUS_POTATO)) {
                    poisonousPotatoes += 1;
                } else {
                    Identifier id = validItem(is);
                    if (id != null) {
                        if (!usedSeeds.contains(id)) usedSeeds.add(id);
                    }
                }
            }
        }

        result.set(ModComponents.POISONOUS_SEED_BALL, new ModComponents.PoisonousComponent(poisonousPotatoes));

        result.set(ModComponents.SEED_TYPES, new ModComponents.SeedTypesComponent(usedSeeds));
        return result;
    }

    private static Identifier validItem(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem && (stack.isIn(CropCritters.SEED_BALL_CROPS) || blockItem.getBlock() instanceof CropBlock)) {
            return Registries.BLOCK.getId(blockItem.getBlock());
        }
        return null;
    }

    @Override
    public RecipeSerializer<SeedBallRecipe> getSerializer() {
        return ModItems.SEED_BALL_RECIPE;
    }
}
