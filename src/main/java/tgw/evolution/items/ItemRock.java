package tgw.evolution.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.blocks.IStoneVariant;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.network.PacketSCOpenKnappingGui;
import tgw.evolution.util.EnumRockNames;
import tgw.evolution.util.EnumRockVariant;

import javax.annotation.Nullable;
import java.util.List;

public class ItemRock extends ItemGenericBlockPlaceable implements IStoneVariant {

    private final EnumRockNames name;
    private EnumRockVariant variant;

    public ItemRock(Block block, Properties builder, EnumRockNames name) {
        super(block, builder);
        this.name = name;
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        switch (this.name.getRockType()) {
            case IGNEOUS_EXTRUSIVE:
                tooltip.add(EvolutionTexts.TOOLTIP_ROCK_TYPE_IGEXTRUSIVE);
                break;
            case IGNEOUS_INTRUSIVE:
                tooltip.add(EvolutionTexts.TOOLTIP_ROCK_TYPE_IGINTRUSIVE);
                break;
            case METAMORPHIC:
                tooltip.add(EvolutionTexts.TOOLTIP_ROCK_TYPE_METAMORPHIC);
                break;
            case SEDIMENTARY:
                tooltip.add(EvolutionTexts.TOOLTIP_ROCK_TYPE_SEDIMENTARY);
                break;
        }
        tooltip.add(EvolutionTexts.TOOLTIP_ROCK_KNAP);
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
    public BlockState getSneakingState(BlockItemUseContext context) {
        return this.variant.getKnapping().getDefaultState();
    }

    @Override
    public EnumRockNames getStoneName() {
        return this.name;
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
    public void sneakingAction(BlockItemUseContext context) {
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) context.getPlayer()),
                                       new PacketSCOpenKnappingGui(context.getPos(), this.variant));
    }
}
