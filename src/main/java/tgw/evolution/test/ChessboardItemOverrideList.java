package tgw.evolution.test;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class ChessboardItemOverrideList extends ItemOverrideList {

    @Nullable
    @Override
    public IBakedModel resolve(IBakedModel originalModel, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
        int numberOfChessPieces = stack.getCount();
        return new ChessboardFinalisedModel(originalModel, numberOfChessPieces);
    }
}
