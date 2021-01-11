package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import tgw.evolution.init.EvolutionTexts;

import java.util.List;

public class ItemBlockMetal extends ItemBlock {

    public ItemBlockMetal(Block block, Properties properties) {
        super(block, properties);
        this.addPropertyOverride(new ResourceLocation("oxidation"), (stack, worldIn, entity) -> {
            CompoundNBT nbt = stack.getTag();
            if (nbt == null) {
                return 0;
            }
            return nbt.getInt("Oxidation");
        });
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flag) {
        CompoundNBT nbt = stack.getTag();
        if (nbt == null) {
            return;
        }
        float oxydation = nbt.getInt("Oxidation") / 0.08F;
        tooltip.add(EvolutionTexts.oxydation(oxydation));
    }
}
