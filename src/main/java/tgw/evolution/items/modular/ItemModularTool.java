package tgw.evolution.items.modular;

import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.modular.ModularTool;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.inventory.SlotType;
import tgw.evolution.items.IBackWeapon;
import tgw.evolution.items.IThrowable;
import tgw.evolution.items.ITwoHanded;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.physics.SI;

import java.util.Random;

public class ItemModularTool extends ItemModular<ModularTool> implements IThrowable, ITwoHanded, IBackWeapon {

    protected static final Random RANDOM = new Random();

    public ItemModularTool(Properties builder) {
        super(builder);
    }

    public static ItemStack createNew(PartTypes.Head headType,
                                      EvolutionMaterials headMaterial,
                                      PartTypes.Handle handleType,
                                      EvolutionMaterials handleMaterial,
                                      boolean sharp) {
        if (!headMaterial.isAllowedBy(headType)) {
            throw new RuntimeException("Invalid material for " + headType.getName() + ": " + headMaterial.getName());
        }
        if (!handleMaterial.isAllowedBy(handleType)) {
            throw new RuntimeException("Invalid material for " + handleType.getName() + ": " + handleMaterial.getName());
        }
        ItemStack stack = new ItemStack(EvolutionItems.MODULAR_TOOL);
        EvolutionItems.MODULAR_TOOL.set(stack, headType, headMaterial, handleType, handleMaterial, sharp);

        return stack;
    }

    @Override
    public boolean canAttackBlock_(BlockState state, Level level, int x, int y, int z, Player player) {
        if (player.isCreative()) {
            return false;
        }
        return !player.shouldRenderSpecialAttack();
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (this.allowdedIn(tab)) {
            for (PartTypes.Head head : PartTypes.Head.VALUES) {
                for (EvolutionMaterials material : EvolutionMaterials.VALUES) {
                    if (material.isAllowedBy(head)) {
                        items.add(createNew(head, material, PartTypes.Handle.ONE_HANDED, EvolutionMaterials.WOOD, true));
                    }
                }
            }
        }
    }

    @Override
    public int getAutoAttackTime(ItemStack stack) {
        return 4;
    }

    @Override
    public int getBackPriority(ItemStack stack) {
        if (!verifyStack(stack)) {
            return -1;
        }
        //noinspection ConstantConditions
        return this.getModular().getBackPriority(stack.getTag());
    }

    @Override
    public @Nullable BasicAttackType getBasicAttackType(ItemStack stack) {
        if (!verifyStack(stack)) {
            return null;
        }
        //noinspection ConstantConditions
        return this.getModular().getBasicAttackType(stack.getTag());
    }

    @Override
    public SoundEvent getBlockHitSound(ItemStack stack) {
        if (!verifyStack(stack)) {
            return EvolutionSounds.STONE_WEAPON_HIT_BLOCK;
        }
        //noinspection ConstantConditions
        return this.getModular().getBlockHitSound(stack.getTag());
    }

    @Override
    public @Nullable ChargeAttackType getChargeAttackType(ItemStack stack) {
        //TODO implementation
        return null;
    }

    @Override
    public int getCooldown(ItemStack stack) {
        if (!verifyStack(stack)) {
            return 0;
        }
        //noinspection ConstantConditions
        return this.getModular().getCooldown(stack.getTag());
    }

    @Override
    public ItemStack getDefaultInstance() {
        PartTypes.Head head = PartTypes.Head.getRandom(RANDOM);
        EvolutionMaterials headMaterial = EvolutionMaterials.getRandom(RANDOM);
        while (!head.hasVariantIn(headMaterial)) {
            headMaterial = EvolutionMaterials.getRandom(RANDOM);
        }
        PartTypes.Handle handle = PartTypes.Handle.getRandom(RANDOM);
        EvolutionMaterials handleMaterial = EvolutionMaterials.getRandom(RANDOM);
        while (!handle.hasVariantIn(handleMaterial)) {
            handleMaterial = EvolutionMaterials.getRandom(RANDOM);
        }
        return createNew(head, headMaterial, handle, handleMaterial, true);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (!verifyStack(stack)) {
            return 1.0f;
        }
        //noinspection ConstantConditions
        return this.getModular().getDestroySpeed(stack.getTag(), state);
    }

    @Override
    public double getDmgMultiplier(ItemStack stack, EvolutionDamage.Type type) {
        if (!verifyStack(stack)) {
            return 1.0;
        }
        //noinspection ConstantConditions
        return this.getModular().getDmgMultiplier(stack.getTag(), type);
    }

    @Override
    public int getMinAttackTime(ItemStack stack) {
        return 4;
    }

    @Override
    protected ModularTool getModular() {
        return ModularTool.INSTANCE;
    }

    @Override
    public float getRenderOffsetY() {
        return -4;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        //TODO use proper harvest level
        this.hurtAndBreak(stack, DamageCause.HIT_ENTITY, attacker, SlotType.equipFromHand(target.getUsedItemHand()), HarvestLevel.HAND);
        return true;
    }

    @Override
    public boolean isCancelable(ItemStack stack, LivingEntity entity) {
        return this.isThrowable(stack, entity);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state, Level level, int x, int y, int z) {
        if (!verifyStack(stack)) {
            return false;
        }
        return this.getModular().isCorrecToolForDrops(stack, state, level, x, y, z);
    }

    @Override
    public boolean isDamageProportionalToMomentum() {
        return true;
    }

    @Override
    public boolean isHoldable(ItemStack stack) {
        return true;
    }

    public boolean isSimilarTo(ItemStack a, ItemStack b) {
        //todo
        return false;
    }

    @Override
    public boolean isThrowable(ItemStack stack, LivingEntity entity) {
        if (!verifyStack(stack)) {
            return false;
        }
        if (entity.getSwimAmount(1.0f) != 0) {
            return false;
        }
        //noinspection ConstantConditions
        return this.getModular().isThrowable(stack.getTag());
    }

    @Override
    public boolean isTwoHanded(ItemStack stack) {
        if (!verifyStack(stack)) {
            return false;
        }
        //noinspection ConstantConditions
        return this.getModular().isTwoHanded(stack.getTag());
    }

    @Override
    public boolean mineBlock_(ItemStack stack, Level level, BlockState state, int x, int y, int z, LivingEntity entity) {
        if (!level.isClientSide && state.getDestroySpeed_() != 0.0F) {
            this.getModular().mineBlock(stack, level, state, x, y, z, entity);
        }
        return true;
    }

    @Override
    public float precision() {
        return 0.75f;
    }

    @Override
    public EvolutionDamage.Type projectileDamageType() {
        return EvolutionDamage.Type.PIERCING;
    }

    @Override
    public double projectileSpeed() {
        return 16.5 * SI.METER / SI.SECOND;
    }

    protected void set(ItemStack stack,
                       PartTypes.Head headType,
                       EvolutionMaterials headMaterial,
                       PartTypes.Handle handleType,
                       EvolutionMaterials handleMaterial,
                       boolean sharp) {
        if (!verifyStack(stack)) {
            return;
        }
        //noinspection ConstantConditions
        this.getModular().set(stack.getTag(), headType, headMaterial, handleType, handleMaterial, sharp);
    }

    @Override
    public boolean shouldPlaySheatheSound(ItemStack stack) {
        return false;
    }
}
