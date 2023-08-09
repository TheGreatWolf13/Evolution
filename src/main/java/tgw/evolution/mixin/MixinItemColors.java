package tgw.evolution.mixin;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ItemColors.class)
public abstract class MixinItemColors {

    @Overwrite
    public static ItemColors createDefault(BlockColors blockColors) {
        ItemColors itemColors = new ItemColors();
        itemColors.register((stack, data) -> data > 0 ? 0xffff_ffff : ((DyeableLeatherItem) stack.getItem()).getColor(stack), Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, Items.LEATHER_HORSE_ARMOR);
        itemColors.register((stack, data) -> GrassColor.get(0.5, 1.0), Blocks.TALL_GRASS, Blocks.LARGE_FERN);
        itemColors.register((stack, data) -> {
            if (data != 1) {
                return 0xffff_ffff;
            }
            CompoundTag tag = stack.getTagElement("Explosion");
            int[] colors = tag != null && tag.contains("Colors", Tag.TAG_INT_ARRAY) ? tag.getIntArray("Colors") : null;
            if (colors != null && colors.length != 0) {
                if (colors.length == 1) {
                    return colors[0];
                }
                int r = 0;
                int g = 0;
                int b = 0;
                for (int m : colors) {
                    r += (m & 0xff_0000) >> 16;
                    g += (m & 0xff00) >> 8;
                    b += m & 0xff;
                }
                r /= colors.length;
                g /= colors.length;
                b /= colors.length;
                return r << 16 | g << 8 | b;
            }
            return 0x8a_8a8a;
        }, Items.FIREWORK_STAR);
        itemColors.register((stack, data) -> data > 0 ? 0xffff_ffff : PotionUtils.getColor(stack), Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);
        for (SpawnEggItem spawnEgg : SpawnEggItem.eggs()) {
            //noinspection ObjectAllocationInLoop
            itemColors.register((stack, data) -> spawnEgg.getColor(data), spawnEgg);
        }
        itemColors.register((stack, data) -> {
            BlockState state = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
            return blockColors.getColor_(state, null, data);
        }, Blocks.GRASS_BLOCK, Blocks.GRASS, Blocks.FERN, Blocks.VINE, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.LILY_PAD);
        itemColors.register((stack, data) -> data == 0 ? PotionUtils.getColor(stack) : 0xffff_ffff, Items.TIPPED_ARROW);
        itemColors.register((stack, data) -> data == 0 ? 0xffff_ffff : MapItem.getColor(stack), Items.FILLED_MAP);
        return itemColors;
    }
}
