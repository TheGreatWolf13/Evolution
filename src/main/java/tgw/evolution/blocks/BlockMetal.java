package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.MetalVariant;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.OXIDATION;

public class BlockMetal extends BlockGravity {

    private final MetalVariant variant;

    public BlockMetal(MetalVariant variant) {
        super(Properties.of(Material.METAL)
                        .harvestLevel(variant.getHarvestLevel())
                        .sound(SoundType.METAL)
                        .strength(variant.getHardness(), variant.getResistance()), variant.getDensity());
        this.variant = variant;
        this.registerDefaultState(this.defaultBlockState().setValue(OXIDATION, 0));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(OXIDATION);
    }

    @Override
    public SoundEvent fallSound() {
        return SoundEvents.ANVIL_LAND;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        ItemStack stack = new ItemStack(EvolutionItems.block_copper.get());
        if (state.getValue(OXIDATION) != 0) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("Oxidation", state.getValue(OXIDATION));
            stack.setTag(nbt);
        }
        List<ItemStack> list = new ArrayList<>(1);
        list.add(stack);
        return list;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return this.variant.getFrictionCoefficient();
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        CompoundNBT nbt = context.getPlayer().getItemInHand(context.getHand()).getTag();
        if (nbt == null) {
            return this.defaultBlockState();
        }
        return this.defaultBlockState().setValue(OXIDATION, nbt.getInt("Oxidation"));
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        //TODO oxidation
    }
}
