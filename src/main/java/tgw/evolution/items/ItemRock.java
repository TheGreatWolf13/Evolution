package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.blocks.IStoneVariant;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketSCOpenKnappingGui;
import tgw.evolution.util.EnumRockNames;
import tgw.evolution.util.EnumRockVariant;

import javax.annotation.Nullable;
import java.util.List;

public class ItemRock extends ItemBlockPlaceable implements IStoneVariant {

    private final EnumRockNames name;
    private EnumRockVariant variant;

    public ItemRock(Block blockIn, Properties builder, EnumRockNames name) {
        super(blockIn, builder);
        this.name = name;
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        String key = "evolution.tooltip.rock_type." + this.name.getRockType().getName();
        String knap = "evolution.tooltip.rock.knap";
        ITextComponent textType = new TranslationTextComponent(key).setStyle(new Style().setColor(TextFormatting.GRAY));
        ITextComponent textKnap = new TranslationTextComponent(knap).setStyle(new Style().setColor(TextFormatting.BLUE));
        tooltip.add(textType);
        tooltip.add(textKnap);
    }

    @Override
    public BlockState getSneakingState(BlockItemUseContext context) {
        return this.variant.getKnapping().getDefaultState();
    }

    @Override
    public boolean customCondition(Block block) {
        return false;
    }

    @Override
    @Nullable
    public BlockState getCustomState(BlockItemUseContext context) {
        return null;
    }

    @Override
    public void sneakingAction(BlockItemUseContext context) {
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) context.getPlayer()), new PacketSCOpenKnappingGui(context.getPos(), this.variant));
    }

    @Override
    public EnumRockVariant getVariant() {
        return this.variant;
    }

    @Override
    public void setVariant(EnumRockVariant variant) {
        this.variant = variant;
    }

    @Override
    public EnumRockNames getStoneName() {
        return this.name;
    }
}
