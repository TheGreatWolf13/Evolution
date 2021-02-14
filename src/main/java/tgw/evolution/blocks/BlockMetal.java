package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.MetalVariant;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.OXIDATION;

public class BlockMetal extends BlockGravity {

    public BlockMetal(MetalVariant name) {
        super(Block.Properties.create(Material.IRON)
                              .harvestLevel(name.getHarvestLevel())
                              .sound(SoundType.METAL)
                              .hardnessAndResistance(name.getHardness(), name.getResistance()), name.getDensity());
        this.setDefaultState(this.getDefaultState().with(OXIDATION, 0));
    }

    @Override
    public SoundEvent fallSound() {
        return SoundEvents.BLOCK_ANVIL_LAND;
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(OXIDATION);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        ItemStack stack = new ItemStack(EvolutionItems.block_copper.get());
        if (state.get(OXIDATION) != 0) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("Oxidation", state.get(OXIDATION));
            stack.setTag(nbt);
        }
        List<ItemStack> list = new ArrayList<>(1);
        list.add(stack);
        return list;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        CompoundNBT nbt = context.getPlayer().getItemStackFromSlot(MathHelper.getEquipFromHand(context.getHand())).getTag();
        if (nbt == null) {
            return this.getDefaultState();
        }
        return this.getDefaultState().with(OXIDATION, nbt.getInt("Oxidation"));
    }

    @Override
    public void randomTick(BlockState state, World world, BlockPos pos, Random rand) {
        //TODO oxidation
    }
}
