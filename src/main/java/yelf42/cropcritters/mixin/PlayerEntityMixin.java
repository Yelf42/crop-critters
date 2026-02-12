package yelf42.cropcritters.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.config.ConfigManager;
import yelf42.cropcritters.items.ModItems;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "onKilledOther", at = @At("HEAD"))
    public void dropLostSouls(ServerWorld world, LivingEntity killedEntity, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack stack = player.getMainHandStack();

        if (killedEntity.getType().isIn(CropCritters.HAS_LOST_SOUL)) {
            int dropChance = ConfigManager.CONFIG.lostSoulDropChance;

            // Hoe +| Critter
            if (stack.isIn(ItemTags.HOES)) {
                dropChance += ConfigManager.CONFIG.lostSoulDropChance;
                dropChance += (killedEntity.getType().isIn(CropCritters.CROP_CRITTERS)) ? ConfigManager.CONFIG.lostSoulDropChance : 0;
            }

            // Silk Touch
            Optional<RegistryEntry.Reference<Enchantment>> e = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.SILK_TOUCH.getValue());
            if (e.isPresent()) {
                dropChance += (EnchantmentHelper.getLevel(e.get(), stack) > 0) ? 2 * ConfigManager.CONFIG.lostSoulDropChance : 0;
            }

            // Looting
            Optional<RegistryEntry.Reference<Enchantment>> e2 = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.LOOTING.getValue());
            if (e2.isPresent()) {
                dropChance += (ConfigManager.CONFIG.lostSoulDropChance / 2) * EnchantmentHelper.getLevel(e2.get(), stack);
            }

            if (world.random.nextInt(100) + 1 < dropChance) {
                Vec3d pos = killedEntity.getEntityPos();
                ItemEntity ls = new ItemEntity(world, pos.x, pos.y, pos.z, new ItemStack(ModItems.LOST_SOUL));
                ls.setToDefaultPickupDelay();
                world.spawnEntity(ls);
            }
        }
    }

}
