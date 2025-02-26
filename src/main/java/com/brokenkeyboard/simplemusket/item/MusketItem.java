package com.brokenkeyboard.simplemusket.item;

import com.brokenkeyboard.simplemusket.Config;
import com.brokenkeyboard.simplemusket.SimpleMusket;
import com.brokenkeyboard.simplemusket.entity.BulletEntity;
import com.brokenkeyboard.simplemusket.entity.MusketPillager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MusketItem extends FirearmItem {

    private static final int DURABILITY = 256;

    public MusketItem(Properties properties) {
        super(properties.defaultDurability(DURABILITY));
    }

    @Override
    public boolean isAmmo(ItemStack stack) {
        return stack.getItem() instanceof BulletItem;
    }

    @Override
    public int getReload(ItemStack stack) {
        double coeff = 1 - (EnchantmentHelper.getTagEnchantmentLevel(SimpleMusket.DEADEYE.get(), stack) * 0.1);
        return (int) (Config.MUSKET_RELOAD_TIME.get() * coeff);
    }

    @Override
    public int getAim(ItemStack stack) {
        double coeff = 1 - (EnchantmentHelper.getTagEnchantmentLevel(SimpleMusket.DEADEYE.get(), stack) * 0.1);
        return (int) (Config.MUSKET_AIM_TIME.get() * coeff);
    }

    @Override
    public float getDeviation() {
        return 12.0F;
    }

    @Override
    public SoundEvent getFireSound() {
        return SoundEvents.GENERIC_EXPLODE;
    }

    @Override
    public void createProjectile(LivingEntity entity, Level level, ItemStack stack, float deviation) {
        if (level.isClientSide) return;

        int pierceLevel = EnchantmentHelper.getTagEnchantmentLevel(SimpleMusket.FIREPOWER.get(), stack);
        int longshotLevel = EnchantmentHelper.getTagEnchantmentLevel(SimpleMusket.LONGSHOT.get(), stack);
        Vec3 initialPos = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
        ArrayList<BulletEntity> projectiles = new ArrayList<>();
        BulletType type = BulletType.values()[getAmmoType(stack)];

        switch (type) {
            case COPPER -> {
                for (int i = 0; i < 5; i++) {
                    projectiles.add(new BulletEntity(level, initialPos, type, pierceLevel, longshotLevel));
                }
                deviation = (float) (this.getDeviation() * (1 - EnchantmentHelper.getTagEnchantmentLevel(SimpleMusket.DEADEYE.get(), stack) * 0.1));
            }
            case GOLD -> {
                projectiles.add(0, new BulletEntity(level, initialPos, type, pierceLevel, longshotLevel));
                Map<Enchantment, Integer> enchantments = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
                if (enchantments.isEmpty()) break;
                BulletEntity bulletEntity = projectiles.get(0);
                int ench = 0;

                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet())
                    ench += getEnchantPower(entry.getKey(), entry.getValue());

                bulletEntity.setMagicBullet(Math.min((ench / 2) + 1, 24));
            }
            default -> projectiles.add(new BulletEntity(level, initialPos, type, pierceLevel, longshotLevel));
        }

        for(BulletEntity bullet : projectiles) {
            bullet.setOwner(entity);
            bullet.setPos(initialPos);

            if (entity instanceof MusketPillager musketAttackMob) {
                bullet.setDamageScaling(Config.REDUCE_PILLAGER_DAMAGE.get() ? 0.75 : 1);
                musketAttackMob.shootBullet(entity, Objects.requireNonNull(musketAttackMob.getTarget()), bullet, 4F);
            } else {
                bullet.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0F, 4F, deviation);
            }
            level.addFreshEntity(bullet);
        }
        stack.hurtAndBreak(type == BulletType.NETHERITE ? 3 : 1, entity, (user) -> user.broadcastBreakEvent(user.getUsedItemHand()));
    }

    private int getEnchantPower(Enchantment enchantment, int level) {
        Enchantment.Rarity rarity = enchantment.getRarity();
        return switch (rarity) {
            case UNCOMMON -> 2 * level;
            case RARE -> 3 * level;
            case VERY_RARE -> 4 * level;
            default -> level;
        };
    }
}