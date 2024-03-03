package tgw.evolution.mixin;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Registry;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.ColorManager;
import tgw.evolution.Evolution;
import tgw.evolution.client.renderer.IBlockColor;
import tgw.evolution.patches.PatchBlockColors;

@Mixin(BlockColors.class)
public abstract class MixinBlockColors implements PatchBlockColors {

    @Shadow @Final private IdMapper<BlockColor> blockColors;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static BlockColors createDefault() {
        BlockColors colors = new BlockColors();
        colors.register((IBlockColor) (state, level, x, y, z, data) -> level != null && x != Integer.MIN_VALUE ? ColorManager.getAverageGrassColor(level, x, state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER ? y - 1 : y, z) : 0xffff_ffff, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
        colors.addColoringState(DoublePlantBlock.HALF, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
        colors.register((IBlockColor) (state, level, x, y, z, data) -> level != null && x != Integer.MIN_VALUE ? ColorManager.getAverageGrassColor(level, x, y, z) : GrassColor.get(0.5, 1.0), Blocks.GRASS_BLOCK, Blocks.FERN, Blocks.GRASS, Blocks.POTTED_FERN);
        colors.register((IBlockColor) (state, level, x, y, z, data) -> FoliageColor.getEvergreenColor(), Blocks.SPRUCE_LEAVES);
        colors.register((IBlockColor) (state, level, x, y, z, data) -> FoliageColor.getBirchColor(), Blocks.BIRCH_LEAVES);
        colors.register((IBlockColor) (state, level, x, y, z, data) -> level != null && x != Integer.MIN_VALUE ? ColorManager.getAverageFoliageColor(level, x, y, z) : FoliageColor.getDefaultColor(), Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.VINE);
        colors.register((IBlockColor) (state, level, x, y, z, data) -> level != null && x != Integer.MIN_VALUE ? ColorManager.getAverageWaterColor(level, x, y, z) : 0xffff_ffff, Blocks.WATER, Blocks.BUBBLE_COLUMN, Blocks.WATER_CAULDRON);
        colors.register((IBlockColor) (state, level, x, y, z, data) -> RedStoneWireBlock.getColorForPower(state.getValue(RedStoneWireBlock.POWER)), Blocks.REDSTONE_WIRE);
        colors.addColoringState(RedStoneWireBlock.POWER, Blocks.REDSTONE_WIRE);
        colors.register((IBlockColor) (state, level, x, y, z, data) -> level != null && x != Integer.MIN_VALUE ? ColorManager.getAverageGrassColor(level, x, y, z) : 0xffff_ffff, Blocks.SUGAR_CANE);
        colors.register((IBlockColor) (state, level, x, y, z, data) -> 0xe0_c71c, Blocks.ATTACHED_MELON_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
        colors.register((IBlockColor) (state, level, x, y, z, data) -> {
            int age = state.getValue(StemBlock.AGE);
            int r = age * 32;
            int g = 255 - age * 8;
            int b = age * 4;
            return r << 16 | g << 8 | b;
        }, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
        colors.addColoringState(StemBlock.AGE, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
        colors.register((IBlockColor) (state, level, x, y, z, data) -> level != null && x != Integer.MIN_VALUE ? 0x20_8030 : 0x71_c35c, Blocks.LILY_PAD);
        return colors;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int data) {
        Evolution.deprecatedMethod();
        if (pos == null) {
            return this.getColor_(state, level, data);
        }
        return this.getColor_(state, level, pos.getX(), pos.getY(), pos.getZ(), data);
    }

    @Override
    public int getColor_(BlockState state, @Nullable BlockAndTintGetter level, int x, int y, int z, int data) {
        IBlockColor blockColor = (IBlockColor) this.blockColors.byId(Registry.BLOCK.getId(state.getBlock()));
        return blockColor == null ? 0xffff_ffff : blockColor.getColor_(state, level, x, y, z, data);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @SuppressWarnings("OverwriteModifiers")
    @Overwrite
    public void register(BlockColor blockColor, Block... blocks) {
        if (!(blockColor instanceof IBlockColor)) {
            throw new RuntimeException("Trying to register invalid BlockColor. All BlockColors should implement IBlockColor!");
        }
        for (Block block : blocks) {
            this.blockColors.addMapping(blockColor, Registry.BLOCK.getId(block));
        }
    }

    @Override
    public void register(BlockColor blockColor, Block block) {
        if (!(blockColor instanceof IBlockColor)) {
            throw new RuntimeException("Trying to register invalid BlockColor. All BlockColors should implement IBlockColor!");
        }
        this.blockColors.addMapping(blockColor, Registry.BLOCK.getId(block));
    }
}
