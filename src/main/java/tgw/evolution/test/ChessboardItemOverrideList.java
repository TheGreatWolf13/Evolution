package tgw.evolution.test;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ChessboardItemOverrideList extends ItemOverrideList {

    @Nullable
    @Override
    public IBakedModel getModelWithOverrides(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable LivingEntity entity) {
        int numberOfChessPieces = stack.getCount();
        return new ChessboardFinalisedModel(originalModel, numberOfChessPieces);
    }
}
