package tgw.evolution.capabilities.modular;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.items.IMelee;
import tgw.evolution.items.modular.ItemModular;
import tgw.evolution.util.collection.lists.EitherList;
import tgw.evolution.util.constants.HarvestLevel;

public class ModularTool implements IModular {

    public static final ModularTool INSTANCE = new ModularTool();

    protected ModularTool() {
    }

    @Override
    public void appendPartTooltip(CompoundTag tag, EitherList<FormattedText, TooltipComponent> tooltip) {
        //TODO implementation

    }

    public int getBackPriority(CompoundTag tag) {
        //TODO
        return 0;
    }

    @Override
    public int getBarColor(CompoundTag tag) {
        //TODO implementation
        return 0;
    }

    @Override
    public int getBarWidth(CompoundTag tag) {
        //TODO implementation
        return 0;
    }

    public @Nullable IMelee.BasicAttackType getBasicAttackType(CompoundTag tag) {
        //TODO
        return null;
    }

    public SoundEvent getBlockHitSound(CompoundTag tag) {
        //todo
        return SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS;
    }

    public int getCooldown(CompoundTag tag) {
        //TODO
        return 0;
    }

    @Override
    public String getDescriptionId(CompoundTag tag) {
        //TODO implementation
        return "null";
    }

    public float getDestroySpeed(CompoundTag tag, BlockState state) {
        //TODO
        return 0;
    }

    public double getDmgMultiplier(CompoundTag tag, EvolutionDamage.Type type) {
        //TODO
        return 1;
    }

    @Override
    public @HarvestLevel int getHarvestLevel(CompoundTag tag) {
        //TODO implementation
        return 0;
    }

    @Override
    public double getMass(CompoundTag tag) {
        //TODO implementation
        return 0;
    }

    @Override
    public int getTotalDurabilityDmg(CompoundTag tag) {
        //TODO implementation
        return 0;
    }

    @Override
    public int getTotalMaxDurability(CompoundTag tag) {
        //TODO implementation
        return 0;
    }

    @Override
    public UseAnim getUseAnimation(CompoundTag tag) {
        //TODO implementation
        return null;
    }

    @Override
    public <E extends LivingEntity> void hurtAndBreak(ItemStack stack,
                                                      ItemModular.DamageCause cause,
                                                      E entity,
                                                      @Nullable EquipmentSlot slot,
                                                      @HarvestLevel int harvestLevel) {
        //TODO implementation

    }

    @Override
    public boolean isAxe(CompoundTag tag) {
        //TODO implementation
        return false;
    }

    @Override
    public boolean isBarVisible(CompoundTag tag) {
        //TODO implementation
        return false;
    }

    @Override
    public boolean isBroken(CompoundTag tag) {
        //TODO implementation
        return false;
    }

    public boolean isCorrecToolForDrops(ItemStack stack, BlockState state, Level level, int x, int y, int z) {
        //TODO IMPLEMENTATIN
        return false;
    }

    @Override
    public boolean isHammer(CompoundTag tag) {
        //TODO implementation
        return false;
    }

    @Override
    public boolean isShovel(CompoundTag tag) {
        //TODO implementation
        return false;
    }

    @Override
    public boolean isSword(CompoundTag tag) {
        //TODO implementation
        return false;
    }

    public boolean isThrowable(CompoundTag tag) {
        //TODO
        return false;
    }

    @Override
    public boolean isTwoHanded(CompoundTag tag) {
        //TODO implementation
        return false;
    }

    public void mineBlock(ItemStack stack, Level level, BlockState state, int x, int y, int z, LivingEntity entity) {
        //TODO
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        //TODO implementation

    }

    public void set(CompoundTag tag,
                    PartTypes.Head headType,
                    EvolutionMaterials headMaterial,
                    PartTypes.Handle handleType,
                    EvolutionMaterials handleMaterial,
                    boolean sharp) {
        //TODO
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack stack, Level level, Player player, InteractionHand hand) {
        //TODO implementation
        return null;
    }
}
