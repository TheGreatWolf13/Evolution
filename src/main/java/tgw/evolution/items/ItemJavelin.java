package tgw.evolution.items;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.World;
import tgw.evolution.Evolution;
import tgw.evolution.entities.projectiles.EntityGenericProjectile;
import tgw.evolution.entities.projectiles.EntitySpear;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionSounds;

import javax.annotation.Nonnull;
import java.util.Set;

public class ItemJavelin extends ItemGenericTool implements IThrowable, ISpear {

    private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet();
    private static final Set<Material> EFFECTIVE_MATS = Sets.newHashSet();
    private final float damage;
    private final double mass;
    private final ResourceLocation modelTexture;

    public ItemJavelin(float attackSpeed, IItemTier tier, Properties builder, float damage, double mass, String name) {
        super(attackSpeed, tier, EFFECTIVE_ON, EFFECTIVE_MATS, builder, ToolTypeEv.SPEAR);
        this.damage = damage;
        this.mass = mass;
        this.modelTexture = Evolution.location("textures/entity/javelin/javelin_" + name + ".png");
        this.addPropertyOverride(new ResourceLocation("throwing"),
                                 (stack, world, entity) -> entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack ?
                                                           1.0F :
                                                           0.0F);
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
    public EvolutionDamage.Type getDamageType() {
        return EvolutionDamage.Type.PIERCING;
    }

    @Override
    public double getMass() {
        return this.mass;
    }

    @Override
    public ResourceLocation getTexture() {
        return this.modelTexture;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (stack.getDamage() >= stack.getMaxDamage() || handIn == Hand.OFF_HAND) {
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entityLiving;
            int i = this.getUseDuration(stack) - timeLeft;
            if (i >= 10) {
                if (!worldIn.isRemote) {
                    EntitySpear spear = new EntitySpear(worldIn, player, stack, this.damage, this.mass);
                    spear.shoot(player, player.rotationPitch, player.rotationYaw, 0.825f, 2.5F);
                    if (player.abilities.isCreativeMode) {
                        spear.pickupStatus = EntityGenericProjectile.PickupStatus.CREATIVE_ONLY;
                    }
                    worldIn.addEntity(spear);
                    worldIn.playMovingSound(null, spear, EvolutionSounds.JAVELIN_THROW.get(), SoundCategory.PLAYERS, 1.0F, 1.0F);
                    if (!player.abilities.isCreativeMode) {
                        player.inventory.deleteStack(stack);
                    }
                }
                player.addStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    @Override
    public float reach() {
        return 5.0f;
    }
}
