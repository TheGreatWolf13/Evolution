package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import tgw.evolution.init.EvolutionStyles;

import java.util.List;
import java.util.Locale;

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
        String text = "evolution.tooltip.metal.oxidation";
        float percentage = nbt.getInt("Oxidation") / 0.08F;
        ITextComponent comp = new TranslationTextComponent(text, String.format(Locale.US, "%.1f%%", percentage)).setStyle(EvolutionStyles.LIGHT_GREY);
        tooltip.add(comp);
    }
}
