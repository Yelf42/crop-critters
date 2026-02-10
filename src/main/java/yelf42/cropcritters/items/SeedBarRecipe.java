package yelf42.cropcritters.items;

import net.minecraft.block.PlantBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class SeedBarRecipe extends SpecialCraftingRecipe {

    public SeedBarRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (input.getStackCount() != 9 || input.getHeight() != 3 || input.getWidth() != 3) return false;
        if (!input.getStackInSlot(1,1).isOf(Items.SWEET_BERRIES) && !input.getStackInSlot(1,1).isOf(Items.GLOW_BERRIES)) return false;

        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++) {
                if (i == 1 && j == 1) continue;
                ItemStack is = input.getStackInSlot(i,j);

                if (!validItem(is)) return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        return new ItemStack(ModItems.SEED_BAR, 2);
    }

    private static boolean validItem(ItemStack stack) {
        Item item = stack.getItem();
        String itemName = stack.getName().getString();
        return itemName.contains("Seeds") && item instanceof BlockItem blockItem && blockItem.getBlock() instanceof PlantBlock;
    }

    @Override
    public RecipeSerializer<SeedBarRecipe> getSerializer() {
        return ModItems.SEED_BAR_RECIPE;
    }
}