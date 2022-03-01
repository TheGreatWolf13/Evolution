package tgw.evolution.items;

import com.google.common.collect.Sets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import tgw.evolution.Evolution;
import tgw.evolution.capabilities.modular.IModular;
import tgw.evolution.entities.projectiles.EntityGenericProjectile;
import tgw.evolution.entities.projectiles.EntitySpear;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.init.ItemMaterial;

import javax.annotation.Nonnull;
import java.util.Set;

public class ItemJavelin extends ItemGenericTool implements IThrowable, ISpear, IBackWeapon {

    private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet();
    private static final Set<Material> EFFECTIVE_MATS = Sets.newHashSet();
    private final float damage;
    private final double mass;
    private final ResourceLocation modelTexture;

    public ItemJavelin(float attackSpeed, ItemMaterial tier, Properties builder, float damage, double mass, String name) {
        super(attackSpeed, tier, EFFECTIVE_ON, EFFECTIVE_MATS, builder, ToolType.SPEAR);
        this.damage = damage;
        this.mass = mass;
        this.modelTexture = Evolution.getResource("textures/entity/javelin/javelin_" + name + ".png");
    }

    @Override
    public float baseDamage() {
        return 0;
    }

    @Override
    public int blockDurabilityDamage() {
        return 2;
    }

    @Override
    public int entityDurabilityDamage() {
        return 1;
    }

    @Nonnull
    @Override
    public EvolutionDamage.Type getDamageType(ItemStack stack) {
        return EvolutionDamage.Type.PIERCING;
    }

    @Override
    public float getEfficiency() {
        return 0;
    }

    @Override
    public double getMass(ItemStack stack) {
        return this.mass;
    }

    @Override
    public IModular getModularCap(ItemStack stack) {
        return IModular.NULL;
    }

    @Override
    public int getPriority(ItemStack stack) {
        return 1;
    }

    @Override
    public ResourceLocation getTexture() {
        return this.modelTexture;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    @Override
    public float reach() {
        return 5.0f;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int i = this.getUseDuration(stack) - timeLeft;
            if (i >= 10) {
                if (!level.isClientSide) {
                    EntitySpear spear = new EntitySpear(level, player, stack, this.damage, this.mass);
                    spear.shoot(player, player.getXRot(), player.getYRot(), 0.825f, 2.5F);
                    if (player.getAbilities().instabuild) {
                        spear.pickupStatus = EntityGenericProjectile.PickupStatus.CREATIVE_ONLY;
                    }
                    level.addFreshEntity(spear);
                    level.playSound(null, spear, EvolutionSounds.JAVELIN_THROW.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    if (!player.getAbilities().instabuild) {
                        player.getInventory().removeItem(stack);
                    }
                }
                player.awardStat(Stats.ITEM_USED.get(this));
                this.addStat(player);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() || hand == InteractionHand.OFF_HAND) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }
        player.startUsingItem(hand);
        return new InteractionResultHolder<>(InteractionResult.CONSUME, stack);
    }
}
